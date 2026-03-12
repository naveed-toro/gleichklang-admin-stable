package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.FilterView.FilterViewListener;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;

public interface FilterView extends NavigateView<FilterViewListener>
{
	interface FilterViewListener extends NavigateView.NavigateViewListener
	{
		void newTemplateFilter();

		void editTemplateFilter(TemplateFilter filter);

		void deleteTemplateFilter(TemplateFilter filter);

		void undeleteTemplateFilter(TemplateFilter filter);
	}

	void setTemplateFilterHandler(LazyBeanFilteredItemsHandler<TemplateFilter> handler);
}
