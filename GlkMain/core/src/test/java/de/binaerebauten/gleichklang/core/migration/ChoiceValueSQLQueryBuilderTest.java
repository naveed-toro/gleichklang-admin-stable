package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceValue;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestion;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionnaire;
import de.binaerebauten.gleichklang.core.migration.querybuilder.ChoiceValueSQLQueryBuilder;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ChoiceValueSQLQueryBuilderTest
{

	@Test
	public void testBuildQuery() throws Exception
	{

		final ChoiceValueSQLQueryBuilder choiceValueSQLQueryBuilder = new ChoiceValueSQLQueryBuilder();

		MigrationQuestionnaire questionnaire = new MigrationQuestionnaire("q1", 0);
		MigrationQuestionGroup questionGroup = new MigrationQuestionGroup("g", 0, questionnaire);
		MigrationQuestion migrationQuestion = new MigrationQuestion("q2", questionGroup, 0);
		MigrationChoiceGroup migrationChoiceGroup = new MigrationChoiceGroup(migrationQuestion);

		MigrationChoiceValue migrationChoiceValue = new MigrationChoiceValue("q2", "test_key", "test g", 0);
		migrationChoiceValue.setCreateDate(LocalDateTime.of(1, 1, 1, 1, 1));
		migrationChoiceGroup.addChoice(migrationChoiceValue);

		String expected = "INSERT INTO choice (legacy_id, i18n_key, create_date, choice, choice_group_id, "
				+ "question_id, sort_order) VALUES "
				+ "('q2.test_key', 'test_g', '0001-01-01 01:01:00.0', 'test_key', (SELECT id FROM choice_group WHERE "
				+ "legacy_id = 'q1.q2' ORDER BY id LIMIT 1), (SELECT id FROM question WHERE legacy_id = 'q1.q2' ORDER BY id LIMIT 1), 0)";

		choiceValueSQLQueryBuilder.addValue(migrationChoiceValue);

		String actual = choiceValueSQLQueryBuilder.getSQLQuery();
		assertThat(actual, equalTo(expected));
	}
	
}
