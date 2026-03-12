package de.binaerebauten.gleichklang.core.migration.querybuilder;

import de.binaerebauten.gleichklang.core.migration.model.MigrationLocalisedBaseEntity;
import de.binaerebauten.gleichklang.core.model.converter.LocalDateTimePersistenceConverter;

import java.util.StringJoiner;

/**
 * Created by michael on 05/05/15.
 */
public abstract class BaseLocalisedSQLQueryBuilder<T extends MigrationLocalisedBaseEntity>
{
	private StringJoiner stringJoiner = new StringJoiner(", ");
	private int count = 0;

	public void addValue(T entity)
	{
		count++;
		stringJoiner.add("(" + getValues(entity) + ")");
	}
	
	public int getCount()
	{
		return count;
	}
	
	public String getSQLQuery()
	{
		return String.format("INSERT INTO %s VALUES %s", getTableSignature(), stringJoiner.toString());
	}

	public boolean isEmpty()
	{
		return stringJoiner.length() == 0;
	}

	public void clear(){
		stringJoiner = new StringJoiner(", ");
	}

	public String getInsertStatementFor(T entity)
	{
		return String.format("INSERT INTO %s VALUES(%s)", getTableSignature(), getValues(entity));
	}

	public String getValues(T entity)
	{
		return getBaseValues(entity) + getTypeSpecificValues(entity);
	}

	protected String getTableSignature()
	{
		return String.format("%s (legacy_id, i18n_key, create_date, %s)", getTableName(), getTypeSpecificSignature());
	}

	protected String getBaseValues(T entity)
	{
		return String.format("%s, %s, %s, ", getStringRepresentation(entity.getLegacyId()),
				getStringRepresentation(entity.getI18nKey()), getStringRepresentation(
						new LocalDateTimePersistenceConverter().convertToDatabaseColumn(entity.getCreateDate())));
	}

	protected String getStringRepresentation(Object object)
	{
		if (object != null)
		{
			if (object instanceof String && ((String) object).contains("\'"))
			{
				object = ((String) object).replace("'", "\\'");
			}
			return String.format("'%s'", object);
		}
		return "null";
	}

	protected String getStringRepresentationOfBoolean(boolean value)
	{
		return value ? "true" : "false";
	}

	protected abstract String getTypeSpecificSignature();

	protected abstract String getTypeSpecificValues(T entity);

	protected abstract String getTableName();

}
