package de.binaerebauten.gleichklang.core.service.payment.heidelpay.processor;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayTransactionResponseController;

/**
 * This interface is used by the {@link HeidelpayTransactionResponseController}
 * to process incoming transactions.
 */
public interface HeidelpayTransactionProcessor
{
	/**
	 * Returns true iff. the given transaction can be processed by this processor.
	 *
	 * @param transactionResponse the non-null transaction response
	 * @return true iff. this processor can process the given transaction response
	 */
	boolean canProcess(TransactionResponseType transactionResponse);

	/**
	 * Is called when {@link #canProcess(TransactionResponseType)} returns true
	 * to process the given transaction response.
	 *
	 * @param transactionResponse the non-null transaction response to process
	 * @throws PaymentException
	 */
	void process(TransactionResponseType transactionResponse)
			throws PaymentException;
}
