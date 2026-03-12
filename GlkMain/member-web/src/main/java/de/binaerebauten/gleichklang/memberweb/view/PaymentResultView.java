package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.view.NavigateView;

/**
 * Shows the result of a payment. Realized as an separate view so that
 * the heidelpay integration can trigger this view with an uri fragment.
 */
public interface PaymentResultView
		extends NavigateView<NavigateView.NavigateViewListener>
{
	/**
	 * Shows the payment result for the given payment.
	 *
	 * @param paymentResultMessage the non-null payment result message
	 */
	void showPaymentResult(String paymentResultMessage);
}
