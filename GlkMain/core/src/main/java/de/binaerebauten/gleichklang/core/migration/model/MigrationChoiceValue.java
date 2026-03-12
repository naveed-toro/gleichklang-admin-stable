package de.binaerebauten.gleichklang.core.migration.model;

import de.binaerebauten.gleichklang.core.migration.queryinserter.I18NKeyGenerator;

/**
 * Created by michael on 15/04/15.
 */

public class MigrationChoiceValue extends MigrationLocalisedBaseEntity
{

	private MigrationChoiceGroup choiceGroup;
	private int sortOrder;
	private String choice;
	private MigrationQuestion question;

	public MigrationChoiceValue(String parentId, String choice, String value, int sortOrder)
	{
		final String legacyId = String.format("%s.%s", parentId, choice);
		String i18nKey = I18NKeyGenerator.generateI18NKey(legacyId, value);
		this.setChoice(choice);
		this.setLegacyId(legacyId);
		this.setSortOrder(sortOrder);
		this.setI18nKey(i18nKey);
	}

	public MigrationChoiceValue(){

	}
	
	public MigrationChoiceGroup getChoiceGroup()
	{
		return choiceGroup;
	}

	public void setChoiceGroup(MigrationChoiceGroup choiceGroup)
	{
		this.choiceGroup = choiceGroup;
	}

	public void setSortOrder(int sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	public int getSortOrder()
	{
		return sortOrder;
	}

	public String getChoice()
	{
		return choice;
	}

	public void setChoice(String choice)
	{
		this.choice = choice;
	}

	public void setQuestion(MigrationQuestion question)
	{
		this.question = question;
	}

	public MigrationQuestion getQuestion()
	{
		return question;
	}
}
