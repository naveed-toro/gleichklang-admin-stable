package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.migration.queryinserter.I18NInserter;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Locale;

/**
 * Created by michael on 22/07/15.
 */
public class V01_00_13__Insert_Labels implements SpringJdbcMigration
{
	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{

		Integer numberOfUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM compuser", Integer.class);

		if(numberOfUsers == 0){
			return;
		}

		I18NInserter i18NInserter = new I18NInserter(jdbcTemplate);

		i18NInserter.insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName.QUESTIONNAIRE_NAME, Locale.GERMAN,
				Locale.ENGLISH);
		i18NInserter.insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName.QUESTIONNAIRE_DESCRIPTION, Locale.GERMAN,
				Locale.ENGLISH);
		i18NInserter.insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName.QUESTION_GROUP_NAME, Locale.GERMAN,
				Locale.ENGLISH);
		i18NInserter.insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName.QUESTION_GROUP_DESCRIPTION, Locale.GERMAN,
				Locale.ENGLISH);

		i18NInserter.insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName.QUESTION_NAME, Locale.GERMAN, Locale.ENGLISH);
		i18NInserter.insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName.CHOICE_VALUE, Locale.GERMAN, Locale.ENGLISH);
		addBoolChoiceTranslations(jdbcTemplate);

		i18NInserter.insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName.CONTINENT, Locale.GERMAN, Locale.ENGLISH);
		i18NInserter.insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName.COUNTRY, Locale.GERMAN, Locale.ENGLISH);
		i18NInserter.insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName.REGION, Locale.GERMAN, Locale.ENGLISH);

	}

	private void addBoolChoiceTranslations(JdbcTemplate jdbcTemplate)
	{
		jdbcTemplate.execute(
				" INSERT INTO i18n (legacy_id, create_date, language, i18n_key, i18n_value, base_name) "
						+ "VALUES "
						+ "('yes_opt', NOW(), 'DE', 'yes_opt', 'ja', 'CHOICE_VALUE'), "
						+ "('no_opt', NOW(), 'DE', 'no_opt', 'nein', 'CHOICE_VALUE'), "
						+ "('yes_opt', NOW(), 'EN', 'yes_opt' ,'yes' , 'CHOICE_VALUE'), "
						+ "('no_opt', NOW(), 'EN', 'no_opt', 'no', 'CHOICE_VALUE')");
	}
}
