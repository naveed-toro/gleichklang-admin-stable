package de.binaerebauten.gleichklang.core.model.payment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * This entity represents the chargeback costs for a payment method {@link #forMethod}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class Chargeback extends Product
{
	@XmlAttribute
	@Enumerated(EnumType.STRING)
	@Column(name = "for_method")
	private PaymentMethod forMethod;

	public PaymentMethod getForMethod()
	{
		return forMethod;
	}

	public void setForMethod(PaymentMethod forMethod)
	{
		this.forMethod = forMethod;
	}

	@Override
	public <T> T accept(ProductVisitor<T> visitor)
	{
		return visitor.visit(this);
	}
	
	@Override
	public ProductType getProductType()
	{
		return ProductType.CHARGEBACK;
	}
}
