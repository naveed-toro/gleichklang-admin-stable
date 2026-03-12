package de.binaerebauten.gleichklang.core.migration.querybuilder;

import de.binaerebauten.gleichklang.core.migration.model.MigrationChoiceGroup;

public class ChoiceGroupSQLQueryBuilder extends BaseSQLQueryBuilder<MigrationChoiceGroup>
{
	@Override
	protected String getTypeSpecificSignature(String... args)
	{
		return "name";
	}

	@Override
	protected String getTypeSpecificValues(MigrationChoiceGroup entity, String... args)
	{
		return String.format("%s", getStringRepresentation(entity.getName()));
	}

	@Override
	protected String getTableName()
	{
		return "choice_group";
	}
}
