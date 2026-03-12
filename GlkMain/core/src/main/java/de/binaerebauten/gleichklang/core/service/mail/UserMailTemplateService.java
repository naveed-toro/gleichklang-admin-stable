package de.binaerebauten.gleichklang.core.service.mail;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.service.DynamicContentTemplateService;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import de.binaerebauten.gleichklang.core.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static de.binaerebauten.gleichklang.core.model.message.MessageMailTemplate.SIGNATURE;

/**
 * This services is responsible for creating mail template instances for the given parameters.
 */
@Service
public class UserMailTemplateService
{

	private static final Logger LOG = LoggerFactory.getLogger(UserMailTemplateService.class);

	private boolean dbDrivenTemplatesEnabled=true;

	private final TemplateEngineService templateEngineService;

	private final EmailLinkService emailLinkService;

	private final DynamicContentTemplateService dynaContentService;


	@Value("${security.salt}")
	private String salt;

	/**
	 * This service uses constructor based dependency injection to ease unit testing.
	 *
	 * @param templateEngineService
	 */
	@Autowired
	public UserMailTemplateService(EmailLinkService emailLinkService, TemplateEngineService templateEngineService, DynamicContentTemplateService dynaContentService)
	{
		this.templateEngineService = templateEngineService;
		this.emailLinkService = emailLinkService;
		this.dynaContentService = dynaContentService;
	}

	/**
	 * Creates a mail template instance without a signableUser, this just works for mail templates that doesn't require
	 * any signableUser data.
	 *
	 * @param mailTemplate the non-null mail template
	 * @return the mail template instance
	 */
	public MailTemplateInstance createMailTemplateInstance(UserMailTemplate mailTemplate)
	{
		Preconditions.checkArgument(UserMailTemplate.SOCIAL_UNREGISTERED_USER.equals(mailTemplate));
		Map<String, Object> variables = ImmutableMap.of("social_action_code", InitialSubscriptionOffer.SOCIAL_CODE);

		return getMailTemplateInstance( variables, mailTemplate, Language.DE);
	}

