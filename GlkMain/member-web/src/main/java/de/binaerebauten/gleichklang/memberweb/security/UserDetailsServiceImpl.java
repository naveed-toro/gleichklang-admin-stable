package de.binaerebauten.gleichklang.memberweb.security;

import de.binaerebauten.gleichklang.core.model.user.I18N;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.user.AdminUserLoginRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticatedUser;
import de.binaerebauten.gleichklang.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * This service implements the spring security {@link UserDetailsService},
 * but currently only for the {@link de.binaerebauten.gleichklang.core.model.user.User}
 * type and not for the {@link de.binaerebauten.gleichklang.core.model.user.Admin} type!
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AdminUserLoginRepository adminUserLoginRepository;

	@Autowired
	@Lazy
	private UserService userService;



	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{

		User user;

		if(isAliasNotEmail(username))
		{

			user = userRepository.findByAlias(username);
		}
		else
		{
			user = userRepository.findByEmail(username);
			if (user == null)
			{
				// try finding by sibling email
				user = userService.findBySiblingEmail(username, true);
			}
		}
		if (user == null) {
			throw new UsernameNotFoundException(I18N.USER_NOT_FOUND.msg(username));
		}
		return new AuthenticatedUser(user, adminUserLoginRepository.findByUser(user));
	}

	//TODO: make changes to support alias
	private boolean isAliasNotEmail(String loginName) {
		if (loginName != null && loginName.contains("@") && loginName.contains("."))
			return false;

		return true;
	}
}