package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.StatisticsView.StatisticsViewListener;
import de.binaerebauten.gleichklang.core.service.RelationshipService.GlobalStatisticData;
import de.binaerebauten.gleichklang.core.view.NavigateView;

import java.util.Map;

public interface StatisticsView extends NavigateView<StatisticsViewListener>
{
	interface StatisticsViewListener extends NavigateView.NavigateViewListener
	{
	}
	
	void setStatistics(Map<GlobalStatisticData, String> statistics);
}
