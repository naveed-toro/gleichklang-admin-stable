package de.binaerebauten.gleichklang.core.service.payment.heidelpay.processor;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayTransactionService;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate.RENEWAL_FAILED_ACTUAL;

/**
 * Processor for {@link TransactionType#DEBIT} transactions.
 */
@Service
public class HeidelpayDebitTransactionProcessor extends AbstractHeidelpayTransactionProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(HeidelpayDebitTransactionProcessor.class);
	
	private final InvoiceService invoiceService;
	
	private final MailSendService mailSendService;
	
	private final UserMailTemplateService userMailTemplateService;
	
	/**
	 * This service uses constructor based dependency injection to ease testing.
	 *
	 * @param externalPaymentRepository
	 * @param heidelpayTransactionService
	 * @param invoiceService
	 * @param mailSendService
	 * @param userMailTemplateService
	 */
	@Autowired
	public HeidelpayDebitTransactionProcessor(ExternalPaymentRepository externalPaymentRepository,
			HeidelpayTransactionService heidelpayTransactionService, InvoiceService invoiceService,
			MailSendService mailSendService, UserMailTemplateService userMailTemplateService)
	{
		super(externalPaymentRepository, heidelpayTransactionService, TransactionType.DEBIT);
		this.invoiceService = invoiceService;
		this.mailSendService = mailSendService;
		this.userMailTemplateService = userMailTemplateService;
	}

	@Transactional
	@Override
	public void process(TransactionResponseType transactionResponse) throws PaymentException
	{
		String externalId = transactionResponse.getIdentification().getUniqueID();
		if (!isProcessed(externalId))
		{
			TransactionID transactionId = heidelpayTransactionService.getTransactionID(transactionResponse);
			ExternalPayment payment = heidelpayTransactionService.getPayment(transactionId);
			
			ProcessingResultType processingResult = ProcessingResultType.from(transactionResponse.getProcessing());
			if (processingResult == ProcessingResultType.ACK)
			{
				switch (transactionId.getType())
				{
					case PAYMENT:
						payment.setExternalId(externalId);
						invoiceService.paymentReceived(payment,null);
						break;
					default:
						throw new PaymentException("Received unknown payment", transactionId.toString());
				}
			}
			else
			{
				LOG.error("Debit failed with result: {} with external id: {}", processingResult, externalId);
				
				switch (transactionId.getType())
				{
					case PAYMENT:
						invoiceService.paymentFailed(payment);
						
						final User user = payment.getUser();
						final Product product = payment.getBaseProduct();
						final Invoice invoice = invoiceService.createAndSaveInvoice(user, product, PaymentMethod.PREPAYMENT);
						final Prepayment prepayment = (Prepayment) invoice.getPayments().stream()
								.filter(p -> PaymentState.PENDING.equals(p.getState()))
								.findFirst()
								.orElseThrow(() -> new PaymentException("Received unknown payment for user", user.getEmail()));
						
						mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(RENEWAL_FAILED_ACTUAL, prepayment));
						
						LOG.warn("External-Payment {} failed, Prepayment is created!", payment.getId());
						break;
					default:
						throw new PaymentException("Received unknown payment", transactionId.toString());
				}
			}
		}
	}
	
}
