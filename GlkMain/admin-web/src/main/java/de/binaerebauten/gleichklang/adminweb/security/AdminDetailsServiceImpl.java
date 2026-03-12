package de.binaerebauten.gleichklang.adminweb.security;

import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.repository.user.AdminRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * This service implements the spring security {@link UserDetailsService},
 * but currently only for the {@link Admin}
 */
@Service
public class AdminDetailsServiceImpl implements UserDetailsService
{
	@Autowired
	private AdminRepository adminRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		final Admin admin = adminRepository.findByEmail(username);

		if (admin == null)
		{
			throw new UsernameNotFoundException(String.format("User '%s' not found", username));
		}

		return new AuthenticatedUser(admin);
	}
}
