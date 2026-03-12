package de.binaerebauten.gleichklang.core.migration.querybuilder;

import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionnaire;

public class QuestionGroupSQLQueryBuilder extends BaseLocalisedSQLQueryBuilder<MigrationQuestionGroup>
{
	@Override
	protected String getTypeSpecificSignature()
	{
		return "questionnaire_id, sort_order";
	}

	@Override
	protected String getTypeSpecificValues(MigrationQuestionGroup entity)
	{
		return String.format("%s, %s", getQuestionnaireId(entity.getQuestionnaire()),
				entity.getSortOrder());
	}

	@Override
	protected String getTableName()
	{
		return "question_group";
	}

	private String getQuestionnaireId(MigrationQuestionnaire questionnaire)
	{
		return String.format("(SELECT id from questionnaire where legacy_id = '%s')", questionnaire.getLegacyId());
	}

}
