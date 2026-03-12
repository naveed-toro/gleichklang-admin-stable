package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Stores all payment related settings of an user {@link User}.
 *
 * To enable a reduce subscription offer the {@link #actionCode} must be equal to
 * the {@link de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer#actionCode}.
 */
@Entity
@Table(name = "user_payment_settings")
public class UserPaymentSettings extends BaseEntity
{
	@NotNull
	@JoinColumn(name = "user_id")
	@OneToOne
	private User user;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method")
	private PaymentMethod paymentMethod = PaymentMethod.PREPAYMENT;

	@Column(name = "action_code")
	private String actionCode;

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public PaymentMethod getPaymentMethod()
	{
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod)
	{
		this.paymentMethod = paymentMethod;
	}

	public String getActionCode()
	{
		return actionCode;
	}

	public void setActionCode(String actionCode)
	{
		this.actionCode = actionCode;
	}

	/**
	 * Returns true iff. the user uses external payment.
	 *
	 * @return true iff. the user uses external payment.
	 */
	public boolean usesExternalPayment()
	{
		return paymentMethod != PaymentMethod.PREPAYMENT;
	}
}
