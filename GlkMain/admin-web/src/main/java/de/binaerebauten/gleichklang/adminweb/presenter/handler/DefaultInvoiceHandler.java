package de.binaerebauten.gleichklang.adminweb.presenter.handler;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.adminweb.service.payment.BackofficePaymentService;
import de.binaerebauten.gleichklang.adminweb.view.popup.InvoicePopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.PaymentReceivedPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.PaymentRefundPopup;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment.PaymentType;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.PopupOpener;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.repository.SubscriptionRepository;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.stream.Collectors;

public class DefaultInvoiceHandler
{
	public interface RefreshListener
	{
		void refreshView();
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultInvoiceHandler.class);
	
	private final RefreshListener refreshListener;
	private final PopupOpener popupOpener;
	private final PaymentRepository paymentRepository;
	private final SubscriptionRepository subscriptionRepository;
	
	private final InvoiceService invoiceService;
	private final PaymentService paymentService;
	private final BackofficePaymentService backofficePaymentService;
	private final SubscriptionService subscriptionService;

	public DefaultInvoiceHandler(ApplicationContext ctx, RefreshListener refreshListener, PopupOpener popupOpener)
	{
		this.refreshListener = refreshListener;
		this.popupOpener = popupOpener;
		
		invoiceService = ctx.getBean(InvoiceService.class);
		paymentRepository = ctx.getBean(PaymentRepository.class);
		subscriptionRepository = ctx.getBean(SubscriptionRepository.class);

		paymentService = ctx.getBean(PaymentService.class);
		subscriptionService =  ctx.getBean(SubscriptionService.class);
		backofficePaymentService = ctx.getBean(BackofficePaymentService.class);
	}
	
	@Deprecated
	public void synchronizePendingExternalPayments()
	{
		backofficePaymentService.synchronizePendingExternalPayments();
	}
	
	@Deprecated
	//TODO can be removed or used later in a separated tab in userManagePopup
	public void setMemberToPrepayment(User user)
	{
		try
		{
			paymentService.updateUserPaymentSettings(user, PaymentMethod.PREPAYMENT);
		}
		catch (ObjectOptimisticLockingFailureException e)
		{
			Notification.show(I18N.INVOICEHANDLER_VALIDATION_CONCURRENTMODIFICTION.msg(), Type.ERROR_MESSAGE);
		}
		catch (PaymentException e)
		{
			Notification.show("Error in deregistration", e.getMessage(), Type.ERROR_MESSAGE);
		}
	}
	
	public void edit(Invoice invoice)
	{
		try
		{
			InvoicePopup popup = new InvoicePopup(invoice, this::paymentReceived, this::paymentRefund, this::cancelPayment,this::cancelUser);
			popup.addCloseListener(e -> refreshView());
			popupOpener.tryOpenPopup(popup);
		}
		catch (ObjectOptimisticLockingFailureException e)
		{
			Notification.show(I18N.INVOICEHANDLER_VALIDATION_CONCURRENTMODIFICTION.msg(), Type.ERROR_MESSAGE);
		}
	}
	
	private void refreshView()
	{
		if (refreshListener != null) refreshListener.refreshView();
	}
	
	private void paymentReceived(Popup sender, Prepayment prepayment)
	{
		final PaymentReceivedPopup paymentReceivedPopup = new PaymentReceivedPopup(comment -> paymentReceived(sender, prepayment, comment),prepayment.getComment());

		popupOpener.tryOpenPopup(paymentReceivedPopup);
	}
	
	private void paymentReceived(Popup sender, Prepayment prepayment, String comment)
	{
		try
		{
			prepayment.setComment(comment);
			invoiceService.paymentReceived(prepayment,null);
			refreshView();
		}
		catch (ObjectOptimisticLockingFailureException e)
		{
			Notification.show(I18N.INVOICEHANDLER_VALIDATION_CONCURRENTMODIFICTION.msg(), Type.ERROR_MESSAGE);
		}
		if (sender != null) sender.close();
	}
	
	private void paymentRefund(AbstractPayment paymentToRefund)
	{
		try
		{
			PaymentRefundPopup paymentRefundPopup = new PaymentRefundPopup(paymentToRefund, this::refundPayment);
			paymentRefundPopup.addCloseListener(event -> refreshView());
			popupOpener.tryOpenPopup(paymentRefundPopup);
		}
		catch (ObjectOptimisticLockingFailureException e)
		{
			Notification.show(I18N.INVOICEHANDLER_VALIDATION_CONCURRENTMODIFICTION.msg(), Type.ERROR_MESSAGE);
		}
	}
	
	public void refundPayment(AbstractPayment paymentToRefund, MonetaryAmount refundAmount, boolean usePrepayment)
	{
		try
		{
			invoiceService.refundPayment(paymentToRefund, refundAmount, usePrepayment);
		}
		catch (PaymentException e)
		{
			LOG.error("refundPayment exception", e);
			Notification.show(I18N.INVOICEHANDLER_VALIDATION_REFUNDFAILED.msg(), Type.ERROR_MESSAGE);
		}
		catch (ObjectOptimisticLockingFailureException e)
		{
			LOG.error("refundPayment exception", e);
			Notification.show(I18N.INVOICEHANDLER_VALIDATION_CONCURRENTMODIFICTION.msg(), Type.ERROR_MESSAGE);
		}
	}
	
	private void cancelPayment(AbstractPayment payment, boolean createPrepayment)
	{
		payment.setState(PaymentState.CANCELED);
		paymentRepository.saveAndFlush(payment);
		
		if (createPrepayment)
		{
			paymentService.createAndSavePayment(payment.getInvoice(), PaymentMethod.PREPAYMENT, payment.getAmount(), payment.getCurrent() != null);
		}
	}
	private void cancelUser(User user,boolean userCancel)
	{

		if (userCancel)
		{
			subscriptionService.cancelUser(user);
			subscriptionRepository.cancelSubscription(user.getId());
		}
	}
	public void paymentReceived(Invoice invoice, boolean askForComment)
	{
		final List<AbstractPayment> pendingPayments = invoice.getPayments().stream().filter(p -> PaymentState.PENDING.equals(p.getState())).collect(Collectors.toList());
		
		if (invoice.getItems().stream().anyMatch(i -> i.getProduct() instanceof Chargeback))
		{
			Notification.show(I18N.INVOICEHANDLER_VALIDATION_CONTAINSCHARGEBACK.msg(), Type.ERROR_MESSAGE);
			return;
		}
		
		if (pendingPayments.isEmpty())
		{
			Notification.show(I18N.INVOICEHANDLER_VALIDATION_NOPENDINGPAYMENTS.msg(), Type.ERROR_MESSAGE);
			return;
		}
		
		if (pendingPayments.size() > 1)
		{
			Notification.show(I18N.INVOICEHANDLER_VALIDATION_MORETHANONEPENDINGPAYMENT.msg(), Type.ERROR_MESSAGE);
			return;
		}
		
		final AbstractPayment pendingPayment = pendingPayments.iterator().next();
		
		if (!PaymentType.PREPAYMENT.equals(pendingPayment.getPaymentType()))
		{
			Notification.show(I18N.INVOICEHANDLER_VALIDATION_NOPREPAYMENT.msg(), Type.ERROR_MESSAGE);
			return;
		}
		
		if (askForComment)
		{
			paymentReceived(null, (Prepayment) pendingPayment);
		}
		else
		{
			paymentReceived(null, (Prepayment) pendingPayment, null);
		}
	}
}
