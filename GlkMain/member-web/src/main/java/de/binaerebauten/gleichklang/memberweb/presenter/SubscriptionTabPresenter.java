package de.binaerebauten.gleichklang.memberweb.presenter;

import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionTabView;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Presenter for a subscription view tab.
 */
abstract class SubscriptionTabPresenter extends NavigatePresenter
{
	SubscriptionTabPresenter(NavigateView<?> view)
	{
		super(view);
	}
	
	protected void showPaymentInfo(SubscriptionTabView view, Collection<AbstractPayment> payments)
	{
		payments.stream().findFirst().ifPresent(payment -> showPaymentInfo(view, payment));
	}
	
	/**
	 * Shows the payment information.
	 * The pending payment doesn't allow user to perform any further purchases
	 * before it is paid.
	 *
	 * @param view
	 * @param payment
	 */
	protected void showPaymentInfo(SubscriptionTabView view, AbstractPayment payment)
	{
		if (payment.getAmount().isFreeOfCharge())
		{
			view.setPaymentInfo(I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PURCHASED.msg());
			return;
		}
		
		final Product product = payment.getBaseProduct();
		
		final String productType = product != null ? product.getProductType().toString() : "";
		final String paymentPurpose = product != null ? product.getName() : "";
		
		if (payment.isRefund())
		{
			view.setPaymentInfo(I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PROCESSING_REFUND.msg(productType, paymentPurpose));
		}
		else
		{
			switch (payment.getPaymentType())
			{
				case EXTERNAL_PAYMENT:
					view.setPaymentInfo(I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PROCESSING_CC_OR_DD.msg(productType, paymentPurpose));
					break;
				case PREPAYMENT:
					final String prepaymentInfo = payment.getTranslatedInfo();
					Objects.requireNonNull(prepaymentInfo, "prepaymentInfo == null");
					
					view.setPaymentInfo(I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PROCESSING_PREPAYMENT.msg(productType, paymentPurpose, prepaymentInfo));
					break;
			}
		}
	}
	
	/**
	 * Shows the pending state of the registration.
	 *
	 * @param view
	 */
	protected void showPendingRegistration(SubscriptionTabView view)
	{
		view.setPaymentInfo(I18N.SUBSCRIPTIONPRESENTER_PENDING_REGISTRATION.msg());
	}
	
	/**
	 * Shows the payment accepted.
	 */
	protected void showPurchased(SubscriptionTabView view, AbstractPayment payment)
	{
		if (payment instanceof Prepayment)
		{
			showPurchased(view, (Prepayment) payment);
		}
		else if (payment instanceof ExternalPayment)
		{
			showPurchased(view, (ExternalPayment) payment);
		}
	}
	
	/**
	 * Shows the prepayment accepted.
	 */
	protected void showPurchased(SubscriptionTabView view, Prepayment prepayment)
	{
		String prepaymentInfo = prepayment.getTranslatedInfo();
		Objects.requireNonNull(prepaymentInfo, "prepaymentInfo == null");
		
		if (prepayment.getAmount().isFreeOfCharge())
		{
			view.setPaymentInfo(I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PURCHASED.msg());
		}
		else
		{
			String message = I18N.SUBSCRIPTIONPRESENTER_UPGRADE_OFFER_PURCHASED_WITH_PREPAYMENT.msg(prepaymentInfo);
			Optional<InvoiceItem> invoiceItem = prepayment.getInvoice().getItems().stream().findFirst();
			if (invoiceItem.isPresent())
			{
				Product product = invoiceItem.get().getProduct();
				if (product instanceof UpgradeOffer)
				{
					message = I18N.SUBSCRIPTIONPRESENTER_UPGRADE_OFFER_PURCHASED_WITH_PREPAYMENT.msg(prepaymentInfo);
				}
				else if (product instanceof ServiceOffer)
				{
					message = I18N.SUBSCRIPTIONPRESENTER_SERVICE_OFFER_PURCHASED_WITH_PREPAYMENT.msg(prepaymentInfo);
				}
			}
			view.setPaymentInfo(message);
		}
	}
	
	/**
	 * Shows the external payment accepted.
	 */
	protected void showPurchased(SubscriptionTabView view, ExternalPayment externalPayment)
	{
		if (externalPayment.getAmount().isFreeOfCharge())
		{
			view.setPaymentInfo(I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PURCHASED.msg());
		}
		else
		{
			String message = I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PURCHASED.msg();
			Optional<InvoiceItem> invoiceItem = externalPayment.getInvoice().getItems().stream().findFirst();
			if (invoiceItem.isPresent())
			{
				Product product = invoiceItem.get().getProduct();
				if (product instanceof UpgradeOffer)
				{
					message = I18N.SUBSCRIPTIONPRESENTER_UPGRADE_OFFER_PURCHASED_WITH_CC_OR_DD.msg();
				}
				else if (product instanceof ServiceOffer)
				{
					message = I18N.SUBSCRIPTIONPRESENTER_SERVICE_OFFER_PURCHASED_WITH_CC_OR_DD.msg();
				}
			}
			view.setPaymentInfo(message);
		}
	}
	
}
