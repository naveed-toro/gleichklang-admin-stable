package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ExternalPaymentForm;

import java.net.URI;

/**
 * Implementation of {@link ExternalPaymentFormView}.
 */
public class ExternalPaymentFormViewImpl
		extends AbstractNavigateView<ExternalPaymentFormView.ExternalPaymentFormViewListener>
		implements ExternalPaymentFormView
{

	private final ExternalPaymentForm externalPaymentForm;

	public ExternalPaymentFormViewImpl()
	{
		externalPaymentForm = new ExternalPaymentForm();
		setCompositionRoot(externalPaymentForm);
	}

	@Override
	public void setPaymentFormUrl(URI paymentFormUrl)
	{
		externalPaymentForm.setPaymentFormUrl(paymentFormUrl);
	}
}
