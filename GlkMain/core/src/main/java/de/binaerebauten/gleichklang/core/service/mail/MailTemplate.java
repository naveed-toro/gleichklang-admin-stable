package de.binaerebauten.gleichklang.core.service.mail;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.service.template.VelocityTemplate;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.util.Objects;

/**
 * Mixin interface so that we can use enums for mail templates.
 */
public interface MailTemplate extends VelocityTemplate
{
	/**
	 * The base location for the mail templates.
	 */
	String MAIL_TEMPLATE_BASE_LOCATION = "mail/templates";

	/**
	 * Returns the name of this template.
	 *
	 * @return the template location
	 */
	String getTemplateName();

	/**
	 * Returns the template location for the given language.
	 * <p/>
	 * This template location can be used as location when using
	 * {@link org.springframework.ui.velocity.VelocityEngineUtils}
	 *
	 * @param language the non-null language
	 * @return the template location for the given language
	 */
	default String getTemplateLocation(I18NEntity.Language language)
	{
		Objects.requireNonNull(language, "language == null");

		return String.format("%s/%s/%s",
				getBaseLocation(),
				language.toLocale().getLanguage(),
				getTemplateName());
	}

	@Override
	default String getBaseLocation() {
		return MAIL_TEMPLATE_BASE_LOCATION;
	};
}
