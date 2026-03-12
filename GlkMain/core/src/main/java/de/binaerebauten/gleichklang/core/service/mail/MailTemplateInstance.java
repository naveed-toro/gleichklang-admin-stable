package de.binaerebauten.gleichklang.core.service.mail;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.DynamicContentTemplateService;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents an instance of {@link MailTemplate}, which has a
 * subject and a content.
 */
public class MailTemplateInstance
{
	private final MailTemplate mailTemplate;
	
	private final String subject;
	
	private final String content;


	
	/**
	 * Creates a new mail template instance from the given parameters.
	 *
	 * @param mailTemplate the non-null subject
	 * @param subject the non-null subject
	 * @param content the non-null content
	 */
	@VisibleForTesting
	MailTemplateInstance(MailTemplate mailTemplate, String subject, String content)
	{
		this.mailTemplate = Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		this.subject = Objects.requireNonNull(subject, "subject == null");
		this.content = Objects.requireNonNull(content, "content == null");
	}
	
	public String getSubject()
	{
		return subject;
	}
	
	public String getContent()
	{
		return content;
	}
	
	public MailTemplate getMailTemplate()
	{
		return mailTemplate;
	}
	
	/**
	 * Creates a new mail templates from the given parameters.
	 *
	 * @param templateEngineService	the non-null velocity engine
	 * @param user           		the non-null user, which will be passed under the
	 *                       		key "user" to the velocity context
	 * @param mailTemplate   		the non-null mail template
	 * @param language       		the non-null language used to locate the template
	 * @return the mail template instance
	 */
	public static MailTemplateInstance create(TemplateEngineService templateEngineService, User user, MailTemplate mailTemplate, I18NEntity.Language language)
	{
		Objects.requireNonNull(user, "user == null");
		
		return create(templateEngineService, ImmutableMap.of("user", user), mailTemplate, language);
	}
	
	/**
	 * Creates a new mail templates from the given parameters.
	 *
	 * @param templateEngineService	the non-null velocity engine
	 * @param model          		the non-null velocity model
	 * @param mailTemplate   		the non-null mail template
	 * @param language       		the non-null language used to locate the template
	 * @return the mail template instance
	 */
	public static MailTemplateInstance create(TemplateEngineService templateEngineService, Map<String, Object> model, MailTemplate mailTemplate, I18NEntity.Language language)
	{
		Objects.requireNonNull(templateEngineService, "velocityEngineService == null");
		Objects.requireNonNull(model, "model == null");
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(language, "language == null");

		String mail = templateEngineService.getContentForTemplate(mailTemplate, language, model);
		int subjectEnd = mail.indexOf("\n");
		
		Preconditions.checkState(subjectEnd < mail.length() - 1);
		
		String subject = mail.substring(0, subjectEnd);
		String content = mail.substring(subjectEnd + 1).replaceAll("\n", "<br>");
		
		return new MailTemplateInstance(mailTemplate, subject, content);
	}


	public static MailTemplateInstance create(DynamicContentTemplateService service, Map<String, Object> model, MailTemplate mailTemplate, I18NEntity.Language language)
	{
		Objects.requireNonNull(service, "dbEngineService == null");
		Objects.requireNonNull(model, "model == null");
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(language, "language == null");

		String mail = service.getContentForTemplate(mailTemplate.getTemplateName(), language, model);
		int subjectEnd = mail.indexOf("\n");

		Preconditions.checkState(subjectEnd < mail.length() - 1);

		String subject = mail.substring(0, subjectEnd);
		String content = mail.substring(subjectEnd + 1).replaceAll("\n", "<br>");

		return new MailTemplateInstance(mailTemplate, subject, content);
	}


	public static MailTemplateInstance createTemplateForTest(DynamicContentTemplateService service, Map<String, Object> model, MailTemplate mailTemplate, I18NEntity.Language language)
	{
		Objects.requireNonNull(service, "dbEngineService == null");
		Objects.requireNonNull(model, "model == null");
		Objects.requireNonNull(mailTemplate, "mailTemplate == null");
		Objects.requireNonNull(language, "language == null");

		String mail = service.getContentForTemplateTest(mailTemplate.getTemplateName(), language, model);
		int subjectEnd = mail.indexOf("\n");

		Preconditions.checkState(subjectEnd < mail.length() - 1);

		String subject = mail.substring(0, subjectEnd);
		String content = mail.substring(subjectEnd + 1).replaceAll("\n", "<br>");

		return new MailTemplateInstance(mailTemplate, subject, content);
	}


}
