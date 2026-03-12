package de.binaerebauten.gleichklang.core.model.payment;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This entity represents an offer for a service that is provided by a
 * GK employee.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class ServiceOffer extends Product
{
	@XmlElement(name = "requiredCategory")
	@XmlElementWrapper
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "serviceOffer")
	private Set<ServiceOfferRequiredCategory> requiredCategories = new HashSet<>();

	public Set<ServiceOfferRequiredCategory> getRequiredCategories()
	{
		return requiredCategories;
	}

	public void setRequiredCategories(Set<ServiceOfferRequiredCategory> requiredCategories)
	{
		this.requiredCategories = requiredCategories;
	}

	@Override
	public <T> T accept(ProductVisitor<T> visitor)
	{
		return visitor.visit(this);
	}
	
	@Override
	public ProductType getProductType()
	{
		return ProductType.SERVICE_OFFER;
	}
}
