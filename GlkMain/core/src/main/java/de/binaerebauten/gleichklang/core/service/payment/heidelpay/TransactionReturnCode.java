package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import java.util.Objects;

/**
 * Enumerates all used heidelpay return codes.
 */
public enum TransactionReturnCode
{
	UNKNOWN("<UNKNOWN>"),

	NOT_SPECIFIED("000.100.200"),
	ACCOUNT_ERROR_INCORRECT("000.100.201"),
	ACCOUNT_ERROR_CANCELLED("000.100.202"),
	INSUFFICIENT("000.100.203"),
	MANDATE_INVALID("000.100.204"),
	MANDATE_CANCELLED("000.100.205"),

	REVOCATION_OR_DISPUTE("000.100.206"),

	CANCELLATION_IN_NETWORK("000.100.207"),
	ACCOUNT_ERROR_BLOCKED("000.100.208"),
	ACCOUNT_ERROR_DOES_NOT_EXIST("000.100.209"),
	INVALID_AMOUNT("000.100.210"),

	OTHER("000.100.277");

	private final String code;

	TransactionReturnCode(String code)
	{
		Objects.requireNonNull(code, "code == null");
		this.code = code;
	}

	public static TransactionReturnCode fromCode(String code)
	{
		Objects.requireNonNull(code, "code == null");

		for (TransactionReturnCode value : values())
		{
			if (value.code.equals(code))
			{
				return value;
			}
		}
		return UNKNOWN;
	}

	public String getCode()
	{
		return code;
	}

}
