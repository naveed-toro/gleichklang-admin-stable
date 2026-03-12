package de.binaerebauten.gleichklang.core.service.payment.heidelpay.processor;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ProcessingType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.MailTemplateInstance;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.payment.ChargebackReason;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate.*;
import static de.binaerebauten.gleichklang.core.service.payment.ChargebackReason.INSUFFICIENT;
import static de.binaerebauten.gleichklang.core.service.payment.ChargebackReason.OTHER;
import static de.binaerebauten.gleichklang.core.utils.FunctionalUtils.nullSafe;

/**
 * Processor for {@link TransactionType#CHARGEBACK} transactions.
 * <p/>
 * Triggers cancelation of external payment and depending on the {@link TransactionReturnCode}
 * triggers a change to prepayment or to create a {@link de.binaerebauten.gleichklang.core.model.payment.Chargeback}
 * payment.
 */
@Service
public class HeidelpayChargebackProcessor extends AbstractHeidelpayTransactionProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(HeidelpayChargebackProcessor.class);

	private final InvoiceService invoiceService;

	private final MailSendService mailSendService;

	private final UserMailTemplateService userMailTemplateService;
	
	private final SubscriptionService subscriptionService;

	/**
	 * This service uses constructor based dependency injection to ease testing.
	 *
	 * @param externalPaymentRepository
	 * @param heidelpayTransactionService
	 * @param invoiceService
	 */
	@Autowired
	public HeidelpayChargebackProcessor(ExternalPaymentRepository externalPaymentRepository,
			HeidelpayTransactionService heidelpayTransactionService,
			InvoiceService invoiceService, MailSendService mailSendService,
			UserMailTemplateService userMailTemplateService,
			SubscriptionService subscriptionService)
	{
		super(externalPaymentRepository, heidelpayTransactionService, TransactionType.CHARGEBACK);
		this.invoiceService = invoiceService;
		this.mailSendService = mailSendService;
		this.userMailTemplateService = userMailTemplateService;
		this.subscriptionService = subscriptionService;
	}

	@Override
	public void process(TransactionResponseType transactionResponse)
	{
		final String externalId = transactionResponse.getIdentification().getUniqueID();
		if (!isProcessed(externalId))
		{
			final String referenceId = transactionResponse.getIdentification().getReferenceID();
			final Optional<ExternalPayment> optionalPayment = heidelpayTransactionService.getPayment(referenceId);
			
			if(!optionalPayment.isPresent())
			{
				LOG.error("No payment found for chargeback with uniqueId {} and refUniqueId {}", externalId, referenceId);
				return;
			}
			
			final ExternalPayment payment = optionalPayment.get();
			payment.setExternalId(externalId);
			
			final ProcessingType processing = transactionResponse.getProcessing();
			heidelpayTransactionService.log(payment, processing);
			
			ProcessingResultType processingResult = ProcessingResultType.from(processing);
			if (processingResult == ProcessingResultType.ACK)
			{
				if (PaymentState.PAID.equals(payment.getState()))
				{
					Optional<TransactionReturnCode> transactionReturnCodeOptional =
							nullSafe(() -> TransactionReturnCode.fromCode(transactionResponse.getProcessing().getReturn().getCode()));
					
					Preconditions.checkArgument(transactionReturnCodeOptional.isPresent(), "transactionReturnCode is not present");

					TransactionReturnCode transactionReturnCode = transactionReturnCodeOptional.get();
					LOG.info("Received chargeback with return code {}", transactionReturnCode);
					
					// For test purposes
					if (TransactionReturnCode.NOT_SPECIFIED == transactionReturnCode)
					{
						String usage = transactionResponse.getPayment().getPresentation().getUsage();
						transactionReturnCode = TransactionReturnCode.fromCode(usage);
					}

					switch (transactionReturnCode)
					{
						case REVOCATION_OR_DISPUTE:
							invoiceService.cancelExternalPaymentAndChargeback(payment);
							if (payment.getInvoice().getItems().stream().anyMatch(i ->
									i.getProduct() instanceof InitialSubscriptionOffer || i.getProduct() instanceof RenewalOffer))
							{
								subscriptionService.findCurrentSubscription(payment.getUser()).ifPresent(subscriptionService::cancelSubscription);
							}
							break;
						case ACCOUNT_ERROR_INCORRECT:
							switchToPrepayment(payment, OTHER, CB_ACCOUNTERROR_FIRST, CB_ACCOUNTERROR_RENEWAL_FIRST);
							break;
						case INSUFFICIENT:
							// E-Mail will be sent from the MailReminder automatically
							invoiceService.cancelExternalPaymentAndCreatePrepayment(payment, INSUFFICIENT);
							break;
						case ACCOUNT_ERROR_CANCELLED:
						case MANDATE_INVALID:
						case MANDATE_CANCELLED:
						case CANCELLATION_IN_NETWORK:
						case ACCOUNT_ERROR_BLOCKED:
						case ACCOUNT_ERROR_DOES_NOT_EXIST:
						case INVALID_AMOUNT:
						case NOT_SPECIFIED:
						case OTHER:
							switchToPrepayment(payment, OTHER, CB_OTHER_FIRST, CB_OTHER_RENEWAL_FIRST);
							break;
						default:
							LOG.error("Ignored unknown chargeback transaction return code: {}", transactionReturnCode);
					}
				}
			}
			else
			{
				LOG.warn("Chargeback failed with result: {} for payment: {}", processingResult, payment.getId());
			}
		}
	}
	
	/**
	 * Cancels the external payment and creates a new prepayment.
	 * Sends a notification E-Mail.
	 *
	 * @param payment
	 * @param initialMailTemplate
	 * @param renewalMailTemplate
	 */
	private void switchToPrepayment(ExternalPayment payment, ChargebackReason reason,
			UserMailTemplate initialMailTemplate, UserMailTemplate renewalMailTemplate)
	{
		Prepayment prepayment = invoiceService.cancelExternalPaymentAndCreatePrepayment(payment, reason);

		Optional<InvoiceItem> invoiceItemOptional = payment.getInvoice().getItems().stream().findFirst();
		if (invoiceItemOptional.isPresent())
		{
			MailTemplateInstance mailTemplate = invoiceItemOptional.get().getProduct() instanceof RenewalOffer ?
					userMailTemplateService.createMailTemplateInstance(renewalMailTemplate, prepayment) :
					userMailTemplateService.createMailTemplateInstance(initialMailTemplate, prepayment);
			mailSendService.sendEmail(payment.getUser(), mailTemplate);
		}
	}
	
}
