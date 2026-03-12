package de.binaerebauten.gleichklang.core.migration.querybuilder;

import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestion;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;

/**
 * Created by michael on 06/05/15.
 */
public class QuestionSQLQueryBuilder extends BaseLocalisedSQLQueryBuilder<MigrationQuestion>
{
	@Override
	protected String getTableName()
	{
		return "question";
	}

	@Override
	protected String getTypeSpecificSignature()
	{
		return getQuestionSignature()
				+ ", choice_group_id, selection_type"
				+ ", max_length, number_of_lines"
				+ ", min_value, max_value";
	}

	private String getQuestionSignature()
	{
		return "label, required, question_group_id, questionnaire_id, sort_order, DTYPE, representation_type";
	}

	private String getQuestionValues(MigrationQuestion question)
	{
		return String.format("%s, %s, %s, %s, %d, %s, %s", getStringRepresentation(question.getLabel()),
				getStringRepresentationOfBoolean(question.isRequired()),
				getQuestionGroupIdQuery(question.getQuestionGroup()),
				getQuestionnaireIdQuery(question.getQuestionGroup()), question.getSortOrder(),
				getStringRepresentation(question.getDTYPE().name()),
				getStringRepresentation(question.getRepresentationType()));
	}

	@Override
	protected String getTypeSpecificValues(MigrationQuestion question)
	{
		String choiceGroupIdQuery = "null";
		if(question.getDTYPE().equals(MigrationQuestion.QUESTION_DTYPE.ChoiceQuestion)){
			final String choiceGroupLegacyId = question.getChoiceGroup() == null ? null : question.getChoiceGroup().getLegacyId();
			choiceGroupIdQuery = getChoiceGroupIdQuery(choiceGroupLegacyId);
		}
		else if(question.getDTYPE().equals(MigrationQuestion.QUESTION_DTYPE.BooleanQuestion)){
			choiceGroupIdQuery = getChoiceGroupIdQuery("Ja/Nein");

			question.setSelectionType(ChoiceQuestion.SelectionType.SINGLE);
		}

		return String.format("%s, %s, %s, %s, %s, %s, %s",
				getQuestionValues(question),
				choiceGroupIdQuery,
				getStringRepresentation(question.getSelectionType()),
				question.getMaxLength(),
				question.getNumberOfLines(),
				question.getMinVal(),
				question.getMaxVal()
		);
	}

	private String getChoiceGroupIdQuery(String choiceGroupLegacyId)
	{
		return String.format("(SELECT id FROM choice_group WHERE legacy_id = '%s' ORDER BY id LIMIT 1)",
				choiceGroupLegacyId);
	}

	private String getQuestionGroupIdQuery(MigrationQuestionGroup questionGroup)
	{
		return String.format("(SELECT id FROM question_group WHERE i18n_key = '%s')", questionGroup.getI18nKey());
	}

	private String getQuestionnaireIdQuery(MigrationQuestionGroup questionGroup)
	{
		return String.format("(SELECT questionnaire_id FROM question_group WHERE i18n_key = '%s')",
				questionGroup.getI18nKey());
	}
}

