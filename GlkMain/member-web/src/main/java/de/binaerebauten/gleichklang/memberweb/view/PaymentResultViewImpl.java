package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.ui.Label;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.NavigateView;

import java.util.Objects;

public class PaymentResultViewImpl
		extends AbstractNavigateView<NavigateView.NavigateViewListener>
		implements PaymentResultView
{
	private final Label paymentResultLabel;

	public PaymentResultViewImpl()
	{
		paymentResultLabel = new Label();

		setCompositionRoot(paymentResultLabel);
	}

	@Override
	public void showPaymentResult(String paymentResultMessage)
	{
		Objects.requireNonNull(paymentResultMessage, "paymentResultMessage == null");

		paymentResultLabel.setValue(paymentResultMessage);
	}
}
