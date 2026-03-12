package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.core.model.payment.BankAccount;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;

/**
 * Views all available bank accounts.
 */
public interface BankAccountView
		extends NavigateView<BankAccountView.BankAcoountViewListener>
{
	interface BankAcoountViewListener extends NavigateView.NavigateViewListener
	{

	}

	void setBankAccountHandler(LazyBeanItemContainer.LazyBeanItemsHandler<BankAccount> handler);
}
