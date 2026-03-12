package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

import java.net.URI;
import java.util.Objects;

/**
 * Component that shows a payment form provided by an external payment services in an iframe.
 */
public class ExternalPaymentForm extends CustomComponent
{
	private final BrowserFrame browserFrame;

	public ExternalPaymentForm()
	{
		final VerticalLayout layout = new VerticalLayout();
		browserFrame = new BrowserFrame(null);
		browserFrame.setWidth(480, Unit.PIXELS);
		browserFrame.setHeight(480, Unit.PIXELS);

		layout.addComponent(browserFrame);
		layout.setComponentAlignment(browserFrame, Alignment.TOP_LEFT);
		layout.setSizeFull();

		setCompositionRoot(layout);
	}

	/**
	 * Sets the url to the payment service form.
	 *
	 * @param paymentFormUrl the non-null external payment form url
	 */
	public void setPaymentFormUrl(URI paymentFormUrl)
	{
		Objects.requireNonNull(paymentFormUrl, "paymentFormUrl == null");

		browserFrame.setSource(new ExternalResource(paymentFormUrl.toString()));
	}
}
