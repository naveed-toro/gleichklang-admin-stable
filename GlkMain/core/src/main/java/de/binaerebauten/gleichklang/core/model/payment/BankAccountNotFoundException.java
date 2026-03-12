package de.binaerebauten.gleichklang.core.model.payment;

public class BankAccountNotFoundException extends RuntimeException
{
	public BankAccountNotFoundException(String message)
	{
		super(message);
	}
}
