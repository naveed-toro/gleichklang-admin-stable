package de.binaerebauten.gleichklang.core.model.payment;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

import static de.binaerebauten.gleichklang.core.model.payment.I18N.PREPAYMENT_INFO;

@Entity
public class Prepayment extends AbstractPayment
{
	public Prepayment()
	{
	}
	
	public Prepayment(BankAccount bankAccount)
	{
		this.bankAccount = bankAccount;
	}
	
	@NotNull
	@Column
	@Enumerated(EnumType.STRING)
	private PaymentMethod method = PaymentMethod.PREPAYMENT;

	@ManyToOne
	@JoinColumn(name = "bank_account_id")
	@NotNull
	private BankAccount bankAccount;

	@Column(name = "reminder_count")
	private int reminderCount;

	/**
	 * Thie next reminder date. This is not a {@link LocalDateTime}, because
	 * we want to send reminder mails during testing in intervals of minutes...
	 */
	@Column(name = "next_reminder_date")
	private LocalDateTime nextReminderDate;

	public PaymentMethod getMethod()
	{
		return method;
	}

	public void setMethod(PaymentMethod method)
	{
		this.method = method;
	}

	public BankAccount getBankAccount()
	{
		return bankAccount;
	}

	public void setBankAccount(BankAccount bankAccount)
	{
		this.bankAccount = bankAccount;
	}

	public int getReminderCount()
	{
		return reminderCount;
	}

	public void setReminderCount(int reminderCount)
	{
		this.reminderCount = reminderCount;
	}

	public LocalDateTime getNextReminderDate()
	{
		return nextReminderDate;
	}

	public void setNextReminderDate(LocalDateTime nextReminderDate)
	{
		this.nextReminderDate = nextReminderDate;
	}

	/**
	 * Creates an external reference from the id {@link de.binaerebauten.gleichklang.core.model.BaseEntity#id}.
	 *
	 * Needs to be called after this entity has been persisted so that the id is correctly set.
	 *
	 * @return the external reference id that the user has to specify as usage
	 */
	public String createExternalReferenceId()
	{
		Objects.requireNonNull(getId(), "id == null");

		int part1 = (int) (getId() % 10000);
		int part2 = (int) (getId() / 10000) % 10000;
		int part3 = (int) (getId() / 100000000) % 10000;

		return String.format("%04d-%04d-%04d", part1, part2, part3);
	}

	/**
	 * Returns the translated prepayment info that tells an user how to
	 * pay this prepayment.
	 *
	 * @return the translated prepayment info
	 */
	public String getTranslatedInfo()
	{
		return PREPAYMENT_INFO.msg(getAmount(), getExternalReferenceId(),
				bankAccount.getFormattedDescription());
	}
	
	@Override
	public PaymentType getPaymentType()
	{
		return PaymentType.PREPAYMENT;
	}
}
