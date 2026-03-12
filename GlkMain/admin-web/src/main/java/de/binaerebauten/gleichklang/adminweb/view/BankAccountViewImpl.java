package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.locatable.Country_;
import de.binaerebauten.gleichklang.core.model.payment.BankAccount;
import de.binaerebauten.gleichklang.core.model.payment.BankAccount_;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;

/**
 * Currently just views all bank accounts and doesn't allow editing of bank accounts.
 */
public class BankAccountViewImpl
		extends AbstractNavigateView<BankAccountView.BankAcoountViewListener>
		implements BankAccountView
{
	private final LazyBeanTable<BankAccount> bankAccountTable;

	public BankAccountViewImpl()
	{
		bankAccountTable = createBankAccountTable();

		final VerticalLayout layout = new VerticalLayout();
		layout.addComponent(bankAccountTable);
		layout.setSizeFull();
		layout.setMargin(true);

		setCompositionRoot(layout);
	}

	private LazyBeanTable<BankAccount> createBankAccountTable()
	{
		final LazyBeanTable<BankAccount> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setSizeFull();
		
		table.addContainerProperty(I18N.BANKACCOUNT_HEADER_ACTIVE.msg(), BankAccount_.active);
		table.addContainerProperty(I18N.BANKACCOUNT_HEADER_BANKNAME.msg(), BankAccount_.bankName);
		table.addContainerProperty(I18N.BANKACCOUNT_HEADER_BANKNUMBER.msg(), BankAccount_.bankNumber);
		table.addContainerProperty(I18N.BANKACCOUNT_HEADER_ACCOUNTNUMBER.msg(), BankAccount_.accountNumber);
		table.addContainerProperty(I18N.BANKACCOUNT_HEADER_HOLDER.msg(), BankAccount_.holder);
		table.addContainerProperty(I18N.BANKACCOUNT_HEADER_BIC.msg(), BankAccount_.bic);
		table.addContainerProperty(I18N.BANKACCOUNT_HEADER_IBAN.msg(), BankAccount_.iban);
		table.addContainerProperty(I18N.BANKACCOUNT_HEADER_COUNTRY.msg(), BankAccount_.country, Country_.i18nKey);

		return table;
	}

	@Override
	public void setBankAccountHandler(LazyBeanItemContainer.LazyBeanItemsHandler<BankAccount> handler)
	{
		bankAccountTable.setHandler(handler);
	}
}
