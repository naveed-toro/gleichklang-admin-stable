package de.binaerebauten.gleichklang.memberweb.controller;

import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.user.EmailLink;
import de.binaerebauten.gleichklang.core.model.user.EmailLink.EmailLinkContext;
import de.binaerebauten.gleichklang.core.model.user.EmailLinkParameter;
import de.binaerebauten.gleichklang.core.model.user.EmailLinkParameter.ParameterType;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.mail.EmailLinkService;
import de.binaerebauten.gleichklang.core.service.mail.NewsletterService;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class AuthenticationControllerTest
{
	@InjectMocks
	private AuthenticationController authenticationController;
	
	@Mock
	private EmailLinkService emailLinkService;
	
	@Mock
	private UserService userService;
	
	private NewsletterService newsletterService;
	private UndeliverableMailService undeliverableMailService;
	
	@Mock
	private AppUrlBuilder appUrlBuilder;
	
	private HttpServletResponse response;
	private User user;
	private static final String EMAIL = "test@example.com";
	
	@Before
	public void setup()
	{
		response = mock(HttpServletResponse.class);
		
		user = new User();
		user.setEmail(EMAIL);
		user.setNewEmail(EMAIL);
		
		newsletterService = mock(NewsletterService.class);
		undeliverableMailService = mock(UndeliverableMailService.class);
		authenticationController = new AuthenticationController(mock(Environment.class), newsletterService, undeliverableMailService);
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testUnsubscribe() throws IOException
	{
		final UserMailTemplate mailTemplate = UserMailTemplate.NEW_FOOTPRINT;
		runLink(createEmailLink(EmailLinkContext.UNSUBSCRIBE, ParameterType.MAIL_TEMPLATE, mailTemplate.name()));
		
		final ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
		verify(userService).unsubscribe(user, mailTemplate);
		verify(response).sendRedirect(redirectCaptor.capture());
		
		assertThat(redirectCaptor.getValue(), CoreMatchers.containsString("email=" + EMAIL));
		assertThat(redirectCaptor.getValue(), CoreMatchers.containsString("unsubscribed"));
	}
	
	@Test
	public void testResetPassword() throws IOException
	{
		final String password = "password";
		runLink(createEmailLink(EmailLinkContext.RESET_PASSWORD, ParameterType.PASSWORD, password));
		
		final ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
		verify(userService).resetPassword(user, password);
		verify(response).sendRedirect(redirectCaptor.capture());
		
		assertThat(redirectCaptor.getValue(), CoreMatchers.containsString(EMAIL));
		assertThat(redirectCaptor.getValue(), CoreMatchers.containsString(password));
	}
	
	@Test
	public void testValidate() throws IOException
	{
		runLink(createEmailLink(EmailLinkContext.VALIDATE, ParameterType.EMAIL, EMAIL));
		
		final ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
		
		verify(newsletterService).removeMail(user);
		verify(undeliverableMailService).remove(user);
		verify(response).sendRedirect(redirectCaptor.capture());
		
		assertThat(redirectCaptor.getValue(), CoreMatchers.containsString("email=" + EMAIL));
		assertThat(redirectCaptor.getValue(), CoreMatchers.containsString("confirm"));
	}
	
	private void runLink(EmailLink emailLink)
	{
		final HttpServletRequest request = mock(HttpServletRequest.class);
		final String token = "token";
		
		when(appUrlBuilder.toAutoLogin(any(String.class), any(String.class))).thenAnswer(invocation -> new URI(invocation.getArguments()[0] + "" + invocation.getArguments()[1]));
		when(emailLinkService.useValidLink(token)).thenReturn(emailLink);
		when(appUrlBuilder.toAppPath(any(String.class))).thenAnswer(invocation -> UriComponentsBuilder.newInstance().path(invocation.getArguments()[0].toString()).build().toUri());
		when(userService.changeMail(request, user, EMAIL)).thenReturn(true);
		
		authenticationController.link(response, token, request);
	}
	
	private EmailLink createEmailLink(EmailLinkContext context, ParameterType parameterType, String value)
	{
		final EmailLink emailLink = new EmailLink();
		emailLink.setUser(user);
		
		final EmailLinkParameter emailLinkParameter = new EmailLinkParameter();
		emailLinkParameter.setParameterType(parameterType);
		emailLinkParameter.setValue(value);
		emailLink.setEmailLinkParameters(Collections.singleton(emailLinkParameter));
		emailLink.setContext(context);
		
		return emailLink;
	}
}
