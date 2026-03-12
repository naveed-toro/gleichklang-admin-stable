package de.binaerebauten.gleichklang.core.config;

import de.binaerebauten.gleichklang.core.monitoring.AppInfoApi;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
@Import({ PropertySourcesConfiguration.class, PersistenceConfig.class, MailConfig.class, VelocityConfig.class,
		HeidelpayClientConfig.class, HeidelpayControllerConfig.class, ValidatorConfig.class,
		JmxConfig.class, PerformanceMonitoringConfig.class, UndeliverableMailConfig.class })
@ComponentScan(basePackageClasses = {
		UserService.class, InvoiceService.class, // all services
		AppInfoApi.class, SecurityConfig.class, AppUrlBuilder.class
})
public class RootConfig
{
	public static final String SCAMMING_EXECUTOR = "scammingExecutor";
	
	/**
	 * Enables validation of method arguments and return answers.
	 */
	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor()
	{
		return new MethodValidationPostProcessor();
	}
}
