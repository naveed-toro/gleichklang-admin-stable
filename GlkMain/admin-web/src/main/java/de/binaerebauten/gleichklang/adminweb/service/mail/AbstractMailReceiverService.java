package de.binaerebauten.gleichklang.adminweb.service.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Abstract service to receive and process incoming emails.
 */
public abstract class AbstractMailReceiverService
{
	private final static Logger LOG = LoggerFactory.getLogger(AbstractMailReceiverService.class);

	private final URLName mailHostUrl;

	private final String mailUser;

	private final String mailPassword;

	protected AbstractMailReceiverService(URLName mailHostUrl, String mailUser, String mailPassword)
	{
		this.mailHostUrl = Objects.requireNonNull(mailHostUrl, "mailHostUrl == null");
		this.mailUser = Objects.requireNonNull(mailUser, "mailUser == null");
		this.mailPassword = Objects.requireNonNull(mailPassword, "mailPassword == null");
	}

	/**
	 * Retrieves the messages to process via {@link #getMessagesToProcess(Folder)},
	 * processes each message {@link #processMessage(MimeMessage)} and deletes the messages.
	 *
	 * @throws MessagingException
	 */
	public void processMessages() throws MessagingException
	{
		Session session = createSession();
		Store store = null;

		try
		{
			store = session.getStore();
			store.connect(mailHostUrl.getHost(), mailUser, mailPassword);
			Folder inboxFolder = store.getFolder("INBOX");
			inboxFolder.open(Folder.READ_WRITE);

			final List<MimeMessage> messagesToProcess = getMessagesToProcess(inboxFolder);
			messagesToProcess.forEach(this::processMessage);

			inboxFolder.expunge();
		}
		finally
		{
			if (store != null)
			{
				store.close();
			}
		}
	}

	/**
	 * Retrieves the message to process. This implementation
	 * retrieves all messages from the given folder, but subclasses
	 * can override this method to filter the message to process.
	 *
	 * @param folder the folder
	 * @return list of mime messages to process
	 * @throws MessagingException
	 */
	protected List<MimeMessage> getMessagesToProcess(Folder folder)
			throws MessagingException
	{
		int messageCount = folder.getMessageCount();
		LOG.debug("Folder {} message count: {}", folder.getName(), messageCount);

		return Arrays.stream(folder.getMessages())
				.filter(m -> m instanceof MimeMessage) // we assume that this is always true
				.map(m -> (MimeMessage) m)
				.collect(Collectors.toList());
	}

	/**
	 * This abstract method must be overriden by subclasses to process the
	 * given message.
	 *
	 * @param message the non-null message to process
	 */
	protected abstract void processMessage(MimeMessage message);

	private Session createSession()
	{
		Properties properties = new Properties();
		properties.put("mail.store.protocol", mailHostUrl.getProtocol());
		properties.put("mail.imaps.ssl.trust", "*");
		properties.put("mail.imaps.ssl.protocols", "TLSv1.2");
		properties.put("mail.imaps.starttls.enable", "true");

		return Session.getInstance(properties);
	}
}
