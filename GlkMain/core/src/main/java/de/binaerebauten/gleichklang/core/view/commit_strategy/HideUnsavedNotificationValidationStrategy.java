package de.binaerebauten.gleichklang.core.view.commit_strategy;

public class HideUnsavedNotificationValidationStrategy implements ValidationStrategy
{
	@Override
	public boolean ignoreEmptyValueExceptions()
	{
		return false;
	}
	
	@Override
	public boolean isShowUnsavedNotification()
	{
		return false;
	}
}
