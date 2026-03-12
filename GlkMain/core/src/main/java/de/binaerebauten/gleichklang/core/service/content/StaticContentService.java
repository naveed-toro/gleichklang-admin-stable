package de.binaerebauten.gleichklang.core.service.content;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomLayout;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import de.binaerebauten.gleichklang.core.utils.LocaleAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Static Content Service for
 */

@Service
public class StaticContentService implements LocaleAware {

    private static final Logger LOG = LoggerFactory.getLogger(StaticContentService.class);

    @Autowired
    private TemplateEngineService templateEngineService;

    /**
     * Returns a CustomLayout for a given advertisement template for current browser language.
     *
     * @param template Content template
     * @return Custom Layout component
     */
    public Component getStaticComponent(AdvertisementContentTemplate template) {

        final I18NEntity.Language language = I18NEntity.Language.valueOf(getLocale());

        final String staticContent = templateEngineService.getContentForTemplate(template, language);
        final CustomLayout customLayout;
        try {
            customLayout = new CustomLayout(new ByteArrayInputStream(staticContent.getBytes()));
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage());
            return new CssLayout();
        }

        return customLayout;
    }
}
