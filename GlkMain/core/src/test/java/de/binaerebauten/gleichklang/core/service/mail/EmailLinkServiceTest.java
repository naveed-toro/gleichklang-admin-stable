//package de.binaerebauten.gleichklang.core.service.mail;
//
//import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
//import de.binaerebauten.gleichklang.core.model.user.EmailLink;
//import de.binaerebauten.gleichklang.core.model.user.EmailLink.EmailLinkContext;
//import de.binaerebauten.gleichklang.core.model.user.EmailLinkParameter.ParameterType;
//import de.binaerebauten.gleichklang.core.model.user.User;
//import de.binaerebauten.gleichklang.core.repository.user.EmailLinkRepository;
//import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//
//import java.net.URI;
//import java.util.Collections;
//import java.util.List;
//
//import static org.hamcrest.CoreMatchers.*;
//import static org.hamcrest.text.IsEmptyString.emptyOrNullString;
//import static org.junit.Assert.assertThat;
//import static org.mockito.AdditionalAnswers.returnsFirstArg;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.*;
//
//@RunWith(MockitoJUnitRunner.class)
//public class EmailLinkServiceTest
//{
//	@InjectMocks
//	private EmailLinkService emailLinkService;
//
//	@Mock
//	private EmailLinkRepository emailLinkRepository;
//
//	@Mock
//	private AppUrlBuilder appUrlBuilder;
//
//	@Test
//	public void cleanUpTest()
//	{
//		final List<Long> expiredIds = Collections.singletonList(1L);
//		when(emailLinkRepository.findExpiredIds(any())).thenReturn(expiredIds);
//
//		emailLinkService.cleanUp();
//
//		verify(emailLinkRepository).findExpiredIds(any());
//		verify(emailLinkRepository).deleteByIdIn(expiredIds);
//	}
//
//	@Test
//	public void createUnsubscribeEmailLinkTest()
//	{
//		final User user = new User();
//		user.setId(42L);
//		user.setEmail("valid@example.com");
//
//		when(emailLinkRepository.save(any(EmailLink.class))).then(returnsFirstArg());
//		when(appUrlBuilder.toApi(anyString())).thenReturn(URI.create("localhost:8080"));
//
//		final UserMailTemplate mailTemplate = UserMailTemplate.NEWS_FROM_GLEICHKLANG;
//		final EmailLink emailLink = emailLinkService.createUnsubscribeEmailLink(user, mailTemplate);
//
//		assertThat(emailLink, notNullValue());
//		assertThat(emailLink.getUser(), equalTo(user));
//		assertThat(emailLink.getUniqueToken(), not(emptyOrNullString()));
//		assertThat(emailLink.getUri(), notNullValue());
//		assertThat(emailLink.getContext(), equalTo(EmailLinkContext.UNSUBSCRIBE));
//		assertThat(emailLink.getEmailLinkParameters().size(), equalTo(1));
//		assertThat(emailLink.getEmailLinkParameters().iterator().next().getParameterType(), equalTo(ParameterType.MAIL_TEMPLATE));
//		assertThat(emailLink.getEmailLinkParameters().iterator().next().getValue(), equalTo(mailTemplate.toString()));
//
//		verify(emailLinkRepository).save(emailLink);
//	}
//
//	@Test
//	public void createPasswordEmailLinkTest()
//	{
//		final User user = new User();
//		user.setId(42L);
//		user.setEmail("valid@example.com");
//
//		when(emailLinkRepository.save(any(EmailLink.class))).then(returnsFirstArg());
//		when(appUrlBuilder.toApi(anyString())).thenReturn(URI.create("localhost:8080"));
//
//		final String password = "password";
//		final EmailLink emailLink = emailLinkService.createPasswordEmailLink(user, password);
//
//		assertThat(emailLink, notNullValue());
//		assertThat(emailLink.getUser(), equalTo(user));
//		assertThat(emailLink.getUniqueToken(), not(emptyOrNullString()));
//		assertThat(emailLink.getUri(), notNullValue());
//		assertThat(emailLink.getContext(), equalTo(EmailLinkContext.RESET_PASSWORD));
//		assertThat(emailLink.getEmailLinkParameters().size(), equalTo(1));
//		assertThat(emailLink.getEmailLinkParameters().iterator().next().getParameterType(), equalTo(ParameterType.PASSWORD));
//		assertThat(emailLink.getEmailLinkParameters().iterator().next().getValue(), equalTo(password));
//
//		verify(emailLinkRepository).save(emailLink);
//	}
//
//	@Test
//	public void createValidateEmailLinkTest()
//	{
//		final User user = new User();
//		user.setId(42L);
//		user.setEmail("valid@example.com");
//
//		when(emailLinkRepository.save(any(EmailLink.class))).then(returnsFirstArg());
//		when(appUrlBuilder.toApi(anyString())).thenReturn(URI.create("localhost:8080"));
//
//		final String newMail = "newMail";
//		final EmailLink emailLink = emailLinkService.createValidateEmailLink(user, newMail);
//
//		assertThat(emailLink, notNullValue());
//		assertThat(emailLink.getUser(), equalTo(user));
//		assertThat(emailLink.getUniqueToken(), not(emptyOrNullString()));
//		assertThat(emailLink.getUri(), notNullValue());
//		assertThat(emailLink.getContext(), equalTo(EmailLinkContext.VALIDATE));
//		assertThat(emailLink.getEmailLinkParameters().size(), equalTo(1));
//		assertThat(emailLink.getEmailLinkParameters().iterator().next().getParameterType(), equalTo(ParameterType.EMAIL));
//		assertThat(emailLink.getEmailLinkParameters().iterator().next().getValue(), equalTo(newMail));
//
//		verify(emailLinkRepository).save(emailLink);
//		verify(emailLinkRepository).deleteByUserAndContext(user, EmailLinkContext.VALIDATE);
//	}
//
//	@Test
//	public void useValidLinkTest()
//	{
//		final String token = "test";
//		final EmailLink emailLink = new EmailLink();
//
//		when(emailLinkRepository.findByUniqueToken(token)).thenReturn(emailLink);
//
//		assertThat(emailLinkService.useValidLink(token), equalTo(emailLink));
//
//		verify(emailLinkRepository).delete(emailLink);
//
//	}
//}
