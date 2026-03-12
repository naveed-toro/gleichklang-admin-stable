package de.binaerebauten.gleichklang.core.service.mail;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.mail.*;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.repository.mail.MailQueueEntryRepository;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.repository.UserNewsRepository;
import de.binaerebauten.gleichklang.core.service.AdminService;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import de.binaerebauten.gleichklang.core.utils.MailReminder;
import de.binaerebauten.gleichklang.core.utils.MailReminder.TemplateConfiguration;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static de.binaerebauten.gleichklang.core.model.message.MessageMailTemplate.SIGNATURE;

/**
 * This service is responsible for sending emails stored as {@link
 * de.binaerebauten.gleichklang.core.model.mail.MailQueueEntry} in the
 * database.
 * <p>
 * This class is located in the core module so that it's easier to test it
 * during development. It later should only run as part of the admin-web
 * module!
 */
@Service
public class MailSendService
{
	private static final int PAGE_SIZE = 10;
	private static final Logger LOG = LoggerFactory.getLogger(MailSendService.class);

	private final RelationshipRepository relationshipRepository;
	private final MailQueueEntryRepository mailQueueEntryRepository;
	private final UserNewsRepository userNewsRepository;
	private final MailQueueService mailQueueService;
	private final UserMailTemplateService userMailTemplateService;
	private final JavaMailSender javaMailSender;
	private final MailReminder<MailQueueEntry> mailReminder;
	private final UndeliverableMailService undeliverableMailService;
	private final TransactionTemplate transactionTemplate;
	private final String sender;
	private final int maxAttempts;
	private final boolean whiteListEnabled;
	private final boolean transformationEnabled;
	private final String transformationGoal;
	private final int[] minutesDelay;

	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	private UserService userService;

	@Autowired
	private AdminService adminService;


	@Autowired
	private TemplateEngineService templateEngineService;

	@Autowired
	public MailSendService(RelationshipRepository relationshipRepository,
						   MailQueueEntryRepository mailQueueEntryRepository,
						   UserNewsRepository userNewsRepository,
						   MailQueueService mailQueueService,
						   UserMailTemplateService userMailTemplateService,
						   UndeliverableMailService undeliverableMailService,
						   JavaMailSender javaMailSender,
						   MailReminder<MailQueueEntry> mailReminder,
						   TransactionTemplate transactionTemplate,
						   Environment environment)
	{
		this.relationshipRepository = relationshipRepository;
		this.mailQueueEntryRepository = mailQueueEntryRepository;
		this.userNewsRepository = userNewsRepository;

		this.mailQueueService = mailQueueService;
		this.userMailTemplateService = userMailTemplateService;
		this.undeliverableMailService = undeliverableMailService;

		this.javaMailSender = javaMailSender;
		this.mailReminder = mailReminder;

		this.transactionTemplate = transactionTemplate;

		this.sender = environment.getProperty("email.sender");
		this.maxAttempts = environment.getProperty("email.max_attempts", int.class);
		this.whiteListEnabled = environment.getProperty("email.white_list.enabled", boolean.class);
		this.transformationGoal = environment.getProperty("email.transformation.goal");
		this.transformationEnabled = !Strings.isNullOrEmpty(transformationGoal);

		minutesDelay = new int[] { 1, 1, 5, 10, 60, 1440 };
	}

	/**
	 * This scheduled method regularly retrieves all pending {@link
	 * MailQueueEntry}s and sets a status pending if necessary.
	 */
	@Scheduled(fixedRate = 180000)
	public void processScheduledMailQueue()
	{
		final Pageable pageable = new PageRequest(0, PAGE_SIZE);
		boolean hasContent;
		try {
		do
		{
			hasContent = transactionTemplate.execute(status ->
			{
				final Page<MailQueueEntry> page = mailQueueEntryRepository.findScheduledMailQueueEntries(pageable);
				page.forEach((mailQueueEntry) -> mailQueueEntry.setDeliveryStatus(MailDeliveryStatus.PENDING));

				return page.hasContent();
			});
		} while (hasContent);
		}catch (Exception e) {
			LOG.error("processScheduledMailQueue", e);
		}
	}

	/**
	 * This scheduled method regularly retrieves all scheduled {@link
	 * MailQueueEntry}s and directly sends an email for each found entry.
	 */
	@Scheduled(fixedRate = 180000)
	public void processPendingMailQueue()
	{
		final Pageable pageable = new PageRequest(0, PAGE_SIZE);
		boolean hasContent;
		try {
			do
			{
				final Page<MailQueueEntry> page = mailQueueEntryRepository.findPendingMailQueueEntries(pageable);
				page.forEach(this::processPendingMailQueueEntry);
	
				hasContent = page.hasContent();
			} while (hasContent);
		}catch (Exception e) {
			LOG.error("processPendingMailQueue", e);
		}
	}

