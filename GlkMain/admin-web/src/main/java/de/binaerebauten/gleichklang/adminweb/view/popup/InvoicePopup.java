package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.google.common.collect.Lists;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.DialogResult;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxButtons;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxStyle;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import org.springframework.data.domain.PageImpl;

import java.util.Set;

/**
 * This popup show the items and payments of an invoice {@link Invoice}.
 *
 * It allows to mark a prepayment as received
 * {@link de.binaerebauten.gleichklang.adminweb.view.popup.InvoicePopup.PaymentReceivedCallback}.
 *
 * It allows to refund a payment
 * {@link de.binaerebauten.gleichklang.adminweb.view.popup.InvoicePopup.PaymentRefundCallback}.
 *
 * It allows to cancel a payment
 * {@link de.binaerebauten.gleichklang.adminweb.view.popup.InvoicePopup.PaymentCancelCallback}.
 */
public class InvoicePopup extends Popup
{
	public interface PaymentReceivedCallback
	{
		/**
		 * Fired when the user sets the given prepayment to received.
		 *
		 * @param prepayment the received prepayment
		 */
		void paymentReceived(InvoicePopup sender, Prepayment prepayment);
	}

	public interface PaymentRefundCallback
	{
		/**
		 * Fired when the admin presses the payment refund button
		 *
		 * @param paymentToRefund the payment to refund
		 */
		void paymentRefund(AbstractPayment paymentToRefund);
	}
	
	public interface PaymentCancelCallback
	{
		/**
		 * Fired when the admin presses the cancel payment button
		 *
		 * @param payment the payment to cancel and replace with a new prepayment
		 * @param createPrepayment whether a new prepayment should be created instead with the same amount and invoice
		 */
		void cancelPayment(AbstractPayment payment, boolean createPrepayment);
	}

	public interface UserCancelCallback
	{
		/**
		 * Fired when the admin presses the cancel user yes button after cancel prepayment of user
		 *
		 * @param user the user to cancel
		 * @param cancelUser whether a user should be cancelled
		 */
		void cancelUser(User user, boolean cancelUser);
	}

	private final Invoice invoice;

	private final PaymentReceivedCallback paymentReceivedCallback;

	private final PaymentRefundCallback paymentRefundCallback;

	private final PaymentCancelCallback paymentCancelCallback;

	private final UserCancelCallback userCancelCallback;

	/**
	 * Creates a new popup with the given parameters.
	 *
	 * @param invoice
	 * @param paymentReceivedCallback
	 */
	public InvoicePopup(Invoice invoice, PaymentReceivedCallback paymentReceivedCallback,
			PaymentRefundCallback paymentRefundCallback,
			PaymentCancelCallback paymentCancelCallback,
			UserCancelCallback userCancelCallback)
	{
		super(I18N.INVOICEPOPUP_CAPTION_TITLE.msg());

		this.invoice = invoice;
		this.paymentReceivedCallback = paymentReceivedCallback;
		this.paymentRefundCallback = paymentRefundCallback;
		this.paymentCancelCallback = paymentCancelCallback;
		this.userCancelCallback = userCancelCallback;

		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);

		TabSheet tabSheet = createTabSheet();
		layout.addComponent(tabSheet);

		layout.setSizeFull();

