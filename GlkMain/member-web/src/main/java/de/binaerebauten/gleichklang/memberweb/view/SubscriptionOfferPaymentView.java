package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.ValidatableView;
import de.binaerebauten.gleichklang.core.view.component.ExternalPaymentForm;
import de.binaerebauten.gleichklang.core.view.component.ProductSelection;

import java.util.List;
import java.util.Set;

/**
 * Initially shows an {@link ProductSelection} and shows a {@link
 * ExternalPaymentForm} when the user subscribed to the selected offer and
 * payment method.
 */
public interface SubscriptionOfferPaymentView
		extends NavigateView<SubscriptionOfferPaymentView.SubscriptionOfferPaymentViewListener>, ValidatableView, RegistrationStepView
{
	interface SubscriptionOfferPaymentViewListener extends NavigateView.NavigateViewListener, ProductSelection.ProductSelectionListener
	{
	}
	
	/**
	 * Sets the user payment settings.
	 *
	 * @param userPaymentSettings the user payment settings to show
	 */
	void setUserPaymentSettings(UserPaymentSettings userPaymentSettings);

	/**
	 * Sets the action code validation flag.
	 *
	 * @param isValid true iff. the action code is valid.
	 */
	void setActionCodeIsValid(boolean isValid);
	
	/**
	 * Sets available payment methods.
	 * External payment methods could be disabled with application properties.
	 */
	void setAvailablePaymentMethods(Set<PaymentMethod> paymentMethods);

	/**
	 * Sets the subscription offers that the user can choose from.
	 *
	 * @param offers the subscription offers to show
	 */
	void setOffers(List<? extends SubscriptionOffer> offers);
	
	ProductSelection getProductSelection();
}
