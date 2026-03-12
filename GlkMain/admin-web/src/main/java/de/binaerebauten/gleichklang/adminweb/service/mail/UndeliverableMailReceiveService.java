package de.binaerebauten.gleichklang.adminweb.service.mail;

import de.binaerebauten.gleichklang.core.config.UndeliverableMailConfig;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FromStringTerm;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Receives mails sent from our mailer daemon, analyzes them and updates the
 * {@link de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail} entities.
 *
 * Uses the {@link UndeliverableMailConfig#mailerDaemonPattern()} to filter
 * the messages to process so that all other messages aren't changed.
 */
@Service
public class UndeliverableMailReceiveService extends AbstractMailReceiverService
{
	private final static Logger LOGGER = LoggerFactory.getLogger(UndeliverableMailReceiveService.class);

	private final FromStringTerm mailerDaemonAddress;

	private final UndeliverableMailService undeliverableMailService;

	private final boolean schedulerEnabled;

	@Autowired
	public UndeliverableMailReceiveService(@Value("${email.server}") URLName mailHostUrl,
			@Value("${email.user}") String mailUser, @Value("${email.password}") String mailPassword,
			UndeliverableMailService undeliverableMailService,
			FromStringTerm mailerDaemonAddress, @Value("${email.undeliverable_mail.scheduler_enabled}") boolean schedulerEnabled)
	{
		super(mailHostUrl, mailUser, mailPassword);
		this.mailerDaemonAddress = mailerDaemonAddress;
		this.undeliverableMailService = undeliverableMailService;
		this.schedulerEnabled = schedulerEnabled;
	}

	@Scheduled(fixedRate = 600000)
	public void scheduled()
	{
		if (schedulerEnabled)
		{
			try
			{
				processMessages();
			}
			catch (MessagingException e)
			{
				LOGGER.error("UndeliverableMailReceiveService.scheduled Error ocurred:", e);
			}
		}
	}

	@Override
	protected List<MimeMessage> getMessagesToProcess(Folder folder)
			throws MessagingException
	{
		return Arrays.stream(folder.search(mailerDaemonAddress))
				.filter(m -> m instanceof MimeMessage) // we assume that this is always true
				.map(m -> (MimeMessage) m)
				.collect(Collectors.toList());
	}

	@Override
	protected void processMessage(MimeMessage message)
	{
		undeliverableMailService.processUndeliverableMessage(message, true);
	}

	@Scheduled(fixedRate = 18000000)
	public void removeSoftBouncedMail()
	{
		try
		{
			undeliverableMailService.removeSoftBounceMail();
		}
		catch (Exception e)
		{
			LOGGER.error("UndeliverableMailReceiveService.bounceBack Error ocurred:", e);
		}
	}
}
