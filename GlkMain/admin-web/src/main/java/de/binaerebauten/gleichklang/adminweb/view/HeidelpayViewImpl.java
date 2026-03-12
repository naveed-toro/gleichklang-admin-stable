package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.adminweb.view.HeidelpayView.HeidelpayViewListener;
import de.binaerebauten.gleichklang.core.model.heidelpay.*;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration_;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.filter.SimpleUserFilter.DirectUserFilter;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.persistence.criteria.CriteriaBuilder.In;

public class HeidelpayViewImpl extends AbstractNavigateView<HeidelpayViewListener> implements HeidelpayView
{
	public enum LocalRegistrationFilter
	{
		EXISTENT_BUT_HP_NOT_ACTIVE
	}
	
	public enum RegistrationFilter
	{
		NOT_ACTIVE_BUT_LOCAL_EXISTENT,
		ACTIVE_WITH_SCHEDULER_BUT_LOCAL_NOT_EXISTENT
	}
	
	private class MissingHpFilter implements Specification<ExternalPaymentRegistration>
	{
		@Override
		public Predicate toPredicate(Root<ExternalPaymentRegistration> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Subquery<String> subQuery = query.subquery(String.class);
			final Root<Registration> hpRoot = subQuery.from(Registration.class);
			
			final Predicate predicate1 = cb.equal(hpRoot.get(Registration_.active), true);
			final Predicate predicate2 = cb.isNotNull(hpRoot.get(Registration_.uniqueId));
			
			
			subQuery.select(hpRoot.get(Registration_.uniqueId)).where(cb.and(predicate1, predicate2));
			
			final In<String> inClause = cb.in(root.get(ExternalPaymentRegistration_.registrationId)).value(subQuery);
			
			return cb.not(inClause);
		}
	}
	
	private class MissingLocallyFilter implements Specification<Registration>
	{
		@Override
		public Predicate toPredicate(Root<Registration> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Subquery<String> subQuery = query.subquery(String.class);
			final Root<ExternalPaymentRegistration> localRoot = subQuery.from(ExternalPaymentRegistration.class);
			
			subQuery.select(localRoot.get(ExternalPaymentRegistration_.registrationId)).where(cb.isNotNull(localRoot.get(ExternalPaymentRegistration_.registrationId)));
			
			final In<String> inClause = cb.in(root.get(Registration_.uniqueId)).value(subQuery);
			final Predicate equal = cb.equal(root.get(Registration_.active), true);
			
			return cb.and(equal, cb.not(inClause));
		}
	}
	
	private class WrongLocallyFilter implements Specification<Registration>
	{
		@Override
		public Predicate toPredicate(Root<Registration> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Subquery<String> subQuery = query.subquery(String.class);
			final Root<ExternalPaymentRegistration> localRoot = subQuery.from(ExternalPaymentRegistration.class);
			
			subQuery.select(localRoot.get(ExternalPaymentRegistration_.registrationId));
			
			final In<String> inClause = cb.in(root.get(Registration_.uniqueId)).value(subQuery);
			final Predicate equal = cb.equal(root.get(Registration_.active), false);
			
			return cb.and(equal, inClause);
		}
	}
	
	private class RegistrationUserFilter implements Specification<Registration>
	{
		private final Specification<User> userSpecification;
		
		public RegistrationUserFilter(Specification<User> userSpecification)
		{
			this.userSpecification = userSpecification;
		}
		
		@Override
		public Predicate toPredicate(Root<Registration> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Subquery<User> userSubQuery = query.subquery(User.class);
			final Root<User> userRoot = userSubQuery.from(User.class);
			
			userSubQuery.select(userRoot).where(userSpecification.toPredicate(userRoot, query, cb));
			
			final Subquery<String> eprSubQuery = query.subquery(String.class);
			final Root<ExternalPaymentRegistration> eprRoot = eprSubQuery.from(ExternalPaymentRegistration.class);
			
			final In<User> eprIn = cb.in(eprRoot.get(ExternalPaymentRegistration_.user)).value(userSubQuery);
			eprSubQuery.select(eprRoot.get(ExternalPaymentRegistration_.registrationId)).where(eprIn);
			
			return cb.in(root.get(Registration_.uniqueId)).value(eprSubQuery);
		}
	}
	
