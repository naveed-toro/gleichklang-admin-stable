package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.view.NavigateView;

public interface ChangePasswordView extends NavigateView<ChangePasswordView.ChangePasswordViewListener>
{
	interface ChangePasswordViewListener extends NavigateView.NavigateViewListener
	{
		void changePassword(String oldPassword, String newPassword, String repeatPassword);
	}
}
