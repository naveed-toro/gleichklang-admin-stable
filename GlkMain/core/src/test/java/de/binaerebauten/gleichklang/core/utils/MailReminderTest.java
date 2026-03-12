package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link MailReminder}.
 */
public class MailReminderTest
{
	private MailReminder mailReminder;

	@Before
	public void setup()
	{
		mailReminder = MailReminder.builder(ChronoUnit.DAYS)
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_NEW, 0, 1, Duration.ofDays(1))
				.addReminder(UserMailTemplate.SOCIAL_REGISTERED_USER, 2, 3, Duration.ofDays(2))
				.build();
	}

	@Test
	public void testGetNextReminderDate()
	{
		LocalDateTime now = LocalDateTime.now();

		Optional<LocalDateTime> nextReminderDate = mailReminder.getNextReminderDate(now, 0);
		assertThat(nextReminderDate.get(), is(now.plusDays(1)));

		nextReminderDate = mailReminder.getNextReminderDate(now, 1);
		assertThat(nextReminderDate.get(), is(now.plusDays(1)));

		nextReminderDate = mailReminder.getNextReminderDate(now, 2);
		assertThat(nextReminderDate.get(), is(now.plusDays(2)));

		nextReminderDate = mailReminder.getNextReminderDate(now, 3);
		assertThat(nextReminderDate.get(), is(now.plusDays(2)));

		nextReminderDate = mailReminder.getNextReminderDate(now, 4);
		assertThat(nextReminderDate.isPresent(), is(false));
	}

	@Test
	public void testGetMailTemplate()
	{
		assertThat(mailReminder.getMailTemplate(0).get(), is(UserMailTemplate.PREPAYMENT_REMINDER_NEW));
		assertThat(mailReminder.getMailTemplate(1).get(), is(UserMailTemplate.PREPAYMENT_REMINDER_NEW));

		assertThat(mailReminder.getMailTemplate(2).get(), is(UserMailTemplate.SOCIAL_REGISTERED_USER));
		assertThat(mailReminder.getMailTemplate(3).get(), is(UserMailTemplate.SOCIAL_REGISTERED_USER));

		assertThat(mailReminder.getMailTemplate(4).isPresent(), is(false));
	}
}
