package de.binaerebauten.gleichklang.core.migration.querybuilder;

import de.binaerebauten.gleichklang.core.migration.model.MigrationI18NEntity;

/**
 * Created by michael on 05/05/15.
 */
public class LabelSQLQueryBuilder
		extends BaseLocalisedSQLQueryBuilder<MigrationI18NEntity>
{

	@Override protected String getTypeSpecificSignature()
	{
		return "i18n_value, base_name, language";
	}

	@Override protected String getTypeSpecificValues(MigrationI18NEntity entity)
	{
		return String.format("%s, %s, %s",
				getStringRepresentation(entity.getValue()),
				getStringRepresentation(entity.getBaseName().name()),
				getStringRepresentation(entity.getLanguage().toUpperCase()));
	}

	@Override protected String getTableName()
	{
		return "i18n";
	}

}
