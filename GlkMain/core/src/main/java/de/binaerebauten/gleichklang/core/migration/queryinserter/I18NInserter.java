package de.binaerebauten.gleichklang.core.migration.queryinserter;

import de.binaerebauten.gleichklang.core.migration.model.MigrationI18NEntity;
import de.binaerebauten.gleichklang.core.migration.querybuilder.LabelSQLQueryBuilder;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Created by michael on 06/05/15.
 */
public class I18NInserter extends BaseInserter
{

	private static final Logger LOG = LoggerFactory.getLogger(I18NInserter.class);

	public I18NInserter(JdbcTemplate jdbcTemplate)
	{
		super(jdbcTemplate);
	}

	/**
	 * Inserts labels from propertiesName to the I18N Table
	 *
	 * @param baseName is first string parameter for labelsProperties for template: <code>translations/%s_%s
	 *                 .properties<code/>
	 * @param locales
	 */
	public void insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName baseName, Locale... locales)
	{
		List<Map<String, Object>> sourceKeys = getSourceKeys(baseName);

		LabelSQLQueryBuilder labelSQLQueryBuilder = new LabelSQLQueryBuilder();


		String propertiesName = baseName.name();
		for (Locale locale : locales)
		{
			final Properties localeProperties = getProperties(baseName, propertiesName, locale);

			for (Map<String, Object> choiceKeyEntry : sourceKeys)
			{
				String sourceI18nKey = String.valueOf(choiceKeyEntry.get("i18n_key"));
				String sourceLegacyId = String.valueOf(choiceKeyEntry.get("legacy_id"));

				String value = localeProperties.getProperty(sourceLegacyId);
				if (value != null)
				{
					value = normaliseValue(value);
					MigrationI18NEntity i18NEntity = new MigrationI18NEntity(sourceI18nKey, baseName, value);
					i18NEntity.setLegacyId(sourceLegacyId);
					i18NEntity.setLanguage(locale.getLanguage());
					i18NEntity.setI18nKey(sourceI18nKey);
					labelSQLQueryBuilder.addValue(i18NEntity);
				}
			}

			String insertQuery = labelSQLQueryBuilder.getSQLQuery();

			int result = 0;
			if (!labelSQLQueryBuilder.isEmpty())
			{
				labelSQLQueryBuilder.clear();
				result = executeOne(insertQuery);
			}
			LOG.info("{} labels were inserted for {}", result, baseName, locale);

		}
	}

	private String normaliseValue(String value)
	{
		return value.replaceAll("\\[<", "<").replaceAll(">]", ">").replaceAll("\n", "<br/>");
	}

	private List<Map<String, Object>> getSourceKeys(I18NEntity.BaseName baseName)
	{
		String sourceTableName = getSourceTableName(baseName);
		String sourceQuery = "SELECT DISTINCT i18n_key, legacy_id FROM " + sourceTableName;
		if (baseName == I18NEntity.BaseName.CONTINENT
				|| baseName == I18NEntity.BaseName.COUNTRY
				|| baseName == I18NEntity.BaseName.REGION)
		{
			sourceQuery += " WHERE DTYPE = '" + getDtype(baseName) + "'";
		}

		return jdbcTemplate.queryForList(sourceQuery);
	}





	private Properties getProperties(I18NEntity.BaseName baseName, String propertiesName, Locale locale)
	{
		final Properties localeProperties = new TranslationPropertiesManager(propertiesName, locale).getProperties();

		localeProperties.stringPropertyNames().forEach(oldKey -> {
			String propertyValue = localeProperties.getProperty(oldKey);
			String newKey = propertyToI18nKey(oldKey, baseName);
			localeProperties.remove(oldKey);
			localeProperties.put(newKey, propertyValue);
		});

		return localeProperties;
	}



	private String getDtype(I18NEntity.BaseName baseName)
	{
		switch (baseName)
		{
			case CONTINENT:
				return "Continent";
			case COUNTRY:
				return "Country";
			case REGION:
				return "Region";
		}
		return null;
	}

	private String getSourceTableName(I18NEntity.BaseName baseName)
	{
		switch (baseName)
		{
			case QUESTIONNAIRE_NAME:
			case QUESTIONNAIRE_DESCRIPTION:
				return "questionnaire";
			case QUESTION_GROUP_NAME:
			case QUESTION_GROUP_DESCRIPTION:
				return "question_group";
			case QUESTION_NAME:
			case QUESTION_DESCRIPTION:
				return "question";
			case CHOICE_VALUE:
				return "choice";
			case CONTINENT:
			case COUNTRY:
			case REGION:
				return "locatable";
		}
		return null;
	}

	private String propertyToI18nKey(String propertyKey, I18NEntity.BaseName baseName)
	{
		switch (baseName)
		{
			case QUESTIONNAIRE_NAME:
				return propertyKey.replace("title.title_", "");
			case QUESTIONNAIRE_DESCRIPTION:
				return propertyKey.replace("text.instruktion_", "");
			case QUESTION_GROUP_NAME:
			case QUESTION_GROUP_DESCRIPTION:
				return propertyKey.replace("fieldset.", "").toLowerCase();
			case CONTINENT:
				return propertyKey.replace("continents.", "");
			case COUNTRY:
				return propertyKey.replace("countries.", "");
			case REGION:
				return propertyKey.replace("states.", "");
			case QUESTION_NAME:
				return propertyKey;
		}

		return propertyKey;
	}


}
