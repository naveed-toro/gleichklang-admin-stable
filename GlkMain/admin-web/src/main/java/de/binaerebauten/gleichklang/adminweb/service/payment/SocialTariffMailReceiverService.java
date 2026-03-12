package de.binaerebauten.gleichklang.adminweb.service.payment;

import de.binaerebauten.gleichklang.adminweb.service.mail.AbstractMailReceiverService;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.repository.UserPaymentSettingsRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Objects;
import java.util.Optional;

/**
 * This scheduled service receives incoming emails and responds with the action code
 * for the social tariff.
 */
@Service
public class SocialTariffMailReceiverService extends AbstractMailReceiverService
{
	private final static Logger LOGGER = LoggerFactory.getLogger(SocialTariffMailReceiverService.class);

	private final boolean schedulerEnabled;

	@Autowired
	private MailQueueService mailQueueService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserPaymentSettingsRepository userPaymentSettingsRepository;
	
	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private UndeliverableMailService undeliverableMailService;

	@Autowired
	public SocialTariffMailReceiverService(@Value("${email.gk_social.server}") URLName mailHostUrl,
			@Value("${email.gk_social.user}") String mailUser, @Value("${email.gk_social.password}") String mailPassword,
			@Value("${email.gk_social.scheduler_enabled}") boolean schedulerEnabled)
	{
		super(mailHostUrl, mailUser, mailPassword);
		LOGGER.info("Scheduler enabled = {}", schedulerEnabled);
		this.schedulerEnabled = schedulerEnabled;
	}

	@Scheduled(fixedRate = 120000)
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
				LOGGER.error("Error ocurred:", e);
			}
		}
	}

	@Override
	protected void processMessage(MimeMessage message)
	{
		if (undeliverableMailService.isFromMailerDaemon(message))
		{
			undeliverableMailService.processUndeliverableMessage(message,false);
		}
		else
		{
			processSocialTariffRequest(message);
		}
	}

	private void processSocialTariffRequest(MimeMessage message)
	{
		try
		{
			Objects.requireNonNull(message.getFrom(), "message.getFrom == null");

			if (message.getFrom().length == 1 && message.getFrom()[0] instanceof InternetAddress)
			{
				InternetAddress address = (InternetAddress) message.getFrom()[0];
				String email = address.getAddress();

				User user = userRepository.findByEmail(email);
				if (user != null)
				{
					Optional<AbstractPayment> currentPayment = paymentRepository.findCurrentPayment(user);
					if (currentPayment.isPresent())
					{
						AbstractPayment payment = currentPayment.get();
						if (payment instanceof Prepayment && payment.getState() == PaymentState.PENDING)
						{
							mailQueueService.enqueue(user, UserMailTemplate.SOCIAL_PP_USER);
						}
						else
						{
							Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
							if (paymentSettings.isPresent())
							{
								UserPaymentSettings userPaymentSettings = paymentSettings.get();
								userPaymentSettings.setActionCode(InitialSubscriptionOffer.SOCIAL_CODE);
								userPaymentSettingsRepository.save(userPaymentSettings);
							}

							mailQueueService.enqueue(user, UserMailTemplate.SOCIAL_REGISTERED_USER);
						}
					}
				}
				else
				{
					mailQueueService.enqueue(email, UserMailTemplate.SOCIAL_UNREGISTERED_USER);
				}
			}
			message.setFlag(Flags.Flag.DELETED, true);

			LOGGER.debug(message.getSubject());
		}
		catch (MessagingException e)
		{
			LOGGER.error("Error ocurred:", e);
		}
	}
}
