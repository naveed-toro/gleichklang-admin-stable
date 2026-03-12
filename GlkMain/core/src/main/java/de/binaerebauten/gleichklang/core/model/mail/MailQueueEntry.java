package de.binaerebauten.gleichklang.core.model.mail;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Represents an email to be sent.
 */
@Entity
@Table(name = "mail_queue_entry")
public abstract class MailQueueEntry extends BaseEntity
{
	@NotNull
	@Column(name = "mail_template")
	@Enumerated(EnumType.STRING)
	private UserMailTemplate mailTemplate;
	
	private int attempts;

	@NotNull
	@Column(name = "delivery_status")
	@Enumerated(EnumType.STRING)
	private MailDeliveryStatus deliveryStatus;

	@NotNull
	@Column(name = "undeliverable_mail_reason")
	@Enumerated(EnumType.STRING)
	private UndeliverableMailReason undeliverableMailReason;
	
	@Column(name = "reminder_count")
	private int reminderCount;
	
	@Column(name = "next_reminder_date")
	private LocalDateTime nextReminderDate;
	
	@Column(name = "next_retry_date")
	private LocalDateTime nextRetryDate;

	public UserMailTemplate getMailTemplate()
	{
		return mailTemplate;
	}

	public void setMailTemplate(UserMailTemplate mailTemplate)
	{
		this.mailTemplate = mailTemplate;
	}
	
	public int getAttempts()
	{
		return attempts;
	}
	
	public void setAttempts(int attempts)
	{
		this.attempts = attempts;
	}
	
	public void incAttempts()
	{
		this.attempts++;
	}
	
	public MailDeliveryStatus getDeliveryStatus()
	{
		return deliveryStatus;
	}

	public void setDeliveryStatus(MailDeliveryStatus deliveryStatus)
	{
		this.deliveryStatus = deliveryStatus;
	}

	public UndeliverableMailReason getUndeliverableMailReason()
	{
		return undeliverableMailReason;
	}

	public void setUndeliverableMailReason(UndeliverableMailReason undeliverableMailReason)
	{
		this.undeliverableMailReason = undeliverableMailReason;
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
	
	public LocalDateTime getNextRetryDate()
	{
		return nextRetryDate;
	}
	
	public void setNextRetryDate(LocalDateTime nextRetryDate)
	{
		this.nextRetryDate = nextRetryDate;
	}
}
