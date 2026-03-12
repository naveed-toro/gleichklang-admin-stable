package de.binaerebauten.gleichklang.core.security;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog;
import de.binaerebauten.gleichklang.core.service.UserActivityService;
import de.binaerebauten.gleichklang.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * This application event listener is responsible for writing a {@link UserActivityLog.UserActivity#LOGIN}
 * activity when an authentication succeeded.
 *
 * This event listener needs to handle {@link InteractiveAuthenticationSuccessEvent} for the form based login
 * and {@link AuthenticationSuccessEvent} for the vaadin based login {@link de.binaerebauten.gleichklang.core.presenter.LoginPresenter}.
 */
@Component
public class AuthenticationSuccessListener
{
	// right now we just log the failed login attempts of users
	@Autowired
	private UserService userService;

	@Autowired
	private UserActivityService userActivityService;

	@EventListener
	public void onLoginSuccess(AuthenticationSuccessEvent event)
	{
		createLoginActivity(event);
	}

	@EventListener
	public void onLoginSuccess(InteractiveAuthenticationSuccessEvent event)
	{
		createLoginActivity(event);
	}

	private void createLoginActivity(AbstractAuthenticationEvent event)
	{
		Authentication authentication = event.getAuthentication();
		User user = userService.findByEmail(authentication.getName());

		if (user != null)
		{
			userActivityService.createActivity(user, UserActivityLog.UserActivity.LOGIN);
		}
	}
}
