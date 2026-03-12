package de.binaerebauten.gleichklang.adminweb.view.component;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.I18N;
import de.binaerebauten.gleichklang.adminweb.view.filter.SubscriptionValidFilter;
import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer_;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer_;
import de.binaerebauten.gleichklang.core.model.payment.Subscription_;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SubscriptionTable extends CustomComponent
{
	public interface SubscriptionHandler
	{
		/**
		 * Fired when the user presses the refresh view button.
		 */
		void refreshSubscriptionTable();
		
		/**
		 * This method is called when an admin has pressed the "edit subscription" button.
		 *
		 * @param subscription the subscription to edit
		 */
		void editSubscription(Subscription subscription);
		
		/**
		 * This method is called when an admin has pressed the "cancel subscription" button.
		 *
		 * @param subscription the subscription to cancel
		 */
		void cancelSubscription(Subscription subscription);
		
		void newSubscription(User user);
	}
	
	public interface NewSubscriptionListener
	{
		void newSubscription();
	}
	
	private SubscriptionHandler subscriptionHandler;
	
	private final Component buttonControl;
	private final LazyBeanTable<Subscription> subscriptionTable;
	
	private NewSubscriptionListener newSubscriptionListener;
	private final Button newSubscriptionButton;
	
	private SubscriptionValidFilter validFilter;
	
	public SubscriptionTable()
	{
		newSubscriptionButton = createNewButton();
		subscriptionTable = createSubscriptionTable();
		buttonControl = createButtonControl();
		buttonControl.setEnabled(false);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		layout.addComponent(createFilterControl());
		layout.addComponent(buttonControl);
		layout.addComponent(subscriptionTable);
		
		layout.setSizeFull();
		
		setCompositionRoot(layout);
	}
	
	private Component createFilterControl()
	{
		final HorizontalLayout controlLayout = new HorizontalLayout();
		controlLayout.setSpacing(true);
		controlLayout.setMargin(false);
		
		final DateField validDateField = ComponentFactory.getInstance().createField(LocalDate.class, DateField.class);
		validDateField.setCaption(I18N.SUBSCRIPTION_CAPTION_VALIDFILTER.msg());
		validDateField.addValueChangeListener(valueChangedEvent -> updateValidFilter(validDateField));
		controlLayout.addComponent(validDateField);
		
		return controlLayout;
	}
	
	private Component createButtonControl()
	{
		final HorizontalLayout controlLayout = new HorizontalLayout();
		controlLayout.setSpacing(true);
		controlLayout.setMargin(false);
		
		controlLayout.addComponent(newSubscriptionButton);
		
		Button editButton = new Button(I18N.SUBSCRIPTION_ACTION_EDIT.msg());
		editButton.setEnabled(false);
		editButton.addClickListener(e -> subscriptionHandler.editSubscription(subscriptionTable.getValue()));
		controlLayout.addComponent(editButton);
		
		Button cancelButton = new Button(I18N.SUBSCRIPTION_ACTION_CANCEL.msg());
		cancelButton.setEnabled(false);
		cancelButton.addClickListener(e -> subscriptionHandler.cancelSubscription(subscriptionTable.getValue()));
		controlLayout.addComponent(cancelButton);
		
		Button refreshViewButton = new Button(I18N.SUBSCRIPTION_ACTION_REFRESH.msg());
		refreshViewButton.addClickListener(clickEvent -> subscriptionHandler.refreshSubscriptionTable()); //TODO subscriptionTable.refresh() could be enough
		controlLayout.addComponent(refreshViewButton);
		
		subscriptionTable.addValueChangeListener(values ->
		{
			boolean isCurrent = false;
			boolean isNearlyEnded = false;
			boolean isNotCanceled = false;
			
			if(values.size() == 1)
			{
				final LocalDateTime lockDate = LocalDateTime.now().plus(1, ChronoUnit.WEEKS);
				final Subscription selectedSubscription = values.iterator().next();
				isCurrent = Boolean.TRUE.equals(selectedSubscription.getCurrent());
				isNearlyEnded = lockDate.isAfter(selectedSubscription.getEnd());
				isNotCanceled = !SubscriptionState.CANCELED.equals(selectedSubscription.getState()) && !SubscriptionState.EXPIRED.equals(selectedSubscription.getState());
			}
			editButton.setEnabled(isCurrent && !isNearlyEnded && isNotCanceled);
			cancelButton.setEnabled(isCurrent && isNotCanceled);
		});
		
		return controlLayout;
	}
	
	private Button createNewButton()
	{
		final Button newButton = new Button(I18N.SUBSCRIPTION_ACTION_NEW.msg());
		newButton.addClickListener(event -> newSubscriptionListener.newSubscription());
		newButton.setVisible(false);
		
		return newButton;
	}
	
	private void updateValidFilter(DateField validDateField)
	{
		LocalDate validDate = (LocalDate) validDateField.getConvertedValue();
		SubscriptionValidFilter oldValidFilter = validFilter;
		
		if (validDate != null)
		{
			validFilter = new SubscriptionValidFilter(validDate);
			subscriptionTable.replaceFilter(oldValidFilter, validFilter);
		}
		else if (oldValidFilter != null)
		{
			subscriptionTable.removeFilter(oldValidFilter);
		}
	}
	
	private LazyBeanTable<Subscription> createSubscriptionTable()
	{
		final LazyBeanTable<Subscription> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();
		
		table.addContainerProperty(I18N.SUBSCRIPTION_HEADER_USEREMAIL.msg(), Subscription_.user, User_.email);
		table.addContainerProperty(I18N.SUBSCRIPTION_HEADER_USERALIAS.msg(), Subscription_.user, User_.alias);
		
		table.addContainerProperty(I18N.SUBSCRIPTION_HEADER_BEGIN.msg(), Subscription_.begin);
		table.addContainerProperty(I18N.SUBSCRIPTION_HEADER_END.msg(), Subscription_.end);
		
		table.addGeneratedColumn(I18N.SUBSCRIPTION_HEADER_STATE.msg(), this::getSubscriptionStateLocalized);
		
		table.addContainerProperty(I18N.SUBSCRIPTION_HEADER_OFFERNAME.msg(), Subscription_.offer, SubscriptionOffer_.name);
		table.addContainerProperty(I18N.SUBSCRIPTION_HEADER_AUTOMATICRENEWAL.msg(), Subscription_.automaticRenewal);
		table.addContainerProperty(I18N.SUBSCRIPTION_HEADER_AUTORENEWALOFFERNAME.msg(), Subscription_.offer,
				InitialSubscriptionOffer_.autoRenewalOffer, SubscriptionOffer_.name);
		
		return table;
	}
	
	private String getSubscriptionStateLocalized(Subscription subscription)
	{
		return subscription.getState().msg();
	}
	
	public void setTableHandler(LazyBeanFilteredItemsHandler<Subscription> tableHandler)
	{
		subscriptionTable.clearValue();
		subscriptionTable.setHandler(tableHandler);
	}
	
	public void replaceFilter(Specification<Subscription> oldSpecification, Specification<Subscription> newSpecification)
	{
		subscriptionTable.replaceFilter(oldSpecification, newSpecification);
	}
	
	public void setSubscriptionHandler(SubscriptionHandler subscriptionHandler)
	{
		this.subscriptionHandler = subscriptionHandler;
		buttonControl.setEnabled(subscriptionHandler != null);
	}
	
	public void setNewSubscriptionListener(NewSubscriptionListener newSubscriptionListener)
	{
		this.newSubscriptionListener = newSubscriptionListener;
		newSubscriptionButton.setVisible(newSubscriptionListener != null);
	}
}
