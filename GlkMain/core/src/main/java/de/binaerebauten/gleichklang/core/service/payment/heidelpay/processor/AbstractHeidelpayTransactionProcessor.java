package de.binaerebauten.gleichklang.core.service.payment.heidelpay.processor;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayTransactionService;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.PaymentCode;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Optional;

/**
 * Abstract base class for {@link HeidelpayTransactionProcessor} implementation.
 * Provides some common helper methods for processing
 * {@link de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType}.
 */
public abstract class AbstractHeidelpayTransactionProcessor implements HeidelpayTransactionProcessor
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractHeidelpayTransactionProcessor.class);

	private final ExternalPaymentRepository externalPaymentRepository;

	protected HeidelpayTransactionService heidelpayTransactionService;

	private final TransactionType transactionType;

	/**
	 * This service uses constructor based dependency injection to ease testing.
	 *
	 * @param heidelpayTransactionService
	 * @param transactionType
	 */
	@Autowired
	public AbstractHeidelpayTransactionProcessor(ExternalPaymentRepository externalPaymentRepository,
			HeidelpayTransactionService heidelpayTransactionService, TransactionType transactionType)
	{
		Objects.requireNonNull(transactionType, "transactionType == null");

		this.externalPaymentRepository = externalPaymentRepository;
		this.heidelpayTransactionService = heidelpayTransactionService;
		this.transactionType = transactionType;
	}

	@Override
	public boolean canProcess(TransactionResponseType transactionResponse)
	{
		PaymentCode paymentCode = heidelpayTransactionService.getPaymentCode(transactionResponse);

		return paymentCode.transactionType == transactionType;
	}

	/**
	 * Checks whether this transaction notification was processed earlier.
	 *
	 * @param externalId
	 * @return
	 */
	protected boolean isProcessed(String externalId)
	{
		// Check whether we got this transaction notification earlier
		Optional<ExternalPayment> existingPayment = externalPaymentRepository.findByExternalId(externalId);
		existingPayment.ifPresent(payment -> LOG.info("Ignored duplicated payment notification", payment.getExternalReferenceId()));
		return existingPayment.isPresent();
	}
}
