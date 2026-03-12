package de.binaerebauten.gleichklang.core.migration.querybuilder;

import de.binaerebauten.gleichklang.core.migration.model.MigrationBaseEntity;
import de.binaerebauten.gleichklang.core.model.converter.LocalDateTimePersistenceConverter;

import java.util.StringJoiner;

public abstract class BaseSQLQueryBuilder<T extends MigrationBaseEntity>
{
	private final StringJoiner stringJoiner = new StringJoiner(", ");

	public void addValue(T entity)
	{
		stringJoiner.add("(" + getValues(entity) + ")");
	}

	public String getSQLQuery()
	{
		if(stringJoiner.length() == 0){
			return "";
		}
		return String.format("INSERT INTO %s VALUES %s", getTableSignature(), stringJoiner.toString());
	}

	public String getValues(T entity)
	{
		return getBaseValues(entity) + getTypeSpecificValues(entity);
	}

	public String getInsertStatementFor(T entity, String... args)
	{
		return String.format("INSERT INTO %s VALUES(%s)", getTableSignature(args),
				getBaseValues(entity) + getTypeSpecificValues(entity, args));
	}

	protected String getTableSignature(String... args)
	{
		return String.format("%s (legacy_id, create_date, %s)", getTableName(), getTypeSpecificSignature(args));
	}

	protected String getBaseValues(T entity)
	{
		return String.format("%s, %s, ", getStringRepresentation(entity.getLegacyId()), getStringRepresentation(
				new LocalDateTimePersistenceConverter().convertToDatabaseColumn(entity.getCreateDate())));
	}

	protected Object getStringRepresentation(Object object)
	{
		if (object != null)
		{
			if (object instanceof String && ((String) object).contains("\'"))
			{
				object = ((String) object).replace("'", "\\'");
			}
			return String.format("'%s'", object);
		}
		return null;
	}

	protected abstract String getTypeSpecificSignature(String... args);

	protected abstract String getTypeSpecificValues(T entity, String... args);

	protected abstract String getTableName();
}
