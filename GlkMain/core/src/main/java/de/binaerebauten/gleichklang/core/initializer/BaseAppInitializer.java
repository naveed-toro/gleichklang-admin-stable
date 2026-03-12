package de.binaerebauten.gleichklang.core.initializer;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.vaadin.server.VaadinServlet;
import com.vaadin.util.CurrentInstance;
import de.binaerebauten.gleichklang.core.config.SpringProfile;
import de.binaerebauten.gleichklang.core.utils.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This abstract class implements registering of the apring root config and the vaadin servlet.
 * Additionally it also unregisters the mysql jdbc driver to prevent memory leaks.
 */
@Order(value = 1)
public abstract class BaseAppInitializer implements WebApplicationInitializer
{
	private static final Logger LOG = LoggerFactory.getLogger(BaseAppInitializer.class);

	private WebApplicationContext rootApplicationContext;

	/**
	 * Deregisters all registered jdbc drivers of this web application when the
	 * context is destroyed. Additionally this class also shuts down the mysql
	 * {@link AbandonedConnectionCleanupThread}.
	 * <p/>
	 * Hopefully will prevent memory leaks caused by tomcat:
	 * <p/>
	 * http://stackoverflow.com/questions/3320400/to-prevent-a-memory-leak-the-
	 * jdbc-driver-has-been-forcibly-unregistered *
	 */
	private final class ShutdownJdbcDriversOnDestroy implements ServletContextListener
	{
		@Override
		public void contextInitialized(ServletContextEvent sce)
		{
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce)
		{
			// the order of the following statements is relevant:
			// - first stop any thread holding references
			shutDownMysqlThread();

			// then deregister this webapp's jdbc driver
			deregisterDrivers();
		}

		private void shutDownMysqlThread()
		{
			try
			{
				AbandonedConnectionCleanupThread.checkedShutdown();
			}
			catch (Exception e)
			{
				LOG.warn("Problem cleaning up", e);
			}
		}

		// Deregister JDBC drivers in this context's ClassLoader:
		private void deregisterDrivers()
		{
			// Get this webapp ClassLoader
			final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

			final List<Driver> drivers = Collections.list(DriverManager.getDrivers())
					.stream()
					.filter(d -> d.getClass().getClassLoader() == contextClassLoader)
					.collect(Collectors.toList());

			for (final Driver d : drivers)
			{
				try
				{
					DriverManager.deregisterDriver(d);
					LOG.info("Programmatically deregistered driver {}", d.getClass().getName());
				}
				catch (final SQLException e)
				{
					LOG.warn("Exception while deregistering driver", e);
				}
			}
		}
	}

	/**
	 * Clears all vaadin current instances.
	 */
	private static class ClearAllVaadinCurrentInstancesOnDestroy implements ServletContextListener
	{
		@Override
		public void contextInitialized(ServletContextEvent sce)
		{
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce)
		{
			CurrentInstance.clearAll();
		}
	}
	
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		rootApplicationContext = createRootApplicationContext();

		registerContextLoaderListener(servletContext);
		servletContext.addListener(new ShutdownJdbcDriversOnDestroy());
		
		registerVaadinServlet(servletContext);
		servletContext.addListener(new ClearAllVaadinCurrentInstancesOnDestroy());

		registerDispatcherServlet(servletContext);
	}

	private void registerDispatcherServlet(ServletContext servletContext)
	{
		WebApplicationContext rootApplicationContext = getRootApplicationContext();

		DispatcherServlet dispatcherServlet = new DispatcherServlet(rootApplicationContext);
		final ServletRegistration.Dynamic registration = servletContext.addServlet("dispatcher", dispatcherServlet);

		registration.setLoadOnStartup(1);
		registration.addMapping("/api/*");
		registration.addMapping("/login");
	}

	private void registerContextLoaderListener(ServletContext servletContext)
	{
		servletContext.addListener(new ContextLoaderListener(rootApplicationContext));
		servletContext.setInitParameter(ContextLoader.CONTEXT_INITIALIZER_CLASSES_PARAM, EnvironmentInitializer.class.getName());
	}

	private WebApplicationContext createRootApplicationContext()
	{
		final AnnotationConfigWebApplicationContext rootAppContext = new AnnotationConfigWebApplicationContext();
		rootAppContext.register(getRootConfigClass());

		return rootAppContext;
	}

	private void registerVaadinServlet(ServletContext servletContext)
	{
		final String servletName = "VaadinServlet";
		
		Properties properties = PropertiesLoader.getProperties("application.properties");
		Objects.requireNonNull(properties, "application.properties not found");

		boolean disableXsrfProtection = Boolean.valueOf(properties.getProperty("disable-xsrf-protection"));
		final VaadinServlet vaadinServlet = disableXsrfProtection ? new JMeterServlet() : new BootstrapServlet();

		final ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, vaadinServlet);
		Objects.requireNonNull(registration, "Failed to preregister servlet with name '" + servletName + "'." +
				"Check if there is another servlet registered under the same name.");

		if (SpringProfile.PROD.isActive()) {
		    registration.setInitParameter("productionMode", "true");
        }

		registration.setInitParameter("UI", getAppUiClass().getName());
		registration.setLoadOnStartup(1);
		registration.addMapping("/*");
		registration.setAsyncSupported(true);
	}

	protected WebApplicationContext getRootApplicationContext()
	{
		return rootApplicationContext;
	}

	/**
	 * Returns the spring configuration class for this app.
	 *
	 * @return the root config class of this app
	 */
	protected abstract Class<?> getRootConfigClass();

	/**
	 * Returns the app ui class of this app.
	 *
	 * @param <T> type of the app ui
	 * @return the app ui class for this app.
	 */
	protected abstract <T extends AppUI> Class<T> getAppUiClass();

	public class AuditRequestFilter extends GenericFilterBean
	{
		@Override
		public void doFilter(final ServletRequest request,
				final ServletResponse response, final FilterChain filterChain)
				throws IOException, ServletException {
			final HttpServletRequest httpRequest = (HttpServletRequest) request;
			final HttpServletResponse httpResponse = (HttpServletResponse) response;
			System.out.println("request is " + httpRequest.getClass());
			System.out.println("request URL : " + httpRequest.getRequestURL());
			System.out.println("response is " + httpResponse.getClass());
			filterChain.doFilter(request, response);
		}
	}

}