	@Scheduled(fixedRate = 300000)
	public void processNewMessageMail()
	{
 		final Pageable pageable = new PageRequest(0, PAGE_SIZE);
		LocalDateTime fiveMinutesMinues = LocalDateTime.now().minusMinutes(5);
		boolean hasContent;
		try {
			do
			{
				final Page<MailQueueEntry> page = mailQueueEntryRepository.findByMailTemplate(fiveMinutesMinues,pageable);
				page.forEach(this::processPendingMailQueueEntry);
				hasContent = page.hasContent();
			} while (hasContent);
		}catch (Exception e) {
			LOG.error("processPendingMailQueue", e);
		}
	}

	/**
	 * This scheduled method regularly removed sent and descheduled {@link
	 * MailQueueEntry}s.
	 */
	@Scheduled(fixedRate = 360000)
	public void cleanupMailQueue()
	{
		final Pageable pageable = new PageRequest(0, PAGE_SIZE);
		boolean hasContent;
		try {
			do
			{
				hasContent = transactionTemplate.execute(status ->
				{
					final Page<MailQueueEntry> page = mailQueueEntryRepository.findMailQueueEntriesForCleanUp(pageable);
					mailQueueEntryRepository.delete(page);
	
					return page.hasContent();
				});
			} while (hasContent);
		}catch (Exception e) {
			LOG.error("cleanupMailQueue", e);
		}
	}

//	@Scheduled(fixedRate = 600000)
//	public void cleanUpFootPrints(){
//		final Pageable pageable  = new PageRequest(0, PAGE_SIZE);
//		boolean hasContent;
//		do {
//			hasContent = transactionTemplate.execute(status ->
//			{
//				final Page<MailQueueEntry> page = mailQueueEntryRepository.findMailQueueEntriesForCleanUpFootPrints(pageable);
//				mailQueueEntryRepository.delete(page);
//				return page.hasContent();
//			});
//		}
//			while (hasContent);
//	}

	/**
	 * Sends an email to the system.
	 *
	 * @param mailTemplateInstance
	 */
	public void sendSystemEmail(MailTemplateInstance mailTemplateInstance)
	{
		sendEmail(sender, mailTemplateInstance);
	}

	/**
	 * Directly sends an email to the given recipient.
	 * An email will not be sent if the user disabled it in his settings.
	 *
	 * @param recipient            the non-null recipient of the email
	 * @param mailTemplateInstance the non-null mail template instance
	 * @throws MailException
	 */
	public MailDeliveryStatus sendEmail(User recipient, MailTemplateInstance mailTemplateInstance) throws MailException
	{
		Objects.requireNonNull(recipient, "recipient == null");

		final MailTemplate mailTemplate = mailTemplateInstance.getMailTemplate();
		final String recipientEmail = getRecipientEmail(recipient, mailTemplate);

		if (!isAllowedDueConfirmation(recipient, mailTemplate) || !hasAllowedMemberStatus(recipient, mailTemplate) ||
				recipientEmail == null || recipient.isBlocked())
		{
			LOG.info("User {} doesn't confirm his E-Mail or is cancelled/deleted. E-Mail will not be sent.", recipientEmail);
			return MailDeliveryStatus.DISCARDED;
		}

		if (isEmailNotificationsDisabled(recipient.getUserSettings(), mailTemplate))
		{
			LOG.info("User {} disabled E-Mail notifications for {}.", recipientEmail, mailTemplate);
			return MailDeliveryStatus.DISCARDED;
		}

		sendEmail(recipientEmail, mailTemplateInstance);
		return MailDeliveryStatus.SENT;
	}

	public MailDeliveryStatus sendEmailUser(User recipient, MailTemplateInstance mailTemplateInstance, User sourceuser) throws MailException
	{
		Objects.requireNonNull(recipient, "recipient == null");

		final MailTemplate mailTemplate = mailTemplateInstance.getMailTemplate();
		final String recipientEmail = getRecipientEmail(recipient, mailTemplate);

		if (!isAllowedDueConfirmation(recipient, mailTemplate) || !hasAllowedMemberStatus(recipient, mailTemplate) ||
				recipientEmail == null || recipient.isBlocked())
		{
			LOG.info("User {} doesn't confirm his E-Mail or is cancelled/deleted. E-Mail will not be sent.", recipientEmail);
			return MailDeliveryStatus.DISCARDED;
		}

		if (isEmailNotificationsDisabled(recipient.getUserSettings(), mailTemplate))
		{
			LOG.info("User {} disabled E-Mail notifications for {}.", recipientEmail, mailTemplate);
			return MailDeliveryStatus.DISCARDED;
		}

		sendEmail(recipientEmail, mailTemplateInstance,sourceuser);
		return MailDeliveryStatus.SENT;
	}

