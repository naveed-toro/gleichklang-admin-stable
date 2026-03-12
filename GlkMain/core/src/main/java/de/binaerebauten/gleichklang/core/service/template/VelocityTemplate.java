package de.binaerebauten.gleichklang.core.service.template;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import java.util.Objects;

/**
 * Created by Domi on 03.02.2017.
 */
public interface VelocityTemplate {

    /**
     * Returns the name of this template.
     *
     * @return the template location
     */
    String getTemplateName();

    /**
     * Returns the base location of the template.
     *
     * @return base location of template
     */
    String getBaseLocation();

    /**
     * Returns the template location for the given language.
     *
     * This template location can be used as location when using
     * {@link org.springframework.ui.velocity.VelocityEngineUtils}
     *
     * @param language the non-null language
     * @return the template location for the given language
     */
    default String getTemplateLocation(I18NEntity.Language language) {
        Objects.requireNonNull(language, "No language");
        return String.format("%s/%s/%s", getBaseLocation(), language.toLocale().getLanguage(), getTemplateName());
    }
}
