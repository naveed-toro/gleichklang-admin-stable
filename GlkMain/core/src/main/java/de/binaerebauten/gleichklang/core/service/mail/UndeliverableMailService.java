package de.binaerebauten.gleichklang.core.service.mail;

import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMailReason;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.mail.UndeliverableMailRepository;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import org.apache.commons.mail.util.MimeMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FromStringTerm;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for working with {@link UndeliverableMail} entities.
 */
@Service
public class UndeliverableMailService
{
	private static final Logger LOG = LoggerFactory.getLogger(UndeliverableMailService.class);
	
	private final Pattern originalRecipientPattern = Pattern.compile("Original-Recipient: rfc822;(.*)");
	
	private final Map<Pattern, UndeliverableMailReason> undeliverableMailReasonPatterns;
	
	private final UndeliverableMailRepository undeliverableMailRepository;
	
	private final FromStringTerm mailerDaemonAddress;
	
	@Autowired
	public UndeliverableMailService(@Value("#{undeliverableMailReasonPatterns}") Map<Pattern, UndeliverableMailReason> undeliverableMailReasonPatterns,
			UndeliverableMailRepository undeliverableMailRepository, FromStringTerm mailerDaemonAddress)
	{
		this.undeliverableMailReasonPatterns = undeliverableMailReasonPatterns;
		this.undeliverableMailRepository = undeliverableMailRepository;
		this.mailerDaemonAddress = mailerDaemonAddress;
	}
	
	/**
	 * Processes and stores the given undeliverable mail as {@link UndeliverableMail}
	 * entity.
	 *
	 * @param message the non-null undeliverable message
	 */
	@Transactional
	public void processUndeliverableMessage(MimeMessage message, boolean undeliverableMailFlag)
	{
		if (isFromMailerDaemon(message))
		{
			Objects.requireNonNull(message, "message == null");
			
			final MimeMessageParser mimeMessageParser = new MimeMessageParser(message);
			try
			{
				mimeMessageParser.parse();
			}
			catch (Exception e)
			{
				LOG.error("parsing message failed! Ignoring message!", e);
			}
			
			final String plainContent = mimeMessageParser.getPlainContent();
			if (plainContent != null) {
				UndeliverableMailReason undeliverableMailReason = UndeliverableMailReason.UNKNOWN;
				for (Map.Entry<Pattern, UndeliverableMailReason> entry : undeliverableMailReasonPatterns.entrySet())
				{
					final Pattern pattern = entry.getKey();
					if (pattern.matcher(plainContent).find())
					{
						undeliverableMailReason = entry.getValue();
						break;
					}
				}
				
				final Matcher matcher = originalRecipientPattern.matcher(plainContent);

				if(undeliverableMailFlag && plainContent.contains("<") && plainContent.contains(">")) {
					String originalRecipientEmail = plainContent.substring(plainContent.indexOf("<") + 1, plainContent.indexOf(">"));
					if (originalRecipientEmail.contains("@")) {
						addToBlacklist(originalRecipientEmail, undeliverableMailReason);
					}
				}
				else if (matcher.find())
				{
					final String originalRecipientEmail = matcher.group(1);
					addToBlacklist(originalRecipientEmail, undeliverableMailReason);
				}
				else
				{
					LOG.error("Could not extract original recipient from message: {}", plainContent);
				}
				
				try
				{
					message.setFlag(Flags.Flag.DELETED, true);
				}
				catch (MessagingException e)
				{
					LOG.error("error deleting message", e);
				}
			}
		}
	}
	
	/**
	 * Checks if the given message is from the mailer daemon.
	 *
	 * @param message the non-null message to check
	 * @return true iff. the given message is from the configured mailer daemon address
	 */
	public boolean isFromMailerDaemon(MimeMessage message)
	{
		Objects.requireNonNull(message, "message == null");
		
		return mailerDaemonAddress.match(message);
	}
	
	/**
	 * Checks if given email is blocked by an entry in the {@link UndeliverableMail}
	 * entities.
	 *
	 * @param email the non-null email
	 * @return true iff. an {@link UndeliverableMail} entity exists with the given email
	 */
	public boolean isBlocked(String email)
	{
		if(email == null) return false;
		
		final Set<UndeliverableMail> undeliverableMails = undeliverableMailRepository.findByRecipientEmail(email);
		return undeliverableMails.stream().anyMatch(um -> um.getIncidents() > um.getUndeliverableMailReason().getMaxIncidents());
	}
	
	@Transactional
	public void remove(User user)
	{
		Objects.requireNonNull(user);
		
		final Set<UndeliverableMail> undeliverableMails = undeliverableMailRepository.findByRecipientEmail(user.getEmail());
		undeliverableMailRepository.delete(undeliverableMails);
	}
	
	@Transactional
	public void remove(UndeliverableMail undeliverableMail)
	{
		undeliverableMailRepository.delete(undeliverableMail);
	}
	
	public LazyBeanFilteredItemsHandler<UndeliverableMail> createUndeliverableMailHandler()
	{
		return undeliverableMailRepository::findAll;
	}
	
	@CheckedTransactional
	public void addUserToBlacklist(User user) throws ValidationException
	{
		Objects.requireNonNull(user);
		
		final UndeliverableMail undeliverableMail = new UndeliverableMail();
		undeliverableMail.setRecipientEmail(user.getEmail());
		undeliverableMail.setUndeliverableMailReason(UndeliverableMailReason.MANUAL);
		undeliverableMail.setIncidents(1);
		
		save(undeliverableMail);
	}


	@CheckedTransactional
	public void deleteFromBlackList(User user) throws ValidationException
	{
		Objects.requireNonNull(user);

		final Set<UndeliverableMail> undeliverableMails = undeliverableMailRepository.findByRecipientEmail(user.getEmail());
		undeliverableMailRepository.delete(undeliverableMails);
	}

	@CheckedTransactional
	public void save(UndeliverableMail undeliverableMail) throws ValidationException
	{
		try
		{
			undeliverableMailRepository.saveAndFlush(undeliverableMail);
		}
		catch (DataIntegrityViolationException ex)
		{
			throw new UniqueValidationException("The combination of reason and mail already exists!");
		}
	}
	
	@Transactional
	public long addToBlacklist(Collection<String> emails, UndeliverableMailReason undeliverableMailReason)
	{
		return emails.stream().filter(e -> addToBlacklist(e, undeliverableMailReason)).count();
	}
	
	@Transactional
	public boolean addToBlacklist(String email, UndeliverableMailReason undeliverableMailReason)
	{
		if(!StringUtils.isValidEmail(email))
		{
			LOG.warn("{} not added to blacklist, because it is not a valid email address", email);
			return false;
		}
		
		UndeliverableMail undeliverableMail = undeliverableMailRepository.findByRecipientEmailAndUndeliverableMailReason(email, undeliverableMailReason);
		
		if (undeliverableMail == null)
		{
			undeliverableMail = new UndeliverableMail();
			undeliverableMail.setRecipientEmail(email);
			undeliverableMail.setUndeliverableMailReason(undeliverableMailReason);
		}
		else
		{
			undeliverableMail.setIncidents(undeliverableMail.getIncidents() + 1);
		}
		
		undeliverableMailRepository.save(undeliverableMail);
		
		return true;
	}

	public void removeSoftBounceMail() {
		LocalDateTime time = LocalDateTime.now().minusDays(28);
		undeliverableMailRepository.deleteSoftBounceMails(time);
	}
}
