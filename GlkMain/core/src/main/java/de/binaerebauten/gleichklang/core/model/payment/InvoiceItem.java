package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.BaseVersionedEntity;
import de.binaerebauten.gleichklang.core.service.payment.ChargebackReason;

import javax.persistence.*;

/**
 * Represents an invoice item which is part of an {@link Invoice}
 */
@Entity
@Table(name = "invoice_item")
public class InvoiceItem extends BaseVersionedEntity
{
	@ManyToOne // default to load user eager is fine here!
	@JoinColumn(name = "invoice_id")
	private Invoice invoice;

	@Embedded
	private MonetaryAmount amount;

	@ManyToOne // default to load user eager is fine here!
	@JoinColumn(name = "subscription_id")
	private Subscription subscription;

	@ManyToOne // default to load user eager is fine here!
	@JoinColumn(name = "product_id")
	private Product product;
	
	@Column(name = "chargeback_reason")
	@Enumerated(EnumType.STRING)
	private ChargebackReason chargebackReason;

	public Invoice getInvoice()
	{
		return invoice;
	}

	public void setInvoice(Invoice invoice)
	{
		this.invoice = invoice;
	}

	public MonetaryAmount getAmount()
	{
		return amount;
	}

	public void setAmount(MonetaryAmount amount)
	{
		this.amount = amount;
	}

	public Subscription getSubscription()
	{
		return subscription;
	}

	public void setSubscription(Subscription subscription)
	{
		this.subscription = subscription;
	}

	public Product getProduct()
	{
		return product;
	}

	public void setProduct(Product product)
	{
		this.product = product;
	}
	
	public ChargebackReason getChargebackReason()
	{
		return chargebackReason;
	}
	
	public void setChargebackReason(ChargebackReason chargebackReason)
	{
		this.chargebackReason = chargebackReason;
	}
	
}