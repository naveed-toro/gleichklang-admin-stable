package de.binaerebauten.gleichklang.adminweb.view.component;

import com.google.common.collect.ImmutableSet;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.I18N;
import de.binaerebauten.gleichklang.adminweb.view.filter.ProductAdminFilter;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.view.component.*;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.EventObject;
import java.util.Objects;
import java.util.Set;

/**
 * This vaadin component encapsulates a product table that can be used to filter
 * and select a product.
 *
 * For further functionality it also exposes the contained {@link TableControl}
 * which can be retrieved with the {@link #getProductTableControl()} method.
 *
 * This component uses a lazy table which handler can be set with the
 * {@link #setProductHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler)}
 * method.
 */
public class ProductSelectionTable extends CustomComponent
{
	private final Set<ProductType> productTypes;
	private final LazyBeanPagingComponent<Product> productTable;
	private final TableControl<Product> productTableControl;

	private ProductAdminFilter<Product> productAdminFilter;

	private TextField actionCodeTextField;

	/**
	 * Creates a component that show any product type {@link ProductType#values()}.
	 */
	public ProductSelectionTable()
	{
		this(EnumSet.allOf(ProductType.class));
	}

	/**
	 * Creates a component that only shows products of the specified types.
	 *
	 * @param productTypes the non-null product types that this component should view
	 */
	public ProductSelectionTable(Set<ProductType> productTypes)
	{
		this.productTypes = Objects.requireNonNull(productTypes, "productTypes == null");
		productTable = createSubscriptionOfferTable();
		productTableControl = new TableControl<>(productTable);

		Component filterControl = createFilterControl();

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
        layout.setWidth(100, Unit.PERCENTAGE);

		layout.addComponent(filterControl);
		layout.addComponent(productTableControl);

		setCompositionRoot(layout);
	}

	/**
	 * Returns the table control of this component so that further properties can be set.
	 *
	 * @return the product table control
	 */
	public TableControl<Product> getProductTableControl()
	{
		return productTableControl;
	}

	/**
	 * Returns the table of this component.
	 *
	 * @return the product table
	 */
	public LazyBeanPagingComponent<Product> getProductTable()
	{
		return productTable;
	}

