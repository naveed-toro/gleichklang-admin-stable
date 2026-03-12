package de.binaerebauten.gleichklang.core.initializer;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

@SuppressWarnings("serial")
@Theme("gk_theme")
@StyleSheet("https://fonts.googleapis.com/css?family=Roboto")
//@Push //TODO probeweise ausgeschaltet, um das memory-leak problem zu lösen
public abstract class AppUI extends UI
{
	protected ApplicationContext rootContext;
	
	protected abstract String getTitle();
	
	static
	{
		SLF4JBridgeHandler.install();
	}
	
	/**
	 * Static helper method to access the application context.
	 *
	 * @return the application context that is connected to the vaadin servlet
	 *         context
	 */
	public static ApplicationContext getApplicationContext()
	{
		final ServletContext servletContext = BootstrapServlet.getCurrent() != null ?
				BootstrapServlet.getCurrent().getServletContext() : null;
		return servletContext != null ?
				WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext) : null;
	}

	@Override
	protected void init(VaadinRequest request)
	{
		rootContext = getApplicationContext();

		Page.getCurrent().setTitle(getTitle());

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		final AuthenticationService authenticationService = rootContext.getBean(AuthenticationService.class);

		authenticationService.updateAuthenticatedUser(authentication, getUserType());

		initView(request);
		final String theme = rootContext.getBean(Environment.class).getProperty("app.theme", "old_theme");
		setTheme(theme);
	}
	
	protected abstract SignableUser.UserType getUserType();

	protected abstract void initView(VaadinRequest request);
	
}
