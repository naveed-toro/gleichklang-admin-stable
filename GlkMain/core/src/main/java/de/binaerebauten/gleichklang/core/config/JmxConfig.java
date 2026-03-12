package de.binaerebauten.gleichklang.core.config;

import de.binaerebauten.gleichklang.core.monitoring.HibernateStatisticsFactoryBean;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.MBeanServerFactoryBean;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Configures advanced monitoring of this application via JMX.
 */
@Configuration
@PropertySource("classpath:/build.properties")
public class JmxConfig
{
	@Autowired
	private Statistics statistics;

	@Autowired
	private Environment environment;

	@Bean
	public MBeanServerFactoryBean mbeanServerFactory()
	{
		MBeanServerFactoryBean mbeanServerFactory = new MBeanServerFactoryBean();
		mbeanServerFactory.setLocateExistingServerIfPossible(true);

		return mbeanServerFactory;
	}

	@Bean
	public MBeanExporter mbeanExporter(MBeanServer mbeanServer)
			throws MalformedObjectNameException
	{
		MBeanExporter mBeanExporter = new MBeanExporter();

		mBeanExporter.setServer(mbeanServer);

		// since we use one tomcat to serve both apps, we need to specify an app specific name for
		// the hibernate statistics
		String appName = environment.getProperty("app.name");
		ObjectName objectName = new ObjectName(appName, "name", "hibernate-statistics");
		mBeanExporter.registerManagedResource(statistics, objectName);

		return mBeanExporter;
	}

	@Bean
	public HibernateStatisticsFactoryBean hibernateStatisticsFactory()
	{
		return new HibernateStatisticsFactoryBean();
	}
}
