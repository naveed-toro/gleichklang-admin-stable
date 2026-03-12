package de.binaerebauten.gleichklang.heidelpaymigration.config;

import de.binaerebauten.gleichklang.core.config.*;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Import({ PropertySourcesConfiguration.class, PersistenceConfig.class,
		HeidelpayClientConfig.class, VelocityConfig.class, MailConfig.class,
		UndeliverableMailConfig.class })
@ComponentScan(basePackageClasses = { AppUrlBuilder.class, AuthenticationService.class, UserService.class })
public class Config
{
	@Bean
	public AuthenticationManager authenticationManager()
	{
		return (Authentication a) -> null;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return null;
	}

}
