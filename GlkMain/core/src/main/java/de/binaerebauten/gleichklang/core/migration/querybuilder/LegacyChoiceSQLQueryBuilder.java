package de.binaerebauten.gleichklang.core.migration.querybuilder;

import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceValue;
import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestion;

public class LegacyChoiceSQLQueryBuilder extends BaseLocalisedSQLQueryBuilder<MigrationChoiceValue>
{

	@Override
	protected String getTypeSpecificSignature()
	{
		return "question_id, choice_id, choice, "
				+ "question_legacy_id";
	}

	@Override
	protected String getTypeSpecificValues(MigrationChoiceValue entity)
	{
		return String.format("%s, %s, %s, %s",
				getQuestionId(entity.getQuestion()),
				getChoiceId(entity.getI18nKey()),
				getStringRepresentation(entity.getChoice()),
				getStringRepresentation(entity.getQuestion().getLegacyId())
		);
	}

	@Override
	protected String getTableName()
	{
		return "legacy_choice";
	}

	private String getQuestionId(MigrationQuestion question)
	{
		if(question == null){
			return "null";
		}
		return String.format("(SELECT id FROM question WHERE legacy_id = '%s' ORDER BY id LIMIT 1)",
				question.getLegacyId());
	}

	private String getChoiceId(String i18nKey)
	{
		return String.format("(SELECT id FROM choice WHERE i18n_key = '%s' ORDER BY id LIMIT 1)",
				i18nKey);
	}
}