	private final NavigationComponent<HeidelpayTab> navigationComponent;
	
	private final LazyBeanTable<ExternalPaymentRegistration> localRegistrationTable;
	private final LazyBeanTable<Registration> registrationTable;
	
	private final ComponentReplacer<FilterControlComponent> localRegistrationUserFilterComponent;
	private final ComponentReplacer<FilterControlComponent> registrationUserFilterComponent;
	
	private Specification<Registration> registrationUserFilter = null;
	private Specification<ExternalPaymentRegistration> localRegistrationSpecialFilter = null;
	private Specification<Registration> registrationSpecialFilter = null;
	
	public HeidelpayViewImpl()
	{
		localRegistrationUserFilterComponent = new ComponentReplacer<>();
		registrationUserFilterComponent = new ComponentReplacer<>();
		
		localRegistrationTable = createLocalRegistrationTable();
		registrationTable = createRegistrationTable();
		
		navigationComponent = createNavigationComponent();
		setCompositionRoot(navigationComponent);
	}
	
	private LazyBeanTable<Registration> createRegistrationTable()
	{
		final LazyBeanTable<Registration> table = new LazyBeanTable<>();
		
		table.addContainerProperty("Unique-Id", Registration_.uniqueId);
		table.addContainerProperty("Transaction-Id", Registration_.transactionId);
		table.addContainerProperty("Active", Registration_.active);
		table.addContainerProperty("Email", Registration_.email);
		table.addContainerProperty("First-Name", Registration_.firstName);
		table.addContainerProperty("Last-Name", Registration_.lastName);
		table.addContainerProperty("HP-Date", Registration_.heidelpayDate);
		
		return table;
	}
	
	private LazyBeanTable<ExternalPaymentRegistration> createLocalRegistrationTable()
	{
		final LazyBeanTable<ExternalPaymentRegistration> table = new LazyBeanTable<>();
		
		table.addContainerProperty("Unique-Id", ExternalPaymentRegistration_.registrationId);
		table.addContainerProperty("Transaction-Id", ExternalPaymentRegistration_.externalReferenceId);
		table.addContainerProperty("Email", ExternalPaymentRegistration_.user, User_.email);
		
		return table;
	}
	
	private ComboBox createLocalRegistrationFilterComponent()
	{
		final ComboBox filter = ComponentFactory.getInstance().createField(LocalRegistrationFilter.class, ComboBox.class);
		filter.setNullSelectionAllowed(true);
		
		filter.addValueChangeListener(event ->
		{
			if (filter.getValue() instanceof LocalRegistrationFilter)
			{
				switch ((LocalRegistrationFilter) filter.getValue())
				{
					case EXISTENT_BUT_HP_NOT_ACTIVE:
						changeLocalRegistrationSpecialFilter(new MissingHpFilter());
						break;
				}
			}
			else
			{
				changeLocalRegistrationSpecialFilter(null);
			}
		});
		
		return filter;
	}
	
	private ComboBox createRegistrationFilterComponent()
	{
		final ComboBox filter = ComponentFactory.getInstance().createField(RegistrationFilter.class, ComboBox.class);
		filter.setNullSelectionAllowed(true);
		
		filter.addValueChangeListener(event ->
		{
			if (filter.getValue() instanceof RegistrationFilter)
			{
				switch ((RegistrationFilter) filter.getValue())
				{
					case NOT_ACTIVE_BUT_LOCAL_EXISTENT:
						changeRegistrationSpecialFilter(new WrongLocallyFilter());
						break;
					case ACTIVE_WITH_SCHEDULER_BUT_LOCAL_NOT_EXISTENT:
						changeRegistrationSpecialFilter(new MissingLocallyFilter());
						break;
				}
			}
			else
			{
				changeRegistrationSpecialFilter(null);
			}
		});
		
		return filter;
	}
	
	private NavigationComponent<HeidelpayTab> createNavigationComponent()
	{
		final NavigationComponent<HeidelpayTab> navigationComponent = new NavigationComponent<>(HeidelpayTab.class);
		
		navigationComponent.addNavigation(HeidelpayTab.LOCAL_REGISTRATION, createLocalRegistrationTab());
		navigationComponent.addNavigation(HeidelpayTab.REGISTRATION, createRegistrationTab());
		
		return navigationComponent;
	}
	
