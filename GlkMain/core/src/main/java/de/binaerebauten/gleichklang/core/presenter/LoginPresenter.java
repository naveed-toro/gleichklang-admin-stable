package de.binaerebauten.gleichklang.core.presenter;

import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.security.AuthenticationResult;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder.ParameterKey;
import de.binaerebauten.gleichklang.core.view.I18N;
import de.binaerebauten.gleichklang.core.view.LoginView;
import de.binaerebauten.gleichklang.core.view.LoginView.LoginViewListener;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxButtons;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxStyle;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

public abstract class LoginPresenter<T extends LoginViewListener> extends ManualNavigatePresenter implements LoginViewListener
{
	private final AuthenticationService authenticationService;
	private final UserService userService;
	private boolean siblingEmailCheckEnabled =true;
	private final LoginView<T> view;
	
	public LoginPresenter(ApplicationContext ctx, LoginView<T> loginView)
	{
		super(loginView);
		
		Objects.requireNonNull(ctx, "ctx == null");
		
		this.view = Objects.requireNonNull(loginView, "loginView == null");
		
		authenticationService = ctx.getBean(AuthenticationService.class);

		userService = ctx.getBean(UserService.class);
	}
	
	protected String getMessage(ParametersHolder parameterMap)
	{
		if (parameterMap == null) return null;
		
		String message = parameterMap.getFirstParameter(ParameterKey.MESSAGE);
		
		if (parameterMap.hasParameter(ParameterKey.CONFIRM))
		{
			message = de.binaerebauten.gleichklang.core.view.I18N.LOGINVIEW_EMAIL_CONFIRMED.msg();
		}
		else if (parameterMap.hasParameter(ParameterKey.UNSUBSCRIBED))
		{
			message = I18N.LOGINVIEW_UNSUBSCRIBED.msg();
		}
		
		return message;
	}
	
	protected String getEmail(ParametersHolder parameterMap)
	{
		if (parameterMap == null) return null;
		
		return parameterMap.getFirstParameter(ParameterKey.EMAIL);
	}
	
	protected String getPassword(ParametersHolder parameterMap)
	{
		if (parameterMap == null) return null;
		
		return parameterMap.getFirstParameter(ParameterKey.PASSWORD);
	}
	
	protected abstract void onLoginSuccess();
	
	@Override
	public void login(String loginName, String password, SignableUser.UserType type) {

		// TODO : Task 2.2 changes - Start
		boolean emailEntered=true;
		if (type.equals(SignableUser.UserType.MEMBER) && isAliasNotEmail(loginName) && siblingEmailCheckEnabled) {
			emailEntered = false; // to identify that user orginally entered alias and not email
			String email = authenticationService.getEmailByAlias(loginName, type);

			if (email != null && !email.isEmpty()) {
				loginName = email;

			}
		}
		// TODO : Task 2.2 changes - End

		final AuthenticationResult result = authenticationService.authenticate(loginName, password, type);
		view.setPassword(null);

		switch (result) {
			case SUCCESSFUL:
				onLoginSuccess();
				break;
			case FAIL:
			case BAD_CREDENTIALS: {
				// TODO Changes for Task 2.1, allowing sign in via twin email address
				if (type.equals(SignableUser.UserType.MEMBER) && emailEntered && siblingEmailCheckEnabled) {
					User foundUser = userService.findBySiblingEmail(loginName,true);
					if(foundUser != null)
					{
						AuthenticationResult retryResult = authenticationService.authenticate(foundUser.getEmail(), password, type);
						if (retryResult == AuthenticationResult.SUCCESSFUL) {
								onLoginSuccess();
								break;}
					}
				}
			}
			default:
				MessageBox.show(de.binaerebauten.gleichklang.core.view.I18N.LOGINVIEW_NOTIFICATION_LOGINFAILED.msg(), MessageBoxButtons.OK, MessageBoxStyle.ATTENTION, null);
		}
	}

	private boolean isAliasNotEmail(String loginName) {
			if (loginName != null && loginName.contains("@") && loginName.contains("."))
				return false;

			return true;
	}

}