	public MailDeliveryStatus sendEmail(User recipient, MailTemplateInstance mailTemplateInstance, List<MessageUploadFile> messageUploadFiles) throws MailException
	{
		Objects.requireNonNull(recipient, "recipient == null");

		final MailTemplate mailTemplate = mailTemplateInstance.getMailTemplate();
		final String recipientEmail = getRecipientEmail(recipient, mailTemplate);

		if (!isAllowedDueConfirmation(recipient, mailTemplate) || !hasAllowedMemberStatus(recipient, mailTemplate) ||
				recipientEmail == null || recipient.isBlocked())
		{
			LOG.info("User {} doesn't confirm his E-Mail or is cancelled/deleted. E-Mail will not be sent.", recipientEmail);
			return MailDeliveryStatus.DISCARDED;
		}

		if (isEmailNotificationsDisabled(recipient.getUserSettings(), mailTemplate))
		{
			LOG.info("User {} disabled E-Mail notifications for {}.", recipientEmail, mailTemplate);
			return MailDeliveryStatus.DISCARDED;
		}

		sendEmailWithAttachements(recipientEmail, mailTemplateInstance, messageUploadFiles);
		return MailDeliveryStatus.SENT;
	}

	private String getRecipientEmail(User recipient, MailTemplate mailTemplate)
	{
//		if (mailTemplate == UserMailTemplate.MAIL_CHANGE_VERIFICATION)
//		{
//			return recipient.getNewEmail();
//		}

		return recipient.getEmail();
	}

	private boolean isAllowedDueConfirmation(User recipient, MailTemplate mailTemplate)
	{
		return recipient.isEmailConfirmed() ||
				mailTemplate == UserMailTemplate.PASSWORD_RESET_NEW ||
				mailTemplate == UserMailTemplate.REGISTRATION_USER ||
				mailTemplate == UserMailTemplate.SOCIAL_PP_USER ||
				mailTemplate == UserMailTemplate.MAIL_CHANGE_VERIFICATION;
	}

	private boolean hasAllowedMemberStatus(User recipient, MailTemplate mailTemplate)
	{
		return mailTemplate==UserMailTemplate.CANCELED_USER_MAIL || !MemberStatus.CANCELED.equals(recipient.getMemberStatus()) && !MemberStatus.DELETED.equals(recipient.getMemberStatus())
				&& !MemberStatus.ADMIN_DELETED.equals(recipient.getMemberStatus()) && !MemberStatus.ADMIN_CANCELED.equals(recipient.getMemberStatus()) ||
				mailTemplate == UserMailTemplate.NEWS_FROM_GLEICHKLANG ||
				mailTemplate == UserMailTemplate.CB_REFUND_FIRST ||
				mailTemplate == UserMailTemplate.CB_REFUND_NEXT ||
				mailTemplate == UserMailTemplate.CB_REVOCATION_FIRST ||
				mailTemplate == UserMailTemplate.CB_REVOCATION_REMINDER ||
				mailTemplate == UserMailTemplate.CB_REVOCATION_RENEWAL_FIRST ||
				mailTemplate == UserMailTemplate.PASSWORD_RESET ||
				mailTemplate == UserMailTemplate.PASSWORD_RESET_NEW ||
				mailTemplate == UserMailTemplate.USER_DELETED ||
				mailTemplate == UserMailTemplate.USER_DELETED_WITH_SATISFACTION;
	}

	/**
	 * Directly sends an email to the given recipient.
	 *
	 * @param recipient            the non-null recipient of the email
	 * @param mailTemplateInstance the non-null mail template instance
	 * @throws MailException
	 */
	public void sendEmail(Admin recipient, MailTemplateInstance mailTemplateInstance) throws MailException
	{
		Objects.requireNonNull(recipient, "recipient == null");

		sendEmail(recipient.getEmail(), mailTemplateInstance);
	}

