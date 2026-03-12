package de.binaerebauten.gleichklang.core.view;

public interface ForgotPasswordView extends NavigateView<ForgotPasswordView.ForgotPasswordViewListener>
{
	interface ForgotPasswordViewListener extends NavigateView.NavigateViewListener
	{
		void forgotPassword(String login);
		
		void backToLogin(String email);
	}
}
