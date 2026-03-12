package de.binaerebauten.gleichklang.adminweb.presenter;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.adminweb.navigation.ManualNavigatorFactory.ManualNavigationItem;
import de.binaerebauten.gleichklang.core.model.user.SignableUser.UserType;
import de.binaerebauten.gleichklang.core.navigation.ManualNavigator;
import de.binaerebauten.gleichklang.core.presenter.ForgotPasswordPresenter;
import de.binaerebauten.gleichklang.core.presenter.I18N;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder.ParameterKey;
import de.binaerebauten.gleichklang.core.view.ForgotPasswordView;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

public class AdminForgotPasswordPresenter extends ForgotPasswordPresenter
{
	private final ManualNavigator navigator;
	
	public AdminForgotPasswordPresenter(ApplicationContext ctx, ForgotPasswordView forgotPasswordView, ManualNavigator navigator)
	{
		super(ctx, forgotPasswordView, UserType.ADMIN);
		
		this.navigator = Objects.requireNonNull(navigator);
	}
	
	public void backToLogin(String email)
	{
		final ParametersHolder parameters = new ParametersHolder();
		if(!Strings.isNullOrEmpty(email))
		{
			parameters.addParameter(ParameterKey.EMAIL, email);
			parameters.addParameter(ParameterKey.MESSAGE, I18N.FORGOTPASSWORD_SUCCESS_ADMIN.msg());
		}
		
		navigator.navigateTo(ManualNavigationItem.LOGIN, parameters);
	}
	
	@Override
	public void enter(ParametersHolder parameterMap)
	{
	
	}
}