	/**
	 * Creates a new mail template instance from the given parameters.
	 *
	 * @param mailTemplate the non-null signableUser mail template
	 * @param payment      the non-null payment
	 * @return the mail template instance
	 */
	public MailTemplateInstance createMailTemplateInstance(UserMailTemplate mailTemplate, AbstractPayment payment)
	{
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(payment, "payment == null");

		User user = payment.getUser();
		Language lang = getLanguage(user);
		Map<String, Object> variables = ImmutableMap.of("user", user);
		switch (mailTemplate)
		{
			case ADMITTANCE_1:
			case ADMITTANCE_2:
			case RENEWAL_NOTIF_DDCC:
				Optional<InvoiceItem> invoiceItemOptional = payment.getInvoice().getItems().stream().findFirst();
				if (invoiceItemOptional.isPresent())
				{
					Product product = invoiceItemOptional.get().getProduct();
					if (product instanceof SubscriptionOffer)
					{
						SubscriptionOffer offer = ((SubscriptionOffer) product);
						SubscriptionOffer autoRenewalOffer = offer.getAutoRenewalOffer();
						variables = ImmutableMap.<String, Object>builder().putAll(variables)
								.put("durationInMonths", offer.getDurationUnit() == DurationUnit.MONTHS ? offer.getDuration() : "")
								.put("autoRenewalDurationInMonths", Objects.nonNull(autoRenewalOffer)
										&& autoRenewalOffer.getDurationUnit() == DurationUnit.MONTHS ? autoRenewalOffer.getDuration() : "")
								.build();
					}
				}
			case CB_ACCOUNTERROR_FIRST:
			case CB_ACCOUNTERROR_RENEWAL_FIRST:
			case CB_INSUFFICIENT_FIRST:
			case CB_INSUFFICIENT_RENEWAL_FIRST:
			case CB_INSUFFICIENT_REMINDER:
			case CB_OTHER_FIRST:
			case CB_OTHER_RENEWAL_FIRST:
			case CB_REVOCATION_FIRST:
			case CB_REVOCATION_REMINDER:
			case CB_REVOCATION_RENEWAL_FIRST:
			case DONATION:
			case EXTENSION:
			case EXTENSION_PP:
			case EXTENSION_DDCC:
			case OPTIMIZATION_PP:
			case OPTIMIZATION_DDCC:
			case OPTIMIZATION_REMINDER:
			case PP_PAID_CB_NOTIF:
			case PP_PAID_DONATION_NOTIF:
			case PP_PAID_OPTIMIZATION_NOTIF:
			case PREPAYMENT_PAID_NOTFICATION:
			case PREPAYMENT_REMINDER_NEW:
			case PREPAYMENT_REMINDER_NEXT:
			case PREPAYMENT_REMINDER_RENEWAL_NEW:
			case PREPAYMENT_REMINDER_RENEWAL_NEXT:
			case PREPAYMENT_REMINDER_UPGRADE_NEW:
			case PREPAYMENT_REMINDER_UPGRADE_NEXT:
			case RENEWAL_FAILED_ACTUAL:
			case ADMIN_UNKNOWN_EXTERNAL_PAYMENT:
				String beginDate = DateTimeFormatter.ISO_LOCAL_DATE.withLocale(lang.toLocale()).format(payment.getCreateDate());
				String now = DateTimeFormatter.ISO_LOCAL_DATE.withLocale(lang.toLocale()).format(LocalDateTime.now());
				variables = ImmutableMap.<String, Object>builder().putAll(variables)
						.put("paymentInfo", payment.getTranslatedInfo())
						.put("beginDate", beginDate)
						.put("now", now)
						.put("amount", payment.getAmount()).build();
				break;
			case CB_REFUND_FIRST:
			case CB_REFUND_NEXT:
				variables = ImmutableMap.<String, Object>builder().putAll(variables)
						.put("paymentInfo", payment.getTranslatedInfo())
						.put("amount", payment.getAmount()).build();
				break;
			case PREPAYMENT_REFUND_NOTFICATION:
				MonetaryAmount monetaryAmount = payment.getAmount();
				variables = ImmutableMap.<String, Object>builder().putAll(variables)
						.put("amount", new MonetaryAmount(monetaryAmount.getAmount().abs(), monetaryAmount.getCurrency())).build();
				break;
			case PREPAYMENT_REFUND_REQUEST:
				break;
			default:
				throw new IllegalArgumentException("Unsupported mail template value: " + mailTemplate);
		}

		return getMailTemplateInstance( variables, mailTemplate, lang);
	}

	public MailTemplateInstance createMailTemplateInstance(UserMailTemplate mailTemplate, Subscription subscription)
	{
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(subscription, "subscription == null");

		final User user = subscription.getUser();
		final Language lang = getLanguage(user);
		final SubscriptionOffer nextOffer = subscription.getOffer().getAutoRenewalOffer();
		final int duration = nextOffer.getDuration();
		final MonetaryAmount amount = nextOffer.getAmount();
		final MonetaryAmount monthAmount = new MonetaryAmount(BigDecimal.valueOf(Math.ceil(amount.getAmount().intValue() / duration)), amount.getCurrency());
		final String endDate = DateTimeFormatter.ISO_LOCAL_DATE.withLocale(lang.toLocale()).format(subscription.getEnd());

		final Builder<String, Object> variableBuilder = ImmutableMap.<String, Object>builder()
				.put("user", user)
				.put("amount", amount)
				.put("monthAmount", monthAmount)
				.put("durationInMonths", duration)
				.put("endDate", endDate);

		switch (mailTemplate)
		{
			case RENEWAL_CHOSEN_REMINDER:
				final String autoRenewalBeginDate = DateTimeFormatter.ISO_LOCAL_DATE.withLocale(lang.toLocale()).format(subscription.getEnd().plusDays(1));

				variableBuilder.put("autoRenewalBeginDate", autoRenewalBeginDate);
				break;
			case RENEWAL_DISABLED_REMINDER:
				final String expirationDate = DateTimeFormatter.ISO_LOCAL_DATE.withLocale(lang.toLocale()).format(subscription.getExpirationDate());

				variableBuilder.put("expirationDate", expirationDate);
				break;
			case RENEWAL_DISABLED:
				break;
			default:
				throw new IllegalArgumentException("Unsupported mail template value: " + mailTemplate);
		}

		return getMailTemplateInstance(variableBuilder.build(), mailTemplate, lang);
	}