	private boolean isMailWhiteListed(String mailAddress)
	{
		final String googleDomain = "@gmail.com";
		final String[] allowedDomains = new String[] { "_neu@gleichklang-mail.de", "@binaere-bauten.de", "@fbltipp.de" };
		final String[] allowedGoogleAliases = new String[] { "gkbb.testing", "binbau.testing" };
		final String[] allowedMails = new String[]
				{
						"martin.lindhorst@gleichklang.de",
						"gerald.lindhorst@gleichklang.de",
						"developer@gleichklang.de",
						"gebauer@gleichklang.de",
						"intern@gleichklang.de",
						"softwaretest-01@gleichklang.de",
						"softwaretest-02@gleichklang.de",
						"softwaretest-03@gleichklang.de",
						"kerstin.weigt@gleichklang.de",
                        "testingpurpose067@gmail.com",
						"media@lindhor.st",
						"invalidemailtest007@gmail.com",
						"prabudh@modulobytes.com",
						"nitesh@mailinator.com",
						"nitesh@modulobytes.com",
						"lokesh@modulobytes.com",
						"nj.hindoli@gmail.com"
				};
		final String lowerCaseMail = mailAddress.toLowerCase();

		for (String allowedDomain : allowedDomains)
		{
			if (lowerCaseMail.endsWith(allowedDomain)) return true;
		}

		for (String allowedGoogleAlias : allowedGoogleAliases)
		{
			if (lowerCaseMail.equals(allowedGoogleAlias + googleDomain))
				return true;
			if (lowerCaseMail.startsWith(allowedGoogleAlias + "+") && lowerCaseMail.endsWith(googleDomain))
				return true;
		}

		return Arrays.asList(allowedMails).contains(lowerCaseMail);
	}

	private String transformMail(String recipientEmail)
	{
		if (Strings.isNullOrEmpty(recipientEmail)) return recipientEmail;
		return recipientEmail.replace('@', '_') + transformationGoal;
	}

	private boolean isAllowedRegardlessBlacklist(MailTemplate mailTemplate)
	{
		return mailTemplate != null && (mailTemplate == UserMailTemplate.PASSWORD_RESET ||
				mailTemplate == UserMailTemplate.ADMITTANCE_1 ||
				mailTemplate == UserMailTemplate.ADMITTANCE_2 ||
				mailTemplate == UserMailTemplate.REGISTRATION_USER);
	}

	/**
	 * Directly sends an email to the given recipient email address.
	 *
	 * @param recipientEmail       the non-null recipient email address
	 * @param mailTemplateInstance the non-null mail template instance
	 * @throws MailException
	 */
	private void sendEmail(String recipientEmail, final MailTemplateInstance mailTemplateInstance) throws MailException
	{
		Objects.requireNonNull(recipientEmail, "recipientEmail == null");
		Objects.requireNonNull(mailTemplateInstance, "mailTemplateInstance == null");

//		if (!isAllowedRegardlessBlacklist(mailTemplateInstance.getMailTemplate()) && undeliverableMailService.isBlocked(recipientEmail))
//			return;

		if (whiteListEnabled && !transformationEnabled && !isMailWhiteListed(recipientEmail))
			return;

		final String finalRecipientMail = transformationEnabled && !isMailWhiteListed(recipientEmail) ? transformMail(recipientEmail) : recipientEmail;

		final MimeMessagePreparator preparator = mimeMessage ->
		{
			mimeMessage.setSubject(mailTemplateInstance.getSubject());
			mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(finalRecipientMail));
			mimeMessage.setFrom(new InternetAddress(sender));

			if(mailTemplateInstance.getMailTemplate().equals(UserMailTemplate.ADMIN_MESSAGE) ||
			mailTemplateInstance.getMailTemplate().equals(UserMailTemplate.CANCELED_USER_MAIL)){

				String contentMail=getEmailContent(mailTemplateInstance);
				final String content = contentMail.replaceAll("\n", "");
				mimeMessage.setText(content, StandardCharsets.ISO_8859_1.name(), "html");
			}

			else{
				final String content = mailTemplateInstance.getContent().replaceAll("\n", "");
				mimeMessage.setText(content, StandardCharsets.ISO_8859_1.name(), "html");
			}

		};

