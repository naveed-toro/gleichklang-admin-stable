package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.adminweb.view.FilterView;
import de.binaerebauten.gleichklang.adminweb.view.FilterView.FilterViewListener;
import de.binaerebauten.gleichklang.adminweb.view.popup.TemplateFilterPopup;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.TemplateContext;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.repository.TemplateFilterRepository;
import de.binaerebauten.gleichklang.core.service.FilterControlService;
import de.binaerebauten.gleichklang.core.service.FilterService;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class FilterPresenter extends NavigatePresenter implements FilterViewListener
{
	private final TemplateFilterRepository templateFilterRepository;
	private final FilterService filterService;
	private final FilterView view;
	private final DefaultFilterControlHandler filterControlHandler;
	private final FilterSpecificationBuilder filterSpecificationBuilder;

	public FilterPresenter(ApplicationContext ctx, FilterView view)
	{
		super(view);
		
		this.view = view;

		templateFilterRepository = ctx.getBean(TemplateFilterRepository.class);
		filterService = ctx.getBean(FilterService.class);

		filterControlHandler = new DefaultFilterControlHandler(ctx.getBean(FilterControlService.class));
		filterControlHandler.setTemplateContext(TemplateContext.TEMPLATE);
		
		filterSpecificationBuilder = ctx.getBean(FilterSpecificationBuilder.class);

		view.setListener(this);
	}

	@Override
	public void enter(String parameters)
	{
		refresh();
	}

	private void refresh()
	{
		this.view.setTemplateFilterHandler(templateFilterRepository::findAll);
	}

	@Override
	public void leave()
	{
		this.view.setTemplateFilterHandler(null);
		
		super.leave();
	}

	@Override
	public void newTemplateFilter()
	{
		editTemplateFilter(new TemplateFilter());
	}

	@Override
	public void editTemplateFilter(TemplateFilter templateFilter)
	{
		final List<AbstractFilter> oldFilters = filterService.getFilterList(templateFilter);
		final List<TemplateFilter> templateFilters = templateFilterRepository.findAllNotDeleted();
		templateFilters.remove(templateFilter);
		final TemplateFilterPopup popup = new TemplateFilterPopup(
				filterControlHandler, filterSpecificationBuilder, templateFilter,
				filter -> filterService.saveFilter(filter, oldFilters));
		popup.addCloseListener(event -> refresh());
		tryOpenPopup(popup);
	}

	@Override
	public void deleteTemplateFilter(TemplateFilter filter)
	{
		templateFilterRepository.markAsDeleted(filter);
		refresh();
	}

	@Override
	public void undeleteTemplateFilter(TemplateFilter filter)
	{
		templateFilterRepository.restoreDeleted(filter);
		refresh();
	}
}
