package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.migration.model.MigrationI18NEntity;
import de.binaerebauten.gleichklang.core.migration.querybuilder.LabelSQLQueryBuilder;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class LabelSQLQueryBuilderTest
{
	@Test
	public void testBuildQuery() throws Exception
	{
		MigrationI18NEntity migrationI18NEntity = new MigrationI18NEntity("test", I18NEntity.BaseName.NONE, "test value");
		migrationI18NEntity.setCreateDate(LocalDateTime.of(1, 1, 1, 1, 1));
		migrationI18NEntity.setLanguage("de");
		String actual = new LabelSQLQueryBuilder().getInsertStatementFor(migrationI18NEntity);
		String expected = "INSERT INTO i18n (legacy_id, i18n_key, create_date, i18n_value, base_name, language) "
				+ "VALUES(null, 'test', '0001-01-01 01:01:00.0', 'test value', 'NONE', 'DE')";
		assertThat(actual, equalTo(expected));
	}
}
