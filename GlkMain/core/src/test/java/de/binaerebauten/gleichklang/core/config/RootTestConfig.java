package de.binaerebauten.gleichklang.core.config;

import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.utils.MailReminder;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Properties;

import static org.mockito.Mockito.mock;

/**
 * Created by michael on 27/05/15.
 */
@Configuration
@Import({PropertySourcesConfiguration.class, UndeliverableMailConfig.class })
@PropertySource("classpath:/application_test.properties")
public class RootTestConfig
{
	/**
	 * Enables hibernate validation.
	 */
	@Bean
	public LocalValidatorFactoryBean localValidatorFactory()
	{
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public AuthenticationService authenticationProvider()
	{
		return mock(AuthenticationService.class);
	}

	@Bean
	public AuthenticationManager authenticationManager()
	{
		return mock(AuthenticationManager.class);
	}
	
	@Bean
	public MailReminder mailReminder()
	{
		return mock(MailReminder.class);
	}

	@Bean
	public PasswordEncoder passwordEncoder(){
		return mock(PasswordEncoder.class);
	}

	@Bean
	public JavaMailSender javaMailSender()
	{
		return mock(JavaMailSender.class);
	}

	@Bean
	public VelocityEngineFactoryBean velocityEngine()
	{
		VelocityEngineFactoryBean velocityEngineFactoryBean = new VelocityEngineFactoryBean();

		Properties props = new Properties();
		props.put("resource.loader", "class");
		props.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		velocityEngineFactoryBean.setVelocityProperties(props);

		return velocityEngineFactoryBean;
	}
}
