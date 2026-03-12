package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.adminweb.view.StatisticsView;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.RelationshipService;
import org.springframework.context.ApplicationContext;

public class StatisticsPresenter extends NavigatePresenter implements StatisticsView.StatisticsViewListener
{
	private final RelationshipService relationshipService;
	private final StatisticsView view;
	
	public StatisticsPresenter(ApplicationContext ctx, StatisticsView view)
	{
		super(view);
		
		this.view = view;
		
		relationshipService = ctx.getBean(RelationshipService.class);
		
		this.view.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		this.view.setStatistics(relationshipService.getStatistics());
	}
}
