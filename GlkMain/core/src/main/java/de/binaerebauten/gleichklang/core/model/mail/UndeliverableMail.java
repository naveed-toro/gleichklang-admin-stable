package de.binaerebauten.gleichklang.core.model.mail;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "undeliverable_mail")
public class UndeliverableMail extends BaseEntity
{
	@NotNull
	@Column(name = "undeliverable_mail_reason")
	@Enumerated(EnumType.STRING)
	private UndeliverableMailReason undeliverableMailReason;

	@NotNull
	@Column(name = "recipient_email")
	private String recipientEmail;
	
	@NotNull
	private int incidents = 1;

	public UndeliverableMailReason getUndeliverableMailReason()
	{
		return undeliverableMailReason;
	}

	public void setUndeliverableMailReason(UndeliverableMailReason undeliverableMailReason)
	{
		this.undeliverableMailReason = undeliverableMailReason;
	}

	public String getRecipientEmail()
	{
		return recipientEmail;
	}

	public void setRecipientEmail(String recipientEmail)
	{
		this.recipientEmail = recipientEmail;
	}
	
	public int getIncidents()
	{
		return incidents;
	}
	
	public void setIncidents(int incidents)
	{
		this.incidents = incidents;
	}
}
