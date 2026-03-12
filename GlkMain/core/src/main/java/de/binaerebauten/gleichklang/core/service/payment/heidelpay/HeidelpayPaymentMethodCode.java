package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;

/**
 * Enumerates all used heidelpay payment methods.
 */
public enum HeidelpayPaymentMethodCode
{
	CREDIT_CARD("CC", PaymentMethod.CREDIT_CARD),
	DIRECT_DEBIT("DD", PaymentMethod.DIRECT_DEBIT);

	HeidelpayPaymentMethodCode(String code, PaymentMethod paymentMethod)
	{
		this.code = code;
		this.paymentMethod = paymentMethod;
	}

	public final String code;

	public final PaymentMethod paymentMethod;

	/**
	 * Conversts the given payment method to a heidelpay payment method code.
	 *
	 * @param paymentMethod
	 * @return the corresponding heidelpay payment method code
	 * @throws IllegalArgumentException when the given payment method isn't used for heidelpay
	 *                                  (e.g. {@link PaymentMethod#PREPAYMENT}).
	 */
	public static HeidelpayPaymentMethodCode from(PaymentMethod paymentMethod)
	{
		for (HeidelpayPaymentMethodCode paymentMethodCode : values())
		{
			if (paymentMethodCode.paymentMethod.equals(paymentMethod))
			{
				return paymentMethodCode;
			}
		}
		throw new IllegalArgumentException("Payment method not supported: " + paymentMethod);
	}

	/**
	 * Conversts the given payment method to a heidelpay payment method code.
	 *
	 * @param paymentMethod
	 * @return the corresponding heidelpay payment method code
	 * @throws IllegalArgumentException when the given payment method isn't used for heidelpay
	 *                                  (e.g. {@link PaymentMethod#PREPAYMENT}).
	 */
	public static String asCode(PaymentMethod paymentMethod)
	{
		for (HeidelpayPaymentMethodCode paymentMethodCode : values())
		{
			if (paymentMethodCode.paymentMethod.equals(paymentMethod))
			{
				return paymentMethodCode.code;
			}
		}
		throw new IllegalArgumentException("Payment method not supported: " + paymentMethod);
	}

	public static PaymentMethod asPaymentMethod(String code)
	{
		for (HeidelpayPaymentMethodCode paymentMethodCode : values())
		{
			if (paymentMethodCode.code.equals(code))
			{
				return paymentMethodCode.paymentMethod;
			}
		}
		throw new IllegalArgumentException("Payment method not code supported:" + code);
	}
}
