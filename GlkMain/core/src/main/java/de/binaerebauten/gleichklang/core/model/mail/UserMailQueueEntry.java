package de.binaerebauten.gleichklang.core.model.mail;

import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Mail queue entries with reference to a user.
 */
@Entity
public class UserMailQueueEntry extends MailQueueEntry
{
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "recipient_id", updatable = false, insertable = false)
	private User recipient;
	
	@NotNull
	@Column(name = "recipient_id")
	private Long recipientId;

	@Column(name = "sender_id")
	private Long senderId;

	public Long getSenderId() {
		return senderId;
	}

	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}

	public User getRecipient()
	{
		return recipient;
	}
	
	public void setRecipient(User recipient)
	{
		this.recipient = recipient;
		this.recipientId = recipient.getId();
	}
	
	public Long getRecipientId()
	{
		return recipientId;
	}
	
	public void setRecipientId(Long recipientId)
	{
		this.recipientId = recipientId;
	}
	
}
