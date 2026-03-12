package de.binaerebauten.gleichklang.memberweb.navigation;

import com.vaadin.ui.ComponentContainer;
import de.binaerebauten.gleichklang.core.DeactivateProlongationPresenter;
import de.binaerebauten.gleichklang.core.model.payment.ServiceOffer;
import de.binaerebauten.gleichklang.core.model.payment.UpgradeOffer;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionnaireActivation;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.navigation.AbstractNavigator.ViewItem;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.view.DeactivateProlongationViewImpl;
import de.binaerebauten.gleichklang.core.view.component.MenuItem;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import de.binaerebauten.gleichklang.memberweb.presenter.*;
import de.binaerebauten.gleichklang.memberweb.view.*;
import de.binaerebauten.gleichklang.memberweb.view.filter.ServiceOfferFilter;
import de.binaerebauten.gleichklang.memberweb.view.filter.UpgradeOfferFilter;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

import static de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem.*;

public class DefaultNavigatorFactory
{
	public enum MemberMenuItem implements DefaultEnumI18N, NavigationEnum
	{
		HOME,
		MEMBER_ACCOUNT,
		DATING,
		USER_DATA,
		MEDIA,
		MESSAGE_TO_GLEICHKLANG,
		STATISTIC,
		SUBSCRIPTION,
		RELATIONSHIP,
		LAST_RELATIONSHIP,
		MESSAGES,
		AUDIO,
		CHAT,
		DEACTIVATE_PROLONGATION;
		
		@Override
		public String toString()
		{
			return msg();
		}
		
		@Override
		public String getPath()
		{
			return name();
		}
	}
	
	private class MemberViewItem implements ViewItem
	{
		private final MemberMenuItem memberMenuItem;
		
		public MemberViewItem(MemberMenuItem memberMenuItem)
		{
			this.memberMenuItem = memberMenuItem;
		}
		
		@Override
		public String getKey()
		{
			return memberMenuItem.getPath();
		}
		
		@Override
		public String getCaption()
		{
			return memberMenuItem.toString();
		}
		
		@Override
		public NavigatePresenter getPresenter(Device device)
		{
			return DefaultNavigatorFactory.this.getPresenter(memberMenuItem, device);
		}
	}
	
	/**
	 * Uri fragment for the Subscription view.
	 * Used for redirecting heidelpay request.
	 */
	public static final MemberMenuItem SUBSCRIPTION_VIEW = MemberMenuItem.SUBSCRIPTION;
	
	private final ApplicationContext ctx;
	
	private QuestionnaireActivation activation;
	private DefaultNavigator navigator;
	private UserService userService;


	
	public DefaultNavigatorFactory(ApplicationContext ctx)
	{
		Objects.requireNonNull(ctx);
		
		this.ctx = ctx;
		userService = ctx.getBean(UserService.class);
	}
	
	public DefaultNavigator buildNavigator(ComponentContainer container)
	{
		final DefaultNavigator navigator = new DefaultNavigator(container);
		
		createMenu(navigator);
		
		final QuestionnaireNavigationConfigurator questionnaireNavigationConfigurator = new QuestionnaireNavigationConfigurator(ctx, navigator);
		questionnaireNavigationConfigurator.createMenusForCategories(false);
		this.navigator = navigator;
		this.activation = questionnaireNavigationConfigurator.getActivation();
		
		//home
		navigator.setStartViewItem(new MemberViewItem(MemberMenuItem.HOME));
		
		return navigator;
	}
	
	private void createMenu(DefaultNavigator navigator)
	{
		// Parents
		final MenuItem home = navigator.createParentMenuItem(new MemberViewItem(HOME));
		final MenuItem memberAccount = navigator.createParentMenuItem(new MemberViewItem(MEMBER_ACCOUNT));
		if(!userService.getCurrentUser().getCategories().isEmpty()){
			final MenuItem dating = navigator.createParentMenuItem(new MemberViewItem(DATING));

			// Dating
			navigator.createMenuItem(new MemberViewItem(MemberMenuItem.RELATIONSHIP), dating);
			navigator.createMenuItem(new MemberViewItem(MemberMenuItem.LAST_RELATIONSHIP), dating);
			navigator.createMenuItem(new MemberViewItem(MemberMenuItem.MESSAGES), dating);
		}

		// Member Account
		navigator.createMenuItem(new MemberViewItem(MemberMenuItem.USER_DATA), memberAccount);
		navigator.createMenuItem(new MemberViewItem(MemberMenuItem.SUBSCRIPTION), memberAccount);
		navigator.createMenuItem(new MemberViewItem(MemberMenuItem.MESSAGE_TO_GLEICHKLANG), memberAccount);
		navigator.createMenuItem(new MemberViewItem(MemberMenuItem.STATISTIC), memberAccount);
		navigator.createMenuItem(new MemberViewItem(MemberMenuItem.DEACTIVATE_PROLONGATION), memberAccount).setVisible(false);

		// navigator.createMenuItem(new MemberViewItem(MemberMenuItem.CHAT), dating);
	}
	
