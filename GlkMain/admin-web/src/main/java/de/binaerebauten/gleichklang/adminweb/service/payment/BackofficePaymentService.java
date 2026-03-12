package de.binaerebauten.gleichklang.adminweb.service.payment;

/**
 * This interface defines all backoffice related operations that an
 * external payment service needs to provide.
 */
public interface BackofficePaymentService
{
	/**
	 * Synchronizes the persisted incomplete external payments with the external payment services.
	 * <p/>
	 * An external payment can be pending when the gleichklang server wasn't available
	 * when the heidelpay server send the payment received message.
	 */
	@Deprecated
	void synchronizePendingExternalPayments();
	
	void synchronizeRegistrations();
	
	void synchronizeRefunds();
	
	void synchronizeChargebacks();
}
