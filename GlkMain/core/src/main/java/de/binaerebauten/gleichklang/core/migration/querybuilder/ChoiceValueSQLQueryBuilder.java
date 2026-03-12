package de.binaerebauten.gleichklang.core.migration.querybuilder;

import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceGroup;
import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceValue;

/**
 * Created by michael on 06/05/15.
 */
public class ChoiceValueSQLQueryBuilder
		extends BaseLocalisedSQLQueryBuilder<MigrationChoiceValue>
{

	@Override protected String getTypeSpecificSignature()
	{
		return "choice, choice_group_id, question_id, sort_order";
	}

	@Override protected String getTypeSpecificValues(MigrationChoiceValue entity)
	{
		return String.format("%s, %s, %s, %d",
				getStringRepresentation(entity.getChoice()),
				getChoiceGroupId(entity.getChoiceGroup()),
				getQuestionId(entity.getChoiceGroup()),
				entity.getSortOrder());
	}

	private String getChoiceGroupId(MigrationChoiceGroup choiceGroup)
	{
		return String.format("(SELECT id FROM choice_group WHERE legacy_id = '%s' ORDER BY id LIMIT 1)", choiceGroup.getLegacyId());
	}

	private String getQuestionId(MigrationChoiceGroup choiceGroup)
	{
		return String.format("(SELECT id FROM question WHERE legacy_id = '%s' ORDER BY id LIMIT 1)",
				choiceGroup.getLegacyId());
	}

	@Override protected String getTableName()
	{
		return "choice";
	}

}
