package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionnaire;
import de.binaerebauten.gleichklang.core.migration.querybuilder.QuestionGroupSQLQueryBuilder;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class QuestionGroupSQLQueryBuilderTest
{
	@Test
	public void testBuildQuery() throws Exception
	{
		MigrationQuestionnaire migrationQuestionnaire = new MigrationQuestionnaire("test", 0);
		MigrationQuestionGroup migrationQuestionGroup = new MigrationQuestionGroup("test", 0, migrationQuestionnaire);
		migrationQuestionGroup.setCreateDate(LocalDateTime.of(1, 1, 1, 1, 1));
		String actual = new QuestionGroupSQLQueryBuilder().getInsertStatementFor(migrationQuestionGroup);
		String expected = "INSERT INTO question_group (legacy_id, i18n_key, create_date, questionnaire_id, sort_order) VALUES('test', 'test', '0001-01-01 01:01:00.0', (SELECT id from questionnaire where legacy_id = 'test'), 0)";
		assertThat(actual, equalTo(expected));
	}
}
