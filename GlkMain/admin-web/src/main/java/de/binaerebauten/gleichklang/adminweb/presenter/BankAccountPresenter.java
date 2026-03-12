package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.adminweb.view.BankAccountView;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.BankAccountRepository;
import org.springframework.context.ApplicationContext;

public class BankAccountPresenter extends NavigatePresenter
		implements BankAccountView.BankAcoountViewListener
{
	private final BankAccountRepository bankAccountRepository;

	private final BankAccountView view;

	public BankAccountPresenter(ApplicationContext ctx, BankAccountView view)
	{
		super(view);
		
		bankAccountRepository = ctx.getBean(BankAccountRepository.class);
		this.view = view;

		this.view.setListener(this);
	}

	@Override
	public void enter(String parameters)
	{
		this.view.setBankAccountHandler(bankAccountRepository::findAll);
	}
}
