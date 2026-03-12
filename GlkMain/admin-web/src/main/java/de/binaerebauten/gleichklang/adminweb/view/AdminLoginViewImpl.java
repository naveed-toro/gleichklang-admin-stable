package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.server.VaadinSession;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.view.LoginView.LoginViewListener;
import de.binaerebauten.gleichklang.core.view.LoginViewImpl;

public class AdminLoginViewImpl extends LoginViewImpl<LoginViewListener> implements AdminLoginView
{
	private static final String AUTHENTICATED_USER_TYPE_ATTRIBUTE = "AUTHENTICATED_USER_TYPE";
	public AdminLoginViewImpl()
	{
		super("Admin");
	}
	
	@Override
	public SignableUser.UserType getType()
	{
		// adding the user type in session.
		VaadinSession vaadinSession = VaadinSession.getCurrent();
		vaadinSession.setAttribute(AUTHENTICATED_USER_TYPE_ATTRIBUTE, SignableUser.UserType.ADMIN);
		return SignableUser.UserType.ADMIN;
	}
}
