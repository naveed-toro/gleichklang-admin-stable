package de.binaerebauten.gleichklang.core.service.template;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Domi on 03.02.2017.
 */

@Service
public class TemplateEngineService
{
	
	private static final Logger LOG = LoggerFactory.getLogger(TemplateEngineService.class);
	
	private final VelocityEngine velocityEngine;
	
	@Autowired
	public TemplateEngineService(VelocityEngine velocityEngine)
	{
		this.velocityEngine = velocityEngine;
	}
	
	/**
	 * Gets Content based on template for a given language merged with data model.
	 *
	 * @param template template for content
	 * @param language language of template content
	 * @param model    data which should be merged with template
	 * @return content based on template merged with data
	 */
	public String getContentForTemplate(VelocityTemplate template, I18NEntity.Language language, Map<String, Object> model)
	{
		Objects.requireNonNull(template, "template == null");
		
		final I18NEntity.Language templateLanguage = language == null ? I18NEntity.Language.getDefault() : language;
		Objects.requireNonNull(templateLanguage, "language == null");
		
		try
		{
			return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template.getTemplateLocation(templateLanguage), "UTF-8", model);
		}
		catch (ResourceNotFoundException ex)
		{
			if (templateLanguage != Language.getDefault())
			{
				LOG.warn("no template for {} in {} exists", templateLanguage, template.getTemplateName());
				return getContentForTemplate(template, Language.getDefault(), model);
			}
			else
			{
				LOG.error("no template exists for ", template.getTemplateName());
				return "";
			}
		}
	}
	
	/**
	 * Gets Content based on template for a given language
	 *
	 * @param template template for content
	 * @param language language of template content
	 * @return
	 */
	public String getContentForTemplate(VelocityTemplate template, I18NEntity.Language language)
	{
		return getContentForTemplate(template, language, null);
	}
}
