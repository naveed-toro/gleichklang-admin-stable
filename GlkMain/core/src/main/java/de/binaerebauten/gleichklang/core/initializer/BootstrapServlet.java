package de.binaerebauten.gleichklang.core.initializer;

import com.vaadin.server.*;

import javax.servlet.ServletException;

/**
 * This extension is necessary for adding meta tags in the head-section of the generated html.
 * Inherits from JMeterServlet in order to allow lasts tests.
 */
public class BootstrapServlet extends VaadinServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void servletInitialized() throws ServletException
	{
		super.servletInitialized();
		getService().addSessionInitListener(new SessionInitListener()
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void sessionInit(SessionInitEvent event)
			{
				//TODO: this should be set in and read from application properties
				VaadinSession.getCurrent().getSession().setMaxInactiveInterval(10800);
				event.getSession().addBootstrapListener(new BootstrapListener()
				{
					private static final long serialVersionUID = 1L;
					
					@Override
					public void modifyBootstrapFragment(BootstrapFragmentResponse response)
					{
					}
					
					@Override
					public void modifyBootstrapPage(BootstrapPageResponse response)
					{
						response.getDocument().head().appendElement("meta")
								.attr("name", "viewport")
								.attr("content", "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0");
					}
				});
			}
		});

		getService().setSystemMessagesProvider(
				(SystemMessagesProvider) systemMessagesInfo -> {
					CustomizedSystemMessages messages =
							new CustomizedSystemMessages();
					messages.setSessionExpiredCaption(I18N.SYSTEM_SESSION_EXPIRED_CAPTION.msg());
					messages.setSessionExpiredMessage(I18N.SYSTEM_SESSION_EXPIRED_MESSAGE.msg());

					messages.setCommunicationErrorCaption(I18N.SYSTEM_COMMUNICATION_ERROR_CAPTION.msg());
					messages.setCommunicationErrorMessage(I18N.SYSTEM_COMMUNICATION_ERROR_MESSAGE.msg());

					messages.setCookiesDisabledCaption(I18N.SYSTEM_DISABLED_COOKIE_CAPTION.msg());
					messages.setCookiesDisabledMessage(I18N.SYSTEM_DISABLED_COOKIE_MESSAGE.msg());

					messages.setInternalErrorCaption(I18N.SYSTEM_INTERNAL_ERROR_CAPTION.msg());
					messages.setInternalErrorMessage(I18N.SYSTEM_INTERNAL_ERROR_MESSAGE.msg());

					return messages;
				});
	}
}

