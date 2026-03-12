package de.binaerebauten.gleichklang.core.config;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.interceptor.PerformanceMonitorInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enables performance logging for repository and service classes based on there name.
 */
@Configuration
public class PerformanceMonitoringConfig
{

	@Bean
	public PerformanceMonitorInterceptor performanceInterceptor()
	{
		PerformanceMonitorInterceptor interceptor = new PerformanceMonitorInterceptor();

		interceptor.setHideProxyClassNames(true);
		interceptor.setLoggerName("performance-monitor-interceptor");

		return interceptor;
	}

	@Bean
	public BeanNameAutoProxyCreator repositoryPerformance()
	{
		BeanNameAutoProxyCreator creator = new BeanNameAutoProxyCreator();

		creator.setInterceptorNames(new String[] { "performanceInterceptor" });
		creator.setBeanNames(new String[] { "*Repository", "*Service" });
		creator.setProxyTargetClass(true); // force using of cglib because our services don't use interfaces

		return creator;
	}
}