	/**
	 * Creates a new mail template instance from the given parameters.
	 *
	 * @param mailTemplate the non-null user mail template
	 * @param user         the non-null user
	 * @return the mail template instance
	 */
	public MailTemplateInstance createMailTemplateInstance(UserMailTemplate mailTemplate, User user)
	{
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(user, "signableUser == null");

		Language lang = getLanguage(user);
		Map<String, Object> variables = ImmutableMap.of("user", user);

		switch (mailTemplate)
		{
			case REGISTRATION_USER:
				final URI confirmationUrl = emailLinkService.createValidateEmailLink(user, user.getEmail()).getUri();
				variables = ImmutableMap.<String, Object>builder().putAll(variables).put("confirmation_url", confirmationUrl).build();
				break;
			case MAIL_CHANGE_VERIFICATION:
				LOG.info("in mail change verification==");
				//final URI changeMailUrl = emailLinkService.createValidateEmailLink(user, user.getNewEmail()).getUri();
				final URI changeMailUrl = emailLinkService.createValidateEmailLinkForMail(user, user.getEmail()).getUri();
				LOG.info("between mail change verification==");
				variables = ImmutableMap.<String, Object>builder().putAll(variables).put("confirmation_url", changeMailUrl).build();
				LOG.info("end mail change verification==");
				break;
			case PASSWORD_RESET:
				final String password = SecurityUtils.generateTempPassword(UUID.randomUUID().toString(), salt);
				final EmailLink passwordResetLink = emailLinkService.createPasswordEmailLink(user, password);

				variables = ImmutableMap.<String, Object>builder()
						.putAll(variables)
						.put("temp_password", password)
						.put("password_reset_url", passwordResetLink.getUri())
						.build();
				break;
			case PASSWORD_RESET_NEW:
				final String passwordNew = SecurityUtils.generateTempPassword(UUID.randomUUID().toString(), salt);
				final EmailLink passwordResetLinkNew = emailLinkService.createPasswordEmailLink(user, passwordNew);

				variables = ImmutableMap.<String, Object>builder()
						.putAll(variables)
						.put("temp_password", passwordNew)
						.put("password_reset_url", passwordResetLinkNew.getUri())
						.build();
				break;

			case MISSING_PAYMENT_REMINDER:
			case MISSING_QUESTIONAIRE_REMINDER:
				final URI unsubscribeUrl = emailLinkService.createUnsubscribeEmailLink(user, mailTemplate).getUri();
				variables = ImmutableMap.<String, Object>builder().putAll(variables).put("unsubscribe_url", unsubscribeUrl).build();
				break;
			case NO_MATCH_MESSAGE:
			case SOCIAL_PP_USER:
			case SOCIAL_REGISTERED_USER:
			case USER_DELETED:
			case USER_DELETED_WITH_SATISFACTION:
			case SUBSCRIPTION_CANCELLED:
			case SUBSCRIPTION_CANCELLED_WITH_SATISFACTION:
			case SUBSCRIPTION_CANCELLED_USER_DELETED:
			case SUBSCRIPTION_CANCELLED_USER_DELETED_WITH_SATISFACTION:
			case NEW_BOXNUMBER_CONTACT:
				break;
			default:
				throw new IllegalArgumentException("Unsupported mail template value: " + mailTemplate);
		}

		return getMailTemplateInstance( variables, mailTemplate, lang);
	}

	public MailTemplateInstance createNewPassword(User user, String password)
	{
		Objects.requireNonNull(user);
		Objects.requireNonNull(password);

		final Language lang = getLanguage(user);
		final Map<String, Object> variables = ImmutableMap.of("user", user, "password", password);

		return getMailTemplateInstance( variables, UserMailTemplate.NEW_PASSWORD, lang);
	}

