package de.binaerebauten.gleichklang.core.migration;

import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceValue;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestion;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionnaire;
import de.binaerebauten.gleichklang.core.migration.querybuilder.QuestionSQLQueryBuilder;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.TextQuestion;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class QuestionSQLQueryBuilderTest
{

	@Test
	public void testTextQuestionQueryBuilder() throws Exception
	{
		MigrationQuestionnaire questionnaire = new MigrationQuestionnaire("test_q", 0);
		MigrationQuestionGroup questionGroup = new MigrationQuestionGroup("test_qg", 0, questionnaire);

		final MigrationQuestion question = new MigrationQuestion("test", questionGroup, 0);
		question.setCreateDate(LocalDateTime.of(1, 1, 1, 1, 1));
		question.setMaxLength(1);
		question.setNumberOfLines(2);
		question.setDTYPE(MigrationQuestion.QUESTION_DTYPE.TextQuestion);
		question.setRequired(true);
		question.setRepresentationType(TextQuestion.RepresentationType.DEFAULT.name());
		String actual = new QuestionSQLQueryBuilder().getInsertStatementFor(question);

		String expected = "INSERT INTO question (legacy_id, i18n_key, create_date, label, required, question_group_id, questionnaire_id, sort_order, DTYPE, representation_type, choice_group_id, selection_type, max_length, number_of_lines, min_value, max_value) VALUES('test_q.test', 'test_q.test', '0001-01-01 01:01:00.0', 'test', true, (SELECT id FROM question_group WHERE i18n_key = 'test_qg'), (SELECT questionnaire_id FROM question_group WHERE i18n_key = 'test_qg'), 0, 'TextQuestion', 'DEFAULT', null, null, 1, 2, 0, 0)";

		assertThat(actual, equalTo(expected));
	}

	@Test
	public void testNumberQuestionQueryBuilder() throws Exception
	{
		MigrationQuestionnaire questionnaire = new MigrationQuestionnaire("test_q", 0);
		MigrationQuestionGroup questionGroup = new MigrationQuestionGroup("test_qg", 0, questionnaire);

		final MigrationQuestion question = new MigrationQuestion("test", questionGroup, 0);
		question.setCreateDate(LocalDateTime.of(1, 1, 1, 1, 1));
		question.setMinVal(1);
		question.setMaxVal(2);
		question.setDTYPE(MigrationQuestion.QUESTION_DTYPE.NumberQuestion);
		question.setRequired(true);
		question.setRepresentationType(TextQuestion.RepresentationType.DEFAULT.name());
		String actual = new QuestionSQLQueryBuilder().getInsertStatementFor(question);

		String expected = "INSERT INTO question (legacy_id, i18n_key, create_date, label, required, question_group_id, questionnaire_id, sort_order, DTYPE, representation_type, choice_group_id, selection_type, max_length, number_of_lines, min_value, max_value) VALUES('test_q.test', 'test_q.test', '0001-01-01 01:01:00.0', 'test', true, (SELECT id FROM question_group WHERE i18n_key = 'test_qg'), (SELECT questionnaire_id FROM question_group WHERE i18n_key = 'test_qg'), 0, 'NumberQuestion', 'DEFAULT', null, null, 0, 0, 1, 2)";

		assertThat(actual, equalTo(expected));
	}

	@Test
	public void testChoiceQuestionQueryBuilder() throws Exception
	{
		MigrationQuestionnaire questionnaire = new MigrationQuestionnaire("test_q", 0);
		MigrationQuestionGroup questionGroup = new MigrationQuestionGroup("test_qg", 0, questionnaire);
		final MigrationQuestion question = new MigrationQuestion("test", questionGroup, 0);
		MigrationChoiceGroup choiceGroup = new MigrationChoiceGroup(question);
		MigrationChoiceValue choiceValue = new MigrationChoiceValue("test", "test cg", "test cg", 0);
		choiceGroup.addChoice(choiceValue);

		question.setCreateDate(LocalDateTime.of(1, 1, 1, 1, 1));
		question.setRepresentationType(ChoiceQuestion.RepresentationType.DEFAULT.name());
		question.setSelectionType(ChoiceQuestion.SelectionType.SINGLE);
		question.setDTYPE(MigrationQuestion.QUESTION_DTYPE.ChoiceQuestion);
		question.setRequired(true);
		question.setChoiceGroup(choiceGroup);
		question.setRepresentationType(TextQuestion.RepresentationType.DEFAULT.name());
		String actual = new QuestionSQLQueryBuilder().getInsertStatementFor(question);

		String expected = "INSERT INTO question (legacy_id, i18n_key, create_date, label, required, question_group_id, questionnaire_id, sort_order, DTYPE, representation_type, choice_group_id, selection_type, max_length, number_of_lines, min_value, max_value) VALUES('test_q.test', 'test_q.test', '0001-01-01 01:01:00.0', 'test', true, (SELECT id FROM question_group WHERE i18n_key = 'test_qg'), (SELECT questionnaire_id FROM question_group WHERE i18n_key = 'test_qg'), 0, 'ChoiceQuestion', 'DEFAULT', (SELECT id FROM choice_group WHERE legacy_id = 'test_q.test' ORDER BY id LIMIT 1), 'SINGLE', 0, 0, 0, 0)";

		assertThat(actual, equalTo(expected));
	}

}