package de.binaerebauten.gleichklang.core.security;

import com.vaadin.server.VaadinSession;
import com.vaadin.server.VaadinSession.State;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.model.user.SignableUser.UserType;
import de.binaerebauten.gleichklang.core.repository.SignableUserRepository;
import de.binaerebauten.gleichklang.core.repository.user.AdminRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * This service encapsulates the spring security authentication and wraps the access to the vaadin session.
 */
@Service
public class AuthenticationService
{
	public enum RedirectGoal
	{
		NONE(""),
		LANDING("landing"),
		PREREGISTER("register");
		
		private final String parameterName;
		
		RedirectGoal(String parameterName)
		{
			this.parameterName = parameterName;
		}
		
		public String getParameterName()
		{
			return parameterName;
		}
	}
	
	private static final String AUTHENTICATED_USER_ATTRIBUTE = "AUTHENTICATED_USER";
	
	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	/**
	 * Returns an authentication result as an enum, containing information
	 * about the reason of unsuccessful (or successful) authentication.
	 *
	 * @param email
	 * @param password
	 * @param type
	 * @return
	 */
	public AuthenticationResult authenticate(String email, String password, UserType type)
	{
		try
		{
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
		}
		catch (AuthenticationException e)
		{
			return AuthenticationResult.BAD_CREDENTIALS;
		}
		
		SignableUser user = getByEmail(email, type);


		if (user == null)
		{
			return AuthenticationResult.FAIL;
		}
		
		updateAuthenticatedUser(user);
		return AuthenticationResult.SUCCESSFUL;
	}
	
	public void updateAuthenticatedUser(Authentication authentication, UserType userType)
	{
		Objects.requireNonNull(authentication, "authentication == null");
		Objects.requireNonNull(userType, "userType == null");
		
		SignableUser signableUser = getByEmail(authentication.getName(), userType);
		
		if (signableUser != null)
		{
			updateAuthenticatedUser(signableUser);
		}
		else
		{
			LOG.error("Unknown user {} !", authentication.getName());
		}
	}
	
	/**
	 * Updates the authenticated user.
	 * Needs to be called when the user changed.
	 * Authenticated user is updated only if an open session exists
	 * Otherwise it isn't necessary because by creation of new session an updated
	 * user from the database is set
	 *
	 * @param signableUser the non-null signable user
	 */
	public void updateAuthenticatedUser(SignableUser signableUser)
	{
		Objects.requireNonNull(signableUser, "signableUser == null");
		
		VaadinSession vaadinSession = VaadinSession.getCurrent();
		if (vaadinSession != null && vaadinSession.getState().equals(State.OPEN))
		{
			vaadinSession.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, signableUser.getId());
		}
	}
	
	/**
	 * Returns the authenticated user id.
	 *
	 * @return the authenticated user id in the current session or null
	 */
	public Long getAuthenticatedUserId()
	{
		Object authenticatedUserId = VaadinSession.getCurrent().getAttribute(AUTHENTICATED_USER_ATTRIBUTE);
		
		return authenticatedUserId != null ? (Long) authenticatedUserId : null;
	}
	
	public void logout(RedirectGoal redirectGoal)
	{
		VaadinSession.getCurrent().setAttribute(AUTHENTICATED_USER_ATTRIBUTE, null);
		VaadinSession.getCurrent().close();
		
		final String location = "logout?" + redirectGoal.getParameterName();
		UI.getCurrent().getPage().setLocation(location);
	}
	
	private SignableUser getByEmail(String email, UserType type)
	{
		return getSignableUserRepository(type).findByEmail(email);
	}
	
	private SignableUserRepository getSignableUserRepository(UserType type)
	{
		return type == SignableUser.UserType.MEMBER ? userRepository : adminRepository;
	}
	// TODO Task 5 changes
	public String getEmailByAlias(String alias, UserType type)
	{

		String emailAdd =null;
		SignableUserRepository userRepository = getSignableUserRepository(type);
		SignableUser user = userRepository.findByAlias(alias);
		if(user != null)
		{
			emailAdd = user.getEmail();
		}

		return emailAdd;

	}
}
