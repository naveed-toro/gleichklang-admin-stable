package de.binaerebauten.gleichklang.memberweb.config;

import de.binaerebauten.gleichklang.core.config.SecurityConfig;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.memberweb.security.MemberHttpAuthenticationEntryPoint;
import de.binaerebauten.gleichklang.memberweb.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackageClasses = {UserDetailsServiceImpl.class, AuthenticationService.class})
@Configuration
public class MemberSecurityConfig extends SecurityConfig
{
	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private MemberHttpAuthenticationEntryPoint authenticationEntryPoint;

	@Override
	public AuthenticationEntryPoint getAuthenticationEntryPoint()
	{
		return authenticationEntryPoint;
	}

	@Override
	public UserDetailsService getSignableUserDetailsService()
	{
		return userDetailsService;
	}
}
