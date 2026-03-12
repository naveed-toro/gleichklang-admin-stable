package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.adminweb.presenter.handler.DefaultInvoiceHandler;
import de.binaerebauten.gleichklang.adminweb.presenter.handler.UserControlHandler;
import de.binaerebauten.gleichklang.adminweb.view.InvoiceAdminView;
import de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType;
import de.binaerebauten.gleichklang.core.model.payment.Invoice;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.presenter.filter.DefaultFilterControlHandler;
import de.binaerebauten.gleichklang.core.repository.InvoiceRepository;
import de.binaerebauten.gleichklang.core.service.FilterControlService;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlFeature;
import org.springframework.context.ApplicationContext;

/**
 * Presenter for the {@link InvoiceAdminView} in the admin-web app.
 */
public class InvoiceAdminPresenter extends NavigatePresenter implements InvoiceAdminView.InvoiceAdminViewListener
{
	private final InvoiceRepository invoiceRepository;
	private final DefaultFilterControlHandler filterControlHandler;
	private final DefaultInvoiceHandler defaultInvoiceHandler;
	private final FilterSpecificationBuilder filterSpecificationBuilder;
	private final InvoiceAdminView view;
	private final UserControlHandler userControlHandler;

	public InvoiceAdminPresenter(ApplicationContext ctx, InvoiceAdminView view)
	{
		super(view);
		
		invoiceRepository = ctx.getBean(InvoiceRepository.class);

		filterControlHandler = new DefaultFilterControlHandler(ctx.getBean(FilterControlService.class));
		filterControlHandler.setUserFilterTypes(UserFilterType.ALIAS_FILTER, UserFilterType.MAIL_FILTER, UserFilterType.USER_ACTIVITY_FILTER,
				UserFilterType.FIRST_NAME_FILTER, UserFilterType.LAST_NAME_FILTER, UserFilterType.PAYMENT_STATE_FILTER, UserFilterType.SUBSCRIPTION_STATE_FILTER,
				UserFilterType.USER_PREPAYMENT_USAGE_FILTER, UserFilterType.EXTERNAL_REFERENCE_ID_FILTER, UserFilterType.USER_ID_FILTER);
		filterControlHandler.setFilterControlFeatures(FilterControlFeature.OR_LINKABLE);
		filterControlHandler.setPinnedUserFilterTypes(UserFilterType.FIRST_NAME_FILTER,UserFilterType.LAST_NAME_FILTER,UserFilterType.EXTERNAL_REFERENCE_ID_FILTER, UserFilterType.PAYMENT_ID_FILTER);

		filterSpecificationBuilder = ctx.getBean(FilterSpecificationBuilder.class);

		defaultInvoiceHandler = new DefaultInvoiceHandler(ctx, this::refreshInvoiceTable, this);
		userControlHandler = new UserControlHandler(ctx, this);
		
		this.view = view;

		this.view.setListener(this);
	}

	@Override
	public void enter(String parameters)
	{
		this.view.setInvoiceHandler(invoiceRepository::findAll);
		this.view.setFilterHandlerAndBuilder(filterControlHandler, filterSpecificationBuilder);
	}

	@Override
	@Deprecated
	public void synchronizePendingExternalPayments()
	{
		defaultInvoiceHandler.synchronizePendingExternalPayments();
	}

	@Override
	public void refreshInvoiceTable()
	{
		view.setInvoiceHandler(invoiceRepository::findAll);
	}

	@Override
	public void edit(Invoice invoice)
	{
		defaultInvoiceHandler.edit(invoice);
	}
	
	@Override
	public void openUserPopup(Invoice invoice)
	{
		userControlHandler.openUser(invoice.getUser());
	}
	@Override
	public void paymentReceived(Invoice invoice, boolean askForComment)
	{
		defaultInvoiceHandler.paymentReceived(invoice, askForComment);
	}
}
