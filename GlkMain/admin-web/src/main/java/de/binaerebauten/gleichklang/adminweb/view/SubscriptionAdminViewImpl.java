package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.adminweb.view.component.SubscriptionTable;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.Subscription_;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentReplacer;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

/**
 * Implementation of the subscription admin view.
 */
public class SubscriptionAdminViewImpl
		extends AbstractNavigateView<SubscriptionAdminView.SubscriptionAdminViewListener>
		implements SubscriptionAdminView
{
	public static class UserFilter implements Specification<Subscription>
	{
		private final Specification<User> userSpecification;

		public UserFilter(Specification<User> userSpecification)
		{
			this.userSpecification = userSpecification;
		}

		@Override
		public Predicate toPredicate(Root<Subscription> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Subquery<Subscription> sq = query.subquery(Subscription.class);
			final Root<Subscription> subscriptionRoot = sq.from(Subscription.class);
			final Root<User> user_root = sq.from(User.class);

			final Predicate restriction1 = userSpecification.toPredicate(user_root, query, cb);
			final Predicate restriction2 = cb.equal(subscriptionRoot.get(Subscription_.user), user_root);

			sq.where(restriction1, restriction2).select(subscriptionRoot);

			return cb.in(root).value(sq);
		}
	}

	private final SubscriptionTable subscriptionTable;

	private final ComponentReplacer<FilterControlComponent> filterComponent = new ComponentReplacer<>();
	private Specification<Subscription> subscriptionFilter = null;

	public SubscriptionAdminViewImpl()
	{
		subscriptionTable = new SubscriptionTable();
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		layout.addComponent(filterComponent);
		layout.addComponent(subscriptionTable);

		layout.setSizeFull();

		setCompositionRoot(layout);
	}

	@Override
	public void setSubscriptionHandler(LazyBeanFilteredItemsHandler<Subscription> handler)
	{
		subscriptionTable.setTableHandler(handler);
	}
	
	@Override
	public void setFilterHandlerAndBuilder(FilterControlHandler filterControlHandler,
			FilterSpecificationBuilder filterSpecificationBuilder)
	{
		changeFilter(null);

		if (filterControlHandler == null)
		{
			filterComponent.setComponent(null);
		}
		else
		{
			filterComponent.setComponent(new FilterControlComponent(filterControlHandler, filterSpecificationBuilder));
			filterComponent.getComponent().addFilterChangedListener(this::changeFilter);
		}
	}

	private void changeFilter(Specification<User> userSpecification)
	{
		final Specification<Subscription> subscriptionSpecification = userSpecification != null ? new UserFilter(userSpecification) : null;
		subscriptionTable.replaceFilter(subscriptionFilter, subscriptionSpecification);
		this.subscriptionFilter = subscriptionSpecification;
	}
	
	@Override
	public void setListener(SubscriptionAdminViewListener listener)
	{
		super.setListener(listener);
		subscriptionTable.setSubscriptionHandler(getListener());
	}
}
