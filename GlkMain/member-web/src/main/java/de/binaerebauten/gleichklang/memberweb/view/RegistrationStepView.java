package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.view.commit_strategy.ValidationStrategy;
import de.binaerebauten.gleichklang.core.view.commit_strategy.HideUnsavedNotificationValidationStrategy;

public interface RegistrationStepView
{
	default ValidationStrategy getValidationStrategy(){
		return new HideUnsavedNotificationValidationStrategy();
	}
}
