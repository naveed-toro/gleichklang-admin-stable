package de.binaerebauten.gleichklang.core.service.payment.heidelpay.processor;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayTransactionService;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * Processor for {@link TransactionType#REGISTRATION} heidelpay transactions.
 */
@Service
public class HeidelpayRegistrationProcessor
		extends AbstractHeidelpayTransactionProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(HeidelpayRegistrationProcessor.class);

	private final ExternalPaymentService externalPaymentService;

	private final InvoiceService invoiceService;

	private final PaymentService paymentService;

	/**
	 * This service uses constructor based dependency injection to ease testing.
	 *
	 * @param externalPaymentRepository
	 * @param heidelpayTransactionService
	 * @param externalPaymentService
	 * @param invoiceService
	 * @param paymentService
	 */
	@Autowired
	public HeidelpayRegistrationProcessor(ExternalPaymentRepository externalPaymentRepository,
			HeidelpayTransactionService heidelpayTransactionService,
			ExternalPaymentService externalPaymentService, InvoiceService invoiceService,
			PaymentService paymentService)
	{
		super(externalPaymentRepository, heidelpayTransactionService, TransactionType.REGISTRATION);
		this.externalPaymentService = externalPaymentService;
		this.invoiceService = invoiceService;
		this.paymentService = paymentService;
	}

	@Transactional
	@Override
	public void process(TransactionResponseType transactionResponse) throws PaymentException
	{
		String externalId = transactionResponse.getIdentification().getUniqueID();
		if (!isProcessed(externalId))
		{
			ProcessingResultType processingResult = ProcessingResultType.from(transactionResponse.getProcessing());
			TransactionID transactionID = heidelpayTransactionService.getTransactionID(transactionResponse);
			switch (transactionID.getType())
			{
				case REGISTRATION:
					requestInitialPayment(transactionResponse, processingResult, transactionID, externalId);
					break;
				case CHANGE_REGISTRATION:
					 updateExternalPaymentRegistration(transactionResponse, processingResult, transactionID);
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
	}

	private void updateExternalPaymentRegistration(TransactionResponseType transactionResponse,
			ProcessingResultType processingResult, TransactionID transactionID)
			throws PaymentException
	{
		ExternalPaymentRegistration externalPaymentRegistration = heidelpayTransactionService.getExternalPaymentRegistration(transactionID);
		if (processingResult == ProcessingResultType.ACK)
		{
			User user = externalPaymentRegistration.getUser();
			String registrationId = transactionResponse.getIdentification().getUniqueID();
			externalPaymentRegistration.setRegistrationId(registrationId);
			
			PaymentMethod paymentMethod = heidelpayTransactionService.getPaymentMethod(transactionResponse);
			paymentService.updateUserPaymentSettings(user, paymentMethod);
		}
		else
		{
			LOG.error("Registration failed with result: {} for external payment registration: {}",
					processingResult, externalPaymentRegistration.getId());
		}
	}

	private void requestInitialPayment(TransactionResponseType transactionResponse,
			ProcessingResultType processingResult, TransactionID transactionID,
			String externalId) throws PaymentException
	{
		ExternalPayment payment = heidelpayTransactionService.getPayment(transactionID);
		payment.setExternalId(externalId);

		if (processingResult == ProcessingResultType.ACK)
		{
			User user = payment.getUser();

			String registrationId = transactionResponse.getIdentification().getUniqueID();

			ExternalPaymentRegistration externalPaymentRegistration =
					externalPaymentService.getOrCreateExternalPaymentRegistration(user, registrationId);
			externalPaymentRegistration.setExternalReferenceId(transactionID.getId());

			PaymentMethod paymentMethod = heidelpayTransactionService.getPaymentMethod(transactionResponse);
			paymentService.createOrUpdateUserPaymentSettings(user, paymentMethod);

			externalPaymentService.requestPayment(payment);
		}
		else
		{
			invoiceService.paymentFailed(payment);
			LOG.error("Registration failed with result: {} for payment: {}", processingResult, payment.getId());
		}
	}
}
