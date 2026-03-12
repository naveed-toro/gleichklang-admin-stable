package de.binaerebauten.gleichklang.core.security;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog;
import de.binaerebauten.gleichklang.core.service.UserActivityService;
import de.binaerebauten.gleichklang.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * This application event listener is responsible for writing a {@link UserActivityLog.UserActivity#LOGIN_FAILED}
 * activity when an authentication failed.
 */
@Component
public class AuthenticationFailureListener
{
	// right now we just log the failed login attempts of users
	@Autowired
	private UserService userService;

	@Autowired
	private UserActivityService userActivityService;

	@EventListener
	public void onLoginFailure(AuthenticationFailureBadCredentialsEvent event)
	{
		Authentication authentication = event.getAuthentication();

		User user = userService.findByEmail(authentication.getName());

		if (user != null)
		{
			userActivityService.createActivity(user, UserActivityLog.UserActivity.LOGIN_FAILED);
		}
	}
}
