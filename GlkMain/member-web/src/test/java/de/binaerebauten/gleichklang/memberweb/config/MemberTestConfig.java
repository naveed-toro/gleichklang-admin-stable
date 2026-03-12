package de.binaerebauten.gleichklang.memberweb.config;

import de.binaerebauten.gleichklang.memberweb.service.SubscriptionOfferService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;

@Configuration
@PropertySource("classpath:/application_test.properties")
@ComponentScan(basePackageClasses = SubscriptionOfferService.class)
public class MemberTestConfig
{
	@Bean
	public JavaMailSender javaMailSender()
	{
		return mock(JavaMailSender.class);
	}
}