	/**
	 * Sets the handler used for this component.
	 *
	 * @param handler the optional handler
	 */
	public void setProductHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Product> handler)
	{
		productTable.setHandler(handler);
	}

	private LazyBeanPagingComponent<Product> createSubscriptionOfferTable()
	{
		final LazyBeanPagingComponent<Product> table = new LazyBeanPagingComponent<>();
		table.setSelectable(true);
        table.setSizeFull();

		table.addGeneratedColumn(I18N.SUBSCRIPTIONOFFER_HEADER_NAME.msg(), Product_->Product_.getName());

		table.addGeneratedColumn(I18N.SUBSCRIPTIONOFFER_HEADER_BEGIN.msg(), Product_->Product_.getBegin());
		table.addGeneratedColumn(I18N.SUBSCRIPTIONOFFER_HEADER_END.msg(), Product_->Product_.getEnd());
		table.addGeneratedColumn(I18N.SUBSCRIPTION_HEADER_AUTORENEWALOFFERNAME.msg(),
				createAutoRenewalColumnGenerator());
		table.addGeneratedColumn(I18N.SUBSCRIPTIONOFFER_HEADER_AMOUNT.msg(), Product_->Product_.getAmount());
		table.addGeneratedColumn(I18N.INITIALSUBSCRIPTIONOFFER_HEADER_AMOUNT.msg(),
				createActionCodeColumnGenerator());
		table.addGeneratedColumn(I18N.SUBSCRIPTIONOFFER_HEADER_TARIFF.msg(), createTariffColumnGenerator());

		table.addGeneratedColumn(I18N.SUBSCRIPTIONOFFER_HEADER_DURATION.msg(),
				createDurationColumnGenerator());

		return table;
	}

	private LazyBeanTable.ColumnGenerator<Product> createTariffColumnGenerator()
	{
		return (source, itemId, columnId) -> {
			if (itemId instanceof SubscriptionOffer) {
				final SubscriptionOffer offer = (SubscriptionOffer) itemId;
				return offer.getTariff();
			}
			return "";
		};
	}

	private LazyBeanTable.ColumnGenerator<Product> createAutoRenewalColumnGenerator()
	{
		return (source, itemId, columnId) -> {
			if (itemId instanceof SubscriptionOffer) {
				SubscriptionOffer offer = (SubscriptionOffer) itemId;
				RenewalOffer autoRenewalOffer = offer.getAutoRenewalOffer();
				return autoRenewalOffer != null ? autoRenewalOffer.getName() : "";
			}
			return "";
		};
	}

	private LazyBeanTable.ColumnGenerator<Product> createActionCodeColumnGenerator()
	{
		return (source, itemId, columnId) -> {
			if (itemId instanceof InitialSubscriptionOffer) {
				InitialSubscriptionOffer offer = (InitialSubscriptionOffer) itemId;
				return offer.getActionCode();
			}
			return "";
		};
	}

	private LazyBeanTable.ColumnGenerator<Product> createDurationColumnGenerator()
	{
		return (source, itemId, columnId) -> {
			if (itemId instanceof SubscriptionOffer) {
				SubscriptionOffer offer = (SubscriptionOffer) itemId;
				return I18N.SUBSCRIPTIONOFFER_CAPTION_DURATION.msg(offer.getDuration(), offer.getDurationUnit());
			}
			return "";
		};
	}

	private Component createFilterControl()
	{
		final HorizontalLayout controlLayout = new HorizontalLayout();
		controlLayout.setSpacing(true);
		controlLayout.setMargin(false);

		productAdminFilter = new ProductAdminFilter<>();

		ComponentFactory factory = ComponentFactory.getInstance();

		ComboBox typeComboBox = factory.createField(ProductType.class, ComboBox.class);
		typeComboBox.setCaption(I18N.SUBSCRIPTIONOFFER_CAPTION_TYPEFILTER.msg());
		factory.populateWithEnumData(typeComboBox, productTypes);

		typeComboBox.setNullSelectionAllowed(true);
		typeComboBox.setTextInputAllowed(false);
		typeComboBox.addValueChangeListener(this::updateTypeFilter);
		controlLayout.addComponent(typeComboBox);

		actionCodeTextField = factory.createField(TextField.class,
				I18N.SUBSCRIPTIONOFFER_CAPTION_ACTIONCODEFILTER.msg());
		actionCodeTextField.setEnabled(false);
		actionCodeTextField.addTextChangeListener(this::updateActionCodeFilter);
		controlLayout.addComponent(actionCodeTextField);

		DateField validDateField = (DateField) factory.createFieldByType(LocalDate.class,
				I18N.SUBSCRIPTIONOFFER_CAPTION_VALIDFILTER.msg());
		validDateField.addValueChangeListener(this::updateValidFilter);
		validDateField.setConvertedValue(LocalDate.now());
		controlLayout.addComponent(validDateField);

		final TextField nameTextField = factory.createField(TextField.class,
				I18N.SUBSCRIPTIONOFFER_CAPTION_NAMEFILTER.msg());
		nameTextField.addTextChangeListener(this::updateNameFilter);
		controlLayout.addComponent(nameTextField);

		return controlLayout;
	}

	private void updateNameFilter(FieldEvents.TextChangeEvent textChangeEvent)
	{
		ProductAdminFilter oldProductAdminFilter = productAdminFilter;
		productAdminFilter = oldProductAdminFilter.withNamePrefix(textChangeEvent.getText());

		productTable.replaceFilter(oldProductAdminFilter, productAdminFilter);
	}

	private void updateActionCodeFilter(FieldEvents.TextChangeEvent textChangeEvent)
	{
		ProductAdminFilter oldProductAdminFilter = productAdminFilter;
		productAdminFilter = oldProductAdminFilter.withActionCodePrefix(textChangeEvent.getText());

		productTable.replaceFilter(oldProductAdminFilter, productAdminFilter);
	}

	private void updateValidFilter(Property.ValueChangeEvent valueChangeEvent)
	{
		EventObject eventObject = (EventObject) valueChangeEvent;
		AbstractField field = (AbstractField) eventObject.getSource();

		LocalDate date = (LocalDate) field.getConvertedValue();
		ProductAdminFilter oldProductAdminFilter = productAdminFilter;
		productAdminFilter = oldProductAdminFilter.withDate(date);

		productTable.replaceFilter(oldProductAdminFilter, productAdminFilter);
	}

	private void updateTypeFilter(Property.ValueChangeEvent valueChangeEvent)
	{
		ProductAdminFilter oldProductAdminFilter = productAdminFilter;
		ProductType productType = (ProductType) valueChangeEvent.getProperty().getValue();

		boolean hasActionCode = productType == ProductType.INITIAL_SUBSCRIPTION_OFFER;
		actionCodeTextField.setEnabled(hasActionCode);

		Set<ProductType> types = productType == null ?
				productTypes : ImmutableSet.of(productType);

		productAdminFilter = oldProductAdminFilter
				.withTypes(types)
				.withActionCodePrefix(hasActionCode ? oldProductAdminFilter.getActionCodePrefix() : null);

		productTable.replaceFilter(oldProductAdminFilter, productAdminFilter);
	}
}
