package de.binaerebauten.gleichklang.memberweb.service.payment.heidelpay;

import com.google.common.collect.ImmutableMap;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRegistrationRepository;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayTransactionService;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID;
import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory;
import de.binaerebauten.gleichklang.memberweb.presenter.SubscriptionDetailsPresenter;
import de.binaerebauten.gleichklang.memberweb.view.component.RegistrationWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * This controller implements the heidelpay hco response callback.
 */
@RestController
public class HeidelpayHcoResponseController
{
	private static final Logger LOG = LoggerFactory.getLogger(HeidelpayHcoResponseController.class);
	
	@Autowired
	private AppUrlBuilder appUrlBuilder;
	
	@Autowired
	private ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;
	
	@Autowired
	private PaymentRepository paymentRepository;
	
	@Autowired
	private ExternalPaymentService externalPaymentService;
	
	@Autowired
	private HeidelpayTransactionService heidelpayTransactionService;
	
	@Autowired
	private PaymentService paymentService;
	
	@Value("${heidelpay.frontend.response_url}")
	private URI frontendResponseUrl;
	
	/**
	 * Returns the current config that is relevant for this controller.
	 *
	 * @return the json representation of the config
	 */
	@RequestMapping(value = "heidelpay/config", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> getConfig()
	{
		return ImmutableMap.of("frontendResponseUrl", frontendResponseUrl);
	}
	
	/**
	 * This endpoint is called by heidelpay when a user has finished a payment
	 * transaction via the {@link SubscriptionDetailsPresenter}
	 *
	 * @param params the incoming heidelpay parameter {@link HeidelpayHcoMessage}
	 * @return the forward url to the response view.
	 */
	@RequestMapping(value = "heidelpay/response", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<String> handleResponse(@RequestBody MultiValueMap<String, String> params)
	{
		final Map<String, String> valueMap = params.toSingleValueMap();
		final HeidelpayHcoMessage message = HeidelpayHcoMessage.fromMap(valueMap);
		final URI forwardUrl = handleResponseMessage(message);
		
		LOG.info("forward after heidelpay/response to {}", forwardUrl);
		
		return ResponseEntity.ok(forwardUrl.toString());
	}
	
	private URI handleResponseMessage(HeidelpayHcoMessage message)
	{
		final URI forwardUrl;
		final TransactionID transactionID = TransactionID.parse(message.getIdentificationTransactionId());
		
		LOG.info("Heildepay::handleResponse {} {} of type {}", message.getProcessingResult(), message.getIdentificationTransactionId(), message.getTransactionType());
		
		if (message.getProcessingResult() == ProcessingResultType.ACK)
		{
			switch (transactionID.getType())
			{
				case REGISTRATION:
					final ExternalPayment payment = heidelpayTransactionService.getPayment(transactionID);
					final String registrationId = message.getIdentificationUniqueId();
					externalPaymentService.getOrCreateExternalPaymentRegistration(payment.getUser(), registrationId);
					forwardUrl = getInitialPaymentForwardUrl(true, null);
					break;
				case CHANGE_REGISTRATION:
					updateUserPaymentSettings(transactionID, message.getIdentificationUniqueId(), message.getPaymentMethod());
					forwardUrl = getPaymentDataForwardUrl(true, null);
					break;
				default:
					throw new IllegalArgumentException("Invalid transaction id type:" + transactionID.getType());
			}
		}
		else if (message.getProcessingResult() == ProcessingResultType.NOK)
		{
			LOG.warn("Payment cancelled with return code: {} return: {}",
					message.getProcessingReturnCode(), message.getProcessingReturn());
			
			switch (transactionID.getType())
			{
				case REGISTRATION:
					forwardUrl = getInitialPaymentForwardUrl(false, message.getProcessingReturn());
					break;
				case CHANGE_REGISTRATION:
					forwardUrl = getPaymentDataForwardUrl(false, message.getProcessingReturn());
					break;
				default:
					throw new IllegalArgumentException("Invalid transaction id type:" + transactionID.getType());
			}
		}
		else
		{
			LOG.error("Payment failed with reason post validation: {} return code: {} return: {}",
					message.getPostValidation(), message.getProcessingReturnCode(),
					message.getProcessingReturn());
			throw new IllegalStateException();
		}
		
		return forwardUrl;
	}
	
	private void updateUserPaymentSettings(TransactionID transactionID, String registrationId, PaymentMethod paymentMethod)
	{
		final ExternalPaymentRegistration externalPaymentRegistration = heidelpayTransactionService.getExternalPaymentRegistration(transactionID);
		if (externalPaymentRegistration.getRegistrationId() == null)
		{
			externalPaymentRegistration.setRegistrationId(registrationId);
			externalPaymentRegistrationRepository.save(externalPaymentRegistration);
		}
		
		final User user = externalPaymentRegistration.getUser();
		try
		{
			// Auto renewal job is created in {@link HeidelpayRegistrationProcessor}
			paymentService.updateUserPaymentSettings(user, paymentMethod);
			
			// Process pending payments
			final Optional<AbstractPayment> currentPayment = paymentRepository.findCurrentPayment(user);
			if (currentPayment.isPresent())
			{
				final AbstractPayment payment = currentPayment.get();
				if (!PaymentMethod.PREPAYMENT.equals(payment.getMethod()) && PaymentState.PENDING.equals(payment.getState()))
				{
					LOG.info("Heidelpay::updateUserPaymentSettings. User payment settings updated for userID({}). " +
									"Current paymentID({}) exists and so payment is being requested for transaction {}",
							user.getId(), payment.getId(), transactionID.toString());
					externalPaymentService.requestPayment((ExternalPayment) payment);
				}
			}
		}
		catch (PaymentException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private URI getInitialPaymentForwardUrl(boolean transactionAcknowledged, String errorMsg)
	{
		final String fragment = transactionAcknowledged ?
				RegistrationWizard.REGISTRATION_PAYMENT_RESULT_STEP :
				String.format("%s%s%s", RegistrationWizard.REGISTRATION_PAYMENT_STEP, RegistrationWizard.ERROR_PARAM, errorMsg);
		return appUrlBuilder.toVaadinFragment(fragment);
	}
	
	private URI getPaymentDataForwardUrl(boolean transactionAcknowledged, String errorMsg)
	{
		final String triggerReloadParam = Long.toString(System.currentTimeMillis(), 32);
		final String mask = transactionAcknowledged ?
				"!%s/%s" + SubscriptionDetailsPresenter.SUCCESS_PARAM :
				"!%s/%s" + SubscriptionDetailsPresenter.ERROR_PARAM + errorMsg;
		final String fragment = String.format(mask, DefaultNavigatorFactory.SUBSCRIPTION_VIEW.name(), triggerReloadParam);
		return appUrlBuilder.toVaadinFragment(fragment);
	}
	
}
