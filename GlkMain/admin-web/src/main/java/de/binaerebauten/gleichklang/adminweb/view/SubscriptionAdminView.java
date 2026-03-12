package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.component.SubscriptionTable.SubscriptionHandler;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;

public interface SubscriptionAdminView
		extends NavigateView<SubscriptionAdminView.SubscriptionAdminViewListener>
{
	interface SubscriptionAdminViewListener	extends NavigateView.NavigateViewListener, SubscriptionHandler
	{
	}

	void setSubscriptionHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Subscription> handler);
	
	void setFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder);
}
