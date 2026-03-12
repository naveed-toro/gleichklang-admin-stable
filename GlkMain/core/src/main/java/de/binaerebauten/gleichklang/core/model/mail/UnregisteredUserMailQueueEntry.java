package de.binaerebauten.gleichklang.core.model.mail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * Mail queue entries with an E-Mail address field only.
 */
@Entity
public class UnregisteredUserMailQueueEntry extends MailQueueEntry
{
	@NotNull
	@Column(name = "recipient_email")
	private String recipientEmail;
	
	public String getRecipientEmail()
	{
		return recipientEmail;
	}
	
	public void setRecipientEmail(String recipientEmail)
	{
		this.recipientEmail = recipientEmail;
	}
	
}
