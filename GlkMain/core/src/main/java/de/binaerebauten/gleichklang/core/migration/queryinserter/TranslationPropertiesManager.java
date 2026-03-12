package de.binaerebauten.gleichklang.core.migration.queryinserter;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.utils.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

public class TranslationPropertiesManager
{
	protected static String TRANSLATION_PROPERTIES_TEMPLATE = "translations/%s_%s.properties";
	private Properties properties = null;
	private static final Logger LOG = LoggerFactory.getLogger(TranslationPropertiesManager.class);

	public TranslationPropertiesManager(String propertiesName, Locale locale){
		String resourceName;
		try
		{
			resourceName = getLocalizedResourceName(locale, propertiesName);
			properties = PropertiesLoader.getProperties(resourceName);
		}
		catch (Exception e)
		{
			LOG.error("Unknown error: ", e);
		}
	}

	public TranslationPropertiesManager(I18NEntity.BaseName baseName, Locale locale){
		this(baseName.name(), locale);
	}

	private String getLocalizedResourceName(Locale locale, String labelsPropertiesName)
			throws Exception
	{
		String localeString;
		if(Objects.equals(locale, Locale.ENGLISH)){
			localeString = "en";
		}
		else if(Objects.equals(locale, Locale.GERMAN)){
			localeString = "de";
		}
		else
		{
			throw new Exception("Unknown locale: " + locale);
		}
		return String.format(TRANSLATION_PROPERTIES_TEMPLATE, labelsPropertiesName, localeString);
	}

	public Properties getProperties()
	{
		return properties;
	}
}
