package de.binaerebauten.gleichklang.core.presenter;

import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.service.AbstractUserService;
import de.binaerebauten.gleichklang.core.service.AdminService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.view.ForgotPasswordView;
import de.binaerebauten.gleichklang.core.view.ForgotPasswordView.ForgotPasswordViewListener;
import org.springframework.context.ApplicationContext;

public abstract class ForgotPasswordPresenter extends ManualNavigatePresenter implements ForgotPasswordViewListener
{
	private final AbstractUserService<?> userService;
	private final SignableUser.UserType userType;
	
	public ForgotPasswordPresenter(ApplicationContext ctx, ForgotPasswordView view, SignableUser.UserType userType)
	{
		super(view);
		
		this.userType = userType;
		this.userService = getAbstractUserService(ctx);
		
		view.setListener(this);
	}
	
	private AbstractUserService<?> getAbstractUserService(ApplicationContext ctx)
	{
		switch (userType)
		{
			case ADMIN:
				return ctx.getBean(AdminService.class);
			case MEMBER:
				return ctx.getBean(UserService.class);
		}
		return null;
	}
	
	@Override
	public void forgotPassword(String email)
	{
		final SignableUser user = userService.findByEmail(email);
		if (user != null)
		{
			userService.sendResetPasswordMail(user);
		}
		
		backToLogin(email);
	}
}
