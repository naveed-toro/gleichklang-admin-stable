package de.binaerebauten.gleichklang.core.repository.mail;

import de.binaerebauten.gleichklang.core.model.mail.UserMailQueueEntry;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

/**
 * Repository for {@link UserMailQueueEntry} entities.
 */
public interface UserMailQueueEntryRepository extends JpaRepository<UserMailQueueEntry, Long>
{
	/**
	 * Deletes all mail queue entries with the given recipient E-Mail and template.
	 *
	 * @param recipient
	 * @param mailTemplate
	 */
	@Transactional
	void deleteByRecipientAndMailTemplate(User recipient, UserMailTemplate mailTemplate);
}
