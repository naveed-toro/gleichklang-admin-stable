package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionnaire;
import de.binaerebauten.gleichklang.core.migration.querybuilder.QuestionnaireSQLQueryBuilder;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class QuestionnaireSQLQueryBuilderTest
{

	@Test
	public void testQuestionnaireQueryBuilder() throws Exception
	{

		final MigrationQuestionnaire questionnaire = new MigrationQuestionnaire("test", 0);
		questionnaire.setCreateDate(LocalDateTime.of(1, 1, 1, 1, 1));
		String actual = new QuestionnaireSQLQueryBuilder().getInsertStatementFor(questionnaire);
		String expected = "INSERT INTO questionnaire (legacy_id, i18n_key, create_date, recommendation_category, sort_order) VALUES('test', 'test', '0001-01-01 01:01:00.0', null, 0)";
		assertThat(actual, equalTo(expected));
	}

	@Test
	public void testQuestionnaireQueryBuilderWithRecommendationCategory() throws Exception
	{

		final MigrationQuestionnaire questionnaire = new MigrationQuestionnaire("test", 0);
		questionnaire.setCreateDate(LocalDateTime.of(1, 1, 1, 1, 1));
		questionnaire.setRecommendationCategory(RecommendationCategory.FRIENDSHIP);
		String actual = new QuestionnaireSQLQueryBuilder().getInsertStatementFor(questionnaire);
		String expected = "INSERT INTO questionnaire (legacy_id, i18n_key, create_date, recommendation_category, sort_order) VALUES('test', 'test', '0001-01-01 01:01:00.0', 'FRIENDSHIP', 0)";
		assertThat(actual, equalTo(expected));
	}
}
