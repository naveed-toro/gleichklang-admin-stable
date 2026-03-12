package de.binaerebauten.gleichklang.core.view.component;

import de.binaerebauten.gleichklang.core.view.component.validator.ValidationResult;

public interface Savable
{
	interface SaveResultListener
	{
		void saveResult(ValidationResult result);
	}
	
	/**
	 * validate, commit & save
	 *
	 */
	default void saveComplete()
	{
		saveComplete(null);
	}
	
	void saveComplete(SaveResultListener saveResultListener);
}
