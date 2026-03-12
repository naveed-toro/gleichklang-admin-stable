package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.adminweb.presenter.handler.DefaultSubscriptionHandler;
import de.binaerebauten.gleichklang.adminweb.view.SubscriptionAdminView;
import de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.repository.SubscriptionRepository;
import de.binaerebauten.gleichklang.core.service.FilterControlService;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlFeature;
import org.springframework.context.ApplicationContext;

/**
 * This class is the presenter for the subscription admin view. It allows an adminstrator to view
 * and change the subscriptions of all users.
 */
public class SubscriptionAdminPresenter extends NavigatePresenter implements SubscriptionAdminView.SubscriptionAdminViewListener
{
	private final SubscriptionRepository subscriptionRepository;
	private final DefaultFilterControlHandler filterControlHandler;
	private final DefaultSubscriptionHandler subscriptionHandler;
	private final FilterSpecificationBuilder filterSpecificationBuilder;

	private final SubscriptionAdminView view;

	public SubscriptionAdminPresenter(ApplicationContext ctx, SubscriptionAdminView subscriptionAdminView)
	{
		super(subscriptionAdminView);
		
		subscriptionRepository = ctx.getBean(SubscriptionRepository.class);
		
		filterControlHandler = new DefaultFilterControlHandler(ctx.getBean(FilterControlService.class));
		filterControlHandler.setUserFilterTypes(UserFilterType.MAIL_FILTER, UserFilterType.ALIAS_FILTER, UserFilterType.FIRST_NAME_FILTER, UserFilterType.LAST_NAME_FILTER, UserFilterType.AUTO_RENEWAL_FILTER, UserFilterType.USER_ID_FILTER);
		filterControlHandler.setFilterControlFeatures(FilterControlFeature.OR_LINKABLE);
		
		subscriptionHandler = new DefaultSubscriptionHandler(ctx, this::refreshView, this);

		filterSpecificationBuilder = ctx.getBean(FilterSpecificationBuilder.class);

		view = subscriptionAdminView;

		view.setListener(this);
	}

	@Override
	public void enter(String parameters)
	{
		refreshView();
	}

	private void refreshView()
	{
		this.view.setFilterHandlerAndBuilder(filterControlHandler, filterSpecificationBuilder);
		this.view.setSubscriptionHandler(subscriptionRepository::findAll);
	}
	
	@Override
	public void editSubscription(Subscription subscription)
	{
		subscriptionHandler.editSubscription(subscription);
	}
	
	@Override
	public void cancelSubscription(Subscription subscription)
	{
		subscriptionHandler.cancelSubscription(subscription);
		refreshSubscriptionTable();
	}
	
	@Override
	public void refreshSubscriptionTable()
	{
		view.setSubscriptionHandler(subscriptionRepository::findAll);
	}
	
	// never called at the moment, only from userControlHandler accessible
	@Override
	public void newSubscription(User user)
	{
		subscriptionHandler.newSubscription(user);
	}
}
