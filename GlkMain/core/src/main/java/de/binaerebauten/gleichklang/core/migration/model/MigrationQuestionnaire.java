package de.binaerebauten.gleichklang.core.migration.model;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;

import java.util.HashSet;
import java.util.Set;

public class MigrationQuestionnaire extends MigrationLocalisedBaseEntity
{

	private RecommendationCategory recommendationCategory;

	private int sortOrder;
	private boolean deleted = false;
	private Set<MigrationQuestionGroup> questionGroups = new HashSet<>();

	public MigrationQuestionnaire(String name, int sortOrder)
	{
		this.setI18nKey(name);
		this.deleted = false;
		this.sortOrder = sortOrder;
		super.setLegacyId(name);
	}

	public RecommendationCategory getRecommendationCategory()
	{
		return recommendationCategory;
	}

	public void setRecommendationCategory(RecommendationCategory recommendationCategory)
	{
		this.recommendationCategory = recommendationCategory;
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

	public Set<MigrationQuestionGroup> getQuestionGroups()
	{
		return questionGroups;
	}

	public void setQuestionGroups(Set<MigrationQuestionGroup> questionGroups)
	{
		this.questionGroups = questionGroups;
	}
}
