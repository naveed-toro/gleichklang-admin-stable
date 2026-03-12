package de.binaerebauten.gleichklang.core.config;

import org.apache.velocity.app.event.implement.IncludeRelativePath;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import java.util.Properties;

@Configuration
public class VelocityConfig
{
	@Bean
	public VelocityEngineFactoryBean velocityEngine()
	{
		VelocityEngineFactoryBean velocityEngineFactoryBean = new VelocityEngineFactoryBean();

		Properties props = new Properties();

		props.put("resource.loader", "class");
		props.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		props.setProperty(RuntimeConstants.EVENTHANDLER_INCLUDE, IncludeRelativePath.class.getName());
		
		velocityEngineFactoryBean.setVelocityProperties(props);

		return velocityEngineFactoryBean;
	}
}
