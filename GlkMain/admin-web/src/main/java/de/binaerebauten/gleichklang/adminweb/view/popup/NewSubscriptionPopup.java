package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.component.ProductSelectionTable;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Objects;

public class NewSubscriptionPopup extends Popup
{
	public interface SaveSubscriptionCallback
	{
		void save(User user, SubscriptionOffer subscriptionOffer, MonetaryAmount amount, boolean value) throws ValidationException;
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(NewSubscriptionPopup.class);
	
	private final User user;
	
	private final SaveSubscriptionCallback saveSubscriptionCallback;
	
	private final ComponentGroup<MonetaryAmount> amountComponentGroup;
	
	private final ProductSelectionTable productSelectionTable;
	private final CheckBox isPaidCheckBox;
	
	public NewSubscriptionPopup(User user, LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Product> handler, SaveSubscriptionCallback saveSubscriptionCallback)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(handler, "handler == null");
		Objects.requireNonNull(saveSubscriptionCallback, "saveSubscriptionCallback == null");
		
		this.user = user;
		this.saveSubscriptionCallback = saveSubscriptionCallback;
		
		amountComponentGroup = new ComponentGroup<>(MonetaryAmount.class, new MonetaryAmount(new BigDecimal(0), AvailableCurrency.EUR));
		
		productSelectionTable = createProductSelectionTable(handler);
		isPaidCheckBox = createIsPaidCheckBox();
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		final Component subscriptionControl = createLayout();
		layout.addComponent(subscriptionControl);
		
		final Button saveButton = new Button(I18N.SUBSCRIPTION_ACTION_SAVE.msg());
		saveButton.addClickListener(event -> save());
		layout.addComponent(saveButton);
		
		setContent(layout);
	}
	
	private CheckBox createIsPaidCheckBox()
	{
		final CheckBox checkBox = ComponentFactory.getInstance().createField(CheckBox.class, "direkt als bezahlt markieren");
		checkBox.setValue(true);
		
		return checkBox;
	}
	
	private Component createAmountComponent()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		
		final TextField amountField = amountComponentGroup.buildAndBind(true, de.binaerebauten.gleichklang.core.model.payment.I18N.MONATARYAMOUNT_CAPTION_AMOUNT.msg(), TextField.class, MonetaryAmount_.amount);
		final ComboBox currencyField = amountComponentGroup.buildAndBind(true, de.binaerebauten.gleichklang.core.model.payment.I18N.MONATARYAMOUNT_CAPTION_CURRENCY.msg(), ComboBox.class, MonetaryAmount_.currency);
		currencyField.setTextInputAllowed(false);
		
		layout.addComponents(amountField, currencyField);
		
		return layout;
	}
	
	private void save()
	{
		try
		{
			final SubscriptionOffer subscriptionOffer = (SubscriptionOffer) productSelectionTable.getProductTable().getValue();
			if(subscriptionOffer == null) throw new ValidationException("Bitte ein Produkt auswählen");
			
			amountComponentGroup.commit();
			final MonetaryAmount amount = amountComponentGroup.getItemDataSource().getBean();
			
			saveSubscriptionCallback.save(user, subscriptionOffer, amount, isPaidCheckBox.getValue());
			
			close();
		}
		catch (ValidationException | FieldGroup.CommitException e)
		{
			Notification.show("Error in create subscription", e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
	}
	
	private Component createLayout()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(false);
		
		layout.addComponent(productSelectionTable);
		layout.addComponent(createAmountComponent());
		layout.addComponent(isPaidCheckBox);
		
		return layout;
	}
	
	private ProductSelectionTable createProductSelectionTable(LazyBeanFilteredItemsHandler<Product> handler)
	{
		final ProductSelectionTable productSelectionTable = new ProductSelectionTable(EnumSet.of(ProductType.INITIAL_SUBSCRIPTION_OFFER, ProductType.RENEWAL_OFFER, ProductType.UPGRADE_OFFER));
		productSelectionTable.setProductHandler(handler);
		
		return productSelectionTable;
	}
}
