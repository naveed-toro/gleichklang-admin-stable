package de.binaerebauten.gleichklang.adminweb.config;

import de.binaerebauten.gleichklang.adminweb.security.AdminDetailsServiceImpl;
import de.binaerebauten.gleichklang.adminweb.security.AdminHttpAuthenticationEntryPoint;
import de.binaerebauten.gleichklang.core.config.SecurityConfig;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ComponentScan(basePackageClasses = {AdminDetailsServiceImpl.class, AuthenticationService.class})
@Configuration
public class AdminSecurityConfig extends SecurityConfig
{
	@Autowired
	private AdminDetailsServiceImpl userDetailsService;

	@Autowired
	private AdminHttpAuthenticationEntryPoint authenticationEntryPoint;

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
