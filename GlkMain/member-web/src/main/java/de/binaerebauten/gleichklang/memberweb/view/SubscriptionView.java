package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.SubNavigateView;
import de.binaerebauten.gleichklang.core.view.component.NavigationComponent.SubNavigationListener;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionView.SubscriptionTab;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionView.SubscriptionViewListener;

/**
 * This is the parent view of the {@link SubscriptionDetailsView} and the {@link ProductPurchaseView}s
 * for purchasing upgrade offers and service offers.
 */
public interface SubscriptionView extends SubNavigateView<SubscriptionTab, SubscriptionViewListener>, ResetableView
{
	enum SubscriptionTab implements DefaultEnumI18N, NavigationEnum
	{
		SUBSCRIPTION("subscription"),
		UPGRADEOFFERS("upgrade_offers"),
		SERVICEOFFERS("service_offers"),
		ACTIVECATEGORIES("active_categories"),
		RECOMMENDATIONBREAK("recommendation_break");
		
		private String path;
		
		SubscriptionTab(String path)
		{
			this.path = path;
		}
		
		@Override
		public String toString()
		{
			return msg();
		}
		
		@Override
		public String getPath()
		{
			return path;
		}
	}
	
	interface SubscriptionViewListener extends NavigateView.NavigateViewListener, SubNavigationListener
	{
	}
}
