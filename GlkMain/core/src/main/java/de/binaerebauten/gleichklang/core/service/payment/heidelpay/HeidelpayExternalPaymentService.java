package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRegistrationRepository;
import de.binaerebauten.gleichklang.core.repository.UserPaymentSettingsRepository;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the {@link ExternalPaymentService} for heidelpay.
 */
@Service
public class HeidelpayExternalPaymentService implements ExternalPaymentService
{
	private static final Logger LOG = LoggerFactory.getLogger(HeidelpayExternalPaymentService.class);
	
	private final ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;
	
	private final UserPaymentSettingsRepository userPaymentSettingsRepository;
	
	private final HeidelpayQueryApiConnector heidelpayQueryApiConnector;
	
	@Autowired
	public HeidelpayExternalPaymentService(
			ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository,
			UserPaymentSettingsRepository userPaymentSettingsRepository,
			HeidelpayQueryApiConnector heidelpayQueryApiConnector)
	{
		this.externalPaymentRegistrationRepository = externalPaymentRegistrationRepository;
		this.userPaymentSettingsRepository = userPaymentSettingsRepository;
		this.heidelpayQueryApiConnector = heidelpayQueryApiConnector;
	}
	
	@Transactional
	@Override
	public ExternalPaymentRegistration getOrCreateExternalPaymentRegistration(User user, String registrationId)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(registrationId, "registrationId == null");
		
		ExternalPaymentRegistration externalPaymentRegistration =
				externalPaymentRegistrationRepository.findByUserAndRegistrationId(user, registrationId);
		
		if (externalPaymentRegistration == null)
		{
			externalPaymentRegistration = createExternalPaymentRegistration(user, registrationId);
		}
		else
		{
			Preconditions.checkState(externalPaymentRegistration.getUser().equals(user),
					String.format("External registration id '%s' is used for user '%s', but requested for user '%s ",
							registrationId, externalPaymentRegistration.getUser().getId(), user.getId()));
		}
		
		return externalPaymentRegistration;
	}
	
	@Transactional
	@Override
	public boolean isRegistered(User user)
	{
		Objects.requireNonNull(user, "user == null");
		
		ExternalPaymentRegistration externalPaymentRegistration = externalPaymentRegistrationRepository.findByUser(user);
		
		return externalPaymentRegistration != null && externalPaymentRegistration.getRegistrationId() != null;
	}
	
	@Transactional
	@Override
	public ExternalPaymentRegistration createExternalPaymentRegistration(User user, String registrationId)
	{
		ExternalPaymentRegistration externalPaymentRegistration = new ExternalPaymentRegistration();
		
		externalPaymentRegistration.setLastUsedDate(LocalDateTime.now());
		externalPaymentRegistration.setRegistrationId(registrationId);
		externalPaymentRegistration.setUser(user);
		String externalReferenceId = UUID.randomUUID().toString();
		externalPaymentRegistration.setExternalReferenceId(externalReferenceId);
		
		externalPaymentRegistrationRepository.save(externalPaymentRegistration);
		return externalPaymentRegistration;
	}
	
	@Transactional
	@Override
	public void requestPayment(ExternalPayment externalPayment)
			throws PaymentException
	{
		Objects.requireNonNull(externalPayment, "externalPayment == null");
		Objects.requireNonNull(externalPayment.getUser(), "externalPayment.getUser() == null");
		
		final User user = externalPayment.getUser();
		final ExternalPaymentRegistration externalPaymentRegistration = externalPaymentRegistrationRepository.findByUser(user);
		
		if(externalPaymentRegistration == null)
		{
			throw new PaymentException("no externalPaymentRegistration for user " + user.getId() + " by requesting payment " + externalPayment.getId());
		}
		
		heidelpayQueryApiConnector.requestPayment(externalPayment, externalPaymentRegistration);
	}
	
	@Transactional
	@Override
	public void requestRefund(ExternalPayment paymentToRefund, ExternalPayment refundPayment)
			throws PaymentException
	{
		Objects.requireNonNull(paymentToRefund, "externalPayment == null");
		Objects.requireNonNull(refundPayment, "refundPayment == null");
		
		User user = paymentToRefund.getUser();
		ExternalPaymentRegistration externalPaymentRegistration = externalPaymentRegistrationRepository.findByUser(user);
		Objects.requireNonNull(externalPaymentRegistration, "externalPaymentRegistration == null");
		
		heidelpayQueryApiConnector.requestRefund(paymentToRefund, refundPayment, externalPaymentRegistration);
	}
	
	/**
	 * Process a deregistration.
	 *
	 * @param user the non-null user
	 * @throws PaymentException
	 */
	@Transactional
	@Override
	public void deregister(User user) throws PaymentException
	{
		Objects.requireNonNull(user, "user == null");
		
		ExternalPaymentRegistration externalPaymentRegistration = externalPaymentRegistrationRepository.findByUser(user);
		
		if (externalPaymentRegistration != null)
		{
			Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
			if (paymentSettings.isPresent())
			{
				heidelpayQueryApiConnector.deregister(paymentSettings.get(), externalPaymentRegistration);
			}
			else
			{
				LOG.error("No user payment settings found for user {}", user.getEmail());
			}
			
			externalPaymentRegistrationRepository.delete(externalPaymentRegistration);
		}
		else
		{
			LOG.warn("Trying to deregister user {} without an existing persistent external payment registration!", user.getId());
		}
	}
	
	@Transactional
	@Override
	public boolean usesExternalPayment(User user)
	{
		Objects.requireNonNull(user, "user == null");
		
		Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
		ExternalPaymentRegistration externalPaymentRegistration = externalPaymentRegistrationRepository.findByUser(user);
		
		return paymentSettings.isPresent() && paymentSettings.get().usesExternalPayment() && externalPaymentRegistration != null && externalPaymentRegistration.getRegistrationId() != null;
	}
	
}
