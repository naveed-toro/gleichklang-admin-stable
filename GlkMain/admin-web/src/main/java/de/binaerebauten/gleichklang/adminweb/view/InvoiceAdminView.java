package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.component.InvoiceTable.InvoiceHandler;
import de.binaerebauten.gleichklang.adminweb.view.component.InvoiceTable.OpenUserHandler;
import de.binaerebauten.gleichklang.core.model.payment.Invoice;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;

/**
 * Views a list of all invoices.
 */
public interface InvoiceAdminView extends NavigateView<InvoiceAdminView.InvoiceAdminViewListener>
{
    interface InvoiceAdminViewListener extends NavigateView.NavigateViewListener, InvoiceHandler, OpenUserHandler
    {
    }

    /**
     * Sets the handler which this view uses to retrieve the invoices.
     *
     * @param handler the invoice handler
     */
    void setInvoiceHandler(LazyBeanFilteredItemsHandler<Invoice> handler);

    void setFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder);
}