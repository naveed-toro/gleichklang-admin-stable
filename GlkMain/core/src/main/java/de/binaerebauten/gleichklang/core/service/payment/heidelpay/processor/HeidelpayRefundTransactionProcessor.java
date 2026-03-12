package de.binaerebauten.gleichklang.core.service.payment.heidelpay.processor;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
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
 * Processor for {@link TransactionType#REFUND} transactions.
 */
@Service
public class HeidelpayRefundTransactionProcessor
		extends AbstractHeidelpayTransactionProcessor
{
	private final static Logger LOG = LoggerFactory.getLogger(HeidelpayRefundTransactionProcessor.class);

	private InvoiceService invoiceService;

	/**
	 * This service uses constructor based dependency injection to ease testing.
	 *
	 * @param externalPaymentRepository
	 * @param heidelpayTransactionService
	 * @param invoiceService
	 */
	@Autowired
	public HeidelpayRefundTransactionProcessor(ExternalPaymentRepository externalPaymentRepository,
			HeidelpayTransactionService heidelpayTransactionService,
			InvoiceService invoiceService)
	{
		super(externalPaymentRepository, heidelpayTransactionService, TransactionType.REFUND);
		this.invoiceService = invoiceService;
	}

	@Transactional
	@Override
	public void process(TransactionResponseType transactionResponse)
	{
		String externalId = transactionResponse.getIdentification().getUniqueID();
		if (!isProcessed(externalId))
		{
			TransactionID transactionID = heidelpayTransactionService.getTransactionID(transactionResponse);
			ExternalPayment payment = heidelpayTransactionService.getPayment(transactionID);
			payment.setExternalId(externalId);
			
			ProcessingResultType processingResult = ProcessingResultType.from(transactionResponse.getProcessing());
			if (processingResult == ProcessingResultType.ACK)
			{
				invoiceService.paymentReceived(payment,null);
			}
			else
			{
				invoiceService.paymentFailed(payment);
				LOG.error("Refund failed with result: {} for payment: {}", processingResult, payment.getId());
			}
		}
	}
}
