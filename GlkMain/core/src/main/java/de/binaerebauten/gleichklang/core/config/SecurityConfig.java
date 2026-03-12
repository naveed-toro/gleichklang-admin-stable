package de.binaerebauten.gleichklang.core.config;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.security.AuthenticationProvider;
import de.binaerebauten.gleichklang.core.security.AuthenticationService.RedirectGoal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configuration class which configures spring security.
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public abstract class SecurityConfig extends WebSecurityConfigurerAdapter
{
	@Autowired
	private Environment environment;
	
	public abstract AuthenticationEntryPoint getAuthenticationEntryPoint();
	
	public abstract UserDetailsService getSignableUserDetailsService();
	
	/**
	 * Exposed as a bean so that this bean gets the deafult application event publisher
	 * injected.
	 *
	 * @return the default event publisher
	 * @see de.binaerebauten.gleichklang.core.security.AuthenticationFailureListener
	 * @see de.binaerebauten.gleichklang.core.security.AuthenticationSuccessListener
	 */
	@Bean
	public DefaultAuthenticationEventPublisher authenticationEventPublisher()
	{
		return new DefaultAuthenticationEventPublisher();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authProvider()
	{
		final AuthenticationProvider authProvider = new AuthenticationProvider();
		authProvider.setUserDetailsService(getSignableUserDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception
	{
		auth.authenticationProvider(authProvider());
	}
	
	/**
	 * Exposed a bean so that our {@link #configure(AuthenticationManagerBuilder)} method is called.
	 *
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean()
			throws Exception
	{
		return super.authenticationManagerBean();
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		final String landingRoot = environment.getProperty("landing.root");
		final String failureUrl = landingRoot == null ? "/login/error" : landingRoot + "/login/error";
		
		http
				.csrf()
				.disable() // Use Vaadin's CSRF protection
				.httpBasic()
				.authenticationEntryPoint(getAuthenticationEntryPoint())
				
				.and().authorizeRequests().anyRequest().permitAll()
				
				.and()
				.formLogin()
				.loginProcessingUrl("/login")
				.usernameParameter("email")
				.passwordParameter("password")
				.failureUrl(failureUrl)
				.successHandler(this::loginSuccess)
				.permitAll()
				.and()
				.logout()
				.logoutUrl("/logout")
				.invalidateHttpSession(true)
				.logoutSuccessHandler(this::logoutSuccess)
				.and()
				.sessionManagement()
				.sessionFixation()
				.migrateSession()
				.and()
				.rememberMe()
				.and()
				.headers()
					.xssProtection()
					.cacheControl();
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception
	{
		web.ignoring().antMatchers("/VAADIN/**");
	}
	
	private void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException
	{
		response.sendRedirect(request.getContextPath());
	}
	
	private void logoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException
	{
		if (request.getParameter(RedirectGoal.LANDING.getParameterName()) != null)
		{
			final String landingPageRoot = environment.getProperty("landing.root");
			if (!Strings.isNullOrEmpty(landingPageRoot))
			{
				response.sendRedirect(landingPageRoot + "/logout/");
				return;
			}
		}
		
		String location = request.getContextPath().contains("gkadm") ? "/gkadm/" : "/Gleichklang/";
		
		if(request.getParameter(RedirectGoal.PREREGISTER.getParameterName()) != null)
		{
			location += "?" + RedirectGoal.PREREGISTER.getParameterName();
		}
		
		response.sendRedirect(location);
	}
}
