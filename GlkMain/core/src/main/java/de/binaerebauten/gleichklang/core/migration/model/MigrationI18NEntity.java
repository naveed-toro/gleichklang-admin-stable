package de.binaerebauten.gleichklang.core.migration.model;

import de.binaerebauten.gleichklang.core.model.I18NEntity;

/**
 * Created by michael on 04/05/15.
 */

public class MigrationI18NEntity extends MigrationLocalisedBaseEntity
{
	private String language;

	private String value;

	private I18NEntity.BaseName baseName;

	public  MigrationI18NEntity(String key, I18NEntity.BaseName baseName, String value)
	{
		this.setValue(value);
		this.setBaseName(baseName);
		this.setI18nKey(key);
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public I18NEntity.BaseName getBaseName()
	{
		return baseName;
	}

	public void setBaseName(I18NEntity.BaseName baseName)
	{
		this.baseName = baseName;
	}
}
