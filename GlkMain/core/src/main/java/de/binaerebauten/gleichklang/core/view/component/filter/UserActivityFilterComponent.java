package de.binaerebauten.gleichklang.core.view.component.filter;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.UserActivityFilter;
import de.binaerebauten.gleichklang.core.model.filter.UserActivityFilter_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;

public class UserActivityFilterComponent extends CustomComponent implements FilterComponent
{
	private final ComponentGroup<UserActivityFilter> userActivityFilterComponentGroup;

	public UserActivityFilterComponent(UserActivityFilter filter)
	{
		userActivityFilterComponentGroup = new ComponentGroup<>(UserActivityFilter.class, filter);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		layout.addComponent(userActivityFilterComponentGroup.buildAndBind("Ereignis", UserActivityFilter_.userActivity));
		layout.addComponent(userActivityFilterComponentGroup.buildAndBind("von", UserActivityFilter_.fromDate));
		layout.addComponent(userActivityFilterComponentGroup.buildAndBind("bis", UserActivityFilter_.toDate));
		layout.addComponent(userActivityFilterComponentGroup.buildAndBind("Kategorie", UserActivityFilter_.category));

		setCompositionRoot(layout);
	}

	@Override
	public void commit() throws CommitException
	{
		userActivityFilterComponentGroup.commit();
	}

	@Override
	public AbstractFilter getFilter()
	{
		return userActivityFilterComponentGroup.getItemDataSource().getBean();
	}
}
