package de.binaerebauten.gleichklang.memberweb.presenter;

import de.binaerebauten.gleichklang.core.model.payment.ServiceOffer;
import de.binaerebauten.gleichklang.core.model.payment.UpgradeOffer;
import de.binaerebauten.gleichklang.core.presenter.SubNavigatePresenter;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionView;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionView.SubscriptionTab;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionView.SubscriptionViewListener;

public class SubscriptionPresenter extends SubNavigatePresenter<SubscriptionTab> implements SubscriptionViewListener
{
	private final SubscriptionDetailsPresenter subscriptionDetailsPresenter;
	private final ProductPurchasePresenter<UpgradeOffer> upgradeOfferProductPurchasePresenter;
	private final ProductPurchasePresenter<ServiceOffer> serviceOfferPurchasePresenter;
	private final ActiveCategoriesPresenter activeCategoriesPresenter;
	private final RecommendationBreakPresenter recommendationBreakPresenter;
	
	public SubscriptionPresenter(SubscriptionView subscriptionView,
			SubscriptionDetailsPresenter subscriptionDetailsPresenter,
			ProductPurchasePresenter<UpgradeOffer> upgradeOfferProductPurchasePresenter,
			ProductPurchasePresenter<ServiceOffer> serviceOfferPurchasePresenter,
			ActiveCategoriesPresenter activeCategoriesPresenter,
			RecommendationBreakPresenter recommendationBreakPresenter)
	{
		super(subscriptionView, SubscriptionTab.class, MemberMenuItem.SUBSCRIPTION);
		
		subscriptionView.setListener(this);
		
		this.subscriptionDetailsPresenter = subscriptionDetailsPresenter;
		this.upgradeOfferProductPurchasePresenter = upgradeOfferProductPurchasePresenter;
		this.serviceOfferPurchasePresenter = serviceOfferPurchasePresenter;
		this.activeCategoriesPresenter = activeCategoriesPresenter;
		this.recommendationBreakPresenter = recommendationBreakPresenter;
	}
	
	@Override
	public void enter(SubscriptionTab navigationEnum, String parameters)
	{
		switch(navigationEnum)
		{
			case SUBSCRIPTION:
				subscriptionDetailsPresenter.enter(parameters);
				break;
			case UPGRADEOFFERS:
				upgradeOfferProductPurchasePresenter.enter(parameters);
				break;
			case SERVICEOFFERS:
				serviceOfferPurchasePresenter.enter(parameters);
				break;
			case ACTIVECATEGORIES:
				activeCategoriesPresenter.enter(parameters);
				break;
			case RECOMMENDATIONBREAK:
				recommendationBreakPresenter.enter(parameters);
				break;
		}
	}
	
	@Override
	public void leave()
	{
		subscriptionDetailsPresenter.leave();
		upgradeOfferProductPurchasePresenter.leave();
		serviceOfferPurchasePresenter.leave();
		activeCategoriesPresenter.leave();
		recommendationBreakPresenter.leave();
		
		super.leave();
	}
}