	public MailTemplateInstance createMailTemplateInstance(UserMailTemplate mailTemplate, User sourceUser, User targetUser, RecommendationCategory category)
	{
		Preconditions.checkArgument(UserMailTemplate.NEW_MATCH_POSITIVE.equals(mailTemplate) || UserMailTemplate.NEW_BOXNUMBER_CONTACT.equals(mailTemplate));

		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(sourceUser, "sourceUser == null");
		Objects.requireNonNull(targetUser, "targetUser == null");
		Objects.requireNonNull(category, "category == null");

		final Language lang = getLanguage(targetUser);
		final String categoryString = category.msgInLanguage(lang);

		final ImmutableMap.Builder<String, Object> variableBuilder = ImmutableMap.builder();
		variableBuilder.put("sender", sourceUser);
		variableBuilder.put("receiver", targetUser);
		variableBuilder.put("unsubscribe_url", emailLinkService.createUnsubscribeEmailLink(targetUser, mailTemplate).getUri());
		variableBuilder.put("recommendation_category", categoryString);

		return getMailTemplateInstance( variableBuilder.build(), mailTemplate, lang);
	}


	public MailTemplateInstance createMailTemplateInstanceForAdminMessage(UserMailTemplate mailTemplate, User targetUser)
	{
		final Map<String, Object> model = new HashMap<>();
		model.put("admin", "Gleichklang");
		String signature = templateEngineService.getContentForTemplate(SIGNATURE, Language.DE, model);
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(targetUser, "targetUser == null");
		final Language lang = getLanguage(targetUser);
		final ImmutableMap.Builder<String, Object> variableBuilder = ImmutableMap.builder();
		variableBuilder.put("user",targetUser);
		variableBuilder.put("signature",signature);
		return getMailTemplateInstance( variableBuilder.build(), mailTemplate, lang);
	}

	public MailTemplateInstance createMailTemplateInstanceForAdminMessage(UserMailTemplate mailTemplate, Admin admin, User targetUser, Message message, Message oldmMessage)
	{
		//String [] messages = message.getBody().split("Es grüßt herzlich");
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(targetUser, "targetUser == null");

		final Language lang = getLanguage(targetUser);
		final ImmutableMap.Builder<String, Object> variableBuilder = ImmutableMap.builder();
		variableBuilder.put("sender", admin);
		variableBuilder.put("senderName", targetUser.getAlias());
		variableBuilder.put("receiver", targetUser);
		/*variableBuilder.put("messagetext", messages[0]);
		variableBuilder.put("signature", messages[1].trim());*/
       /* String signature = templateEngineService.getContentForTemplate(SIGNATURE, Language.DE);
        variableBuilder.put("signature",signature);*/
       if(mailTemplate.equals(UserMailTemplate.ADMIN_FIRST_MESSAGE)) {
		   String signature = "<span style=\"margin-bottom:1em;color:#777777;width:100%\">" + message.getBody().split("<span style=\"margin-bottom:1em;color:#777777;width:100%\">")[1];
		   variableBuilder.put("signature",signature);
       }
		variableBuilder.put("messagetext", message.getBody());
		variableBuilder.put("memberStatus", targetUser.getMemberStatus().name());
		variableBuilder.put("userMessage", "userMessage");
		if(oldmMessage!=null) {
			variableBuilder.put("oldMessage", oldmMessage.getBody());
			variableBuilder.put("subject", oldmMessage.getSubject());
		}
		else{
			variableBuilder.put("subject", message.getSubject());
		}
		variableBuilder.put("signatureCheck", "signatureCheck");

		return getMailTemplateInstance( variableBuilder.build(), mailTemplate, lang);
	}

