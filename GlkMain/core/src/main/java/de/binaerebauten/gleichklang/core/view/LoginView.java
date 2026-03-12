package de.binaerebauten.gleichklang.core.view;

import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.view.LoginView.LoginViewListener;

public interface LoginView<T extends LoginViewListener> extends NavigateView<T>
{
	interface LoginViewListener extends NavigateView.NavigateViewListener
	{
		void login(String username, String password, SignableUser.UserType userType);
		
		void forgotPassword();
	}
	
	void setLogin(String login);
	
	void setPassword(String password);
	
	SignableUser.UserType getType();
	
	void setMessage(String message);
	
	void setLoginCaption(String loginCaption);
}
