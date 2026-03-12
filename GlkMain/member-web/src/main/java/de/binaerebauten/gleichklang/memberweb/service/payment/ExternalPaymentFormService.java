package de.binaerebauten.gleichklang.memberweb.service.payment;

import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;

import java.net.URI;

/**
 * This service encapsulates the retrieval of external payment form url that are
 * displayed as an iframe in the member-web app.
 */
public interface ExternalPaymentFormService
{
	/**
	 * Returns the initial registration form url for this payment service.
	 * This url is used to show an external form page which allows the user to initiate
	 * a payment.
	 *
	 * @param externalPayment the external payment for which the payment form url should be generated
	 * @return the payment form url
	 */
	URI getInitialRegistrationFormUrl(ExternalPayment externalPayment)
			throws PaymentException;

	/**
	 * Returns the payment data update form url for this payment service.
	 * This url is used to show an external form page which allows the user to change
	 * the payment data for further payments.
	 *
	 * @param user          the payment data update request
	 * @param paymentMethod the payment method
	 * @return the payment data update form url
	 */
	URI getPaymentDataUpdateFormUrl(User user, PaymentMethod paymentMethod)
			throws PaymentException;

	/**
	 * Returns the payment data change form url for this payment service.
	 * This url is used to show an external form page which allows the user to change
	 * the payment data for further payments.
	 *
	 * @param user          the payment data update request
	 * @param paymentMethod the payment method
	 * @return the payment data form change url
	 */
	URI getPaymentMethodChangeFormUrl(User user, PaymentMethod paymentMethod)
			throws PaymentException;
}
