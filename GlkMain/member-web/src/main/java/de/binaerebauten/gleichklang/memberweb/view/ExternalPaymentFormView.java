package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.view.NavigateView;

import java.net.URI;

/**
 * Shows the external payment form view.
 */
public interface ExternalPaymentFormView extends NavigateView<ExternalPaymentFormView.ExternalPaymentFormViewListener>, RegistrationStepView
{
	interface ExternalPaymentFormViewListener extends NavigateView.NavigateViewListener
	{}

	/**
	 * Sets the payment form url to show in this view.
	 *
	 * @param paymentFormUrl the url to view
	 */
	void setPaymentFormUrl(URI paymentFormUrl);
}
