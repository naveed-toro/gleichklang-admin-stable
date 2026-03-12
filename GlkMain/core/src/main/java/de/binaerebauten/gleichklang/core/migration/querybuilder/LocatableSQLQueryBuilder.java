package de.binaerebauten.gleichklang.core.migration.querybuilder;

import de.binaerebauten.gleichklang.core.migration.model.MigrationLocatableEntity;

import java.util.Locale;

public class LocatableSQLQueryBuilder extends BaseLocalisedSQLQueryBuilder<MigrationLocatableEntity>
{
	@Override
	protected String getTypeSpecificSignature()
	{
		return "DTYPE, zip, latitude, longitude, parent_id, region_name, region_id, sort_order";
	}

	@Override
	protected String getTypeSpecificValues(MigrationLocatableEntity entity)
	{
		return String.format(Locale.ENGLISH, "%s, %s, %.2f, %.2f, %d, %s, %d, %d",
				getStringRepresentation(entity.getType().name()),
				getStringRepresentation(entity.getZip()),
				entity.getLatitude(),
				entity.getLongitude(),
				entity.getParentId(),
				getStringRepresentation(entity.getRegionName()),
				entity.getRegionId(),
				entity.getSortOrder());
	}

	@Override
	protected String getTableName()
	{
		return "locatable";
	}
}
