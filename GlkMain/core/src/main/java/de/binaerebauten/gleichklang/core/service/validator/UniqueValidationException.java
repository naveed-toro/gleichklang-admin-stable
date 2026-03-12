package de.binaerebauten.gleichklang.core.service.validator;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Checked exception to wrap a {@link DataIntegrityViolationException}.
 */
public class UniqueValidationException extends ValidationException
{
	public UniqueValidationException(String message)
	{
		super(message);
	}

	public UniqueValidationException(DataIntegrityViolationException cause)
	{
		super(cause);
	}
}
