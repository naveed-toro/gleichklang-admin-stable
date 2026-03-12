package de.binaerebauten.gleichklang.core.migration.querybuilder;

import de.binaerebauten.gleichklang.core.migration.model.MigrationQuestionnaire;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;

/**
 * Created by michael on 30/04/15.
 */
public class QuestionnaireSQLQueryBuilder
		extends BaseLocalisedSQLQueryBuilder<MigrationQuestionnaire>
{
	@Override protected String getTypeSpecificSignature()
	{
		return "recommendation_category, sort_order";
	}

	@Override protected String getTypeSpecificValues(MigrationQuestionnaire entity)
	{
		return String.format("%s, %s", getRecommendationCategory(entity),
				entity.getSortOrder());
	}

	private Object getRecommendationCategory(MigrationQuestionnaire entity)
	{
		final RecommendationCategory recommendationCategory = entity.getRecommendationCategory();
		return	recommendationCategory == null ? "null" : getStringRepresentation(recommendationCategory.name());
	}

	@Override protected String getTableName()
	{
		return "questionnaire";
	}

}
