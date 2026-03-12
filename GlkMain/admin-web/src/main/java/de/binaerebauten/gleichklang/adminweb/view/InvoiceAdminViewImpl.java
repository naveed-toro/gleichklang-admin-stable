package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.adminweb.view.component.InvoiceTable;
import de.binaerebauten.gleichklang.core.model.payment.Invoice;
import de.binaerebauten.gleichklang.core.model.payment.Invoice_;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentReplacer;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

public class InvoiceAdminViewImpl
		extends AbstractNavigateView<InvoiceAdminView.InvoiceAdminViewListener>
		implements InvoiceAdminView
{
	private static class UserFilter implements Specification<Invoice>
	{
		private final Specification<User> userSpecification;
		
		public UserFilter(Specification<User> userSpecification)
		{
			this.userSpecification = userSpecification;
		}
		
		@Override
		public Predicate toPredicate(Root<Invoice> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Subquery<Invoice> sq = query.subquery(Invoice.class);
			final Root<Invoice> invoiceRoot = sq.from(Invoice.class);
			final Root<User> user_root = sq.from(User.class);
			
			final Predicate restriction1 = userSpecification.toPredicate(user_root, query, cb);
			final Predicate restriction2 = cb.equal(invoiceRoot.get(Invoice_.user), user_root);
			
			sq.where(restriction1, restriction2).select(invoiceRoot);
			
			return cb.in(root).value(sq);
		}
	}
	
	private final InvoiceTable invoiceTable;
	
	private final ComponentReplacer<FilterControlComponent> filterComponent = new ComponentReplacer<>();
	;
	private Specification<Invoice> invoiceFilter = null;
	
	public InvoiceAdminViewImpl()
	{
		invoiceTable = new InvoiceTable();
		
		final AbstractOrderedLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		layout.addComponent(filterComponent);
		layout.addComponent(invoiceTable);
		
		layout.setSizeFull();
		
		setCompositionRoot(layout);
	}
	
	@Override
	public void setInvoiceHandler(LazyBeanFilteredItemsHandler<Invoice> handler)
	{
		invoiceTable.setTableHandler(handler);
	}
	
	@Override
	public void setFilterHandlerAndBuilder(FilterControlHandler filterControlHandler,
			FilterSpecificationBuilder filterSpecificationBuilder)
	{
		changeFilter(null);
		
		if (filterControlHandler == null)
		{
			filterComponent.setComponent(null);
		}
		else
		{
			filterComponent.setComponent(new FilterControlComponent(filterControlHandler, filterSpecificationBuilder));
			filterComponent.getComponent().addFilterChangedListener(this::changeFilter);
		}
	}
	
	private void changeFilter(Specification<User> userSpecification)
	{
		final Specification<Invoice> invoiceSpecification = userSpecification != null ? new UserFilter(userSpecification) : null;
		invoiceTable.replaceFilter(invoiceFilter, invoiceSpecification);
		this.invoiceFilter = invoiceSpecification;
	}
	
	@Override
	public void setListener(InvoiceAdminViewListener listener)
	{
		super.setListener(listener);
		
		invoiceTable.removeButtons();
		
		if (getListener() != null)
		{
			invoiceTable.addControlButtons(getListener());
			invoiceTable.addOpenUserButton(getListener());
		}
	}
}
