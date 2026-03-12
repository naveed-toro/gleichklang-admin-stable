package de.binaerebauten.gleichklang.core.service.mail;

import de.binaerebauten.gleichklang.core.model.mail.Newsletter;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.mail.NewsletterRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NewsletterServiceTest
{
	@InjectMocks
	private NewsletterService newsletterService;
	
	@Mock
	private NewsletterRepository newsletterRepository;
	
	@Test
	public void addMailTest()
	{
		final String email = "test@example.com";
		final String confirmationIp = "testIp";
		final LocalDateTime confirmationDate = LocalDateTime.of(2000, 1, 24, 3, 42);
		
		final User user = new User();
		user.setEmail(email);
		user.setEmailConfirmed(true);
		user.setConfirmationIp(confirmationIp);
		user.setConfirmationDate(confirmationDate);
		
		newsletterService.addMail(user);
		
		final ArgumentCaptor<Newsletter> newsletterCaptor = ArgumentCaptor.forClass(Newsletter.class);
		verify(newsletterRepository).saveAndFlush(newsletterCaptor.capture());
		final Newsletter newsletter = newsletterCaptor.getValue();
		assertThat(newsletter.getEmail(), equalTo(email));
		assertThat(newsletter.getConfirmationIp(), equalTo(confirmationIp));
		assertThat(newsletter.getConfirmationDate(), equalTo(confirmationDate));
		
		Mockito.reset(newsletterRepository);
		
		user.setEmail(null);
		newsletterService.addMail(user);
		user.setEmail(email);
		
		user.setEmailConfirmed(false);
		newsletterService.addMail(user);
		user.setEmailConfirmed(true);
		
		newsletterService.addMail(null);
		
		verifyZeroInteractions(newsletterRepository);
	}
	
	@Test
	public void removeMailTest()
	{
		final String email = "test@example.com";
		
		final User user = new User();
		user.setEmail(email);
		user.setEmailConfirmed(true);
		
		newsletterService.removeMail(user);
		
		verify(newsletterRepository).deleteByEmail(email);
	}
	
	@Test
	public void createNewsletterHandler()
	{
		assertThat(newsletterService.createNewsletterHandler(), notNullValue());
	}
	
	@Test
	public void deleteTest()
	{
		final Newsletter newsletter = new Newsletter();
		
		newsletterService.delete(newsletter);
		
		verify(newsletterRepository).delete(newsletter);
	}
	
	@Test
	public void removeFromNewsletterTest()
	{
		final HashSet<Newsletter> newsletters = new HashSet<>();
		newsletters.add(new Newsletter());
		
		when(newsletterRepository.findByEmailIn(anyCollectionOf(String.class))).thenReturn(newsletters);
		
		final List<String> emails = new ArrayList<>();
		
		final int count = 1500;
		
		for (int i = 0; i < count; i++)
		{
			emails.add(i + "test@example.com");
		}
		
		final long result = newsletterService.removeFromNewsletter(emails);
		
		assertThat(result, equalTo(2L));
		
		verify(newsletterRepository, times(2)).findByEmailIn(anyCollectionOf(String.class));
		verify(newsletterRepository, times(2)).delete(newsletters);
	}
	
	@Test
	public void exportTest() throws IOException
	{
		final List<Newsletter> newsletters = new ArrayList<>();
		final Newsletter newsletter = new Newsletter();
		newsletter.setEmail("test@example.com");
		newsletters.add(newsletter);
		
		when(newsletterRepository.findAll()).thenReturn(newsletters);
		
		final InputStream stream = newsletterService.export();
		
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		final String[] header = reader.readLine().split(";");
		final String[] values = reader.readLine().split(";");
		
		assertThat(header.length, equalTo(3));
		assertThat(values.length, equalTo(1));
		assertThat(values[0], equalTo(newsletter.getEmail()));
	}
}
