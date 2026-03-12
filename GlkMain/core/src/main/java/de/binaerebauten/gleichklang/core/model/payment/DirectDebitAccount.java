package de.binaerebauten.gleichklang.core.model.payment;

/**
 * Represents the direct debit account information to pass the registration method as an argument.
 */
public class DirectDebitAccount extends Account
{
	private String bank;

	private String bankName;

	/**
	 * A two-letters country code.
	 */
	private String country;

	public String getBank()
	{
		return bank;
	}

	public void setBank(String bank)
	{
		this.bank = bank;
	}

	public String getBankName()
	{
		return bankName;
	}

	public void setBankName(String bankName)
	{
		this.bankName = bankName;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}
}
