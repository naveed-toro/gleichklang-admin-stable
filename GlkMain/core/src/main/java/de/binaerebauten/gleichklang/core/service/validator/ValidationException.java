package de.binaerebauten.gleichklang.core.service.validator;

/**
 * Thrown when a validation fails.
 */
public class ValidationException extends Exception
{
	public ValidationException(Exception cause)
	{
		super(cause);
	}

	public ValidationException(String message)
	{
		super(message);
	}
}
