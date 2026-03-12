package de.binaerebauten.gleichklang.core.model.payment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.util.UUID;

import static de.binaerebauten.gleichklang.core.model.payment.I18N.EXTERNAL_PAYMENT_INFO;

@Entity
public class ExternalPayment extends AbstractPayment
{
	public ExternalPayment()
	{
	}
	
	public ExternalPayment(PaymentMethod method)
	{
		this.method = method;
	}
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private PaymentMethod method;

	@Column(name = "synchronization_count")
	private int synchronizationCount;

	@Column(name = "charge_id")
	private String chargeId;

	@Column(name = "payment_id")
	private String paymentId;

	@Column(name = "auth_id")
	private String authId;
	
	/**
	 * A unique external id to identify an external payment.
	 */
	@Column(name = "external_id")
	private String externalId;

	public PaymentMethod getMethod()
	{
		return method;
	}

	public void setMethod(PaymentMethod method)
	{
		this.method = method;
	}

	public int getSynchronizationCount()
	{
		return synchronizationCount;
	}

	public void setSynchronizationCount(int synchronizationCount)
	{
		this.synchronizationCount = synchronizationCount;
	}
	
	public String getExternalId()
	{
		return externalId;
	}
	
	public void setExternalId(String externalId)
	{
		this.externalId = externalId;
	}
	
	/**
	 * Creates a UUID based reference id so that payments can be globally identified.
	 *
	 * @return the created external reference id.
	 */
	public String createExternalReferenceId()
	{
		return UUID.randomUUID().toString();
	}

	/**
	 * Returns the payment overview without bank details.
	 *
	 * @return
	 */
	public String getTranslatedInfo()
	{
		return EXTERNAL_PAYMENT_INFO.msg(getAmount(), getExternalReferenceId(), getMethod());
	}
	
	@Override
	public PaymentType getPaymentType()
	{
		return PaymentType.EXTERNAL_PAYMENT;
	}

	public String getChargeId() {
		return chargeId;
	}

	public void setChargeId(String chargeId) {
		this.chargeId = chargeId;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getAuthId() {
		return authId;
	}

	public void setAuthId(String authId) {
		this.authId = authId;
	}
}
