package de.binaerebauten.gleichklang.core.config;

import de.binaerebauten.gleichklang.core.model.mail.MailQueueEntry;
import de.binaerebauten.gleichklang.core.model.mail.UnregisteredUserMailQueueEntry;
import de.binaerebauten.gleichklang.core.model.mail.UserMailQueueEntry;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.QuestionnaireService;
import de.binaerebauten.gleichklang.core.service.payment.ChargebackReason;
import de.binaerebauten.gleichklang.core.utils.MailReminder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.URLName;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * The spring configuration for sending and receiving of emails.
 */
@Configuration
public class MailConfig
{
	@Autowired
	private PaymentRepository paymentRepository;
	
	@Autowired
	private RelationshipRepository relationshipRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private QuestionnaireService questionnaireService;
	
	@Value("${email.server}")
	private URLName mailHostUrl;

	@Value("${email.user}")
	private String mailUser;

	@Value("${email.password}")
	private String mailPassword;
	
	@Value("${email.short_reminder_interval}")
	private boolean shortReminderInterval;

	@Bean
	public JavaMailSender javaMailSender()
	{
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

		javaMailSender.setHost(mailHostUrl.getHost());
		javaMailSender.setPort(mailHostUrl.getPort());
		javaMailSender.setUsername(mailUser);
		javaMailSender.setPassword(mailPassword);

		Properties javaMailProperties = new Properties(System.getProperties());

		javaMailProperties.put("mail.smtp.starttls.enable", "true");
		javaMailProperties.put("mail.smtp.auth", "true");
		javaMailProperties.put("mail.smtp.ssl.trust", mailHostUrl.getHost());
		javaMailProperties.put("mail.smtp.ssl.protocols", "TLSv1.2");

		javaMailSender.setJavaMailProperties(javaMailProperties);

		return javaMailSender;
	}
	
	private final Predicate<Prepayment> isPendingPrepayment =
			p -> p.getInvoice().getItems().stream().allMatch(i -> i.getProduct() instanceof InitialSubscriptionOffer);

	private final Predicate<Prepayment> isRenewalPayment =
			p -> p.getInvoice().getItems().stream().anyMatch(i -> i.getProduct() instanceof RenewalOffer);
	
	private final Predicate<Prepayment> isUpgradePayment =
			p -> p.getInvoice().getItems().stream().anyMatch(i -> i.getProduct() instanceof UpgradeOffer);
	
	private final Predicate<Prepayment> isInsufficientPayment =
			p -> p.getInvoice().getItems().stream().anyMatch(i -> i.getChargebackReason() == ChargebackReason.INSUFFICIENT);
	
	private final Predicate<Prepayment> isServicePayment =
			p -> p.getInvoice().getItems().stream().anyMatch(i -> i.getProduct() instanceof ServiceOffer);
	
	private final Predicate<Prepayment> isInitialOfferChargebackPayment =
			p -> p.getInvoice().getItems().stream().anyMatch(i -> i.getProduct() instanceof Chargeback) &&
					p.getInvoice().getItems().stream().noneMatch(i -> i.getProduct() instanceof RenewalOffer);

	private final Predicate<Prepayment> isChargebackPayment =
			p -> p.getInvoice().getItems().stream().anyMatch(i -> i.getProduct() instanceof Chargeback);
	
	private final Predicate<Prepayment> isRenewalOfferChargebackPayment =
			p -> p.getInvoice().getItems().stream().anyMatch(i -> i.getProduct() instanceof Chargeback) &&
					p.getInvoice().getItems().stream().anyMatch(i -> i.getProduct() instanceof RenewalOffer);
	
	/*
	 * Returns true if a user has a birthday, address and completed all questionnaires,
	 * but doesn't have any payment.
	 *
	 * WARNING: If a required question will be added after payment selection,
	 * it will never return true.
	 */
	private final Predicate<MailQueueEntry> isMissingPayment =
			m ->
			{
				User user = getRecipient(m);
				return Objects.nonNull(user.getBirthDate()) &&
						!user.getAddresses().isEmpty() &&
						questionnaireService.isAllQuestionnairesAnswered(user) &&
						!paymentRepository.existsByUser(user);
			};
	
	private final Predicate<MailQueueEntry> isMissingQuestionnaire =
			m -> !questionnaireService.isAllQuestionnairesAnswered(getRecipient(m));
	
	private final Predicate<MailQueueEntry> isNoMatch =
			m -> Arrays.stream(RecommendationCategory.values())
					.mapToLong(category -> relationshipRepository.countBySourceUserAndCategory(
							getRecipient(m), category))
					.sum() == 0;

	private User getRecipient(MailQueueEntry mailQueueEntry)
	{
		if (mailQueueEntry instanceof UserMailQueueEntry)
		{
			return ((UserMailQueueEntry) mailQueueEntry).getRecipient();
		}
		else if (mailQueueEntry instanceof UnregisteredUserMailQueueEntry)
		{
			String recipientEmail = ((UnregisteredUserMailQueueEntry) mailQueueEntry).getRecipientEmail();
			return userRepository.findByEmail(recipientEmail);
		}
		
		throw new NullPointerException("Unknown recipient");
	}
	
	@Bean
	public MailReminder<?> mailReminder()
	{
		return shortReminderInterval ? devMailReminder() : prodMailReminder();
	}
	
