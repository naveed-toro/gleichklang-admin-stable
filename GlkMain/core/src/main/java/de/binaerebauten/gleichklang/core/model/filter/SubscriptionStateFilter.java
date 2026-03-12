package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
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
 * This filter filters users with regards to their current subscription state.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class SubscriptionStateFilter extends EnumFilter<SubscriptionState>
{
    @XmlAttribute
	@Column(name = "enum_value")
	@Enumerated(EnumType.STRING)
	@NotNull
	private SubscriptionState subscriptionState;

	@Override
	public SubscriptionState getEnumValue()
	{
		return subscriptionState;
	}

	@Override
	public void setEnumValue(SubscriptionState enumValue)
	{
		this.subscriptionState = enumValue;
	}

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
}