		LOG.trace("Sending email to {}", finalRecipientMail);
		try {
			javaMailSender.send(preparator);
		}
		catch (Exception ex){
			Log.info("Mail can not be sent to this address" +ex);
		}
	}

	private String getEmailContent(MailTemplateInstance mailTemplateInstance) {
		String contentMail = mailTemplateInstance.getContent();

		if (mailTemplateInstance.getMailTemplate().equals(UserMailTemplate.ADMIN_MESSAGE) && contentMail.contains("Ihre ursprüngliche Anfrage lautete") || contentMail.contains("Your original message was as follows")) {

			String mesageBody[] = null;
			if (contentMail.contains("Ihre ursprüngliche Anfrage lautete")) {
				mesageBody = contentMail.split("Ihre ursprüngliche Anfrage lautete:");
			}
			if (contentMail.contains("Your original message was as follows")) {
				mesageBody = contentMail.split("Your original message was as follows:");
			}
			List<String> mesageBodyList = new LinkedList<>(Arrays.asList(mesageBody));

			if (mesageBodyList.size() == 2) {
				String signature = "<span style=\"margin-bottom:1em;color:#777777;width:100%\">" + mesageBodyList.get(0).split("<span style=\"margin-bottom:1em;color:#777777;width:100%\">")[1];
				String messagetext = mesageBodyList.get(0).split("<span style=\"margin-bottom:1em;color:#777777;width:100%\">")[0];
				String history = mesageBodyList.get(1);
				contentMail = messagetext +"<br/>"+"Ihre ursprüngliche Anfrage lautete:"+"<br/>"+ history + "<br/>"+signature;
			}
			contentMail=contentMail.replace("${signature}","");
		}
		else if(mailTemplateInstance.getMailTemplate().equals(UserMailTemplate.ADMIN_FIRST_MESSAGE))
		{
			contentMail = contentMail.replace("${signature}","<br/>"+ templateEngineService.getContentForTemplate(SIGNATURE, I18NEntity.Language.DE));
			contentMail = contentMail.replace("${admin}","<br/>"+ adminService.getCurrentUser().getAlias());

		}
		else if(mailTemplateInstance.getMailTemplate().equals(UserMailTemplate.CANCELED_USER_MAIL))
		{
			contentMail = contentMail.replace("Ihre ursprüngliche Anfrage lautete","");
			contentMail = contentMail.replace("${signature}","");
			contentMail = contentMail.replace("${oldMessage}","");
		}
		return contentMail;
	}

	private void sendEmail(String recipientEmail, final MailTemplateInstance mailTemplateInstance, User sourceuser) throws MailException
	{
		Objects.requireNonNull(recipientEmail, "recipientEmail == null");
		Objects.requireNonNull(mailTemplateInstance, "mailTemplateInstance == null");

//		if (!isAllowedRegardlessBlacklist(mailTemplateInstance.getMailTemplate()) && undeliverableMailService.isBlocked(recipientEmail))
//			return;

		if (whiteListEnabled && !transformationEnabled && !isMailWhiteListed(recipientEmail))
			return;

		final String finalRecipientMail = transformationEnabled && !isMailWhiteListed(recipientEmail) ? transformMail(recipientEmail) : recipientEmail;

		if(mailTemplateInstance.getMailTemplate().equals(UserMailTemplate.NEW_BOXNUMBER_CONTACT)) {
			mailQueueService.enqueue(userService.findByEmail(recipientEmail), UserMailTemplate.NEW_BOXNUMBER_CONTACT,sourceuser);
			return;
		}

		final MimeMessagePreparator preparator = mimeMessage ->
		{
			mimeMessage.setSubject(mailTemplateInstance.getSubject());
			mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(finalRecipientMail));
			mimeMessage.setFrom(new InternetAddress(sender));

			final String content = mailTemplateInstance.getContent().replaceAll("\n", "");
			mimeMessage.setText(content, StandardCharsets.ISO_8859_1.name(), "html");
		};

		LOG.trace("Sending email to {}", finalRecipientMail);
		try {
			javaMailSender.send(preparator);
		}
		catch (Exception ex){
			Log.info("Mail can not be sent to this address" +ex);
		}
	}

	/**
	 *Directly sends an email to the given recipient email address with attachments
	 *
	 * @param recipientEmail
	 * @param mailTemplateInstance
	 * @param messageUploadFiles
	 * @throws MailException
	 */
	private void sendEmailWithAttachements(String recipientEmail, final MailTemplateInstance mailTemplateInstance, List<MessageUploadFile> messageUploadFiles) throws MailException
	{
		Objects.requireNonNull(recipientEmail, "recipientEmail == null");
		Objects.requireNonNull(mailTemplateInstance, "mailTemplateInstance == null");

//		if (!isAllowedRegardlessBlacklist(mailTemplateInstance.getMailTemplate()) && undeliverableMailService.isBlocked(recipientEmail))
//			return;

		if (whiteListEnabled && !transformationEnabled && !isMailWhiteListed(recipientEmail))
			return;

		final String finalRecipientMail = transformationEnabled && !isMailWhiteListed(recipientEmail) ? transformMail(recipientEmail) : recipientEmail;

		final MimeMessagePreparator preparator = mimeMessage ->
		{
			mimeMessage.setSubject(mailTemplateInstance.getSubject());
			mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(finalRecipientMail));
			mimeMessage.setFrom(new InternetAddress(sender));

			String content="";

			if(mailTemplateInstance.getMailTemplate().equals(UserMailTemplate.ADMIN_MESSAGE) ||
					(mailTemplateInstance.getMailTemplate().equals(UserMailTemplate.ADMIN_FIRST_MESSAGE))||
					mailTemplateInstance.getMailTemplate().equals(UserMailTemplate.CANCELED_USER_MAIL)) {

				content = getEmailContent(mailTemplateInstance).replaceAll("\n", "");
			}
			else
			{
				content = mailTemplateInstance.getContent().replaceAll("\n", "");
			}
			MimeBodyPart messageBodyPart1 = new MimeBodyPart();

			messageBodyPart1.setText(content, StandardCharsets.ISO_8859_1.name(), "html");

			MimeBodyPart messageBodyPart;
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart1);

			for(MessageUploadFile messageUploadFile : messageUploadFiles){
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(messageUploadFile.getPath().toString());
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(messageUploadFile.getMessageAttachment().getFile().getName());
				multipart.addBodyPart(messageBodyPart);
			}


			mimeMessage.setContent(multipart);
		};

		LOG.trace("Sending email to {}", finalRecipientMail);
		try {
			javaMailSender.send(preparator);
		}
		catch (Exception ex){
			Log.info("Mail can not be sent to this address" +ex);
		}
	}


	private boolean isEmailNotificationsDisabled(UserSettings userSettings, MailTemplate mailTemplate)
	{
		if (userSettings == null) return false;

		return mailTemplate == UserMailTemplate.NEW_MATCH && userSettings.isDisableRecommendationNotifications() ||
				mailTemplate == UserMailTemplate.NEW_BOXNUMBER_CONTACT && userSettings.isDisableCipherMessageNotifications() ||
				mailTemplate == UserMailTemplate.NEW_MATCH_POSITIVE && userSettings.isDisablePositiveRankingNotifications() ||
				mailTemplate == UserMailTemplate.NEWS_FROM_GLEICHKLANG && userSettings.isDisableNewsNotifications() ||
				mailTemplate == UserMailTemplate.NEW_FOOTPRINT && userSettings.isDisableFootprintNotifications() ||
				mailTemplate == UserMailTemplate.MISSING_PAYMENT_REMINDER && !userSettings.isEnableMarketingNotifications() ||
				mailTemplate == UserMailTemplate.MISSING_QUESTIONAIRE_REMINDER && !userSettings.isEnableMarketingNotifications();
	}

	private void processPendingMailQueueEntry(MailQueueEntry mailQueueEntry)
	{
		LOG.info("in processPendingMailQueueEntry===", mailQueueEntry.getMailTemplate().getTemplateName());
		transactionTemplate.execute(t ->
		{
			if (mailQueueEntry.getAttempts() < maxAttempts)
			{
				final UserMailTemplate mailTemplate = mailQueueEntry.getMailTemplate();
				final TemplateConfiguration<?> templateConfiguration = mailReminder.getTemplateConfiguration(mailTemplate);
				final int maxReminderCount = Objects.nonNull(templateConfiguration) ? templateConfiguration.getMaxReminderCount() : -1;
				if (mailQueueEntry.getReminderCount() < maxReminderCount || maxReminderCount < 0)
				{
					try
					{
						final MailDeliveryStatus mailDeliveryStatus = performSendMailQueueEntry(mailQueueEntry, mailTemplate, templateConfiguration);

						Preconditions.checkArgument(mailDeliveryStatus != MailDeliveryStatus.PENDING);
						mailQueueEntry.setDeliveryStatus(mailDeliveryStatus);
					}
					catch (Exception ex)
					{
						LOG.error("Error sending mail", ex);

						final int delay;
						if (mailQueueEntry.getAttempts() < minutesDelay.length - 1)
						{
							delay = minutesDelay[mailQueueEntry.getAttempts()];
						}
						else
						{
							delay = minutesDelay.length > 0 ? minutesDelay[minutesDelay.length - 1] : 1;
						}

						mailQueueEntry.setNextRetryDate(LocalDateTime.now().plus(delay, ChronoUnit.MINUTES));
						mailQueueEntry.incAttempts();
					}
				}
				else
				{
					mailQueueEntry.setDeliveryStatus(MailDeliveryStatus.UNDELIVERABLE);
					mailQueueEntry.setUndeliverableMailReason(UndeliverableMailReason.UNKNOWN);
				}
			}
			else
			{
				mailQueueEntry.setDeliveryStatus(MailDeliveryStatus.UNDELIVERABLE);
				mailQueueEntry.setUndeliverableMailReason(UndeliverableMailReason.UNKNOWN);
			}

			// E-Mail queue entry should be saved because the object is detached
			mailQueueEntryRepository.save(mailQueueEntry);

			return null;
		});
	}

	private MailDeliveryStatus performSendMailQueueEntry(MailQueueEntry mailQueueEntry, UserMailTemplate mailTemplate, TemplateConfiguration<?> templateConfiguration)
	{

		if(mailTemplate.equals(UserMailTemplate.NEW_BOXNUMBER_CONTACT)) {
			Long recipientId = ((UserMailQueueEntry) mailQueueEntry).getRecipientId();
			Long senderId = ((UserMailQueueEntry) mailQueueEntry).getSenderId();

			User recipientUser = userService.findById(recipientId);
			User senderUser = userService.findById(senderId);

			if (recipientUser.isBlocked() || senderUser.isBlocked()) {
				return MailDeliveryStatus.DISCARDED;
			}
			else{
				final Relationship relationship = relationshipRepository.findRelationshipBySourceUserIdAndTargetUserId(senderId, recipientId);
				final MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(mailTemplate,senderUser,recipientUser, relationship.getMainCategory());
			return sendEmail(recipientUser, mailTemplateInstance);
			}
		}

		if (mailQueueEntry instanceof UnregisteredUserMailQueueEntry)
		{
			LOG.info("Sending email with template {} with unregistered recipient", mailTemplate);

			final MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(mailTemplate);
			String recipientEmail = ((UnregisteredUserMailQueueEntry) mailQueueEntry).getRecipientEmail();
			if (recipientEmail == null)
			{
				LOG.info("User's E-Mail is empty. E-Mail will not be sent.");
				return MailDeliveryStatus.DISCARDED;
			}

			//((UserMailQueueEntry) mailQueueEntry).getSenderId();
			sendEmail(recipientEmail, mailTemplateInstance);
			return MailDeliveryStatus.SENT;
		}
		else
		{
			final UserMailQueueEntry userMailQueueEntry = (UserMailQueueEntry) mailQueueEntry;
			final MailTemplateInstance mailTemplateInstance;

			final User recipient = userMailQueueEntry.getRecipient();
			Objects.requireNonNull(recipient);
			if (recipient.getEmail() == null)
			{
				LOG.info("User's E-Mail is empty. E-Mail will not be sent.");
				return MailDeliveryStatus.DISCARDED;
			}

			switch (mailTemplate)
			{
				case RENEWAL_DISABLED_REMINDER:
					mailTemplateInstance = createRenewalReminderMailTemplateInstance(userMailQueueEntry, mailTemplate);
					break;
				case NEW_MATCH:
					mailTemplateInstance = createNewMatchMailTemplateInstance(userMailQueueEntry);
					break;
				// These mails are directly sent
//				case NEW_FOOTPRINT:
//					mailTemplateInstance = createNewFootprintMailTemplateInstance(userMailQueueEntry);
//					break;
				case NEWS_FROM_GLEICHKLANG:
					mailTemplateInstance = createNewsFromGleichklangMailTemplateInstance(userMailQueueEntry);
					break;
				default:
					LOG.info("in default=="+mailTemplate.getTemplateName()+" receipent=="+recipient);
					mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(mailTemplate, recipient);
			}
			if (mailTemplateInstance == null)
			{
				return MailDeliveryStatus.DISCARDED;
			}

			if (Objects.isNull(templateConfiguration))
			{
				return sendEmail(recipient, mailTemplateInstance);
			}
			else
			{
				return sendConditionalEmail(userMailQueueEntry, mailTemplateInstance, templateConfiguration);
			}
		}
	}

	/**
	 * Create a NEWS_FROMGLEICHKLANG E-Mail.
	 *
	 * @param mailQueueEntry
	 */
	private MailTemplateInstance createNewsFromGleichklangMailTemplateInstance(UserMailQueueEntry mailQueueEntry)
	{
		final User recipient = mailQueueEntry.getRecipient();
		final List<UserNews> userNews = userNewsRepository.findNewsToSendViaEmail(recipient);

		if (!userNews.isEmpty())
		{
			userNews.forEach(u -> u.setNotified(true));
			userNewsRepository.save(userNews);

			return userMailTemplateService.createMailTemplateInstance(UserMailTemplate.NEWS_FROM_GLEICHKLANG, recipient, userNews);
		}
		else
		{
			LOG.info("No news found to send via email for user: {}", recipient.getEmail());
			return null;
		}
	}

	/**
	 * Create a NEW_MATCH E-Mail. New match entries require
	 * setting notification flag in the Recommendation.
	 *
	 * @param mailQueueEntry
	 * @return true, if successful sent
	 */
	private MailTemplateInstance createNewMatchMailTemplateInstance(UserMailQueueEntry mailQueueEntry)
	{
		final User recipient = mailQueueEntry.getRecipient();
		final List<Relationship> relationships = relationshipRepository.findAllNotNotifiedRelationshipsForUser(recipient);

		if (!relationships.isEmpty())
		{
			relationships.forEach(relationship -> relationship.setNotified(true));
			return userMailTemplateService.createMailTemplateInstance(UserMailTemplate.NEW_MATCH, recipient, relationships);
		}
		else
		{
			LOG.info("No new recommendations found for user: {}", recipient.getEmail());
			return null;
		}
	}

	/**
	 * Create a NEW_FOOTPRINT E-Mail. New footprint entries require
	 * setting notification flag in the Recommendation.
	 *
	 * @param mailQueueEntry
	 */
