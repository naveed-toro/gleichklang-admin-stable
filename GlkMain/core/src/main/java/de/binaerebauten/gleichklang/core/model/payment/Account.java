package de.binaerebauten.gleichklang.core.model.payment;

/**
 * Abstract class to hold all information regarding a credit card, wallet or bank account.
 * This is not an entity, because we don't want to store this information in the DB.
 */
public abstract class Account
{
	private String holder;

	private String number;

	public String getHolder()
	{
		return holder;
	}

	public void setHolder(String holder)
	{
		this.holder = holder;
	}

	public String getNumber()
	{
		return number;
	}

	public void setNumber(String number)
	{
		this.number = number;
	}
}
