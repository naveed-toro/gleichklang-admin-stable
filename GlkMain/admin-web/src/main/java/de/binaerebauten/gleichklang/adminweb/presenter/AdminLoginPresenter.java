package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.adminweb.navigation.ManualNavigatorFactory.ManualNavigationItem;
import de.binaerebauten.gleichklang.adminweb.view.AdminLoginView;
import de.binaerebauten.gleichklang.core.navigation.ManualNavigator;
import de.binaerebauten.gleichklang.core.presenter.LoginPresenter;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.view.I18N;
import de.binaerebauten.gleichklang.core.view.LoginView.LoginViewListener;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

public class AdminLoginPresenter extends LoginPresenter<LoginViewListener>
{
	private final ManualNavigator navigator;
	private final AdminLoginView view;
	
	public AdminLoginPresenter(ApplicationContext ctx, AdminLoginView loginView, ManualNavigator navigator)
	{
		super(ctx, loginView);
		
		this.navigator = Objects.requireNonNull(navigator, "navigator");
		this.view = Objects.requireNonNull(loginView, "loginView");
		
		this.view.setListener(this);
	}
	
	@Override
	public void enter(ParametersHolder parameterMap)
	{
		final String message = getMessage(parameterMap);
		final String email = getEmail(parameterMap);
		
		view.setLogin(email);
		view.setPassword(null);
		
		if (message != null)
		{
			view.setMessage(message);
			view.setLoginCaption(I18N.LOGINVIEW_ACTION_LOGIN.msg());
		}
	}
	
	@Override
	public void forgotPassword()
	{
		navigator.navigateTo(ManualNavigationItem.FORGET_PASSWORD);
	}
	
	@Override
	protected void onLoginSuccess()
	{
		navigator.navigateTo(ManualNavigationItem.MAIN);
	}
}
