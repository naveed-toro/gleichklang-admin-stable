package de.binaerebauten.gleichklang.core.view.commit_strategy;

public class IgnoreEmptyValuesValidationStrategy implements ValidationStrategy
{
	@Override
	public boolean isShowUnsavedNotification()
	{
		return true;
	}
	
	@Override
	public boolean ignoreEmptyValueExceptions()
	{
		return true;
	}
}
