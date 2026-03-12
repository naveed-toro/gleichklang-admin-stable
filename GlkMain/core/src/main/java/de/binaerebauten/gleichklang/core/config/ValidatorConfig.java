package de.binaerebauten.gleichklang.core.config;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidatorConfig
{
	/**
	 * Enables hibernate validation.
	 */
	@Bean
	public LocalValidatorFactoryBean localValidatorFactory()
	{
		final ResourceBundleLocator rbl = new PlatformResourceBundleLocator(getClass().getPackage().getName() + ".Validator");
		final ResourceBundleMessageInterpolator rbmi = new ResourceBundleMessageInterpolator(rbl);
		final LocalValidatorFactoryBean lvfb = new LocalValidatorFactoryBean();
		lvfb.setMessageInterpolator(rbmi);
		
		return lvfb;
	}
}
