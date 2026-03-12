package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

/**
 * This entity represents the recommendation category required by a service offer.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "service_offer_category")
public class ServiceOfferRequiredCategory extends BaseEntity implements Comparable<ServiceOfferRequiredCategory>
{
	@XmlIDREF
	@ManyToOne
	@JoinColumn(name = "service_offer_id")
	private ServiceOffer serviceOffer;

	@XmlAttribute
	@Enumerated(EnumType.STRING)
	private RecommendationCategory category;

	public ServiceOfferRequiredCategory()
	{
	}

	public ServiceOfferRequiredCategory(ServiceOffer serviceOffer, RecommendationCategory category)
	{
		this.serviceOffer = serviceOffer;
		this.category = category;
	}

	public ServiceOffer getServiceOffer()
	{
		return serviceOffer;
	}

	public void setServiceOffer(ServiceOffer serviceOffer)
	{
		this.serviceOffer = serviceOffer;
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
	public int compareTo(ServiceOfferRequiredCategory o)
	{
		return Integer.compare(this.category.ordinal(), o.category.ordinal());
	}
}
