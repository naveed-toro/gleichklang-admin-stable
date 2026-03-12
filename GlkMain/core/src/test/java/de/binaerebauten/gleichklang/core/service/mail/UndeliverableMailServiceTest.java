package de.binaerebauten.gleichklang.core.service.mail;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import de.binaerebauten.gleichklang.core.config.UndeliverableMailConfig;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMailReason;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.mail.UndeliverableMailRepository;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.internet.MimeMessage;
import javax.mail.search.FromStringTerm;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UndeliverableMailService}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = UndeliverableMailConfig.class)
public class UndeliverableMailServiceTest
{
	private static final String RECIPIENT_EMAIL = "test-me@invalid-address.com";
	
	@Value("#{undeliverableMailReasonPatterns}")
	private Map<Pattern, UndeliverableMailReason> undeliverableMailReasonPatterns;
	
	@Autowired
	private FromStringTerm mailerDaemonPattern;
	
	private UndeliverableMailService undeliverableMailService;
	
	private UndeliverableMailRepository undeliverableMailRepository;
	
	private JavaMailSenderImpl sender;
	
	@Before
	public void setup()
	{
		sender = new JavaMailSenderImpl();
		sender.setHost("mail.host.com");
		
		undeliverableMailRepository = mock(UndeliverableMailRepository.class);
		undeliverableMailService = new UndeliverableMailService(undeliverableMailReasonPatterns,
				undeliverableMailRepository, mailerDaemonPattern);
	}
	
	@Test
	public void testProcessUndeliverableMessage() throws Exception
	{
		final MimeMessage invalidAdressMessage = createMimeMessage("INVALID_ADDRESS-MAILER-DAEMON-response.txt");
		
		undeliverableMailService.processUndeliverableMessage(invalidAdressMessage,true);
		
		final ArgumentCaptor<UndeliverableMail> undeliverableMailArgumentCaptor =
				ArgumentCaptor.forClass(UndeliverableMail.class);
		
		verify(undeliverableMailRepository).save(undeliverableMailArgumentCaptor.capture());
		
		final UndeliverableMail savedUndeliverableMail = undeliverableMailArgumentCaptor.getValue();
		assertThat(savedUndeliverableMail.getUndeliverableMailReason(), is(UndeliverableMailReason.INVALID_ADDRESS));
	}
	
	@Test
	public void testProcessUndeliverableMessage_UpdateExisting() throws Exception
	{
		final MimeMessage invalidAddressMessage = createMimeMessage("INVALID_ADDRESS-MAILER-DAEMON-response.txt");
		
		final UndeliverableMail undeliverableMail = new UndeliverableMail();
		undeliverableMail.setRecipientEmail(RECIPIENT_EMAIL);
		undeliverableMail.setUndeliverableMailReason(UndeliverableMailReason.MAILBOX_FULL);
		
		when(undeliverableMailRepository.findByRecipientEmailAndUndeliverableMailReason(RECIPIENT_EMAIL, UndeliverableMailReason.MAILBOX_FULL)).thenReturn(undeliverableMail);
		
		undeliverableMailService.processUndeliverableMessage(invalidAddressMessage,true);
		
		final ArgumentCaptor<UndeliverableMail> undeliverableMailArgumentCaptor =
				ArgumentCaptor.forClass(UndeliverableMail.class);
		
		verify(undeliverableMailRepository).save(undeliverableMailArgumentCaptor.capture());
		
		final UndeliverableMail savedUndeliverableMail = undeliverableMailArgumentCaptor.getValue();
		assertThat(savedUndeliverableMail.getUndeliverableMailReason(), is(UndeliverableMailReason.INVALID_ADDRESS));
	}
	
	private MimeMessage createMimeMessage(String resourceName) throws Exception
	{
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		helper.setFrom("MAILER-DAEMON@binaere-bauten.de");
		helper.setTo("test@host.com");
		final URL resource = Resources.getResource(resourceName);
		
		final String messageText =
				Resources.toString(resource, Charsets.UTF_8);
		
		helper.setText(messageText);
		
		return message;
	}
	
	@Test
	public void testAddToBlacklist() throws ValidationException
	{
		final String[] validMailAdresses = { "foobar@gmx.de", "foobar@gmx.euro", "FOObar0123._%+-@Gmx09-..dL9" };
		final String[] invalidMailAdresses = { "foobar", "foobar@", "foobar@gmx.dexxx", "foobar@gmx.§§", "foobar@gmx.d", "foobar@$$.de", "foo$$bar@gmx.de", "foobar@gmx" };
		final UndeliverableMailReason reason = UndeliverableMailReason.UNKNOWN;
		
		Arrays.stream(invalidMailAdresses).forEach(e -> assertThat(undeliverableMailService.addToBlacklist(e, reason), equalTo(false)));
		verifyZeroInteractions(undeliverableMailRepository);
		
		Arrays.stream(validMailAdresses).forEach(e -> assertThat(undeliverableMailService.addToBlacklist(e, reason), equalTo(true)));
		verify(undeliverableMailRepository, times(validMailAdresses.length)).save(Matchers.any(UndeliverableMail.class));
		
		Mockito.reset(undeliverableMailRepository);
		
		assertThat(undeliverableMailService.addToBlacklist(Arrays.asList(invalidMailAdresses), reason), equalTo(0L));
		verifyZeroInteractions(undeliverableMailRepository);
		assertThat(undeliverableMailService.addToBlacklist(Arrays.asList(validMailAdresses), reason), equalTo((long) validMailAdresses.length));
		verify(undeliverableMailRepository, times(validMailAdresses.length)).save(Matchers.any(UndeliverableMail.class));
		
		Mockito.reset(undeliverableMailRepository);
		
		final String testMail = "foobar@gmx.de";
		final User user = DefaultStaticEntityFactory.createDefaultUser(testMail, testMail);
		
		undeliverableMailService.addUserToBlacklist(user);
		final ArgumentCaptor<UndeliverableMail> undeliverableMail = ArgumentCaptor.forClass(UndeliverableMail.class);
		verify(undeliverableMailRepository).saveAndFlush(undeliverableMail.capture());
		assertThat(undeliverableMail.getValue().getRecipientEmail(), equalTo(testMail));
		assertThat(undeliverableMail.getValue().getUndeliverableMailReason(), equalTo(UndeliverableMailReason.MANUAL));
	}
	
	@Test
	public void testRemove()
	{
		final String email = "test@example.com";
		
		final User user = new User();
		user.setEmail(email);
		
		final UndeliverableMail undeliverableMail = new UndeliverableMail();
		final Set<UndeliverableMail> undeliverableMails = Collections.singleton(undeliverableMail);
		
		// test user
		when(undeliverableMailRepository.findByRecipientEmail(email)).thenReturn(undeliverableMails);
		
		undeliverableMailService.remove(user);
		
		verify(undeliverableMailRepository).findByRecipientEmail(email);
		verify(undeliverableMailRepository).delete(undeliverableMails);
		
		reset(undeliverableMailRepository);
		
		//test undeliverableMail
		undeliverableMailService.remove(undeliverableMail);
		
		verify(undeliverableMailRepository).delete(undeliverableMail);
	}
}
