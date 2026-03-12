package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test factory for payment entities.
 */
@Component
public class PaymentEntityFactory
{
	public static final int SUBSCRIPTION_EXPIRATION_IN_DAY = 14;

	@Autowired(required = false)
	private ProductRepository productRepository;

	@Autowired(required = false)
	private SubscriptionRepository subscriptionRepository;

	@Autowired(required = false)
	private ChargebackRepository chargebackRepository;

	@Autowired(required = false)
	private ExternalPaymentRepository externalPaymentRepository;

	@Autowired(required = false)
	private PrepaymentRepository prepaymentRepository;

	@Autowired(required = false)
	private ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;

	@Autowired(required = false)
	private UserPaymentSettingsRepository userPaymentSettingsRepository;

	@Autowired(required = false)
	private InvoiceRepository invoiceRepository;

	@Autowired(required = false)
	private HeidelpayTransactionRepository heidelpayTransactionRepository;

	@Autowired(required = false)
	private BankAccountRepository bankAccountRepository;

	public Subscription createSubscription(User user, SubscriptionOffer offer, LocalDateTime begin)
	{
		final Subscription subscription = offer.createSubscription(begin, null);

		subscription.setUser(user);
		subscription.setExpirationDate(subscription.getEnd().plusDays(SUBSCRIPTION_EXPIRATION_IN_DAY));
		final Set<RecommendationCategory> categories =
				offer.getCategories().stream()
						.map(SubscriptionOfferCategory::getCategory)
						.collect(Collectors.toSet());
		user.setCategories(categories);

		return subscription;
	}

	public InitialSubscriptionOffer createIntialSubscriptionOffer(String name,
			LocalDateTime begin, int durationInMonths, RecommendationCategory... categories)
	{
		final InitialSubscriptionOffer initialSubscriptionOffer = new InitialSubscriptionOffer();

		init(initialSubscriptionOffer, name, begin, durationInMonths, categories);

		return initialSubscriptionOffer;
	}

	public UpgradeOffer createUpgradeOffer(UpgradeType upgradeType, String name,
			LocalDateTime begin, int durationInMonths, RecommendationCategory... categories)
	{
		final UpgradeOffer upgradeOffer = new UpgradeOffer();
		upgradeOffer.setUpgradeType(upgradeType);

		init(upgradeOffer, name, begin, durationInMonths, categories);

		return upgradeOffer;
	}

	public ServiceOffer createServiceOffer(String name,
			LocalDateTime begin, int durationInMonths, RecommendationCategory... categories)
	{
		final ServiceOffer serviceOffer = new ServiceOffer();
		Set<ServiceOfferRequiredCategory> requiredCategories = Stream.of(categories)
				.map(c -> new ServiceOfferRequiredCategory(serviceOffer, c))
				.collect(Collectors.toSet());

		serviceOffer.setRequiredCategories(requiredCategories);
		initProduct(serviceOffer, name, begin, durationInMonths);

		return serviceOffer;
	}

	public RenewalOffer createRenewalOffer(String name,
			LocalDateTime begin, int durationInMonths, RecommendationCategory... categories)
	{
		final RenewalOffer renewalOffer = new RenewalOffer();

		init(renewalOffer, name, begin, durationInMonths, categories);

		return renewalOffer;
	}

	private void init(SubscriptionOffer subscriptionOffer, String name, LocalDateTime begin, int durationInMonths, RecommendationCategory[] categories)
	{
		initProduct(subscriptionOffer, name, begin, durationInMonths);

		subscriptionOffer.setDuration(durationInMonths);
		subscriptionOffer.setDurationUnit(DurationUnit.MONTHS);
		subscriptionOffer.setTariff(Tariff.STANDARD);
		
		if(subscriptionOffer instanceof RenewalOffer)
		{
			subscriptionOffer.setAutoRenewalOffer((RenewalOffer) subscriptionOffer);
		}
		else
		{
			subscriptionOffer.setAutoRenewalOffer(createRenewalOffer("Renewal", begin, durationInMonths, categories));
		}

		for (RecommendationCategory category : categories)
		{
			SubscriptionOfferCategory offerCategory = new SubscriptionOfferCategory();
			offerCategory.setCategory(category);
			offerCategory.setSubscriptionOffer(subscriptionOffer);
			subscriptionOffer.getCategories().add(offerCategory);
		}
	}

