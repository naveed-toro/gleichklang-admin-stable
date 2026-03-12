package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

/**
 * This entity represents the recommendation category assocaited to a subscription offer.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "subscription_offer_category")
public class SubscriptionOfferCategory extends BaseEntity
		implements Comparable<SubscriptionOfferCategory>
{
    @XmlIDREF
	@ManyToOne
	@JoinColumn(name = "subscription_offer_id")
	private SubscriptionOffer subscriptionOffer;

    @XmlAttribute
	@Enumerated(EnumType.STRING)
	private RecommendationCategory category;

	public SubscriptionOfferCategory()
	{
	}

	public SubscriptionOfferCategory(SubscriptionOffer subscriptionOffer, RecommendationCategory category)
	{
		this.subscriptionOffer = subscriptionOffer;
		this.category = category;
	}

	public SubscriptionOffer getSubscriptionOffer()
	{
		return subscriptionOffer;
	}

	public void setSubscriptionOffer(SubscriptionOffer subscriptionOffer)
	{
		this.subscriptionOffer = subscriptionOffer;
	}

	public RecommendationCategory getCategory()
	{
		return category;
	}

	public void setCategory(RecommendationCategory category)
	{
		this.category = category;
	}

	public final static String LOCALIZED_LABEL_PROPERTY = "localizedLabel";

	/**
	 * Returns the localized label.
	 *
	 * @return the loaclized label
	 */
	public String getLocalizedLabel()
	{
		return category.msg();
	}

	/**
	 * Compares objects based on the ordinal value of their category.
	 *
	 * @param o the object to compare with
	 * @return see {@link Comparable#compareTo(Object)}
	 */
	@Override
	public int compareTo(SubscriptionOfferCategory o)
	{
		return Integer.compare(this.category.ordinal(), o.category.ordinal());
	}
}
