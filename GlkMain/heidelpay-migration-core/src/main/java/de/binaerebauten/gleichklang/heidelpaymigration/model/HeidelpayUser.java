package de.binaerebauten.gleichklang.heidelpaymigration.model;

import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;

/**
 * Contains information about heidelpay user and his external payment registration.
 */
public class HeidelpayUser
{
	private Long id;

	private Long subscriptionId;

	private PaymentMethod paymentMethod;

	private String registrationId;

	private String externalReferenceId;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}
	
	public Long getSubscriptionId()
	{
		return subscriptionId;
	}
	
	public void setSubscriptionId(Long subscriptionId)
	{
		this.subscriptionId = subscriptionId;
	}
	
	public PaymentMethod getPaymentMethod()
	{
		return paymentMethod;
	}
	
	public void setPaymentMethod(PaymentMethod method)
	{
		this.paymentMethod = method;
	}
	
	public String getRegistrationId()
	{
		return registrationId;
	}

	public void setRegistrationId(String registrationId)
	{
		this.registrationId = registrationId;
	}

	public String getExternalReferenceId()
	{
		return externalReferenceId;
	}

	public void setExternalReferenceId(String externalReferenceId)
	{
		this.externalReferenceId = externalReferenceId;
	}
	
}
