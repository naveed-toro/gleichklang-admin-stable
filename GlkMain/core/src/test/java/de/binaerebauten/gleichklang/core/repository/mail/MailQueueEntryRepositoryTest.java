package de.binaerebauten.gleichklang.core.repository.mail;

import de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus;
import de.binaerebauten.gleichklang.core.model.mail.MailQueueEntry;
import de.binaerebauten.gleichklang.core.model.mail.UserMailQueueEntry;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.MailEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link MailQueueEntryRepository}.
 */
public class MailQueueEntryRepositoryTest extends AbstractRepositoryTest<MailQueueEntry>
{
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Autowired
	private MailEntityFactory mailEntityFactory;

	@Autowired
	private MailQueueEntryRepository mailQueueEntryRepository;
	
	@Autowired
	private UserMailQueueEntryRepository userMailQueueEntryRepository;

	private User user;
	private UserMailQueueEntry pendingMailQueueEntry;
	private UserMailQueueEntry sentMailQueueEntry;
	private UserMailQueueEntry scheduledMailQueueEntry;

	@Override
	protected Collection<MailQueueEntry> getPersistedEntities()
	{
		user = defaultEntityFactory.persistDefaultUser("me");
		
		pendingMailQueueEntry = mailEntityFactory.
				persistMailQueueEntry(UserMailTemplate.SOCIAL_REGISTERED_USER, user, MailDeliveryStatus.PENDING);
		
		sentMailQueueEntry = mailEntityFactory.
				persistMailQueueEntry(UserMailTemplate.SOCIAL_REGISTERED_USER, user, MailDeliveryStatus.SENT);
		
		scheduledMailQueueEntry = mailEntityFactory.
				persistMailQueueEntry(UserMailTemplate.SOCIAL_REGISTERED_USER, user, MailDeliveryStatus.SCHEDULED);
		scheduledMailQueueEntry.setNextReminderDate(LocalDateTime.now().minus(1, ChronoUnit.MINUTES));
		scheduledMailQueueEntry = userMailQueueEntryRepository.save(scheduledMailQueueEntry);

		return Arrays.asList(pendingMailQueueEntry, sentMailQueueEntry, scheduledMailQueueEntry);
	}

	@Override
	protected JpaRepository<MailQueueEntry, Long> getRepository()
	{
		return mailQueueEntryRepository;
	}

	@Test
	public void testFindByDeliveryStatus()
	{
		PageRequest pageRequest = new PageRequest(0, 5);

		Page<MailQueueEntry> page = mailQueueEntryRepository.
				findByDeliveryStatus(MailDeliveryStatus.PENDING, pageRequest);

		assertThat(page.getTotalElements(), is(1L));
		assertThat(page.getContent(), hasItem(pendingMailQueueEntry));

		page = mailQueueEntryRepository.
				findByDeliveryStatus(MailDeliveryStatus.SENT, pageRequest);

		assertThat(page.getTotalElements(), is(1L));
		assertThat(page.getContent(), hasItem(sentMailQueueEntry));
	}
	
	@Test
	public void testFindScheduledMailQueueEntries()
	{
		PageRequest pageRequest = new PageRequest(0, 5);
		
		Page<MailQueueEntry> page = mailQueueEntryRepository.findScheduledMailQueueEntries(pageRequest);
		
		assertThat(page.getTotalElements(), is(1L));
		assertThat(page.getContent(), hasItem(scheduledMailQueueEntry));
	}
	
	@Test
	public void testDeleteByRecipientEmailAndMailTemplate()
	{
		assertThat(userMailQueueEntryRepository.findAll().size(), equalTo(3));
		
		userMailQueueEntryRepository.deleteByRecipientAndMailTemplate(user, UserMailTemplate.SOCIAL_REGISTERED_USER);
		
		assertThat(userMailQueueEntryRepository.findAll().size(), equalTo(0));
	}
	
}
