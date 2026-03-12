package de.binaerebauten.gleichklang.memberweb.service.payment.heidelpay;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.CustomerType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRegistrationRepository;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.*;
import de.binaerebauten.gleichklang.memberweb.service.payment.ExternalPaymentFormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.URI;

/**
 * Implementation of {@link ExternalPaymentFormService} for heidelpay.
 */
@Service
public class HeidelpayFormService implements ExternalPaymentFormService
{
	private static final Logger LOG = LoggerFactory.getLogger(HeidelpayFormService.class);

	@Autowired
	private HeidelpayHcoApiConnector hcoApiConnector;

	@Autowired
	private ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;

	@Autowired
	private HeidelpayQueryApiConnector heidelpayQueryApiConnector;

	@Autowired
	private ExternalPaymentService externalPaymentService;

	@Override
	public URI getInitialRegistrationFormUrl(ExternalPayment externalPayment)
			throws PaymentException
	{
		return hcoApiConnector.getInitialRegistrationFormUrl(externalPayment);
	}

	@Override
	public URI getPaymentDataUpdateFormUrl(User user, PaymentMethod paymentMethod)
			throws PaymentException
	{
		ExternalPaymentRegistration externalPaymentRegistration =
				externalPaymentRegistrationRepository.findByUser(user);

		CustomerType customer = getCustomer(externalPaymentRegistration);

		return hcoApiConnector.getPaymentDataUpdateFormUrl(customer, paymentMethod, externalPaymentRegistration);
	}

	@Transactional
	@Override
	public URI getPaymentMethodChangeFormUrl(User user, PaymentMethod paymentMethod)
			throws PaymentException
	{
		ExternalPaymentRegistration externalPaymentRegistration = externalPaymentRegistrationRepository.findByUser(user);
		if (externalPaymentRegistration == null)
		{
			externalPaymentRegistration = externalPaymentService.createExternalPaymentRegistration(user, null);
			return hcoApiConnector.getPaymentRegistrationFormUrl(externalPaymentRegistration, paymentMethod);
		}
		else if (externalPaymentRegistration.getRegistrationId() == null)
		{
			// Means that registration was created, but the registration process was not finished (at least correctly)
			return hcoApiConnector.getPaymentRegistrationFormUrl(externalPaymentRegistration, paymentMethod);
		}
		else
		{
			CustomerType customer = getCustomer(externalPaymentRegistration);
			return hcoApiConnector.getPaymentDataUpdateFormUrl(customer, paymentMethod, externalPaymentRegistration);
		}
	}

	private CustomerType getCustomer(ExternalPaymentRegistration externalPaymentRegistration)
			throws PaymentException
	{
		TransactionResponseType transactionResponse = heidelpayQueryApiConnector.getRegistrationTransaction(externalPaymentRegistration);
		return transactionResponse.getCustomer();
	}

}
