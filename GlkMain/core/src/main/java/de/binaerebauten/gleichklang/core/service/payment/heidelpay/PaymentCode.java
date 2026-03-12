package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import com.google.common.base.Joiner;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small helper class to convert a {@link PaymentMethod} and {@link TransactionType}
 * to a heidelpay payment code.
 *
 * @see de.binaerebauten.gleichklang.core.model.heidelpay.query.PaymentRequestType#setCode(String)
 */
public class PaymentCode
{
	private final static Pattern PAYMENT_CODE_REGEX = Pattern.compile("([A-Z][A-Z])\\.([A-Z][A-Z])");

	public final PaymentMethod paymentMethod;

	public final TransactionType transactionType;

	private PaymentCode(PaymentMethod paymentMethod, TransactionType transactionType)
	{
		Objects.requireNonNull(paymentMethod, "paymentMethod == null");
		Objects.requireNonNull(transactionType, "transactionType == null");

		this.paymentMethod = paymentMethod;
		this.transactionType = transactionType;
	}

	/**
	 * Converts the given parameter to a heidelpay payment code.
	 *
	 * @param paymentMethod the non-null payment method
	 * @param transactionType the non-null transaction type
	 * @return
	 */
	public static String of(PaymentMethod paymentMethod, TransactionType transactionType)
	{
		return new PaymentCode(paymentMethod, transactionType).toString();
	}

	/**
	 * Parses the given heidelpay payment code.
	 *
	 * @param code the non-null heidelpay payment code (e.g. "DB.DR")
	 *
	 * @return the parsed payment code
	 */
	public static PaymentCode parse(String code)
	{
		Objects.requireNonNull(code, "code == null");

		final Matcher matcher = PAYMENT_CODE_REGEX.matcher(code);
		if (matcher.matches())
		{
			final PaymentMethod paymentMethod = HeidelpayPaymentMethodCode.asPaymentMethod(matcher.group(1));
			final TransactionType transactionType = TransactionType.fromCode(matcher.group(2));

			return new PaymentCode(paymentMethod, transactionType);
		}
		else
		{
			throw new IllegalArgumentException("Invalid payment code: " + code);
		}
	}

	@Override
	public String toString()
	{
		return Joiner.on(".")
				.join(HeidelpayPaymentMethodCode.asCode(paymentMethod), transactionType.getCode())
				.toString();
	}
}
