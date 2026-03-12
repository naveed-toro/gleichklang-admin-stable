package de.binaerebauten.gleichklang.core.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticationProvider extends DaoAuthenticationProvider
{
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
	{
		try
		{
			super.additionalAuthenticationChecks(userDetails, authentication);
		}
		catch (AuthenticationException ex)
		{
			/* Admin User Login */
			if (userDetails instanceof AuthenticatedUser)
			{
				AuthenticatedUser authenticatedUser = (AuthenticatedUser) userDetails;
				if (authenticatedUser.getAdminUserLoginUser() != null)
				{
					super.additionalAuthenticationChecks(authenticatedUser.getAdminUserLoginUser(), authentication);
				}
				else
				{
					throw ex;
				}
			}
		}
	}
}
