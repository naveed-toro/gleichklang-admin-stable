package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * This filter filters users with regards to their current payment state.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class PaymentStateFilter extends EnumFilter<PaymentState>
{
	@XmlAttribute
	@Column(name = "enum_value")
	@Enumerated(EnumType.STRING)
	@NotNull
	private PaymentState paymentState;

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}

	@Override
	public PaymentState getEnumValue()
	{
		return paymentState;
	}

	@Override
	public void setEnumValue(PaymentState paymentState)
	{
		this.paymentState = paymentState;
	}
}
