package de.binaerebauten.gleichklang.core.view.commit_strategy;

import de.binaerebauten.gleichklang.core.view.I18N;

public interface ValidationStrategy
{
	boolean ignoreEmptyValueExceptions();
	
	boolean isShowUnsavedNotification();
	
	default String getSuccessMessage()
	{
		return I18N.DATA_SAVED.msg();
	}
	
	default String getFailMessage()
	{
		return I18N.DATA_NOT_SAVED.msg();
	}
}
