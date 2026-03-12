package de.binaerebauten.gleichklang.core.repository.mail;

import de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus;
import de.binaerebauten.gleichklang.core.model.mail.MailQueueEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for {@link MailQueueEntry} entities.
 */
public interface MailQueueEntryRepository extends JpaRepository<MailQueueEntry, Long>
{
	/**
	 * Finds all mail queue entries with the given delivery status.
	 *
	 * @param deliveryStatus the non-null delivery status
	 * @param pageable       the pageable
	 *
	 * @return the page with the mail queue entries.
	 */
	Page<MailQueueEntry> findByDeliveryStatus(MailDeliveryStatus deliveryStatus, Pageable pageable);
	
	/**
	 * Finds all current pending mail queue entries.
	 *
	 * @param pageable       the pageable
	 *
	 * @return the page with the mail queue entries.
	 */
	@Query("FROM MailQueueEntry e WHERE e.deliveryStatus = de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus.PENDING AND e.mailTemplate != de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate.NEW_BOXNUMBER_CONTACT AND (e.nextRetryDate IS NULL OR e.nextRetryDate < NOW())")
	Page<MailQueueEntry> findPendingMailQueueEntries(Pageable pageable);

	/**
	 * Finds all scheduled mail queue entries.
	 *
	 * @param pageable       the pageable
	 *
	 * @return the page with the mail queue entries.
	 */
	@Query("FROM MailQueueEntry e WHERE e.deliveryStatus = de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus.SCHEDULED AND e.nextReminderDate < NOW()")
	Page<MailQueueEntry> findScheduledMailQueueEntries(Pageable pageable);
	
	/**
	 * Finds all scheduled mail queue entries.
	 *
	 * @param pageable       the pageable
	 *
	 * @return the page with the mail queue entries.
	 */
	@Query("FROM MailQueueEntry e WHERE "
			+ "e.deliveryStatus = de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus.SENT OR "
			+ "e.deliveryStatus = de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus.DESCHEDULED OR "
			+ "e.deliveryStatus = de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus.DISCARDED")
	Page<MailQueueEntry> findMailQueueEntriesForCleanUp(Pageable pageable);

//	@Query("FROM MailQueueEntry e WHERE e.mailTemplate = de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate.NEW_FOOTPRINT")
//	Page<MailQueueEntry> findMailQueueEntriesForCleanUpFootPrints(Pageable pageable);

	@Query("SELECT COUNT(*) FROM MailQueueEntry e WHERE e.deliveryStatus = de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus.PENDING")
	int countPendingMails();

	@Transactional
	@Query("FROM MailQueueEntry e WHERE "
            +"e.deliveryStatus = de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus.PENDING")
	List<MailQueueEntry> deleteExcedingMails();

	@Query("FROM MailQueueEntry e where e.mailTemplate = de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate.NEW_BOXNUMBER_CONTACT AND createDate<?1 AND e.deliveryStatus = de.binaerebauten.gleichklang.core.model.mail.MailDeliveryStatus.PENDING")
	Page<MailQueueEntry> findByMailTemplate(LocalDateTime localDateTime,Pageable pageable);

}
