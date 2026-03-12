package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.NavigationComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionView.SubscriptionViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.GenericViewHeader;

/**
 * Implementation for {@link SubscriptionView}.
 */
public class SubscriptionViewImpl extends AbstractNavigateView<SubscriptionViewListener> implements SubscriptionView
{
	private final NavigationComponent<SubscriptionTab> navigationComponent;
	
	public SubscriptionViewImpl(SubscriptionDetailsView subscriptionDetailsView,
			ProductPurchaseView upgradeOfferPurchaseView,
			ProductPurchaseView serviceOfferPurchaseView,
			ActiveCategoriesView activeCategoriesView,
			RecommendationBreakView recommendationBreakView)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setStyleName(CssStyle.SUBSCRIPTION_VIEW.getStyleName());
		
		navigationComponent = new NavigationComponent<>(SubscriptionTab.class);
		
		navigationComponent.addNavigation(SubscriptionTab.SUBSCRIPTION, subscriptionDetailsView);
		navigationComponent.addNavigation(SubscriptionTab.UPGRADEOFFERS, upgradeOfferPurchaseView);
		navigationComponent.addNavigation(SubscriptionTab.SERVICEOFFERS, serviceOfferPurchaseView);
		navigationComponent.addNavigation(SubscriptionTab.ACTIVECATEGORIES, activeCategoriesView);
		navigationComponent.addNavigation(SubscriptionTab.RECOMMENDATIONBREAK, recommendationBreakView);
		
		layout.addComponents(createHeader(),  navigationComponent);

		setCompositionRoot(layout);
	}
	
	@Override
	public void selectSubNavigation(SubscriptionTab navigationEnum)
	{
		navigationComponent.setSelectedNavigation(navigationEnum);
	}
	
	private Component createHeader()
	{
		final GenericViewHeader header = new GenericViewHeader();
		header.addStyleName(CssStyle.GENERIC_HEADER_BLUE.getStyleName());
		header.setCaption(MemberMenuItem.SUBSCRIPTION.msg());
		header.setDescription(I18N.USERDATA_ABONNEMENT_DESCRIPTION.msg());
		header.setIcon(new ThemeResource("img/cog-outline.svg"));
		
		return header;
	}
	
	@Override
	public void setListener(SubscriptionViewListener listener)
	{
		super.setListener(listener);
		navigationComponent.setSubNavigationListener(listener);
	}
	
	@Override
	public void reset()
	{
		if(!navigationComponent.getAvailableTabs().isEmpty())
		navigationComponent.setSelectedNavigation(navigationComponent.getAvailableTabs().iterator().next());
	}
}