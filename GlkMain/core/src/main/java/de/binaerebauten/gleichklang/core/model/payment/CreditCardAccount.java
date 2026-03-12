package de.binaerebauten.gleichklang.core.model.payment;

import java.time.YearMonth;

/**
 * Represents the credit card information to pass the registration method as an argument.
 */
public class CreditCardAccount extends Account
{
	private String brand;

	private YearMonth expirationDate;

	private String verification;

	public String getBrand()
	{
		return brand;
	}

	public void setBrand(String brand)
	{
		this.brand = brand;
	}

	public YearMonth getExpirationDate()
	{
		return expirationDate;
	}

	public void setExpirationDate(YearMonth expirationDate)
	{
		this.expirationDate = expirationDate;
	}

	public String getVerification()
	{
		return verification;
	}

	public void setVerification(String verification)
	{
		this.verification = verification;
	}
}
