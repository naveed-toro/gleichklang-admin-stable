package de.binaerebauten.gleichklang.core.service.payment;

import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.user.User;

import java.util.EnumSet;
import java.util.Set;

/**
 * This interface describes all external payment operations.
 *
 * @author matthias.koester@binaere-bauten.de
 */
public interface ExternalPaymentService
{
	/**
	 * AbstractPayment options for which an external payment service is used.
	 */
	Set<PaymentMethod> PAYMENT_OPTIONS =
			EnumSet.of(PaymentMethod.CREDIT_CARD, PaymentMethod.DIRECT_DEBIT);

	/**
	 * Either gets the existing external payment registration for the given user
	 * and registration id if it exists. Or create a new external registration otherwise.
	 *
	 * @param user           the user
	 * @param registrationId the registration id
	 * @return the external payment registration, always non null
	 */
	ExternalPaymentRegistration getOrCreateExternalPaymentRegistration(User user, String registrationId);

	/**
	 * Returns true iff. the given user is registered in the external payment system.
	 *
	 * @param user the non-null user
	 * @return true iff. the given user is registered in the external payment system
	 */
	boolean isRegistered(User user);

	/**
	 * Creates and persist a new external payment registration from the given parameters.
	 *
	 * @param user           the non-null user
	 * @param registrationId the nullable registration id
	 * @return the newly created and persisted external payment registration
	 */
	ExternalPaymentRegistration createExternalPaymentRegistration(User user, String registrationId);

	/**
	 * Request a payment with the given parameters.
	 *
	 * @param externalPayment the non-null external payment
	 * @throws PaymentException
	 */
	void requestPayment(ExternalPayment externalPayment)
			throws PaymentException;

	/**
	 * Requests a refund with the given parameters.
	 *
	 * @param paymentToRefund the non-null payment to refund
	 * @param refundPayment   the non-null refund payment
	 * @throws PaymentException
	 */
	void requestRefund(ExternalPayment paymentToRefund, ExternalPayment refundPayment)
			throws PaymentException;

	/**
	 * Deregisters the given user in the external payment system and delete the users
	 * {@link ExternalPaymentRegistration}.
	 *
	 * @param user the non-null user to deregister
	 * @throws PaymentException
	 */
	void deregister(User user) throws PaymentException;

	/**
	 * Returns true iff. the given user uses external payment and is registered.
	 * @param user the non-null user
	 * @return true iff. the given user uses external payment.
	 */
	boolean usesExternalPayment(User user);
}
