package de.binaerebauten.gleichklang.core.view.commit_strategy;

public class DefaultValidationStrategy implements ValidationStrategy
{
	private boolean showUnsavedNotification = true;
	private boolean ignoreEmptyValueExceptions = false;
	private String successMessage = null;
	private String failMessage = null;
	
	public void setShowUnsavedNotification(boolean showUnsavedNotification)
	{
		this.showUnsavedNotification = showUnsavedNotification;
	}
	
	public void setIgnoreEmptyValueExceptions(boolean ignoreEmptyValueExceptions)
	{
		this.ignoreEmptyValueExceptions = ignoreEmptyValueExceptions;
	}
	
	public void setSuccessMessage(String successMessage)
	{
		this.successMessage = successMessage;
	}
	
	public void setFailMessage(String failMessage)
	{
		this.failMessage = failMessage;
	}
	
	@Override
	public boolean isShowUnsavedNotification()
	{
		return showUnsavedNotification;
	}
	
	@Override
	public boolean ignoreEmptyValueExceptions()
	{
		return ignoreEmptyValueExceptions;
	}
	
	@Override
	public String getSuccessMessage()
	{
		return successMessage == null ? ValidationStrategy.super.getSuccessMessage() : successMessage;
	}
	
	@Override
	public String getFailMessage()
	{
		return failMessage == null ? ValidationStrategy.super.getFailMessage() : failMessage;
	}
}
