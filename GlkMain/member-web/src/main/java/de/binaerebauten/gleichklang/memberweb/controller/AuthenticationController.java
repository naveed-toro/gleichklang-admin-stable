package de.binaerebauten.gleichklang.memberweb.controller;

import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.user.EmailLink;
import de.binaerebauten.gleichklang.core.model.user.EmailLinkParameter;
import de.binaerebauten.gleichklang.core.model.user.EmailLinkParameter.ParameterType;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.mail.EmailLinkService;
import de.binaerebauten.gleichklang.core.service.mail.NewsletterService;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import de.binaerebauten.gleichklang.core.utils.AppUrlBuilder;
import de.binaerebauten.gleichklang.memberweb.controller.exception.WebExceptionHandling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class AuthenticationController extends WebExceptionHandling
{
	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);

	private final String landingPageRoot;

	@Autowired
	private UserService userService;

	@Autowired
	private EmailLinkService emailLinkService;

	@Autowired
	private AppUrlBuilder appUrlBuilder;

	private final NewsletterService newsletterService;
	private final UndeliverableMailService undeliverableMailService;

	@Autowired
	public AuthenticationController(Environment environment, NewsletterService newsletterService, UndeliverableMailService undeliverableMailService)
	{
		landingPageRoot = environment.getProperty("landing.root");

		this.newsletterService = Objects.requireNonNull(newsletterService);
		this.undeliverableMailService = Objects.requireNonNull(undeliverableMailService);
	}

	@RequestMapping(EmailLinkService.API_PATH)
	public void link(HttpServletResponse response, @RequestParam(EmailLinkService.TOKEN_PARAMETER) String token, HttpServletRequest request)
	{
		final EmailLink emailLink = emailLinkService.useValidLink(token);
		if (emailLink == null)
		{
			redirectTo(response, landingPageRoot + "/fehler-meldung/");
			LOG.error("Validation for token {} failed", token);
			return;
		}

		if (!(emailLink.getUser() instanceof User))
		{
			redirectTo(response, landingPageRoot + "/fehler-meldung/");
			LOG.error("User {} is an admin user and cannot handle here", emailLink.getUser().getEmail());
			return;
		}

		final User user = (User) emailLink.getUser();
		final String email = user.getEmail();
		final Map<ParameterType, String> parameterMap = emailLink.getEmailLinkParameters().stream().collect(Collectors.toMap(EmailLinkParameter::getParameterType, EmailLinkParameter::getValue));

		LOG.debug("{} link clicked by user {} ", emailLink.getContext(), email);

		switch (emailLink.getContext())
		{
			case UNSUBSCRIBE:
				userService.unsubscribe(user, UserMailTemplate.valueOf(parameterMap.get(ParameterType.MAIL_TEMPLATE)));
				redirectTo(response, appUrlBuilder.toAppPath("?email=" + email + "&unsubscribed").getPath());
				break;
			case RESET_PASSWORD:
				final String tmpPassword = parameterMap.get(ParameterType.PASSWORD);
				userService.resetPassword(user, tmpPassword);
				redirectTo(response, appUrlBuilder.toAutoLogin(email, tmpPassword).toString());
				break;
			case VALIDATE:
				final String newMail = parameterMap.get(ParameterType.EMAIL);

				if ((Objects.equals(newMail, user.getNewEmail()) || Objects.equals(newMail, user.getEmail())) && userService.changeMail(request, user, newMail))
				{
					newsletterService.removeMail(user);
					undeliverableMailService.remove(user);
					// hier muss wieder user.getEmail() verwendet werden, da diese im changeMail geändert wurde
					redirectTo(response, appUrlBuilder.toAppPath("?email=" + user.getEmail() + "&confirm").getPath());
				}
				else
				{
					redirectTo(response, landingPageRoot + "/fehler-meldung/");
				}
				break;
		}

	}

	@RequestMapping("registered")
	public void registered(HttpServletResponse response)
	{
		redirectTo(response, landingPageRoot + "/registered");
	}

	private void redirectTo(HttpServletResponse response, String path)
	{
		try
		{
			response.sendRedirect(path);
			LOG.info("Redirected to {} ", path);
		}
		catch (IOException e)
		{
			LOG.error("Redirection failed", e);
		}
	}

}
