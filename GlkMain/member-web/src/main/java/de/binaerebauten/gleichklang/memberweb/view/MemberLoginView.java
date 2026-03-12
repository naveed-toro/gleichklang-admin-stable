package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.view.LoginView;
import de.binaerebauten.gleichklang.memberweb.view.MemberLoginView.MemberLoginViewListener;

public interface MemberLoginView extends LoginView<MemberLoginViewListener>
{
	interface MemberLoginViewListener extends LoginView.LoginViewListener
	{
		void startRegistration();
		
		void changeLanguage(Language language);
	}
	
	void setRegistrationVisible(boolean visible);
}
