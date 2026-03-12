package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceValue;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestion;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionnaire;
import de.binaerebauten.gleichklang.core.migration.querybuilder.ChoiceGroupSQLQueryBuilder;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ChoiceGroupSQLQueryBuilderTest
{

	@Test
	public void testBuildQuery() throws Exception
	{
		MigrationQuestionnaire questionnaire = new MigrationQuestionnaire("q1", 0);
		MigrationQuestionGroup questionGroup = new MigrationQuestionGroup("g", 0, questionnaire);
		MigrationQuestion migrationQuestion = new MigrationQuestion("q2", questionGroup, 0);
		MigrationChoiceGroup migrationChoiceGroup = new MigrationChoiceGroup(migrationQuestion);

		MigrationChoiceValue migrationChoiceValue = new MigrationChoiceValue("q2", "test g", "test g", 0);
		migrationChoiceGroup.addChoice(migrationChoiceValue);

		migrationChoiceGroup.setCreateDate(LocalDateTime.of(1, 1, 1, 1, 1));
		String expected = "INSERT INTO choice_group (legacy_id, create_date, name) VALUES('q1.q2', "
				+ "'0001-01-01 01:01:00.0', null)";
		String actual = new ChoiceGroupSQLQueryBuilder().getInsertStatementFor(migrationChoiceGroup);
		assertThat(actual, equalTo(expected));
	}
	
}
