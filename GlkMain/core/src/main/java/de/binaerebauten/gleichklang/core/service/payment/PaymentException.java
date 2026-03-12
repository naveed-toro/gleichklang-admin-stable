package de.binaerebauten.gleichklang.core.service.payment;

/**
 * A general payment exception.
 *
 * @author matthias.koester@binaere-bauten.de
 */
public class PaymentException extends Exception
{
	private final String code;

	public PaymentException(String message, String code)
	{
		super(message);
		this.code = code;
	}

	public PaymentException(String message, int code)
	{
		super(message);
		this.code = Integer.toString(code);
	}
	
	public PaymentException(String message)
	{
		super(message);
		code = null;
	}

	public PaymentException(String message, Throwable cause)
	{
		super(message, cause);
		this.code = null;
	}

	public PaymentException(Throwable cause)
	{
		super(cause);
		this.code = null;
	}

	public String getCode()
	{
		return code;
	}
	
	@Override
	public String getMessage()
	{
		return String.format("%s: %s", super.getMessage(), code);
	}
}