		setContent(layout);
	}

	private TabSheet createTabSheet() {
		TabSheet tabSheet = new TabSheet();

		tabSheet.addTab(createItemsPanel(), I18N.INVOICE_TAB_ITEMS.msg());
		tabSheet.addTab(createPaymentsPanel(), I18N.INVOICE_TAB_PAYMENTS.msg());
		tabSheet.setSizeFull();

		return tabSheet;
	}

	private Component createItemsPanel()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);

		layout.addComponent(createItemsTable());
		layout.setSizeFull();

		return layout;
	}

	private LazyBeanTable<InvoiceItem> createItemsTable() {
		LazyBeanTable<InvoiceItem> itemsTable = new LazyBeanTable<>();

		itemsTable.addContainerProperty(I18N.INVOICEITEM_HEADER_PRODUCTNAME.msg(), InvoiceItem_.product, Product_.name);
		itemsTable.addContainerProperty(I18N.INVOICEITEM_HEADER_AMOUNT.msg(), InvoiceItem_.amount);
		itemsTable.addContainerProperty(I18N.INVOICEITEM_HEADER_SUBSCRIPTIONBEGIN.msg(), InvoiceItem_.subscription, Subscription_.begin);
		itemsTable.addContainerProperty(I18N.INVOICEITEM_HEADER_SUBSCRIPTIONEND.msg(), InvoiceItem_.subscription, Subscription_.end);

		itemsTable.setHandler((specification, pageable) -> new PageImpl<>(Lists.newArrayList(invoice.getItems())));

		itemsTable.setSizeFull();

		return itemsTable;
	}

	private Component createPaymentsPanel() {
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);

		LazyBeanTable<AbstractPayment> paymentsTable = createPaymentsTable();
		layout.addComponent(paymentsTable);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		layout.addComponent(buttonLayout);
		
		Button paymentReceivedButton = new Button(I18N.PAYMENT_ACTION_PAYMENTRECEIVED.msg());
		paymentReceivedButton.setEnabled(true); // TODO : task 1 changes
		buttonLayout.addComponent(paymentReceivedButton);
		paymentsTable.addValueChangeListener(selection -> updatePaymentReceivedButton(paymentReceivedButton, selection));
		paymentReceivedButton.addClickListener(clickEvent -> paymentReceivedCallback.paymentReceived(this, (Prepayment) paymentsTable.getValue()));
		
		Button paymentRefundButton = new Button(I18N.PAYMENT_ACTION_PAYMENTREFUND.msg());
		paymentRefundButton.setEnabled(false);
		buttonLayout.addComponent(paymentRefundButton);
		paymentsTable.addValueChangeListener(selection -> updatePaymentRefundButton(paymentRefundButton, selection));
		paymentRefundButton.addClickListener(clickEvent ->
		{
			AbstractPayment payment = paymentsTable.getValue();
			paymentRefundCallback.paymentRefund(payment);
			close();
		});
		
		Button cancelButton = new Button(I18N.PAYMENT_ACTION_PAYMENTCANCEL.msg());
		cancelButton.setEnabled(false);
		buttonLayout.addComponent(cancelButton);
		paymentsTable.addValueChangeListener(selection -> updatePaymentCancelButton(cancelButton, selection));
		cancelButton.addClickListener(clickEvent ->
				MessageBox.show(I18N.PAYMENT_ACTION_PAYMENTCANCEL.msg(),
						I18N.PAYMENT_ACTION_PAYMENTCANCEL_CREATE_PREPAYMENT.msg(),
						MessageBoxButtons.YES_NO_CANCEL, MessageBoxStyle.QUESTION,
						dialogResult -> {
					if (dialogResult == DialogResult.YES)
					{
						AbstractPayment payment = paymentsTable.getValue();
						paymentCancelCallback.cancelPayment(payment, true);
						close();
					}
					else if (dialogResult == DialogResult.NO)
					{
						AbstractPayment payment = paymentsTable.getValue();
						paymentCancelCallback.cancelPayment(payment, false);
						close();
						MessageBox.show(I18N.PAYMENT_ACTION_USERCANCEL.msg(),
								I18N.PAYMENT_ACTIONUSERCANCEL_CREATE_PREPAYMENT.msg(),
								MessageBoxButtons.YES_NO_CANCEL, MessageBoxStyle.QUESTION,
								dialogResultUser -> {
									if (dialogResultUser == DialogResult.YES) {
										userCancelCallback.cancelUser(payment.getUser(),true);
										close();
									}
									else if (dialogResult == DialogResult.NO)
									{
										close();
									}
								});
					}
				}));

		return layout;
	}

	private LazyBeanTable<AbstractPayment> createPaymentsTable() {
		LazyBeanTable<AbstractPayment> paymentsTable = new LazyBeanTable<>();
		paymentsTable.setSelectable(true);

		paymentsTable.addContainerProperty(I18N.PAYMENT_HEADER_CREATEDATE.msg(), AbstractPayment_.createDate);
		paymentsTable.addContainerProperty(I18N.PAYMENT_HEADER_CHANGEDATE.msg(), AbstractPayment_.changeDate);
		paymentsTable.addGeneratedColumn(I18N.PAYMENT_HEADER_PAYMENTMETHOD.msg(), AbstractPayment::getMethod);
		paymentsTable.addContainerProperty(I18N.PAYMENT_HEADER_STATE.msg(), AbstractPayment_.state);
		paymentsTable.addContainerProperty(I18N.PAYMENT_HEADER_AMOUNT.msg(), AbstractPayment_.amount);
		paymentsTable.addContainerProperty(I18N.PAYMENT_HEADER_EXTERNAL_REFERENCE_ID.msg(), AbstractPayment_.externalReferenceId);
		paymentsTable.addContainerProperty(I18N.PAYMENT_COMMENT.msg(), AbstractPayment_.comment);
		paymentsTable.setHandler((specification, pageable) -> new PageImpl<>(Lists.newArrayList(invoice.getPayments())));

		paymentsTable.setSizeFull();

		return paymentsTable;
	}

	private void updatePaymentReceivedButton(Button paymentReceivedButton, Set<AbstractPayment> selection) {
		boolean enablePaymentReceived = false;
		if (selection.size() == 1)
		{
			AbstractPayment selectedPayment = selection.iterator().next();
			enablePaymentReceived =  PaymentMethod.PREPAYMENT.equals(selectedPayment.getMethod())
					&& PaymentState.PENDING.equals(selectedPayment.getState());
		}
		paymentReceivedButton.setEnabled(true); // TODO : Task 1 changes
	}


	private void updatePaymentRefundButton(Button paymentRefundButton, Set<AbstractPayment> selection)
	{
		boolean enabled = false;
		if(selection.size() == 1)
		{
			final AbstractPayment payment = selection.iterator().next();
			
			enabled = payment.getState() == PaymentState.PAID && !payment.getAmount().isRefund();
		}
		paymentRefundButton.setEnabled(enabled);
	}
	
	private void updatePaymentCancelButton(Button paymentRefundButton, Set<AbstractPayment> selection)
	{
		boolean enabled = false;
		if (selection.size() == 1)
		{
			AbstractPayment payment = selection.iterator().next();
			enabled = payment.getState() != PaymentState.PAID && payment.getState() != PaymentState.CANCELED;
		}
		paymentRefundButton.setEnabled(enabled);
	}
}
