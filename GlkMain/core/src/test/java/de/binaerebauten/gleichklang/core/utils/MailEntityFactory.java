package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMailReason;
import de.binaerebauten.gleichklang.core.model.mail.UserMailQueueEntry;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.mail.UserMailQueueEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Component
public class MailEntityFactory
{
	@Autowired
	private UserMailQueueEntryRepository userMailQueueEntryRepository;

	@Transactional
	public UserMailQueueEntry persistMailQueueEntry(UserMailTemplate mailTemplate, User recipient, MailDeliveryStatus mailDeliveryStatus)
	{
		UserMailQueueEntry userMailQueueEntry = createMailQueueEntry(mailTemplate, recipient, mailDeliveryStatus);
		
		userMailQueueEntryRepository.save(userMailQueueEntry);

		return userMailQueueEntry;
	}

	public UserMailQueueEntry createMailQueueEntry(UserMailTemplate mailTemplate, User recipient, MailDeliveryStatus mailDeliveryStatus)
	{
		UserMailQueueEntry userMailQueueEntry = new UserMailQueueEntry();
		
		userMailQueueEntry.setRecipient(recipient);
		
		userMailQueueEntry.setDeliveryStatus(mailDeliveryStatus);
		userMailQueueEntry.setMailTemplate(mailTemplate);
		userMailQueueEntry.setUndeliverableMailReason(UndeliverableMailReason.NONE);
		userMailQueueEntry.setNextReminderDate(LocalDateTime.now());

		return userMailQueueEntry;
	}
}
