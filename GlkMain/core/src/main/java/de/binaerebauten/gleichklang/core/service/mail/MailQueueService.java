package de.binaerebauten.gleichklang.core.service.mail;

import de.binaerebauten.gleichklang.core.model.mail.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.mail.MailQueueEntryRepository;
import de.binaerebauten.gleichklang.core.repository.mail.UserMailQueueEntryRepository;
import de.binaerebauten.gleichklang.core.utils.MailReminder;
import de.binaerebauten.gleichklang.core.utils.MailReminder.TemplateConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * This service is responsible for storing mails as {@link MailQueueEntry} for later sending.
 */
@Service
public class MailQueueService
{
	@Autowired
	private MailQueueEntryRepository mailQueueEntryRepository;
	
	@Autowired
	private UserMailQueueEntryRepository userMailQueueEntryRepository;
	
	@Autowired
	private MailReminder<MailQueueEntry> mailReminder;
	
	/**
	 * Creates a new {@link UnregisteredUserMailQueueEntry} and stores it in the database so that
	 * the {@link MailSendService} can asynchronously send it.
	 *
	 * @param recipientEmail
	 * @param mailTemplate
	 */
	@Transactional
	public MailQueueEntry enqueue(String recipientEmail, UserMailTemplate mailTemplate)
	{
		Objects.requireNonNull(recipientEmail, "recipientEmail == null");
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		
		UnregisteredUserMailQueueEntry mailQueueEntry = new UnregisteredUserMailQueueEntry();
		mailQueueEntry.setRecipientEmail(recipientEmail);
		mailQueueEntry.setMailTemplate(mailTemplate);
		mailQueueEntry.setDeliveryStatus(MailDeliveryStatus.PENDING);
		mailQueueEntry.setUndeliverableMailReason(UndeliverableMailReason.NONE);
		
		return mailQueueEntryRepository.save(mailQueueEntry);
	}
	
	/**
	 * Creates a new {@link UserMailQueueEntry} and stores it in the database so that
	 * the {@link MailSendService} can asynchronously send it.
	 *
	 * @param recipient
	 * @param mailTemplate
	 */
	@Transactional
	public MailQueueEntry enqueue(User recipient, UserMailTemplate mailTemplate)
	{
		return enqueue(recipient.getId(), mailTemplate, null);
	}

	@Transactional
	public MailQueueEntry enqueue(User recipient, UserMailTemplate mailTemplate, User sourceuser)
	{
		return enqueue(recipient.getId(), mailTemplate, null,sourceuser);
	}
	
	/**
	 * Creates a new {@link UserMailQueueEntry} and stores it in the database so that
	 * the {@link MailSendService} can asynchronously send it on specific date (if date is presented).
	 *
	 * @param recipient
	 * @param mailTemplate
	 */
	@Transactional
	public MailQueueEntry enqueue(User recipient, UserMailTemplate mailTemplate, LocalDateTime dateTime)
	{
		return enqueue(recipient.getId(), mailTemplate, dateTime);
	}
	
	/**
	 * Creates a new {@link UserMailQueueEntry} and stores it in the database so that
	 * the {@link MailSendService} can asynchronously send it.
	 *
	 * @param recipientId
	 * @param mailTemplate
	 */
	@Transactional
	public MailQueueEntry enqueue(Long recipientId, UserMailTemplate mailTemplate)
	{
		return enqueue(recipientId, mailTemplate, null);
	}
	
	/**
	 * Creates a new {@link UserMailQueueEntry} and stores it in the database so that
	 * the {@link MailSendService} can asynchronously send it on specific date (if date is presented).
	 *
	 * @param recipientId
	 * @param mailTemplate
	 * @param dateTime
	 */
	private MailQueueEntry enqueue(Long recipientId, UserMailTemplate mailTemplate, LocalDateTime dateTime)
	{
		Objects.requireNonNull(recipientId, "recipientId == null");
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		
		UserMailQueueEntry userMailQueueEntry = new UserMailQueueEntry();
		userMailQueueEntry.setRecipientId(recipientId);
		userMailQueueEntry.setMailTemplate(mailTemplate);
		userMailQueueEntry.setUndeliverableMailReason(UndeliverableMailReason.NONE);
		
		if (Objects.nonNull(dateTime))
		{
			userMailQueueEntry.setDeliveryStatus(MailDeliveryStatus.SCHEDULED);
			userMailQueueEntry.setNextReminderDate(dateTime);
		}
		else
		{
			TemplateConfiguration templateConfiguration = mailReminder.getTemplateConfiguration(mailTemplate);
			if (Objects.nonNull(templateConfiguration))
			{
				userMailQueueEntry.setDeliveryStatus(MailDeliveryStatus.SCHEDULED);
				userMailQueueEntry.setNextReminderDate(LocalDateTime.now()
						.plus(templateConfiguration.getDelay())
						.with(templateConfiguration.getTime()));
			}
			else
			{
				userMailQueueEntry.setDeliveryStatus(MailDeliveryStatus.PENDING);
			}
		}
		
		return mailQueueEntryRepository.save(userMailQueueEntry);
	}


	private MailQueueEntry enqueue(Long recipientId, UserMailTemplate mailTemplate, LocalDateTime dateTime,User sourceUser)
	{
		Objects.requireNonNull(recipientId, "recipientId == null");
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");

		UserMailQueueEntry userMailQueueEntry = new UserMailQueueEntry();
		userMailQueueEntry.setRecipientId(recipientId);
		userMailQueueEntry.setMailTemplate(mailTemplate);
		userMailQueueEntry.setUndeliverableMailReason(UndeliverableMailReason.NONE);
		userMailQueueEntry.setSenderId(sourceUser.getId());

		if (Objects.nonNull(dateTime))
		{
			userMailQueueEntry.setDeliveryStatus(MailDeliveryStatus.SCHEDULED);
			userMailQueueEntry.setNextReminderDate(dateTime);
		}
		else
		{
			TemplateConfiguration templateConfiguration = mailReminder.getTemplateConfiguration(mailTemplate);
			if (Objects.nonNull(templateConfiguration))
			{
				userMailQueueEntry.setDeliveryStatus(MailDeliveryStatus.SCHEDULED);
				userMailQueueEntry.setNextReminderDate(LocalDateTime.now()
						.plus(templateConfiguration.getDelay())
						.with(templateConfiguration.getTime()));
			}
			else
			{
				userMailQueueEntry.setDeliveryStatus(MailDeliveryStatus.PENDING);
			}
		}

		return mailQueueEntryRepository.save(userMailQueueEntry);
	}
	
	/**
	 * Removes all {@link MailQueueEntry} entries with given template for specific user.
	 *
	 * @param user
	 * @param template
	 */
	public void dequeue(User user, UserMailTemplate template)
	{
		userMailQueueEntryRepository.deleteByRecipientAndMailTemplate(user, template);
	}

	public int countPendingMails(){
	return 	mailQueueEntryRepository.countPendingMails();
	}

	@Transactional
	public void removeExcedingMails(){
		mailQueueEntryRepository.delete(mailQueueEntryRepository.deleteExcedingMails());
	}
}