	private void initProduct(Product product, String name, LocalDateTime begin, int durationInMonths)
	{
		product.setAmount(new MonetaryAmount(BigDecimal.TEN, AvailableCurrency.EUR));
		product.setName(name);
		product.setBegin(begin);
	}

	@Transactional
	public Chargeback persistChargeback(PaymentMethod forMethod, BigDecimal amountInEur)
	{
		Chargeback chargeback = new Chargeback();

		chargeback.setName(String.format("%s-%s_EUR", forMethod, amountInEur));
		chargeback.setForMethod(forMethod);
		chargeback.setBegin(LocalDateTime.now());
		chargeback.setAmount(new MonetaryAmount(amountInEur, AvailableCurrency.EUR));

		chargebackRepository.save(chargeback);

		return chargeback;
	}

	@Transactional
	public ExternalPaymentRegistration persistExternalPaymentRegistration(User user, String registrationId)
	{
		ExternalPaymentRegistration externalPaymentRegistration = createExternalPaymentRegistration(user, registrationId);

		externalPaymentRegistrationRepository.save(externalPaymentRegistration);

		return externalPaymentRegistration;
	}

	public ExternalPaymentRegistration createExternalPaymentRegistration(User user, String registrationId)
	{
		ExternalPaymentRegistration externalPaymentRegistration = new ExternalPaymentRegistration();

		externalPaymentRegistration.setUser(user);
		externalPaymentRegistration.setRegistrationId(registrationId);
		externalPaymentRegistration.setLastUsedDate(LocalDateTime.now());
		externalPaymentRegistration.setExternalReferenceId(UUID.randomUUID().toString());

		return externalPaymentRegistration;
	}

	@Transactional
	public UserPaymentSettings persistUserPaymentSettings(User user, PaymentMethod paymentMethod)
	{
		UserPaymentSettings userPaymentSettings = createUserPaymentSettings(user, paymentMethod);

		userPaymentSettingsRepository.save(userPaymentSettings);

		return userPaymentSettings;
	}

	public UserPaymentSettings createUserPaymentSettings(User user, PaymentMethod paymentMethod)
	{
		UserPaymentSettings userPaymentSettings = new UserPaymentSettings();

		userPaymentSettings.setUser(user);
		userPaymentSettings.setPaymentMethod(paymentMethod);

		return userPaymentSettings;
	}

	@Transactional
	public Subscription persistSubscription(User user, SubscriptionOffer offer, LocalDateTime begin)
	{
		final Subscription subscription = createSubscription(user, offer, begin);
		subscriptionRepository.save(subscription);

		return subscription;
	}

	@Transactional
	public InitialSubscriptionOffer persistInitialSubscriptionOffer(String name,
			LocalDateTime begin, RecommendationCategory... categories)
	{
		RenewalOffer renewalOffer = createRenewalOffer(name + "Renewal", begin, 12, categories);
		productRepository.save(renewalOffer);

		final InitialSubscriptionOffer offer = createIntialSubscriptionOffer(name, begin, 12, categories);
		offer.setAutoRenewalOffer(renewalOffer);

		productRepository.save(offer);

		return offer;
	}


	@Transactional
	public UpgradeOffer persistUpgradeOffer(UpgradeType upgradeType, String name,
			LocalDateTime begin, int durationInMonths, RecommendationCategory... categories)
	{
		RenewalOffer renewalOffer = createRenewalOffer(name + "Renewal", begin, 12, categories);
		productRepository.save(renewalOffer);

		UpgradeOffer upgradeOffer = createUpgradeOffer(upgradeType, name, begin, durationInMonths, categories);
		upgradeOffer.setAutoRenewalOffer(renewalOffer);

		productRepository.save(upgradeOffer);

		return upgradeOffer;
	}

	@Transactional
	public ExternalPayment persistExternalPayment(Invoice invoice, PaymentState state)
	{
		ExternalPayment externalPayment = createExternalPayment(invoice.getUser(), state);

		externalPayment.setInvoice(invoice);
		invoice.getPayments().add(externalPayment);

		externalPaymentRepository.save(externalPayment);

		return externalPayment;
	}

	public ExternalPayment createExternalPayment(User user, PaymentState state)
	{
		ExternalPayment externalPayment = new ExternalPayment();

		externalPayment.setState(state);
		externalPayment.setUser(user);
		externalPayment.setMethod(PaymentMethod.CREDIT_CARD);
		externalPayment.setAmount(new MonetaryAmount(BigDecimal.TEN, AvailableCurrency.EUR));
		externalPayment.setExternalReferenceId(externalPayment.createExternalReferenceId());

		return externalPayment;
	}

