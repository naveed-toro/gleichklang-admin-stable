package de.binaerebauten.gleichklang.core.migration.queryinserter;

import java.util.Locale;

public class I18NKeyGenerator
{
	final static TranslationPropertiesManager TRANSLATION_PROPERTIES_MANAGER = new TranslationPropertiesManager("synonyms", Locale.GERMAN);

	public static String generateI18NKey(String oldKey, String value){
		String i18nKey = value.trim().replace(" ", "_").replace("-", "");
		if (i18nKey.length() > 20)
		{
			i18nKey = i18nKey.substring(0, 15) + "..." + i18nKey.substring(i18nKey.length()-15);
		}
		i18nKey = i18nKey.toLowerCase();

		String synonym = TRANSLATION_PROPERTIES_MANAGER.getProperties().getProperty(i18nKey);
		final String[] split = oldKey.split("\\.");


		if(split.length > 1){
			Object suffixValue = TRANSLATION_PROPERTIES_MANAGER.getProperties().get(split[1]);

			if(synonym != null && suffixValue != null){
				i18nKey = (String) suffixValue;
			}
		}

		return i18nKey;
	}

}
