package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.adminweb.view.FilterView.FilterViewListener;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter;
import de.binaerebauten.gleichklang.core.model.filter.TemplateFilter_;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.TableControl;

public class FilterViewImpl extends AbstractNavigateView<FilterViewListener> implements FilterView
{
	private final LazyBeanTable<TemplateFilter> table;

	public FilterViewImpl()
	{
		VerticalLayout layout = new VerticalLayout();
		table = createTemplateFilterTable();

		final TableControl<TemplateFilter> tableControl = new TableControl<>(table);
		tableControl.setNewCallback(() -> fireEvent(FilterViewListener::newTemplateFilter));
		tableControl.setEditCallback(filter -> fireEvent(action -> action.editTemplateFilter(filter)));
		tableControl.setDeleteCallback(filter -> fireEvent(action -> action.deleteTemplateFilter(filter)));
		tableControl.setUndeleteCallback(filter -> fireEvent(action -> action.undeleteTemplateFilter(filter)));
		layout.addComponent(tableControl);
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();

		setCompositionRoot(layout);
	}

	private LazyBeanTable<TemplateFilter> createTemplateFilterTable()
	{
		final LazyBeanTable<TemplateFilter> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();

		table.addContainerProperty("Name", TemplateFilter_.name);

		return table;
	}

	@Override
	public void setTemplateFilterHandler(LazyBeanFilteredItemsHandler<TemplateFilter> handler)
	{
		table.setHandler(handler);
	}
}
