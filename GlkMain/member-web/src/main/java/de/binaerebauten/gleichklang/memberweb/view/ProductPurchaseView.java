package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.view.NavigateView;

import java.util.List;

/**
 * This view shows a list of purchaseable products and allows an user to purchase
 * the selected product.
 */
public interface ProductPurchaseView extends SubscriptionTabView,
		 NavigateView<ProductPurchaseView.ProductPurchaseViewListener>
{
	interface ProductPurchaseViewListener extends NavigateView.NavigateViewListener
	{
		/**
		 * This method is called when an user presses the purchase button.
		 *
		 * @param product the non-null
		 */
		void purchase(Product product);
	}

	/**
	 * Sets the list of products.
	 *
	 * @param products the non-null list of products
	 */
	void setProducts(List<? extends Product> products);

	void setCaption(String title);

}
