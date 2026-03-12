package de.binaerebauten.gleichklang.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * This configuration just provides the ability to use {@link org.springframework.core.env.PropertySource}
 * annotation and is separated into this class to improve the reusability.
 */
@Configuration
public class PropertySourcesConfiguration
{
	/**
	 * Enables using {@link org.springframework.context.annotation.PropertySource} annotation.
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
	{
		return new PropertySourcesPlaceholderConfigurer();
	}
}