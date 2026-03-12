package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer_;
import de.binaerebauten.gleichklang.core.model.payment.Subscription_;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.LocalDateTimeField;
import de.binaerebauten.gleichklang.core.view.component.Popup;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import static de.binaerebauten.gleichklang.core.model.payment.I18N.*;

/**
 * This popup allows editing of a subscription
 * {@link de.binaerebauten.gleichklang.core.model.payment.Subscription}.
 */
public class EditSubscriptionPopup extends Popup
{
	public interface SaveSubscriptionCallback
	{
		/**
		 * Saves the given subscription.
		 *
		 * @param subscription the non-null subscription
		 */
		void save(Subscription subscription);
	}

	private final SaveSubscriptionCallback saveSubscriptionCallback;

	private ComponentGroup<Subscription> subscriptionComponentGroup;
	
	private DateField endDateField;
	
	private LocalDateTimeField expirationDateField;

	public EditSubscriptionPopup(Subscription subscription,
			LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Product> handler,
			SaveSubscriptionCallback saveSubscriptionCallback)
	{
		super(I18N.EDITSUBSCRIPTIONPOPUP_CAPTION_TITLE.msg());

		Objects.requireNonNull(subscription, "subscription == null");
		Objects.requireNonNull(handler, "handler == null");
		Objects.requireNonNull(saveSubscriptionCallback, "saveSubscriptionCallback == null");

		this.saveSubscriptionCallback = saveSubscriptionCallback;

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		Component subscriptionControl = createSubscriptionControl(subscription);
		layout.addComponent(subscriptionControl);

		Button saveButton = new Button(I18N.SUBSCRIPTION_ACTION_SAVE.msg());
		saveButton.addClickListener(event -> commitAndSave());
		layout.addComponent(saveButton);

		setContent(layout);
	}

	private void commitAndSave()
	{
		try
		{
			subscriptionComponentGroup.commit();
			Subscription subscription = subscriptionComponentGroup.getItemDataSource().getBean();
			saveSubscriptionCallback.save(subscription);

			close();
		}
		catch (FieldGroup.CommitException e)
		{
			Notification.show("Error in subscription update", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}

	private Component createSubscriptionControl(Subscription subscription)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(false);

		subscriptionComponentGroup = new ComponentGroup<>(Subscription.class, subscription);

		Field<?> textField = subscriptionComponentGroup.buildAndBind(SUBSCRIPTION_CAPTION_NAME.msg(),
				Subscription_.offer, SubscriptionOffer_.name);
		textField.setEnabled(false);
		layout.addComponent(textField);

		HorizontalLayout datePanel = new HorizontalLayout();
		datePanel.setSpacing(true);
		datePanel.setMargin(false);
		
		final DateField beginDateField = (DateField) subscriptionComponentGroup.buildAndBind(SUBSCRIPTION_CAPTION_BEGIN.msg(), Subscription_.begin);
		datePanel.addComponent(beginDateField);

		endDateField = (DateField) subscriptionComponentGroup.buildAndBind(SUBSCRIPTION_CAPTION_END.msg(), Subscription_.end);
		endDateField.addValueChangeListener(this::updateExpirationDateRange);
		datePanel.addComponent(endDateField);

		expirationDateField = new LocalDateTimeField();
		expirationDateField.setCaption(SUBSCRIPTION_CAPTION_EXPIRATIONDATE.msg());
		expirationDateField.setDateOutOfRangeMessage(I18N.SUBSCRIPTION_EXPIRATION_DATE_OUT_OF_RANGE.msg());
		expirationDateField.addValueChangeListener(this::updateExpirationDateRange);
		subscriptionComponentGroup.bind(expirationDateField, Subscription_.expirationDate);
		datePanel.addComponent(expirationDateField);

		layout.addComponent(datePanel);
		
		CheckBox automaticRenewalField = subscriptionComponentGroup.buildAndBind(SUBSCRIPTION_CAPTION_AUTOMATICRENEWAL.msg(),
				CheckBox.class, Subscription_.automaticRenewal);
		layout.addComponent(automaticRenewalField);

		return layout;
	}
	
	private void updateExpirationDateRange(Property.ValueChangeEvent event)
	{
		LocalDate endDate = endDateField.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate expirationDate = endDate.plusDays(14);
		Date allowedExpirationDate = Date.from(expirationDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		expirationDateField.setRangeStart(allowedExpirationDate);
		if (expirationDateField.getValue().compareTo(allowedExpirationDate) < 0)
		{
			expirationDateField.setValue(allowedExpirationDate);
		}
	}
}
