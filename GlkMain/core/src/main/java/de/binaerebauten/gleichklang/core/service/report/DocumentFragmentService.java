package de.binaerebauten.gleichklang.core.service.report;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import de.binaerebauten.gleichklang.core.utils.pdf.HTMLElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Template Service for User Reports
 */
@Service
public class DocumentFragmentService {

    @Autowired
    private TemplateEngineService templateEngineService;


    /**
     * Gets the text fragments for the given language.
     *
     * @param template text fragment template
     * @param locale language of the text fragment
     * @return text fragment as pdf html elements
     */
    public HTMLElement createDocumentFragmentFromTemplate(ReportFragmentTemplate template, Locale locale) {
        Map<String, Object> model = new HashMap<>();
        return createDocumentFragmentFromTemplate(template, model, locale);
    }

    /**
     * Gets the text fragments for the given language and inserted text values.
     *
     * @param template text fragment template
     * @param model values which should be included
     * @param locale language of the text fragment
     * @return text fragment as pdf html elements
     */
    public HTMLElement createDocumentFragmentFromTemplate(ReportFragmentTemplate template, Map<String, Object> model, Locale locale) {
        String content = getMergedTemplateText(template, model, I18NEntity.Language.valueOf(locale));
        return  new HTMLElement(content);
    }

    /**
     * Gets the text fragments for the given language and inserted text values.
     *
     * @param template text fragment template
     * @param model values which should be included
     * @param locale language of the text fragment
     * @return text fragment as pdf html elements
     */
    public List<HTMLElement> createDetailDescription(ReportFragmentTemplate template, Map<String, Object> model, Locale locale) {
        String content = getMergedTemplateText(template, model, I18NEntity.Language.valueOf(locale));

        String[] splittedContent = content.split("~~~");

        List<HTMLElement> bulletListContent = new ArrayList<>();
        for (String item : splittedContent) {
            bulletListContent.add(new HTMLElement(item));
        }
        return bulletListContent;
    }

    /**
     * Get text from template for given language and merge with given values.
     *
     * @param template text template
     * @param model values which should be merged with the text
     * @param language language of the text templates
     * @return merged text from the template with given values in specific language
     */
    private String getMergedTemplateText(ReportFragmentTemplate template, Map<String, Object> model, I18NEntity.Language language) {
        Objects.requireNonNull(template, "ReportTemplate not given");
        Objects.requireNonNull(model, "No model");

        if(language == null){
            language = Language.getDefault();
        }

        Objects.requireNonNull(language, "No language");

        return templateEngineService.getContentForTemplate(template, language, model);
    }
}
