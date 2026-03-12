package de.binaerebauten.gleichklang.core.model.message;

import de.binaerebauten.gleichklang.core.service.mail.MailTemplate;
import de.binaerebauten.gleichklang.core.service.template.VelocityTemplate;

/**
 * Currently only for message signature
 */
public enum MessageMailTemplate implements MailTemplate
{
	SIGNATURE("include_footer.html.vm"),
	ADMIN_ANSWER("admin_answer.html.vm");
	
	private final String templateName;

	MessageMailTemplate(String templateName)
	{
		this.templateName = templateName;
	}

	@Override
	public String getTemplateName()
	{
		return templateName;
	}
}
