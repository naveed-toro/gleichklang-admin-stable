package de.binaerebauten.gleichklang.core.service.mail;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.user.EmailLink;
import de.binaerebauten.gleichklang.core.model.user.EmailLink.EmailLinkContext;
import de.binaerebauten.gleichklang.core.model.user.EmailLinkParameter;
import de.binaerebauten.gleichklang.core.model.user.EmailLinkParameter.ParameterType;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.user.EmailLinkRepository;
import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class EmailLinkService
{
	public static final String QUICK_LOGIN_FRAGMENT = "quicklogin";
	public static final String TOKEN_PARAMETER = "token";
	public static final String API_PATH = "link";
	public static final String TEMP_PASSWORD = "tempPassword";
	public static final String EMAIL_PARAMETER = "email";


	private static final int PAGE_SIZE = 100;
	
	private static final Logger LOG = LoggerFactory.getLogger(EmailLinkService.class);
	
	@Autowired
	private EmailLinkRepository emailLinkRepository;
	
	@Autowired
	private AppUrlBuilder appUrlBuilder;

	@Scheduled(fixedRate = 1800000)
	@Transactional
	public void cleanUp()
	{
		LOG.info("cleanUp started");
		// Increase the mail validity upto 3 days.
		try
		{
			final List<Long> ids = emailLinkRepository.findExpiredIds(LocalDateTime.now().minus(3, ChronoUnit.DAYS));
			Lists.partition(ids, PAGE_SIZE).forEach(emailLinkRepository::deleteByIdIn);
		}
		catch (Exception e)
		{
			LOG.error("EmailLinkService.cleanUp", e);
		}
	}
	
	/**
	 * Creates and persist a new token for a user. Used for disposable Mail-Links.
	 *
	 * @param user
	 * @param context
	 * @return
	 */
	private EmailLink createEmailLink(User user, EmailLinkContext context)
	{
		Objects.requireNonNull(user);
		Objects.requireNonNull(context);
		
		final String token = UUID.randomUUID().toString();
		
		final EmailLink emailLink = new EmailLink();
		emailLink.setUser(user);
		emailLink.setUniqueToken(token);
		emailLink.setContext(context);
		emailLink.setUri(appUrlBuilder.toApi(token));
		
		return emailLink;
	}

    private EmailLink createEmailLinkMailConfirmation(User user, EmailLinkContext context) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(context);

        final String token = UUID.randomUUID().toString();

        final EmailLink emailLink = new EmailLink();
        emailLink.setUser(user);
        emailLink.setUniqueToken(token);
        emailLink.setContext(context);
        emailLink.setUri(appUrlBuilder.toApiMailConfirmation(token));
		LOG.info("in createEmailLinkMailConfirmation");
        return emailLink;
    }

    public EmailLink createValidateEmailLinkForMail(User user, String newMail) {
		LOG.info("in createValidateEmailLinkForMail==");
        emailLinkRepository.deleteByUserAndContext(user, EmailLinkContext.VALIDATE);

        final EmailLink emailLink = createEmailLinkMailConfirmation(user, EmailLinkContext.VALIDATE);
		LOG.info("emailLink=="+emailLink.toString());
        emailLink.addEmailLinkParameter(new EmailLinkParameter(ParameterType.EMAIL, user.getEmail()));
		LOG.info("emailLink1=="+emailLink.toString());
        return emailLinkRepository.save(emailLink);
    }

	private EmailLink createEmailLink(User user, EmailLinkContext context,String tempPassword) {
		Objects.requireNonNull(user);
		Objects.requireNonNull(context);

		final String token = UUID.randomUUID().toString();

		final EmailLink emailLink = new EmailLink();
		emailLink.setUser(user);
		emailLink.setUniqueToken(token);
		emailLink.setContext(context);
		emailLink.setUri(appUrlBuilder.toApi(token,tempPassword));

		return emailLink;
	}


	/**
	 * Creates and persist a new token for a user. Used for disposable Mail-Links.
	 *
	 * @param user
	 * @param mailTemplate
	 * @return
	 */

	public EmailLink createUnsubscribeEmailLink(User user, UserMailTemplate mailTemplate)
	{
		final EmailLink emailLink = createEmailLink(user, EmailLinkContext.UNSUBSCRIBE);
		emailLink.addEmailLinkParameter(new EmailLinkParameter(ParameterType.MAIL_TEMPLATE, mailTemplate.toString()));
		return emailLinkRepository.save(emailLink);
	}
	
	/**
	 * Creates and persist a new token for a user. Used for disposable Mail-Links.
	 *
	 * @param user
	 * @param newMail
	 * @return
	 */
	@Transactional
	public EmailLink createValidateEmailLink(User user, String newMail)
	{
		emailLinkRepository.deleteByUserAndContext(user, EmailLinkContext.VALIDATE);
		
		final EmailLink emailLink = createEmailLink(user, EmailLinkContext.VALIDATE);
		emailLink.addEmailLinkParameter(new EmailLinkParameter(ParameterType.EMAIL, newMail));
		return emailLinkRepository.save(emailLink);
	}
	
	/**
	 * Creates and persist a new token for a user. Used for disposable Mail-Links.
	 *
	 * @param user
	 * @param tmpPassword
	 * @return
	 */
	/*@Transactional
	public EmailLink createPasswordEmailLink(User user, String tmpPassword)
	{
		final EmailLink emailLink = createEmailLink(user, EmailLinkContext.RESET_PASSWORD);
		emailLink.addEmailLinkParameter(new EmailLinkParameter(ParameterType.PASSWORD, tmpPassword));
		return emailLinkRepository.save(emailLink);
	}
	*/

	@Transactional
	public EmailLink createPasswordEmailLink(User user, String tmpPassword) {
		final EmailLink emailLink = createEmailLink(user, EmailLinkContext.RESET_PASSWORD,tmpPassword);
		emailLink.addEmailLinkParameter(new EmailLinkParameter(ParameterType.PASSWORD, tmpPassword));
		return emailLinkRepository.saveAndFlush(emailLink);
	}

	/**
	 * Check the validation of token.
	 * Second call results in null, because this call also invalidate the link.
	 *
	 * @param token token of the link
	 * @return EmailLink if a valid link exist, else null
	 */
	@Transactional
	public EmailLink useValidLink(String token)
	{
		if (Strings.isNullOrEmpty(token)) return null;
		
		final EmailLink emailLink = emailLinkRepository.findByUniqueToken(token);
		if (emailLink != null) emailLinkRepository.delete(emailLink);
		return emailLink;
	}
}
