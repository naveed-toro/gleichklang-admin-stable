package de.binaerebauten.gleichklang.core.model.payment;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlIDREF;

/**
 * This entity represents a renewal offer used for the automatic renewal of a subscription offer
 * {@link SubscriptionOffer#getAutoRenewalOffer()}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class RenewalOffer extends SubscriptionOffer
{
	private final static int DEFAULT_DURATION = 12;

	@XmlIDREF
	@ManyToOne
	@JoinColumn(name = "auto_renewal_offer_id")
	private RenewalOffer autoRenewalOffer;

	/**
	 * Returns the auto renewal offer of this offer or this offer if no auto renewal offer
	 * was persisted.
	 *
	 * @return the auto renewal offer or null
	 */
	@Override
	public RenewalOffer getAutoRenewalOffer()
	{
		return autoRenewalOffer != null ? autoRenewalOffer : this;
	}

	@Override
	public void setAutoRenewalOffer(RenewalOffer autoRenewalOffer)
	{
		this.autoRenewalOffer = autoRenewalOffer;
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
		return ProductType.RENEWAL_OFFER;
	}
}
