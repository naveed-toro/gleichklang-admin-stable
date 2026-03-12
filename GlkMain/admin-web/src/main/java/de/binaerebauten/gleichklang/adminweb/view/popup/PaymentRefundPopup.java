package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.validator.BigDecimalRangeValidator;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.MonetaryAmount;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.Popup;

import java.math.BigDecimal;

/**
 * This popup shows a payment to refund {@link #paymentToRefund} and allows to
 * refund this payment {@link de.binaerebauten.gleichklang.adminweb.view.popup.PaymentRefundPopup.RefundPaymentCallback}.
 */
public class PaymentRefundPopup extends Popup
{
	public interface RefundPaymentCallback
	{
		/**
		 * Triggers the actual refund of the p
		 *  @param paymentToRefund the payment to refund
		 * @param refundAmount    the amount to refund (is always negative)
		 * @param usePrepayment
		 */
		void refundPayment(AbstractPayment paymentToRefund, MonetaryAmount refundAmount, boolean usePrepayment);
	}
	
	private final AbstractPayment paymentToRefund;
	private final RefundPaymentCallback refundPaymentCallback;
	private final TextField refundAmount;
	private final CheckBox refundAsPrepaymentCheckBox;
	
	public PaymentRefundPopup(AbstractPayment paymentToRefund, RefundPaymentCallback refundPaymentCallback)
	{
		super(I18N.PAYMENTREFUNDPOPUP_CAPTION_TITLE.msg());
		
		this.paymentToRefund = paymentToRefund;
		this.refundPaymentCallback = refundPaymentCallback;
		
		final BigDecimal minRefundAmount = paymentToRefund.getAmount().getAmount().negate();
		final BigDecimal maxRefundAmount = BigDecimal.ZERO;
		
		this.refundAsPrepaymentCheckBox = createRefundAsPrePaymentCheckBox(paymentToRefund);
		this.refundAmount = createRefundAmountTextField(minRefundAmount, maxRefundAmount);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		
		final HorizontalLayout paymentPanel = new HorizontalLayout();
		paymentPanel.setMargin(true);
		paymentPanel.setSpacing(true);
		layout.addComponent(paymentPanel);
		
		final Label originalAmount = new Label(I18N.PAYMENTREFUNDPOPUP_CAPTION_PAYMENTAMOUNT.msg(paymentToRefund.getAmount()));
		paymentPanel.addComponent(originalAmount);
		
		final Label refundAmountLabel = new Label(I18N.PAYMENTREFUNDPOPUP_CAPTION_REFUNDAMOUNT.msg(minRefundAmount, maxRefundAmount));
		paymentPanel.addComponent(refundAmountLabel);
		
		paymentPanel.addComponents(refundAmount, refundAsPrepaymentCheckBox);
		
		final HorizontalLayout buttonPanel = new HorizontalLayout();
		buttonPanel.setMargin(true);
		layout.addComponent(buttonPanel);
		
		final Button refundButton = createSaveButton();
		buttonPanel.addComponent(refundButton);
		
		setContent(layout);
	}
	
	private Button createSaveButton()
	{
		final Button button = new Button(I18N.PAYMENTREFUNDPOPUP_ACTION_REFUND.msg());
		button.addClickListener(listener -> refundPayment());
		refundAmount.addValueChangeListener(event -> button.setEnabled(refundAmount.isValid()));
		
		return button;
	}
	
	private TextField createRefundAmountTextField(BigDecimal minRefundAmount, BigDecimal maxRefundAmount)
	{
		final TextField textField = ComponentFactory.getInstance().createField(TextField.class);
		textField.setConverter(BigDecimal.class);
		
		final String validationMessage = I18N.PAYMENTREFUNDPOPUP_NOTIFICATION_INVALIDREFUNDAMOUNT.msg(minRefundAmount, maxRefundAmount);
		final BigDecimalRangeValidator refundAmountValidator = new BigDecimalRangeValidator(validationMessage, minRefundAmount, maxRefundAmount);
		refundAmountValidator.setMaxValueIncluded(false);
		
		textField.addValidator(refundAmountValidator);
		textField.setConvertedValue(minRefundAmount);
		textField.setRequired(true);
		
		return textField;
	}
	
	private CheckBox createRefundAsPrePaymentCheckBox(AbstractPayment paymentToRefund)
	{
		final CheckBox checkBox = ComponentFactory.getInstance().createField(CheckBox.class, "Rücküberweisen als Vorkasse");
		checkBox.setVisible(paymentToRefund instanceof ExternalPayment);
		
		return checkBox;
	}
	
	private void refundPayment()
	{
		final MonetaryAmount refundMonetaryAmount = new MonetaryAmount((BigDecimal) refundAmount.getConvertedValue(), this.paymentToRefund.getAmount().getCurrency());
		this.refundPaymentCallback.refundPayment(this.paymentToRefund, refundMonetaryAmount, refundAsPrepaymentCheckBox.getValue());
		
		close();
	}
}
