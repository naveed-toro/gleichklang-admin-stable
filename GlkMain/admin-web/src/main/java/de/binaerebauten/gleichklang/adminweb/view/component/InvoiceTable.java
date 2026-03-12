package de.binaerebauten.gleichklang.adminweb.view.component;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import de.binaerebauten.gleichklang.adminweb.view.I18N;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.repository.PrepaymentRepository;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.TableControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public class InvoiceTable extends CustomComponent
{
	public interface InvoiceHandler
	{
		/**
		 * Fired when the user presses the refresh view button.
		 */
		void refreshInvoiceTable();
		
		void synchronizePendingExternalPayments();
		
		/**
		 * Fired when the admin presses the edit button.
		 * @param invoice the invoice to edit
		 */
		void edit(Invoice invoice);
		
		void paymentReceived(Invoice invoice, boolean askForComment);
	}
	
	public interface OpenUserHandler
	{
		void openUserPopup(Invoice invoice);
	}

	@Autowired
	PrepaymentRepository prepaymentRepository;
	private final TableControl<Invoice> tableControl;
	private final LazyBeanTable<Invoice> invoiceTable;
	
	public InvoiceTable()
	{
		invoiceTable = createInvoiceTable();
		
		tableControl = new TableControl<>(invoiceTable);
		
		setCompositionRoot(tableControl);
	}

	private LazyBeanTable<Invoice> createInvoiceTable()
	{
		final LazyBeanTable<Invoice> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();

		table.addContainerProperty(I18N.INVOICE_HEADER_USERALIAS.msg(), Invoice_.user, User_.alias);
		table.addContainerProperty(I18N.INVOICE_HEADER_USEREMAIL.msg(), Invoice_.user, User_.email);
		table.addContainerProperty(I18N.INVOICE_HEADER_DATE.msg(), Invoice_.createDate);

		table.addGeneratedColumn(I18N.INVOICE_HEADER_NEWESTPAYMENTMETHOD.msg(),(invoice) -> invoice.getNewestPaymentMethod() == PaymentMethod.PREPAYMENT ? getPrepaymentMethod(invoice) : invoice.getNewestPaymentMethod());
		table.addGeneratedColumn(I18N.INVOICE_HEADER_AMOUNT.msg(), Invoice::getAmount);
		table.addGeneratedColumn(I18N.INVOICE_HEADER_NEWESTPAYMENTSTATE.msg(), Invoice::getNewestPaymentState);
		table.addGeneratedColumn(I18N.INVOICE_HEADER_EXTERNAL_REFERENCE_ID.msg(),
				invoice -> invoice.getNewestPayment().isPresent() ? invoice.getNewestPayment().get().getExternalReferenceId() : "");

		return table;
	}

	private Button getPrepaymentMethod(Invoice invoice) {
		final Button button = new Button(String.valueOf(PaymentMethod.PREPAYMENT));
		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				prepaymentRepository = AppUI.getApplicationContext().getBean(PrepaymentRepository.class);
				Prepayment prepayment = prepaymentRepository.findByUser(invoice,invoice.getNewestPaymentState(),PaymentMethod.PREPAYMENT);
				if(prepayment!=null) {
					MessageBox.show(prepayment.getTranslatedInfo());
				}
			}
		});
		return button;
	}
	
	public void setTableHandler(LazyBeanFilteredItemsHandler<Invoice> handler)
	{
		invoiceTable.setHandler(handler);
	}
	
	public void replaceFilter(Specification<Invoice> oldSpecification, Specification<Invoice> newSpecification)
	{
		invoiceTable.replaceFilter(oldSpecification, newSpecification);
	}
	
	public void removeButtons()
	{
		tableControl.removeAdditionalButtons();
	}
	
	public void addControlButtons(InvoiceHandler invoiceHandler)
	{
		Objects.requireNonNull(invoiceHandler);
		
		tableControl.addButton(I18N.INVOICE_ACTION_EDIT.msg(), invoiceHandler::edit);
		tableControl.addButton(I18N.PAYMENT_CAPTION_REFRESHVIEW.msg(), invoiceHandler::refreshInvoiceTable);
		tableControl.addButton(I18N.INVOICE_ACTION_PAYMENTRECEIVED.msg(), invoice -> invoiceHandler.paymentReceived(invoice, false), this::hasPendingPayments);
		tableControl.addButton(I18N.INVOICE_ACTION_PAYMENTRECEIVEDWITHCOMMENT.msg(), invoice -> invoiceHandler.paymentReceived(invoice, true), this::hasPendingPayments);
		tableControl.addButton(I18N.INVOICE_ACTION_REFUNDSENT.msg(), invoice -> invoiceHandler.paymentReceived(invoice, false), this::hasRefund);
		
		// @deprecated nicht sinnvoll, da nur der payment state umgesetzt wird, aber z.B. die subscription nicht aktiviert wird
		//		Button synchronizePendingPaymentsButton = new Button(I18N.PAYMENT_CAPTION_SYNCHRONIZEPENDINGPAYMENTS.msg());
		//		synchronizePendingPaymentsButton.addClickListener(clickEvent -> invoiceHandler.synchronizePendingExternalPayments());
		//		buttonLayout.addComponent(synchronizePendingPaymentsButton);
	}
	
	private boolean hasRefund(Invoice item)
	{
		return item.getPayments().stream().filter(p -> PaymentState.PENDING.equals(p.getState())).anyMatch(AbstractPayment::isRefund);
	}
	
	private boolean hasPendingPayments(Invoice item)
	{
		return item.getPayments().stream().filter(p -> PaymentState.PENDING.equals(p.getState())).anyMatch(p -> !p.isRefund());
	}
	
	public void addOpenUserButton(OpenUserHandler openUserHandler)
	{
		Objects.requireNonNull(openUserHandler);
		
		tableControl.addButton(I18N.INVOICE_ACTION_OPENUSER.msg(), openUserHandler::openUserPopup);
	}
}