	private MailReminder<?> prodMailReminder()
	{
		// reminder used in production
		return MailReminder.builder(ChronoUnit.DAYS)
				.addReminder(UserMailTemplate.CB_INSUFFICIENT_FIRST, isInsufficientPayment, 0, 0, Duration.ofDays(14))
				.addReminder(UserMailTemplate.CB_INSUFFICIENT_REMINDER, isInsufficientPayment, 1, 3, Duration.ofDays(14))
				.addReminder(UserMailTemplate.CB_REVOCATION_FIRST, isInitialOfferChargebackPayment, 0, 0, Duration.ofDays(14))
				.addReminder(UserMailTemplate.CB_REVOCATION_REMINDER, isChargebackPayment, 1, 5000, Duration.ofDays(20))
				.addReminder(UserMailTemplate.CB_REVOCATION_RENEWAL_FIRST, isRenewalOfferChargebackPayment, 0, 0, Duration.ofDays(14))
				.addReminder(UserMailTemplate.OPTIMIZATION_PP, isServicePayment, 0, 0, Duration.ofDays(16))
				.addReminder(UserMailTemplate.OPTIMIZATION_REMINDER, isServicePayment, 1, 3, Duration.ofDays(16))
				.addReminder(UserMailTemplate.ADMITTANCE_1, isPendingPrepayment, 0, 0, Duration.ofDays(16))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_NEW, isPendingPrepayment, 1, 1, Duration.ofDays(16))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_NEXT, isPendingPrepayment, 2, 1001, Duration.ofDays(20))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_RENEWAL_NEW, isRenewalPayment, 0, 0, Duration.ofDays(16))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_RENEWAL_NEXT, isRenewalPayment, 1, 1000, Duration.ofDays(20))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_UPGRADE_NEW, isUpgradePayment, 0, 0, Duration.ofDays(16))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_UPGRADE_NEXT, isUpgradePayment, 1, 1000, Duration.ofDays(20))
			.and()
				.configureTemplate(UserMailTemplate.MISSING_PAYMENT_REMINDER, isMissingPayment,
						LocalTime.of(7, 0), Duration.ofDays(3), Duration.ofDays(7), -1)
				.configureTemplate(UserMailTemplate.MISSING_QUESTIONAIRE_REMINDER, isMissingQuestionnaire,
						LocalTime.of(7, 0), Duration.ofDays(1), Duration.ofDays(7), -1)
			.and()
				.configureTemplate(UserMailTemplate.NO_MATCH_MESSAGE, isNoMatch, null, Duration.ofDays(2), Duration.ZERO, 1)
			.build();
	}

	private MailReminder<?> devMailReminder()
	{
		// reminder used for testing and development
		return MailReminder.builder(ChronoUnit.MINUTES)
				.addReminder(UserMailTemplate.CB_INSUFFICIENT_FIRST, isInsufficientPayment, 0, 0, Duration.ofMinutes(14))
				.addReminder(UserMailTemplate.CB_INSUFFICIENT_REMINDER, isInsufficientPayment, 1, 3, Duration.ofMinutes(14))
				.addReminder(UserMailTemplate.CB_REVOCATION_FIRST, isInitialOfferChargebackPayment, 0, 0, Duration.ofMinutes(14))
				.addReminder(UserMailTemplate.CB_REVOCATION_REMINDER, isChargebackPayment, 1, 2, Duration.ofMinutes(20))
				.addReminder(UserMailTemplate.CB_REVOCATION_RENEWAL_FIRST, isRenewalOfferChargebackPayment, 0, 0, Duration.ofMinutes(14))
				.addReminder(UserMailTemplate.OPTIMIZATION_PP, isServicePayment, 0, 0, Duration.ofMinutes(16))
				.addReminder(UserMailTemplate.OPTIMIZATION_REMINDER, isServicePayment, 1, 3, Duration.ofMinutes(16))
				.addReminder(UserMailTemplate.ADMITTANCE_1, isPendingPrepayment, 0, 0, Duration.ofMinutes(16))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_NEW, isPendingPrepayment, 1, 1, Duration.ofMinutes(16))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_NEXT, isPendingPrepayment, 2, 1001, Duration.ofMinutes(20))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_RENEWAL_NEW, isRenewalPayment, 0, 0, Duration.ofMinutes(15))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_RENEWAL_NEXT, isRenewalPayment, 1, 1000, Duration.ofMinutes(20))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_UPGRADE_NEW, isUpgradePayment, 0, 0, Duration.ofMinutes(15))
				.addReminder(UserMailTemplate.PREPAYMENT_REMINDER_UPGRADE_NEXT, isUpgradePayment, 1, 1000, Duration.ofMinutes(20))
			.and()
				.configureTemplate(UserMailTemplate.MISSING_PAYMENT_REMINDER, isMissingPayment,
						null, Duration.ofMinutes(3), Duration.ofMinutes(7), -1)
				.configureTemplate(UserMailTemplate.MISSING_QUESTIONAIRE_REMINDER, isMissingQuestionnaire,
						null, Duration.ofMinutes(1), Duration.ofMinutes(7), -1)
			.and()
				.configureTemplate(UserMailTemplate.NO_MATCH_MESSAGE, isNoMatch, null, Duration.ofDays(2), Duration.ZERO, 1)
			.build();
	}

}