	private SubscriptionPresenter createSubscriptionPresenter()
	{
		final SubscriptionDetailsView subscriptionDetailsView = new SubscriptionDetailsViewImpl();
		final SubscriptionDetailsPresenter subscriptionDetailsPresenter = new SubscriptionDetailsPresenter(ctx, subscriptionDetailsView);
		
		final ProductPurchaseView upgradeOfferPurchaseView = new ProductPurchaseViewImpl(
				I18N.PRODUCTPURCHASEVIEW_CAPTION_UPGRADEOFFERS.msg(),
				I18N.PRODUCTPURCHASEVIEW_NO_UPGRADEOFFERS_AVAILABLE.msg());
		final ProductPurchasePresenter<UpgradeOffer> upgradeOfferPresenter = new ProductPurchasePresenter<>(ctx, upgradeOfferPurchaseView, UpgradeOfferFilter::new, UpgradeOffer.class);
		
		final ProductPurchaseView serviceOfferPurchaseView = new ProductPurchaseViewImpl(
				I18N.PRODUCTPURCHASEVIEW_CAPTION_SERVICEOFFERS.msg(),
				I18N.PRODUCTPURCHASEVIEW_NO_SERVICEOFFERS_AVAILABLE.msg());
		final ProductPurchasePresenter<ServiceOffer> serviceOfferPresenter = new ProductPurchasePresenter<>(ctx, serviceOfferPurchaseView, ServiceOfferFilter::new, ServiceOffer.class);

		final ActiveCategoriesView activeCategoriesView = new ActiveCategoriesViewImpl();
		final ActiveCategoriesPresenter activeCategoriesPresenter = new ActiveCategoriesPresenter(ctx, activeCategoriesView);

		final RecommendationBreakView recommendationBreakView = new RecommendationBreakViewImpl();
		final RecommendationBreakPresenter recommendationBreakPresenter = new RecommendationBreakPresenter(ctx, recommendationBreakView);

		final SubscriptionViewImpl subscriptionView = new SubscriptionViewImpl(subscriptionDetailsView,
				upgradeOfferPurchaseView, serviceOfferPurchaseView, activeCategoriesView, recommendationBreakView);

		return new SubscriptionPresenter(subscriptionView, subscriptionDetailsPresenter, upgradeOfferPresenter, serviceOfferPresenter, activeCategoriesPresenter, recommendationBreakPresenter);
	}
	
	private NavigatePresenter getPresenter(MemberMenuItem menuItem, Device device)
	{
		switch (menuItem)
		{
			case HOME:
				return new HomePresenter(ctx, new HomeViewImpl(device), navigator, activation);

			case MEMBER_ACCOUNT:
				return null;
			
			case DATING:
				return null;
			
			case USER_DATA:
				return new UserDataPresenter(ctx, new UserDataViewImpl(),navigator);
			
			case MEDIA:
				return new MediaPresenter(ctx, new MediaViewImpl(), device);
			
			case MESSAGE_TO_GLEICHKLANG:
				return new UserAdminMessagePresenter(ctx, new UserAdminMessageViewImpl(device));
			
			case STATISTIC:
				return new StatisticPresenter(ctx, new StatisticViewImpl());

			case SUBSCRIPTION:
				return createSubscriptionPresenter();

			case RELATIONSHIP:

					return new RelationshipPresenter(ctx, new RelationshipViewImpl(device), device);

			case LAST_RELATIONSHIP:

					return new LastRelationshipPresenter(ctx, new LastRelationshipViewImpl(device), device);

			case AUDIO:

				return new AudioPresenter(ctx, new AudioViewImpl(device), device);

			case MESSAGES: {

					return new MessagePresenter(ctx, new MessageViewImpl(device), device);

			}
            case DEACTIVATE_PROLONGATION: {

                return new DeactivateProlongationPresenter(ctx, new DeactivateProlongationViewImpl(device) {
                }, device,navigator) {};

            }
			case CHAT:
				//				return new ChatPresenter(ctx, new ChatViewImpl());
				return null; //disabled
		}
		
		return null;
	}
}
