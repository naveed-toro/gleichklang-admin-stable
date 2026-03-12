package de.binaerebauten.gleichklang.core.migration.model;

public class MigrationLocalisedBaseEntity extends MigrationBaseEntity
{
	protected String i18nKey;

	public String getI18nKey()
	{
		return i18nKey;
	}

	public void setI18nKey(String i18nKey)
	{
		this.i18nKey = i18nKey;
	}
}
