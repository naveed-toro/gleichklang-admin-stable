package de.binaerebauten.gleichklang.core.view;

import com.vaadin.data.Validator.InvalidValueException;
import de.binaerebauten.gleichklang.core.view.component.Savable;
import de.binaerebauten.gleichklang.core.view.component.validator.ValidationResult;

/**
 * @deprecated try to use {@link Savable} instead
 */
@Deprecated
public interface ValidatableView extends Savable
{
	ValidationResult validate();
	
	void commit();
	
	void save();
	
	@Override
	default void saveComplete(SaveResultListener saveResultListener)
	{
		final ValidationResult validationResult = validate();
		if (validationResult.isSuccess())
		{
			try
			{
				commit();
				save();
			}
			catch (InvalidValueException e)
			{
				validationResult.addOtherError(e.getLocalizedMessage());
			}
		}
		
		validationResult.showValidationNotification();
		
		if(saveResultListener != null) saveResultListener.saveResult(validationResult);
	}
}
