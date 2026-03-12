package de.binaerebauten.gleichklang.core.model.affiliate;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Stores the external id of an affiliate partner reference.
 */
@Entity
@Table(name = "affiliate_payment_state")
public class AffiliatePaymentState extends BaseEntity
{
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "affiliate_partner")
	private AffiliatePartner partner;
	
	@NotNull
	@Column(name = "external_id")
	private String externalId;
	
	@NotNull
	@Column(name = "paid_to_partner")
	private boolean paidToPartner;
	
	/*
	 * One payment can reference on multiple affiliate payment state records,
	 * because it is not always possible to determine which affiliate partner
	 * to choose (e.g. when cookies of two affiliate partners are presented).
	 */
	@NotNull
	@ManyToOne
	@JoinColumn(name = "payment_id")
	private AbstractPayment payment;
	
	public AffiliatePartner getPartner()
	{
		return partner;
	}
	
	public void setPartner(AffiliatePartner partner)
	{
		this.partner = partner;
	}
	
	public String getExternalId()
	{
		return externalId;
	}
	
	public void setExternalId(String externalId)
	{
		this.externalId = externalId;
	}
	
	public boolean isPaidToPartner()
	{
		return paidToPartner;
	}
	
	public void setPaidToPartner(boolean paidToPartner)
	{
		this.paidToPartner = paidToPartner;
	}
	
	public AbstractPayment getPayment()
	{
		return payment;
	}
	
	public void setPayment(AbstractPayment payment)
	{
		this.payment = payment;
	}
}
