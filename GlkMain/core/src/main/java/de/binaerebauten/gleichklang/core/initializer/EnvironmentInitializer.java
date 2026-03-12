package de.binaerebauten.gleichklang.core.initializer;

import de.binaerebauten.gleichklang.core.env.HostPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{
	private static final Logger LOG = LoggerFactory.getLogger(EnvironmentInitializer.class);
	
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext)
	{
		try
		{
			final ConfigurableEnvironment environment = applicationContext.getEnvironment();
			final MutablePropertySources propertySources = environment.getPropertySources();
			
			final List<Resource> resources = new ArrayList<>();
			resources.add(new ClassPathResource("/application.properties"));
			for (final String activeProfile : environment.getActiveProfiles())
			{
				LOG.info("Profil ausgwählt: {}", activeProfile);
				resources.add(new ClassPathResource("/application-" + activeProfile + ".properties"));
			}
			
			final PropertiesFactoryBean bean = new PropertiesFactoryBean();
			bean.setLocations(resources.toArray(new Resource[resources.size()]));
			bean.afterPropertiesSet();
			
			propertySources.addLast(new PropertiesPropertySource("appProperties", bean.getObject()));
			propertySources.addLast(new HostPropertySource("hostProperties"));
		}
		catch (final IOException e)
		{
			throw new ApplicationContextException("environment initialization failed.", e);
		}
	}
}
