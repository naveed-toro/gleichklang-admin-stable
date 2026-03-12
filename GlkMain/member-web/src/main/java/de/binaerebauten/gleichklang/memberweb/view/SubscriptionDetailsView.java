package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.view.NavigateView;

import java.net.URI;
import java.util.Set;

/**
 * Views the users newest subscription.
 */
public interface SubscriptionDetailsView extends SubscriptionTabView, NavigateView<SubscriptionDetailsView.SubscriptionViewListener>
{
	interface SubscriptionViewListener extends NavigateView.NavigateViewListener
	{
		void changePaymentData(PaymentMethod paymentMethod);

		void changePaymentMethod(PaymentMethod to);
		
		void activateAutoRenewal(Subscription subscription);
	}

	/**
	 * Sets the users newest subscription with state.
	 *
	 * @param subscription the nullable subscription
	 */
	void setSubscription(Subscription subscription, String additionalInfo);

	void setUserPaymentSettings(UserPaymentSettings userPaymentSettings);
	
	/**
	 * Sets available payment methods.
	 * External payment methods could be disabled with application properties.
	 */
	void setAvailablePaymentMethods(Set<PaymentMethod> paymentMethods);

	/**
	 * The change payment data button is only enabled when the user is
	 * registered at the external payment system.
	 *
	 * @param userIsRegistered the userIsRegistered flag
	 */
	void setUserIsRegistered(boolean userIsRegistered);
	
	void showPaymentFormView(URI paymentFormUrl);
}
