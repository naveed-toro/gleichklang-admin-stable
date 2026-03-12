package de.binaerebauten.gleichklang.heidelpaymigration.service;

import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.repository.UserPaymentSettingsRepository;
import de.binaerebauten.gleichklang.core.repository.user.CompleteUserRepository;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayQueryApiConnector;
import de.binaerebauten.gleichklang.heidelpaymigration.model.HeidelpayUser;
import de.binaerebauten.gleichklang.heidelpaymigration.model.MigrationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Saves fake registration accounts for existing Gleichklang users
 * on the Heidelpay test-system in order to do an acceptance testing.
 */
@Service
public class HeidelpayRegistrationMigration
{
	private static final Logger logger = LoggerFactory.getLogger(HeidelpayRegistrationMigration.class);

	// Select users with external payment method, which have a subscription, but are not registered
	private static String query = "SELECT u.id, ps.payment_method, t.registrationid, t.identification_transactionid AS external_reference_id\n"
			+ "FROM user_ u\n"
			+ "  JOIN subscription s ON s.user_id = u.id AND s.current = TRUE AND NOW() BETWEEN s.begin AND s.end\n"
			+ "  JOIN user_payment_settings ps ON ps.user_id = u.id\n"
			+ "  JOIN comppayment_transaction t ON t.no = s.legacy_id\n"
			+ "WHERE\n"
			+ "  s.automatic_renewal = TRUE AND\n"
			+ "  ps.payment_method IN ('CREDIT_CARD', 'DIRECT_DEBIT') AND\n"
			+ "  t.method IN ('CC', 'DD') AND t.protocolstate = 'PAID' AND\n"
			+ "  NOT EXISTS(SELECT 1 FROM external_payment_registration r WHERE r.user_id = u.id);";

	private final JdbcTemplate jdbcTemplate;

	private final CompleteUserRepository completeUserRepository;

	private final ExternalPaymentRepository externalPaymentRepository;

	private final UserPaymentSettingsRepository userPaymentSettingsRepository;

	private final HeidelpayQueryApiConnector heidelpayQueryApiConnector;

	private final SubscriptionService subscriptionService;
	
	@Autowired
	public HeidelpayRegistrationMigration(HeidelpayQueryApiConnector heidelpayQueryApiConnector,
			SubscriptionService subscriptionService, ExternalPaymentRepository externalPaymentRepository,
			UserPaymentSettingsRepository userPaymentSettingsRepository, JdbcTemplate jdbcTemplate,
			CompleteUserRepository completeUserRepository)
	{
		this.heidelpayQueryApiConnector = heidelpayQueryApiConnector;
		this.subscriptionService = subscriptionService;
		this.externalPaymentRepository = externalPaymentRepository;
		this.userPaymentSettingsRepository = userPaymentSettingsRepository;
		this.jdbcTemplate = jdbcTemplate;
		this.completeUserRepository = completeUserRepository;
	}
	
	public void migrate()
	{
		List<HeidelpayUser> users = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(HeidelpayUser.class));
		
		logger.info(String.format("---> Migration start: USERS: %d <---", users.size()));
		
		// Create CSV files for results
		HeidelpayRegistrationCSVLogger successfulCSV = new HeidelpayRegistrationCSVLogger("successful.csv");
		HeidelpayRegistrationCSVLogger errorCSV = new HeidelpayRegistrationCSVLogger("error.csv");

		List<MigrationResult> results = users.stream()
				.map(this::register)
				.collect(Collectors.toList());
		
		// Collect all successfully migrated
		long success = results.stream()
				.filter(MigrationResult::isPassed)
				.peek(successfulCSV::writeLine)
				.count();
		successfulCSV.close();
		
		// Collect all failed
		long failed = results.stream()
				.filter(r -> !r.isPassed())
				.peek(errorCSV::writeLine)
				.count();
		errorCSV.close();
		
