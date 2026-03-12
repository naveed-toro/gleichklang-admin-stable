package de.binaerebauten.gleichklang.core.repository.mail;

import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMailReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Repository for accessing the {@link UndeliverableMail} entities.
 */
@Repository
public interface UndeliverableMailRepository extends JpaRepository<UndeliverableMail, Long>, JpaSpecificationExecutor<UndeliverableMail>
{
	/**
	 * Finds the undeliverable mails with the given recipient email.
	 *
	 * @param recipientEmail the recipient email
	 * @return the undeliverable mails
	 */
	Set<UndeliverableMail> findByRecipientEmail(String recipientEmail);
	
	/**
	 * Finds the undeliverable mail with the given recipient email.
	 *
	 * @param recipientEmail the recipient email
	 * @return the undeliverable mail or null if no entity was found
	 */
	UndeliverableMail findByRecipientEmailAndUndeliverableMailReason(String recipientEmail, UndeliverableMailReason undeliverableMailReason);

	@Modifying
	@Transactional
	@Query("DELETE FROM UndeliverableMail u WHERE (u.undeliverableMailReason= 'MAILBOX_FULL' or u.undeliverableMailReason= 'UNKNOWN' or u.undeliverableMailReason='NONE' or u.undeliverableMailReason= 'UNROUTABLE_ADDRESS' or u.undeliverableMailReason= 'RELAY_ACCESS_DENIED')  AND u.createDate < ?1")
	void deleteSoftBounceMails(LocalDateTime time);
}
