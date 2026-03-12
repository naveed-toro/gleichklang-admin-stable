package de.binaerebauten.gleichklang.core.config;

import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMailReason;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.internet.MimeMessage;
import javax.mail.search.FromStringTerm;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link UndeliverableMailConfig}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = UndeliverableMailConfig.class)
public class UndeliverableMailConfigTest
{
	@Value("#{undeliverableMailReasonPatterns}")
	private Map<Pattern, UndeliverableMailReason> undeliverableMailReasonPatterns;

	@Autowired
	private FromStringTerm mailerDaemonPattern;

	@Test
	public void testConfig_undeliverableMailReasonPatterns()
	{
		assertThat(undeliverableMailReasonPatterns, not(nullValue()));

		assertThat(undeliverableMailReasonPatterns.values(), hasItem(UndeliverableMailReason.DOMAIN_NOT_FOUND));
		assertThat(undeliverableMailReasonPatterns.values(), hasItem(UndeliverableMailReason.INVALID_ADDRESS));
		assertThat(undeliverableMailReasonPatterns.values(), hasItem(UndeliverableMailReason.MAILBOX_FULL));
		assertThat(undeliverableMailReasonPatterns.values(), hasItem(UndeliverableMailReason.RELAY_ACCESS_DENIED));
		assertThat(undeliverableMailReasonPatterns.values(), hasItem(UndeliverableMailReason.SPAM_FILTER));
		assertThat(undeliverableMailReasonPatterns.values(), hasItem(UndeliverableMailReason.UNROUTABLE_ADDRESS));

		final Optional<UndeliverableMailReason> noMailBox =
				undeliverableMailReasonPatterns.entrySet().stream()
						.filter(e -> e.getKey().matcher("no mailbox").find()).findFirst().map(Map.Entry::getValue);
		assertThat(noMailBox.isPresent(), is(true));
	}

	@Test
	public void testConfig_mailerDaemonPattern() throws Exception
	{
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("MAILER-DAEMON@binaere-bauten.de");

		assertThat(mailerDaemonPattern.match(message), is(true));

		helper.setFrom("mailer-daemon@binaere-bauten.de");

		assertThat(mailerDaemonPattern.match(message), is(true));

		helper.setFrom("mailer-daemon@gleichklang.de");

		assertThat(mailerDaemonPattern.match(message), is(true));
	}
}
