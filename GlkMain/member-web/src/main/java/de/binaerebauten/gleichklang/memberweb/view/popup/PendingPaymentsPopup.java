package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import de.binaerebauten.gleichklang.core.model.payment.AvailableCurrency;
import de.binaerebauten.gleichklang.core.model.payment.MonetaryAmount;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class PendingPaymentsPopup extends GenericPopup
{
	public PendingPaymentsPopup(Collection<AbstractPayment> pendingPayments)
	{
		final HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.setSpacing(true);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		boolean onlyRefund = true;
		boolean sameCurrency = true;
		BigDecimal amount = BigDecimal.ZERO;
		AvailableCurrency currency = null;
		
		for (AbstractPayment payment : pendingPayments)
		{
			final MonetaryAmount paymentAmount = payment.getAmount();
			final AvailableCurrency amountCurrency = paymentAmount.getCurrency();
			
			if (!payment.isRefund()) onlyRefund = false;
			amount = amount.add(paymentAmount.getAmount());
			if (currency == null) currency = amountCurrency;
			if (!Objects.equals(currency, amountCurrency)) sameCurrency = false;
		}
		
		if (onlyRefund)
		{
			layout.addComponent(new Label(I18N.PENDINGPAYMENTSPOPUP_CAPTION_REFUNDAWAITING.msg()));
		}
		else
		{
			final String amountString;
			final String paymentInfos = pendingPayments.stream().map(AbstractPayment::getTranslatedInfo).collect(Collectors.joining("\n\n"));
			
			if (sameCurrency)
			{
				amountString = de.binaerebauten.gleichklang.core.model.payment.I18N.MONATARYAMOUNT_FORMAT.msg(amount, currency);
			}
			else
			{
				amountString = pendingPayments.stream()
						.map(AbstractPayment::getAmount)
						.map(MonetaryAmount::toString)
						.collect(Collectors.joining(", "));
			}
			
			layout.addComponent(new Label(I18N.PENDINGPAYMENTSPOPUP_CAPTION_PAYMENTAWAITING.msg(amountString, paymentInfos)));
		}
		
		setPopupContent(layout);
		setFooter(footerLayout);
		
		footerLayout.addComponent(new Button(I18N.PENDINGPAYMENTSPOPUP_ACTION_OK.msg(), e -> close()));
	}
}
