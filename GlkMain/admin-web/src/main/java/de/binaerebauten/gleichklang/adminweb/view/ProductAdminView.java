package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;

/**
 * This view allows creating and editing of a subscription offer {@link SubscriptionOffer}.
 */
public interface ProductAdminView
		extends NavigateView<ProductAdminView.ProductAdminViewListener>
{
	interface ProductAdminViewListener
			extends NavigateView.NavigateViewListener
	{
		/**
		 * Called when an admin presses the new button in this view.
		 */
		void newProduct();

		/**
		 * Called when admin has selected a product and presses
		 * the edit button.
		 *
		 * @param product the non-null product to edit
		 */
		void editProduct(Product product);
	}

	void setProductHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Product> handler);
}
