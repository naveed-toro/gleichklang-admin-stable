package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.adminweb.view.component.ProductSelectionTable;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.TableControl;

/**
 * Implementation of {@link ProductAdminView}.
 */
public class ProductAdminViewImpl
		extends AbstractNavigateView<ProductAdminView.ProductAdminViewListener>
		implements ProductAdminView
{
	private final ProductSelectionTable productSelectionTable;

	public ProductAdminViewImpl() {
		productSelectionTable = new ProductSelectionTable();
        productSelectionTable.setSizeFull();
		TableControl<Product> productTableControl = productSelectionTable.getProductTableControl();

		productTableControl.setNewCallback(() -> fireEvent(ProductAdminViewListener::newProduct));
		productTableControl.setEditCallback((offer) -> fireEvent(action -> action.editProduct(offer)));
        setSizeFull();
		setCompositionRoot(productSelectionTable);

	}

	@Override
	public void setProductHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Product> handler)
	{
		productSelectionTable.setProductHandler(handler);
	}
}