		logger.info(String.format("---> Migration result: SUCCESS: %d, FAILED: %d <---", success, failed));
	}

	/**
	 * Registration with a fake (example) account data.
	 * Only userIds and transkationIds are real.
	 *
	 * @param heidelpayUser
	 * @return MigrationResult
	 */
	private MigrationResult register(HeidelpayUser heidelpayUser)
	{
		logger.info(String.format("Migrating user %s", heidelpayUser.getId()));
		User user = completeUserRepository.findById(heidelpayUser.getId());
		ExternalPayment externalPayment = externalPaymentRepository.findByExternalReferenceId(heidelpayUser.getExternalReferenceId());
		if (externalPayment == null || !PaymentState.PAID.equals(externalPayment.getState()))
		{
			String msg = "No external payment exists for user " + user.getId();
			logger.error(msg);
			return new MigrationResult(user.getEmail(), false, msg);
		}

		// Fill missing user data with default values
		fulfillUserData(user);

		SubscriptionWithState subscriptionWithState =
				subscriptionService.getCurrentSubscriptionWithState(user, LocalDateTime.now());
		if (Subscription.SubscriptionState.ACTIVE.equals(subscriptionWithState.getState()))
		{
			Subscription subscription = subscriptionWithState.getSubscription();
			Optional<Subscription> paymentSubscription = externalPayment.getInvoice().getItems().stream()
					.filter(i -> i.getSubscription() != null)
					.findFirst()
					.map(InvoiceItem::getSubscription);
			if (paymentSubscription.isPresent() && subscription.equals(paymentSubscription.get()))
			{
				try
				{
					Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
					if (paymentSettings.isPresent())
					{
						UserPaymentSettings userPaymentSettings = paymentSettings.get();
						switch (heidelpayUser.getPaymentMethod())
						{
							case CREDIT_CARD:
							{
								CreditCardAccount creditCard = new CreditCardAccount();
								creditCard.setHolder(String.format("%s %s", user.getFirstName(), user.getLastName()));
								creditCard.setNumber("4012888888881881");
								creditCard.setVerification("123");
								creditCard.setExpirationDate(YearMonth.of(2017, 10));
								creditCard.setBrand("VISA");
								ExternalPaymentRegistration registration =
										heidelpayQueryApiConnector.register(user, userPaymentSettings, externalPayment, creditCard);
								if (subscription.isAutomaticRenewal() && subscription.getOffer().getAutoRenewalOffer() != null)
								{
//									heidelpayQueryApiConnector.scheduleSubscriptionAutoRenewalJob(subscription,
//											externalPayment, PaymentMethod.CREDIT_CARD, registration);
								}
								return new MigrationResult(user.getEmail(), true);
							}
							case DIRECT_DEBIT:
							{
								DirectDebitAccount directDebitAccount = new DirectDebitAccount();
								directDebitAccount.setHolder(String.format("%s %s", user.getFirstName(), user.getLastName()));
								directDebitAccount.setNumber("5320130");
								directDebitAccount.setBank("37040044");
								directDebitAccount.setBankName("Commerzbank AG");
								directDebitAccount.setCountry("DE");
								ExternalPaymentRegistration registration =
										heidelpayQueryApiConnector.register(user, userPaymentSettings, externalPayment, directDebitAccount);
								if (subscription.isAutomaticRenewal() && subscription.getOffer().getAutoRenewalOffer() != null)
								{
//									heidelpayQueryApiConnector.scheduleSubscriptionAutoRenewalJob(subscription,
//											externalPayment, PaymentMethod.DIRECT_DEBIT, registration);
								}
								return new MigrationResult(user.getEmail(), true);
							}
							default:
								String msg = "Payment method not supported: " + heidelpayUser.getPaymentMethod();
								return new MigrationResult(user.getEmail(), false, msg);
						}
					}
					else
					{
						String msg = "No user payments settings found for user: " + user.getEmail();
						return new MigrationResult(user.getEmail(), false, msg);
					}
				}
				catch (PaymentException e) {
					logger.error(e.getMessage());
					return new MigrationResult(user.getEmail(), false, e.getMessage());
				}
				catch (IllegalArgumentException e)
				{
					logger.error(e.getMessage());
					return new MigrationResult(user.getEmail(), false, e.getMessage());
				}
			}
			else
			{
				String msg = "No active subscription found for payment " + externalPayment.getId();
				logger.error(msg);
				return new MigrationResult(user.getEmail(), false, msg);
			}
		}
		else
		{
			String msg = "No active subscription found for user " + user.getId();
			logger.error(msg);
			return new MigrationResult(user.getEmail(), false, msg);
		}
	}
	
	private void fulfillUserData(User user)
	{
		if (StringUtils.isEmpty(user.getFirstName())) {
			user.setFirstName("Marvin");
		}

		if (StringUtils.isEmpty(user.getLastName())) {
			user.setLastName("Dummy");
		}

		if (user.getRegisterIp() == null) {
			user.setRegisterIp("87.139.190.159");
		}

		Optional<Address> billingAddress = user.getAddresses().stream()
				.filter(Address::isPayment)
				.findFirst();

		Zip defaultZip = new Zip();
		defaultZip.setZip("12345");

		Country defaultCountry = new Country();
		defaultCountry.setI18nKey("DE");

		if (billingAddress.isPresent())
		{
			Address address = billingAddress.get();
			address.setZip(Objects.isNull(address.getZip()) ? defaultZip : address.getZip());
			address.setCity(StringUtils.isEmpty(address.getCity()) || !StringUtils.hasLength(address.getCity()) ?
					"Musterdorf" : address.getCity());
			address.setCountry(Objects.isNull(address.getCountry()) ? defaultCountry : address.getCountry());
			address.setStreetWithNumber(StringUtils.isEmpty(address.getStreetWithNumber()) || StringUtils.hasLength(address.getStreetWithNumber()) ?
					"Musterstr 12" : address.getStreetWithNumber());
		}
		else
		{
			Address address = new Address();
			address.setPayment(true);
			address.setUser(user);
			address.setZip(defaultZip);
			address.setCity("Musterdorf");
			address.setCountry(defaultCountry);
			address.setStreetWithNumber("Musterstr 12");
			user.getAddresses().add(address);
		}
	}
	
}
