package de.binaerebauten.gleichklang.core.service.payment.heidelpay.processor;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
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
 * Processor for {@link TransactionType#REREGISTRATION} heidelpay transactions.
 */
@Service
public class HeidelpayReregistrationProcessor
		extends AbstractHeidelpayTransactionProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(HeidelpayReregistrationProcessor.class);

	private final PaymentService paymentService;

	/**
	 * This service uses constructor based dependency injection to ease testing.
	 *
	 * @param externalPaymentRepository
	 * @param heidelpayTransactionService
	 * @param paymentService
	 */
	@Autowired
	public HeidelpayReregistrationProcessor(ExternalPaymentRepository externalPaymentRepository,
			HeidelpayTransactionService heidelpayTransactionService,
			PaymentService paymentService)
	{
		super(externalPaymentRepository, heidelpayTransactionService, TransactionType.REREGISTRATION);
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
			PaymentMethod paymentMethod = heidelpayTransactionService.getPaymentMethod(transactionResponse);
			paymentService.updateUserPaymentSettings(user, paymentMethod);
		}
		else
		{
			LOG.error("Reregistration failed with result: {} for external payment registration: {}", processingResult, externalPaymentRegistration.getId());
		}
	}
}
