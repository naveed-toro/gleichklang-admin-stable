package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.service.RelationshipService;
import de.binaerebauten.gleichklang.core.view.NavigateView;

import java.util.Map;

public interface StatisticView extends NavigateView<StatisticView.StatisticViewListener>
{
	interface StatisticViewListener extends NavigateView.NavigateViewListener
	{
	}

	void setNoOfMatches(Map<RecommendationCategory, Long> values);

	void setNoOfReceivedMessages(Map<RecommendationCategory, Long> values);

	void setNoOfSentMessages(Map<RecommendationCategory, Long> values);

	void setStatistics(Map<RelationshipService.StatisticData, String> statistics);
}
