package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.migration.queryinserter.I18NInserter;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Locale;

public class V02_61__Migrate_QuestionGroup_Descriptions implements SpringJdbcMigration
{
	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		jdbcTemplate.execute("DELETE FROM i18n WHERE base_name = 'QUESTION_GROUP_DESCRIPTION'");

		I18NInserter i18NInserter = new I18NInserter(jdbcTemplate);
		i18NInserter.insertLabelsFromPropertiesForExistingElements(I18NEntity.BaseName.QUESTION_GROUP_DESCRIPTION, Locale.GERMAN,
				Locale.ENGLISH);
	}
}
