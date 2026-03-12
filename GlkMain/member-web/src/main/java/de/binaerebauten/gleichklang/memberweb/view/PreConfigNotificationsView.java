package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.Savable;
import de.binaerebauten.gleichklang.memberweb.view.PreConfigNotificationsView.PreConfigNotificationsViewListener;

public interface PreConfigNotificationsView extends NavigateView<PreConfigNotificationsViewListener>, Savable
{
	interface PreConfigNotificationsViewListener extends NavigateView.NavigateViewListener
	{
		void setNotifications(boolean systemNotificationsEnabled, boolean marketingNotificationsEnabled) throws ValidationException;
	}
}
