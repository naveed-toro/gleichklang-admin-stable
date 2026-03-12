package de.binaerebauten.gleichklang.core.service.content;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.service.template.VelocityTemplate;

/**
 * Created by Domi on 03.02.2017.
 */
public enum AdvertisementContentTemplate implements VelocityTemplate {

    AKTUELLES("static-content-aktuelles.html.vm"),
    COMMUNITY("static-content-community.html.vm"),
    MOEGLICHKEITEN("static-content-moeglichkeiten.html.vm");

    private final String templateName;

    private final static String STATIC_CONTENT_BASE_LOCATION = "static-content/templates";


    AdvertisementContentTemplate(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public String getTemplateName() {
        return templateName;
    }

    @Override
    public String getBaseLocation() {
        return STATIC_CONTENT_BASE_LOCATION;
    }

}
