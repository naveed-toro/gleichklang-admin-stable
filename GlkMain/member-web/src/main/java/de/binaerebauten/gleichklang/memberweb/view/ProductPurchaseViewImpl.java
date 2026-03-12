package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.FooterCommandBar;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.List;

public class ProductPurchaseViewImpl
		extends AbstractNavigateView<ProductPurchaseView.ProductPurchaseViewListener>
		implements ProductPurchaseView
{
	private final VerticalLayout compositionRoot;
	private final OptionGroup productOptionGroup;
	private final FormPanel formPanel;
	
	private final Button purchaseButton;
	
	private final Label paymentInfoLabel;
	private String noOffersAvailable;

	public ProductPurchaseViewImpl()
	{
		compositionRoot = new VerticalLayout();

		compositionRoot.setSpacing(true);

        formPanel = new FormPanel();
        formPanel.setStyleName(CssStyle.GK_PANEL.getStyleName());

		productOptionGroup = createProductOptionGroup();
        formPanel.addComponent(productOptionGroup);

		final SaveHelper saveHelper = new SaveHelper(() -> fireEvent(event -> event.purchase(getProduct())));
		purchaseButton = saveHelper.getSaveButton();
		purchaseButton.setEnabled(false);
		purchaseButton.setCaption(I18N.PRODUCTPURCHASEVIEW_ACTION_PURCHASE.msg());
		purchaseButton.setIcon(FontAwesome.SHOPPING_CART);
		final FooterCommandBar commandBar = new FooterCommandBar(purchaseButton);

		paymentInfoLabel = new Label();
		paymentInfoLabel.setContentMode(ContentMode.HTML);
		paymentInfoLabel.setVisible(false);
        formPanel.addComponent(paymentInfoLabel);

		productOptionGroup.addValueChangeListener(this::select);

		compositionRoot.setSizeFull();
		compositionRoot.addComponents(formPanel, commandBar);

		setCompositionRoot(compositionRoot);
	}

    public ProductPurchaseViewImpl(String caption, String noOffersAvailable) {
        this();

        if (caption != null) {
            formPanel.setCaption(caption);
        }

		this.noOffersAvailable = noOffersAvailable;
    }
	
	private OptionGroup createProductOptionGroup()
	{
		ComponentFactory componentFactory = ComponentFactory.getInstance();
		
		OptionGroup optionGroup = componentFactory.createField(OptionGroup.class);
		optionGroup.setMultiSelect(false);
		optionGroup.setHtmlContentAllowed(true);
		optionGroup.setItemCaptionPropertyId(Product.DESCRIPTION);
		optionGroup.setContainerDataSource(new BeanItemContainer<>(Product.class));
		
		return optionGroup;
	}
	
	private Product getProduct()
	{
		return (Product) productOptionGroup.getValue();
	}
	
	private void select(Property.ValueChangeEvent e)
	{
		purchaseButton.setEnabled(e.getProperty().getValue() != null);
	}

	@Override
	public void setProducts(List<? extends Product> products)
	{
		Container containerDataSource = productOptionGroup.getContainerDataSource();
		containerDataSource.removeAllItems();

		if (products.isEmpty())
		{
			setPaymentInfo(noOffersAvailable);
		}
		else
		{
			products.forEach(containerDataSource::addItem);
		}
	}

	@Override
	public void setPaymentInfo(String paymentInfo)
	{
		paymentInfoLabel.setValue(paymentInfo);
		paymentInfoLabel.setVisible(true);
		productOptionGroup.setVisible(false);
		purchaseButton.setVisible(false);
	}

    @Override
    public void setCaption(String caption) {
        formPanel.setCaption(caption);
    }

}