//	private MailTemplateInstance createNewFootprintMailTemplateInstance(UserMailQueueEntry mailQueueEntry)
//
//	{
//		final User recipient = mailQueueEntry.getRecipient();
//		final List<Relationship> relationships = relationshipRepository.findAllNotNotifiedNewFootprintsForUser(recipient);
//		Stream<Relationship> streamRelationsips = relationships.stream();
//		Stream<Relationship> filteredUsers = streamRelationsips.filter(str -> str.getSourceUser().isBlocked()==false);
//		if (!relationships.isEmpty())
//		{
//			filteredUsers.forEach(relationship -> relationship.setFootprintNotified(true));
//			return userMailTemplateService.createMailTemplateInstance(UserMailTemplate.NEW_FOOTPRINT, recipient, relationships);
//		}
//		else
//		{
//			LOG.info("No new footprints found for user: {}", recipient.getEmail());
//			return null;
//		}
//	}

	/**
	 * Create a new renewal reminder E-Mail.
	 *
	 * @param mailQueueEntry
	 * @param mailTemplate
	 */
	private MailTemplateInstance createRenewalReminderMailTemplateInstance(UserMailQueueEntry mailQueueEntry, UserMailTemplate mailTemplate)
	{
		final User recipient = mailQueueEntry.getRecipient();
		final Optional<Subscription> subscription = subscriptionService.findCurrentSubscription(recipient);

		if (subscription.isPresent())
		{
			return userMailTemplateService.createMailTemplateInstance(mailTemplate, subscription.get());
		}
		else
		{
			LOG.info("No new subscription found for user: {}", recipient.getEmail());
			return null;
		}
	}

	private MailDeliveryStatus sendConditionalEmail(UserMailQueueEntry mailQueueEntry, MailTemplateInstance mailTemplateInstance, TemplateConfiguration conf)
	{
		if (conf.isScheduled(mailQueueEntry))
		{
			final User recipient = mailQueueEntry.getRecipient();

			final UserMailTemplate mailTemplate = mailQueueEntry.getMailTemplate();
			LocalDateTime nextReminderDate = LocalDateTime.now()
					.plus(conf.getDuration())
					.with(conf.getTime());
			MailQueueEntry scheduledMailQueueEntry = mailQueueService.enqueue(recipient, mailTemplate, nextReminderDate);
			scheduledMailQueueEntry.setReminderCount(mailQueueEntry.getReminderCount() + 1);

			return sendEmail(recipient, mailTemplateInstance);
		}
		else
		{
			return MailDeliveryStatus.DESCHEDULED;
		}
	}

	public void sendBlockedMail(long userId) throws MailException
	{
		final MimeMessagePreparator preparator = mimeMessage ->
		{
			mimeMessage.setSubject("User " +userId+ "is blocked");
			mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress("superadmin@gleichklang.de"));
			mimeMessage.setFrom(new InternetAddress(sender));
			mimeMessage.setText("User " +userId+ " added to block list", StandardCharsets.ISO_8859_1.name(), "html");
		};
		javaMailSender.send(preparator);
	}

	public void sendAdminReply(AdminEmail email, String recipient) throws MailException
	{

		MimeMessage message = javaMailSender.createMimeMessage();

		try{
			MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.ISO_8859_1.name());

			helper.setFrom((new InternetAddress(sender)));
			helper.setTo(new InternetAddress(recipient));
			helper.setSubject(email.getSubject());
			helper.setText(email.getText(),true);

			if(email.getAttachments() != null && !email.getAttachments().isEmpty())
			{
				for(File file:email.getAttachments())
				{
					try {
						helper.addAttachment(file.getName(), file);
					}catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}}

			// send mail once all the attachments have been uploaded
			javaMailSender.send(message);
		}catch (Exception e) {
			throw new MailParseException(e);
		}
	}

}