package de.binaerebauten.gleichklang.core.model.payment;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;

/**
 * A subscription offer for new users with an optional action code. The action code
 * can be entered by an user to get reduced offers or social offers.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class InitialSubscriptionOffer extends SubscriptionOffer
{
	private final static int DEFAULT_DURATION = 12;

	public final static String SOCIAL_CODE = "SZ-2016";

    @XmlIDREF
	@NotNull
	@ManyToOne
	@JoinColumn(name = "auto_renewal_offer_id")
	private RenewalOffer autoRenewalOffer;

	/**
	 * Returns the auto renewal offer of this offer.
	 *
	 * @return the non-null auto renwal offer
	 */
	@Override
	public RenewalOffer getAutoRenewalOffer()
	{
		return autoRenewalOffer;
	}

	@Override
	public void setAutoRenewalOffer(RenewalOffer autoRenewalOffer)
	{
		this.autoRenewalOffer = autoRenewalOffer;
	}

	public String getActionCode()
	{
		return actionCode;
	}

	public void setActionCode(String actionCode)
	{
		this.actionCode = actionCode;
	}
	
	@Override
	public int getDefaultDuration()
	{
		return DEFAULT_DURATION;
	}
	
	@Override
	public <T> T accept(ProductVisitor<T> visitor)
	{
		return visitor.visit(this);
	}
	
	@Override
	public ProductType getProductType()
	{
		return ProductType.INITIAL_SUBSCRIPTION_OFFER;
	}
}
