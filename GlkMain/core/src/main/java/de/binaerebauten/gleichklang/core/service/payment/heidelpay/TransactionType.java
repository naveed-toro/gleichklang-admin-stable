package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

/**
 * Enumerates the used heidelpay transaction types with their corresponding code.
 */
public enum TransactionType
{
	REGISTRATION("RG"),
	REREGISTRATION("RR"),
	DEREGISTRATION("DR"),

	SCHEDULE("SD"),
	RESCHEDULE("RS"),
	DESCHEDULE("DS"),

	CHARGEBACK("CB"),
	REVERSAL("RV"),

	RESEVATION("PA"),

	REFUND("RF"),
	DEBIT("DB");
	
	private final String code;

	TransactionType(String code)
	{
		this.code = code;
	}

	/**
	 * Converts a heidelpay transaction type code to a value of this enum.
	 *
	 * @param code the non-null transaction type code
	 * @return the corresponding enum value
	 */
	public static TransactionType fromCode(String code)
	{
		for (TransactionType t : values())
		{
			if (t.code.equals(code))
			{
				return t;
			}
		}
		throw new IllegalArgumentException("Invalid transaction type code:" + code);
	}
	
	public String getCode()
	{
		return code;
	}

}