	private Component createLocalRegistrationTab()
	{
		final AbstractOrderedLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		final TableControl<ExternalPaymentRegistration> tableControl = new TableControl<>(localRegistrationTable);
		
		tableControl.addButton("open user", (item) -> fireEvent(action -> action.openUser(item.getUser().getId())));
		tableControl.addButton("synchronize chargebacks", () -> fireEvent(HeidelpayViewListener::synchronizeChargebacks));
		tableControl.addButton("synchronize refunds", () -> fireEvent(HeidelpayViewListener::synchronizeRefunds));
		
		layout.addComponent(localRegistrationUserFilterComponent);
		layout.addComponent(createLocalRegistrationFilterComponent());
		layout.addComponent(tableControl);
		
		return layout;
	}
	
	private Component createRegistrationTab()
	{
		final AbstractOrderedLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		final TableControl<Registration> tableControl = new TableControl<>(registrationTable);
		
		tableControl.addButton("synchronize", () -> fireEvent(HeidelpayViewListener::synchronizeRegistrations));
		tableControl.addButton("open user", (item) -> fireEvent(action -> action.openUser(item.getUniqueId())));
		
		layout.addComponent(registrationUserFilterComponent);
		layout.addComponent(createRegistrationFilterComponent());
		layout.addComponent(tableControl);
		
		return layout;
	}
	
	@Override
	public void setListener(HeidelpayViewListener listener)
	{
		super.setListener(listener);
		navigationComponent.setSubNavigationListener(listener);
	}
	
	@Override
	public void selectSubNavigation(HeidelpayTab navigationEnum)
	{
		navigationComponent.setSelectedNavigation(navigationEnum);
	}
	
	@Override
	public void setLocalRegistrationHandler(LazyBeanFilteredItemsHandler<ExternalPaymentRegistration> handler)
	{
		localRegistrationTable.setHandler(handler);
	}
	
	@Override
	public void setRegistrationHandler(LazyBeanFilteredItemsHandler<Registration> handler)
	{
		registrationTable.setHandler(handler);
	}
	
	@Override
	public void setUserFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder)
	{
		localRegistrationTable.removeAllFilters();
		registrationTable.removeAllFilters();
		
		if (filterControlHandler == null)
		{
			localRegistrationUserFilterComponent.setComponent(null);
			registrationUserFilterComponent.setComponent(null);
		}
		else
		{
			final DirectUserFilter<ExternalPaymentRegistration> localRegistrationUserFilter = new DirectUserFilter<>(ExternalPaymentRegistration_.user);
			localRegistrationUserFilter.setItemComponent(localRegistrationTable);
			
			final FilterControlComponent localRegistrationFilterControlComponent = new FilterControlComponent(filterControlHandler, filterSpecificationBuilder);
			localRegistrationFilterControlComponent.addFilterChangedListener(localRegistrationUserFilter::setValue);
			localRegistrationUserFilterComponent.setComponent(localRegistrationFilterControlComponent);
			
			final FilterControlComponent registrationFilterControlComponent = new FilterControlComponent(filterControlHandler, filterSpecificationBuilder);
			registrationFilterControlComponent.addFilterChangedListener(this::changeRegistrationFilter);
			registrationUserFilterComponent.setComponent(registrationFilterControlComponent);
		}
	}
	
	private void changeRegistrationFilter(Specification<User> userSpecification)
	{
		final Specification<Registration> registrationSpecification = userSpecification != null ? new RegistrationUserFilter(userSpecification) : null;
		registrationTable.replaceFilter(registrationUserFilter, registrationSpecification);
		this.registrationUserFilter = registrationSpecification;
	}
	
	private void changeLocalRegistrationSpecialFilter(Specification<ExternalPaymentRegistration> specialFilter)
	{
		localRegistrationTable.replaceFilter(localRegistrationSpecialFilter, specialFilter);
		localRegistrationSpecialFilter = specialFilter;
	}
	
	private void changeRegistrationSpecialFilter(Specification<Registration> specialFilter)
	{
		registrationTable.replaceFilter(registrationSpecialFilter, specialFilter);
		registrationSpecialFilter = specialFilter;
	}
}
