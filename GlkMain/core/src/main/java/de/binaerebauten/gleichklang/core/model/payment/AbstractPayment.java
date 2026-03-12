package de.binaerebauten.gleichklang.core.model.payment;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.model.BaseVersionedEntity;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * This class represents an abstract payment.
 */
@Entity
@Table(name = "payment")
public abstract class AbstractPayment extends BaseVersionedEntity
{
	public enum PaymentType
	{
		EXTERNAL_PAYMENT(ExternalPayment.class),
		PREPAYMENT(Prepayment.class);
		
		private final Class<? extends AbstractPayment> paymentClass;
		
		PaymentType(Class<? extends AbstractPayment> paymentClass)
		{
			this.paymentClass = paymentClass;
		}
		
		public Class<? extends AbstractPayment> getPaymentClass()
		{
			return paymentClass;
		}
		
		public static PaymentType of(AbstractPayment payment)
		{
			Objects.requireNonNull(payment, "o == null");
			
			return Arrays.stream(PaymentType.values())
					.filter(t -> t.getPaymentClass() == payment.getClass())
					.findFirst().get();
		}
	}
	
	@NotNull
	@Embedded
	private MonetaryAmount amount;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private PaymentState state = PaymentState.PENDING;
	
	@NotNull
	@ManyToOne // default to load user eager is fine here!
	@JoinColumn(name = "user_id")
	private User user;
	
	@NotNull
	@ManyToOne // default to load user eager is fine here!
	@JoinColumn(name = "invoice_id")
	private Invoice invoice;
	
	/**
	 * This attribute has the type Boolean so that it can be set to null.
	 * It's used as a combined unique key
	 * together with {@link #user}. This prevents that an user has more then one
	 * current payment at the same time.
	 */
	@Column
	private Boolean current = null;
	
	/**
	 * The external reference id is used to reference this payment.
	 * <p>
	 * It's used as the id that a user has to put in the usage of a prepayment.
	 * <p>
	 * It's used as the id to reference this payment in an external system.
	 */
	@NotEmpty
	@Column(name = "external_reference_id")
	private String externalReferenceId;
	
	/**
	 * This redundant attribute is true iff. this payment was refunded by at least one other payment.
	 * <p>
	 * Formally this means:
	 * true iff. another payment p exists where p.paymentToRefund == this AND p.state == PAID
	 */
	@Column(name = "refunded")
	private boolean refunded;
	
	/**
	 * This field is used by a refund payment and points to the payment that this
	 * payment refunds.
	 */
	@ManyToOne // default to load user eager is fine here!
	@JoinColumn(name = "payment_to_refund_id")
	private AbstractPayment paymentToRefund;

	@Column(name = "revocation_amount")
	private BigDecimal revocationAmount;

	@Column(name = "revocation_date")
	private Date revocationDate;

	/**
	 * Comment allows admin to make a note about the payment.
	 * For example, why payment is refunded or marked as paid.
	 */
	@Column
	private String comment;
	
	public boolean isRefund()
	{
		return amount != null && amount.isRefund();
	}
	
	public MonetaryAmount getAmount()
	{
		return amount;
	}
	
	public void setAmount(MonetaryAmount amount)
	{
		this.amount = amount;
	}
	
	public PaymentState getState()
	{
		return state;
	}
	
	public void setState(PaymentState state)
	{
		this.state = state;
	}
	
	public abstract PaymentMethod getMethod();
	
	public User getUser()
	{
		return user;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}
	
	public Invoice getInvoice()
	{
		return invoice;
	}
	
	public void setInvoice(Invoice invoice)
	{
		this.invoice = invoice;
	}
	
	public Boolean getCurrent()
	{
		return current;
	}
	
	/**
	 * Sets the current flag of this subscription.
	 *
	 * @param current true or null to
	 * @throws IllegalArgumentException when trying to set this field to false.
	 */
	
	public void setCurrent(Boolean current)
	{
		Preconditions.checkArgument(Boolean.TRUE.equals(current) || current == null);
		
		this.current = current;
	}
	
	public String getExternalReferenceId()
	{
		return externalReferenceId;
	}
	
	public void setExternalReferenceId(String externalReferenceId)
	{
		this.externalReferenceId = externalReferenceId;
	}
	
	public boolean isRefunded()
	{
		return refunded;
	}
	
	public void setRefunded(boolean refunded)
	{
		this.refunded = refunded;
	}
	
	public AbstractPayment getPaymentToRefund()
	{
		return paymentToRefund;
	}
	
	public void setPaymentToRefund(AbstractPayment paymentToRefund)
	{
		this.paymentToRefund = paymentToRefund;
	}
	
	public String getComment()
	{
		return comment;
	}
	
	public void setComment(String comment)
	{
		this.comment = comment;
	}
	
	public abstract PaymentType getPaymentType();
	
	/**
	 * This callback must be implemented to set the external reference id.
	 * <p>
	 * Sub classes have different requirement with regards to the external reference.
	 *
	 * @return the external reference id
	 */
	public abstract String createExternalReferenceId();
	
	/**
	 * Prepaymet should return a translated info that tells user bank details (IBAN, payment reference, etc.).
	 * External payment should return the payment overview without bank details.
	 *
	 * @return payment info
	 */
	public abstract String getTranslatedInfo();
	
	public Product getBaseProduct()
	{
		if(getInvoice() != null) return getInvoice().getBaseProduct();
		
		return null;
	}

	public BigDecimal getRevocationAmount() {
		return revocationAmount;
	}

	public void setRevocationAmount(BigDecimal revocationAmount) {
		this.revocationAmount = revocationAmount;
	}

	public Date getRevocationDate() {
		return revocationDate;
	}

	public void setRevocationDate(Date revocationDate) {
		this.revocationDate = revocationDate;
	}
}
