package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.StatisticsView.StatisticsViewListener;
import de.binaerebauten.gleichklang.core.service.RelationshipService.GlobalStatisticData;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.LabelField;

import java.util.Map;

public class StatisticsViewImpl extends AbstractNavigateView<StatisticsViewListener> implements StatisticsView
{
	private final FormPanel layout;
	
	public StatisticsViewImpl()
	{
		layout = new FormPanel("Statistics");
		layout.setMargin(true);
		
		setCompositionRoot(layout);
	}
	
	@Override
	public void setStatistics(Map<GlobalStatisticData, String> statistics)
	{
		layout.removeAllComponents();
		
		statistics.forEach((statisticData, s) -> layout.addComponent(new LabelField(statisticData.toString(), s)));
	}
}
