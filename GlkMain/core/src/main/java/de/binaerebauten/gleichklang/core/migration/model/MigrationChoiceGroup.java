package de.binaerebauten.gleichklang.core.migration.model;

import java.util.ArrayList;
import java.util.List;

public class MigrationChoiceGroup extends MigrationBaseEntity
{
	private boolean deleted;

	private List<MigrationChoiceValue> choices = new ArrayList<>();
	private String name;

	public MigrationChoiceGroup(MigrationQuestion question)
	{
		this.setLegacyId(question.getLegacyId());
		this.setDeleted(false);
		question.setChoiceGroup(this);
	}

	public boolean isDeleted()
	{
		return deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}

	public String getName()
	{
		return name;
	}

	public List<MigrationChoiceValue> getChoices()
	{
		return choices;
	}

	public void setChoices(List<MigrationChoiceValue> choices)
	{
		this.choices = choices;
	}

	public void addChoice(MigrationChoiceValue choiceValue)
	{
		choiceValue.setChoiceGroup(this);
		this.choices.add(choiceValue);
	}

	public void addChoices(List<MigrationChoiceValue> choiceValues)
	{
		choiceValues.forEach(this::addChoice);
	}

	public void createName(List<MigrationChoiceValue> choices)
	{
		StringBuilder choiceGroupNameBilder = new StringBuilder();

		for (MigrationChoiceValue choiceValue : choices)
		{
			choiceGroupNameBilder.append(choiceValue.getI18nKey()).append(";");
		}

		this.name = choiceGroupNameBilder.length() > 255 ? choiceGroupNameBilder.substring(0,
				250) : choiceGroupNameBilder.toString();
	}
}
