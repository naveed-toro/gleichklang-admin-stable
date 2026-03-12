package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ProductSelection;
import de.binaerebauten.gleichklang.core.view.component.validator.ValidationComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.ValidationResult;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.List;
import java.util.Set;

public class SubscriptionOfferPaymentViewImpl
		extends AbstractNavigateView<SubscriptionOfferPaymentView.SubscriptionOfferPaymentViewListener>
		implements SubscriptionOfferPaymentView
{
	private final ProductSelection productSelection;
	private final ValidationComponent validationComponent;
	
	public SubscriptionOfferPaymentViewImpl()
	{
		VerticalLayout root = new VerticalLayout();
		HorizontalLayout layout=new HorizontalLayout();
		HorizontalLayout hl = new HorizontalLayout();

		Label l1= new Label("Gleichklang limited\n" +
				"Marienstr.38 \n" +
				"D-30171 Hannover");

		Label l2= new Label("Handelsregisternummer\n" +
				"HRB202495");
		Label l3= new Label("Umsatzsteueridentifikationsnr\n" +
				"DE246694217");
		Label l4= new Label("Englischer Sitz\n" +
				"69 Great Hampton Street\n" +
				"Birmingham, B186EW\n" +
				"Company No.:5669824");

		l1.setSizeFull();
		l2.setSizeFull();
		l3.setSizeFull();
		l4.setSizeFull();

		hl.addComponent(l1);
		hl.addComponent(l2);
		hl.addComponent(l3);
		hl.addComponent(l4);
		hl.setSpacing(true);
		hl.setMargin(true);
		hl.setSizeFull();


		final Label label = new Label(I18N.SUBSCRIPTIONOFFERVIEW_TITLE.msg());
		label.addStyleName(CssStyle.REGISTRATION_SUBSCRITPIONOFFERVIEW_TITLE.getStyleName());

		validationComponent = new ValidationComponent();
		validationComponent.setValidationStrategy(getValidationStrategy());
		
		productSelection = new ProductSelection();

		List<Field<?>> validatableFields = productSelection.getValidatableFields();
		validationComponent.addFields(validatableFields);

		root.addComponents(label ,validationComponent, productSelection);

		setCompositionRoot(root);
	}
	
	@Override
	public void setListener(SubscriptionOfferPaymentViewListener listener)
	{
		super.setListener(listener);
		productSelection.addProductSelectionListener(listener);
	}

	@Override
	public void setUserPaymentSettings(UserPaymentSettings userPaymentSettings)
	{
		productSelection.setActionCode(userPaymentSettings.getActionCode());
		productSelection.setPaymentMethod(userPaymentSettings.getPaymentMethod());
	}

	@Override
	public void setActionCodeIsValid(boolean isValid)
	{
		String validationError = isValid ? null : I18N.USER_VALIDATION_ACTIONCODEINVALID.msg();
		productSelection.setActionCodeValidationError(validationError);
	}
	
	@Override
	public void setAvailablePaymentMethods(Set<PaymentMethod> paymentMethods)
	{
		productSelection.setAvailablePaymentMethods(paymentMethods);
	}
	
	@Override
	public void setOffers(List<? extends SubscriptionOffer> offers)
	{
		productSelection.setProducts(offers);
	}
	
	@Override
	public ProductSelection getProductSelection()
	{
		return productSelection;
	}
	
	@Override
	public ValidationResult validate()
	{
		ValidationResult validationResult = validationComponent.validate();
		ProductSelection productSelection = getProductSelection();
		Product product = productSelection.getProduct();
		if (product == null)
		{
			validationResult.addOtherError(I18N.SUBSCRIPTION_OFFER_PRODUCT_ERROR.msg());
		}
		PaymentMethod paymentMethod = productSelection.getPaymentMethod();
		
		if (paymentMethod == null)
		{
			validationResult.addOtherError(I18N.SUBSCRIPTION_OFFER_PAYMENT_ERROR.msg());
		}
		validationComponent.showValidationResult(validationResult);
		
		return validationResult;
	}
	
	@Override
	public void commit()
	{
	}
	
	@Override
	public void save()
	{
	}

}