	@Transactional
	public Invoice persistDefaultInvoice(User user)
	{
		Invoice invoice = createDefaultInvoice(user);

		invoiceRepository.save(invoice);

		return invoice;
	}

	public Invoice createDefaultInvoice(User user)
	{
		Invoice invoice = new Invoice();
		invoice.setUser(user);
		return invoice;
	}
	
	@Transactional
	public InvoiceItem persistDefaultInvoiceItem(Invoice invoice, Product product)
	{
		InvoiceItem invoiceItem = createDefaultInvoiceItem(invoice, product);
		invoice.getItems().add(invoiceItem);
		
		invoiceRepository.save(invoice);
		
		return invoiceItem;
	}
	
	public InvoiceItem createDefaultInvoiceItem(Invoice invoice, Product product)
	{
		InvoiceItem invoiceItem = new InvoiceItem();
		invoiceItem.setAmount(new MonetaryAmount(BigDecimal.ONE, AvailableCurrency.EUR));
		invoiceItem.setInvoice(invoice);
		invoiceItem.setProduct(product);
		return invoiceItem;
	}

	@Transactional
	public HeidelpayTransaction persistDefaultHeidelpayEvent(ExternalPayment payment)
	{
		HeidelpayTransaction heidelpayTransaction = new HeidelpayTransaction();

		heidelpayTransaction.setReturnMessage("RESULT_MESSAGE");
		heidelpayTransaction.setResult(ProcessingResultType.ACK);
		heidelpayTransaction.setReturnCode("CC.RG.01");
		heidelpayTransaction.setPayment(payment);

		heidelpayTransactionRepository.save(heidelpayTransaction);

		return heidelpayTransaction;
	}

	@Transactional
	public BankAccount persistDefaultBankAccount(Country country)
	{
		final BankAccount bankAccount = createDefaultBankAccount(country);

		bankAccountRepository.save(bankAccount);

		return bankAccount;
	}

	public BankAccount createDefaultBankAccount(Country country)
	{
		final BankAccount bankAccount = new BankAccount();

		bankAccount.setAccountNumber("1234");
		bankAccount.setBankName("Test Bank");
		bankAccount.setBankNumber("12345");
		bankAccount.setBic("BIC");
		bankAccount.setHolder("Gleichklang Ltd");
		bankAccount.setIban("IBAN");

		bankAccount.setCountry(country);
		bankAccount.setActive(true);

		return bankAccount;
	}

	@Transactional
	public Prepayment persistPrepayment(User user, BankAccount bankAccount, Invoice invoice, String externalReferenceId)
	{
		final Prepayment prepayment = createPrepayment(user, bankAccount, invoice, externalReferenceId);

		return prepaymentRepository.save(prepayment);
	}

	public Prepayment createPrepayment(User user, BankAccount bankAccount, Invoice invoice, String externalReferenceId)
	{
		final Prepayment prepayment = new Prepayment();
		prepayment.setUser(user);

		final MonetaryAmount amount = new MonetaryAmount();
		amount.setAmount(BigDecimal.TEN);
		amount.setCurrency(AvailableCurrency.EUR);

		prepayment.setAmount(amount);
		prepayment.setBankAccount(bankAccount);
		prepayment.setInvoice(invoice);
		prepayment.setExternalReferenceId(externalReferenceId);
		invoice.getPayments().add(prepayment);
		return prepayment;
	}
	
	@Transactional
	public Prepayment persistPrepayment(User user, BankAccount bankAccount, Invoice invoice)
	{
		return persistPrepayment(user, bankAccount, invoice, "1");
	}
	
	public Prepayment createRefund(Prepayment paymentToRefund)
	{
		Prepayment refundPayment = createPrepayment(paymentToRefund.getUser(),
				paymentToRefund.getBankAccount(), paymentToRefund.getInvoice(), "2");
		refundPayment.setState(PaymentState.PENDING);
		refundPayment.setPaymentToRefund(paymentToRefund);
		MonetaryAmount refundAmount = new MonetaryAmount(BigDecimal.valueOf(-90), AvailableCurrency.EUR);
		refundPayment.setAmount(refundAmount);
		
		return prepaymentRepository.save(refundPayment);
	}

	@Transactional
	public Prepayment persistRefund(Prepayment paymentToRefund)
	{
		final Prepayment prepayment = createRefund(paymentToRefund);
		
		return prepaymentRepository.save(prepayment);
	}

}
