package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.UserError;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This component shows a list of {@link Product}s and provides a purchase/subscribe button.
 */
public class ProductSelection extends CustomComponent
{
	/**
	 * This listener interface defines the events that this component fires.
	 */
	public interface ProductSelectionListener
	{
		/**
		 * The current user has selected the given
		 * product and payment method.
		 *
		 * @param product       the selected non-null product
		 * @param paymentMethod the selected non-null payment method
		 */
		void select(Product product, PaymentMethod paymentMethod);

		/**
		 * This event is fired when the user changed the action code and should trigger
		 * setting of new offers.
		 *
		 * @param actionCode the updated action code
		 */
		void actionCodeChanged(String actionCode);

		/**
		 * Called when the user changed the payment method.
		 * The implementation then needs to save the payment method {@link UserPaymentSettings#paymentMethod}.
		 *
		 * @param paymentMethod the selected payment method
		 */
		void paymentMethodChanged(PaymentMethod paymentMethod);
	}

	private final List<ProductSelectionListener> listeners = new ArrayList<>();

	private TextField actionCodeTextField;

	private ComboBox paymentMethodComboBox;

	private OptionGroup productOptionGroup;

	private final List<Field<?>> validatableFields = new ArrayList<>();

	public ProductSelection()
	{
		Component controlComponent = createContainerComponent();
		setCompositionRoot(controlComponent);
	}

	/**
	 * Adds the given listener to this component.
	 *
	 * @param l the listener to add
	 */
	public void addProductSelectionListener(ProductSelectionListener l)
	{
		listeners.add(l);
	}

	/**
	 * Sets the products viewed by this component.
	 *
	 * @param products the products to view
	 */
	public void setProducts(List<? extends Product> products)
	{
		Container containerDataSource = productOptionGroup.getContainerDataSource();
		containerDataSource.removeAllItems();

		products.forEach(containerDataSource::addItem);
	}

	/**
	 * Sets available payment methods.
	 *
	 * @param paymentMethods available payment methods
	 */
	public void setAvailablePaymentMethods(Set<PaymentMethod> paymentMethods)
	{
		paymentMethodComboBox.addItems(paymentMethods);
	}

	/**
	 * Sets the payment method to use.
	 *
	 * @param paymentMethod the payment method
	 */
	public void setPaymentMethod(PaymentMethod paymentMethod)
	{
		paymentMethodComboBox.setValue(paymentMethod);
	}

	/**
	 * Returns the selected payment method.
	 *
	 * @return the selected payment method
	 */
	public PaymentMethod getPaymentMethod()
	{
		return (PaymentMethod) paymentMethodComboBox.getValue();
	}

	/**
	 * Returns the selected product.
	 *
	 * @return the selected product
	 */
	public Product getProduct()
	{
		return (Product) productOptionGroup.getValue();
	}

	/**
	 * Selects the product.
	 *
	 * @return the selected product
	 */
	public void setProduct(Product value)
	{
		productOptionGroup.setValue(value);
	}

	/**
	 * Sets the action code validation error.
	 *
	 * @param errorMessage the error message or null to reset the error message
	 */
	public void setActionCodeValidationError(String errorMessage)
	{
		UserError componentError = errorMessage == null ? null : new UserError(errorMessage);
		actionCodeTextField.setComponentError(componentError);
	}

	/**
	 * Sets the action code.
	 *
	 * @param actionCode the action code
	 */
	public void setActionCode(String actionCode)
	{
		actionCodeTextField.setValue(actionCode);
	}

	private Component createContainerComponent()
	{
		ComponentFactory componentFactory = ComponentFactory.getInstance();

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		actionCodeTextField = componentFactory.createField(TextField.class, I18N.OFFERSELECTION_CAPTION_ACTIONCODE.msg());
		layout.addComponent(actionCodeTextField);
		actionCodeTextField.addTextChangeListener(event -> listeners.forEach(l -> l.actionCodeChanged(event.getText())));

		paymentMethodComboBox = componentFactory.createField(ComboBox.class, I18N.OFFERSELECTION_CAPTION_PAYMENTMETHOD.msg());
		paymentMethodComboBox.addValueChangeListener(this::select);
		paymentMethodComboBox.setTextInputAllowed(false);
		paymentMethodComboBox.setNullSelectionAllowed(false);

		layout.addComponent(paymentMethodComboBox);

		productOptionGroup = createProductOptionGroup();
		productOptionGroup.addValueChangeListener(this::select);
		layout.addComponent(productOptionGroup);

		validatableFields.add(actionCodeTextField);
		validatableFields.add(paymentMethodComboBox);
		validatableFields.add(productOptionGroup);

		return layout;
	}

	private OptionGroup createProductOptionGroup()
	{
		ComponentFactory componentFactory = ComponentFactory.getInstance();

		OptionGroup optionGroup = componentFactory.createField(OptionGroup.class);
		optionGroup.setMultiSelect(false);
		optionGroup.setHtmlContentAllowed(true);
		optionGroup.setItemCaptionPropertyId("description");
		BeanItemContainer bic = new BeanItemContainer<>(Product.class);
		bic.sort(new String[]{"createDate"},new boolean[]{true});
		optionGroup.setContainerDataSource(bic);
		return optionGroup;
	}

	private void select(Property.ValueChangeEvent e)
	{
		listeners.forEach(l -> l.paymentMethodChanged(getPaymentMethod()));
		listeners.forEach(l -> l.select(getProduct(), getPaymentMethod()));
	}

	public List<Field<?>> getValidatableFields()
	{
		return validatableFields;
	}
}