	/**
	 * Creates a new mail template instance from the given parameters.
	 *
	 * TODO Object... params is suboptimal and should be refactored
	 *
	 * @param mailTemplate
	 * @param user
	 * @param params
	 * @return
	 */
	public MailTemplateInstance createMailTemplateInstance(UserMailTemplate mailTemplate, User user, Object... params)
	{
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(user, "signableUser == null");

		final Language lang = getLanguage(user);

		final ImmutableMap.Builder<String, Object> variableBuilder = ImmutableMap.builder();
		variableBuilder.put("user", user);

		switch (mailTemplate)
		{
			case OPTIMIZATION_DONE:
				Objects.requireNonNull(params[0], "params[0] == 0");
				variableBuilder.put("recommendation_category", params[0]);
				Objects.requireNonNull(params[1], "params[1] == 0");
				variableBuilder.put("text", params[1]);
				break;
			case NEW_MATCH:
				Objects.requireNonNull(params[0], "params[0] == 0");
				String recommendations = params[0] instanceof List ? ((List<Object>) params[0]).stream()
						.filter(obj -> obj instanceof Relationship)
						.map(obj -> ((Relationship) obj).getTargetUser().getAlias())
						.collect(Collectors.joining(", ")) : "";
				variableBuilder.put("recommendations", recommendations);
				variableBuilder.put("unsubscribe_url", emailLinkService.createUnsubscribeEmailLink(user, mailTemplate).getUri());
				break;
			case NEW_FOOTPRINT:
				Objects.requireNonNull(params[0], "params[0] == 0");
				String pseudonyms = params[0] instanceof List ? ((List<Object>) params[0]).stream()
						.filter(obj -> obj instanceof Relationship)
						.map(obj -> ((Relationship) obj).getSourceUser().getAlias())
						.collect(Collectors.joining(", ")) : "";
				variableBuilder.put("pseudonyms", pseudonyms);
				variableBuilder.put("unsubscribe_url", emailLinkService.createUnsubscribeEmailLink(user, mailTemplate).getUri());
				break;
			case NEWS_FROM_GLEICHKLANG:
				Objects.requireNonNull(params[0], "params[0] == 0");
				String news = params[0] instanceof List ? ((List<Object>) params[0]).stream()
						.filter(obj -> obj instanceof UserNews)
						.map(obj -> ((UserNews) obj).getNews().getTitle() + "<br />" + ((UserNews) obj).getNews().getTeaserText() + "<br />" + ((UserNews) obj).getNews().getText())
						.collect(Collectors.joining("<hr />")) : "";
				variableBuilder.put("news", news);
				variableBuilder.put("unsubscribe_url", emailLinkService.createUnsubscribeEmailLink(user, mailTemplate).getUri());
				break;
			case SIGNOFF_ACK_MSG:
				Objects.requireNonNull(params[0], "params[0] == 0");
				variableBuilder.put("info", params[0]);
				break;
			default:
				throw new IllegalArgumentException("Unsupported mail template value: " + mailTemplate);
		}

		return getMailTemplateInstance( variableBuilder.build(), mailTemplate, lang);
	}

	public MailTemplateInstance createAdminMailTemplateInstance(UserMailTemplate mailTemplate, Admin admin, String password)
	{
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(admin, "admin == null");
		Objects.requireNonNull(password, "password == null");

		final Language lang = getLanguage(admin);

		Map<String, Object> variables = ImmutableMap.of("user", admin);
		switch (mailTemplate)
		{
			case ADMIN_PASSWORD_RESET:
				variables = ImmutableMap.<String, Object>builder().putAll(variables).put("temp_password", password).build();
				break;
			default:
				throw new IllegalArgumentException("Unsupported mail template value: " + mailTemplate);
		}

		return getMailTemplateInstance( variables, mailTemplate, lang);
	}

	public MailTemplateInstance createAdminPasswordResetRequest(Admin sender, Admin recipient)
	{
		Objects.requireNonNull(recipient, "recipient == null");
		Objects.requireNonNull(sender, "sender == null");
		Map<String, Object> variables = ImmutableMap.<String, Object>builder().put("admin", sender).put("user", recipient).build();

		Language lang = getLanguage(recipient);
		return getMailTemplateInstance( variables, UserMailTemplate.ADMIN_PASSWORD_RESET_REQUEST, lang);
	}

	private Language getLanguage(SignableUser user)
	{
		if(user instanceof User) return ((User) user).getUserSettings().getLanguage();
		return Language.DE;
	}

	private MailTemplateInstance getMailTemplateInstance(Map<String, Object> variables, UserMailTemplate template,  Language lang )

	{

		MailTemplateInstance instance = null;

		try
		{
			if(dbDrivenTemplatesEnabled)
			{
					instance = MailTemplateInstance.create(dynaContentService, variables, template, lang);

				if(instance.getContent() != null && !instance.getContent().isEmpty())
				{
					// Valid usable instance returned
					return instance;
				}
			}}catch (Exception ex)
		{
			ex.printStackTrace();
		}
		// Default Flow
		instance = MailTemplateInstance.create(templateEngineService,variables,template,lang);

		return  instance;
	}

}