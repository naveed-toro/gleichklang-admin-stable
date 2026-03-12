package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.ProcessingType;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;

import java.util.Objects;

/**
 * Enumerates all supported types for {@link ProcessingType#getResult()}
 */
public enum ProcessingResultType
{
	ACK,
	NOK,
	UNKNOWN;

	public static ProcessingResultType from(ProcessingType processingResult)
	{
		Objects.requireNonNull(processingResult, "processingResult == null");

		try
		{
			return valueOf(processingResult.getResult());
		}
		catch (RuntimeException e)
		{
			return UNKNOWN;
		}
	}

	public static PaymentState to(ProcessingType processingResult)
	{
		switch (ProcessingResultType.from(processingResult))
		{
			case ACK:
				return PaymentState.PAID;
			case NOK:
				return PaymentState.FAILED;
			default:
				return PaymentState.FAILED;
		}
	}
}
