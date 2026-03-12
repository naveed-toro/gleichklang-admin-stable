package de.binaerebauten.gleichklang.core.migration.model;

public class MigrationQuestionGroup extends MigrationLocalisedBaseEntity
{
	private MigrationQuestionnaire questionnaire;
	private int sortOrder;
	private boolean deleted = false;

	public MigrationQuestionGroup(String name, int sortOrder, MigrationQuestionnaire questionnaire){
		this.sortOrder = sortOrder;
		this.setI18nKey(name);
		this.setLegacyId(name);
		this.setQuestionnaire(questionnaire);
	}

	public MigrationQuestionnaire getQuestionnaire()
	{
		return questionnaire;
	}

	public void setQuestionnaire(MigrationQuestionnaire questionnaire)
	{
		this.questionnaire = questionnaire;
	}

	public int getSortOrder()
	{
		return sortOrder;
	}

	public void setSortOrder(int sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	public boolean isDeleted()
	{
		return deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}





	//	insert into question_group(legacy_id, i18n_key, questionnaire_id, deleted, sort_order) VALUES();
}
