package de.binaerebauten.gleichklang.core.model.message;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.security.SanitizeContent;
import org.hibernate.annotations.Formula;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message extends BaseEntity
{
	public enum MessageType
	{
		DEFAULT,
		CANCEL_MESSAGE,
		LOVE_SCAMMER,
		ABUSE,
		PENDING_PAYMENT,
		NEW_ZIP
	}
	
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "message")
	private ReceiverEnvelope receiverEnvelope;
	
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "message")
	private SenderEnvelope senderEnvelope;

	@ManyToOne
	@SanitizeContent
	@JoinColumn(name = "reply_to_message_id")
	private Message replyToMessage;

	// TODO fabian: i18n
	@SanitizeContent
	@NotBlank(message = "Bitte einen Betreff eingeben")
	@Size(min = 1, max = 255)
	@Column
	private String subject;

	// TODO fabian: i18n
	@SanitizeContent
	@NotBlank(message = "Bitte einen Nachrichtentext eingeben")
	@Lob
	@Column
	private String body;

	@NotNull
	@Column
	private boolean sent = false;

	@Column(name = "send_date")
	private LocalDateTime sendDate;
	
	@Column
	private boolean deleted = false;
	
	@Column(name = "message_type")
	@Enumerated(value = EnumType.STRING)
	@NotNull
	private MessageType messageType = MessageType.DEFAULT;

	@Formula("(SELECT COUNT(m.id) FROM message_attachment m WHERE m.message_id = id)")
	private Long sumAttachements;

	@Formula("(SELECT COUNT(m.id) FROM message m WHERE m.reply_to_message_id = id)")
	private Long sumReplies;
	
	public ReceiverEnvelope getReceiverEnvelope()
	{
		return receiverEnvelope;
	}
	
	public void setReceiverEnvelope(ReceiverEnvelope receiverEnvelope)
	{
		this.receiverEnvelope = receiverEnvelope;
	}
	
	public SenderEnvelope getSenderEnvelope()
	{
		return senderEnvelope;
	}
	
	public void setSenderEnvelope(SenderEnvelope senderEnvelope)
	{
		this.senderEnvelope = senderEnvelope;
	}
	
	public Message getReplyToMessage()
	{
		return replyToMessage;
	}
	
	public void setReplyToMessage(Message replyToMessage)
	{
		this.replyToMessage = replyToMessage;
	}
	
	public String getSubject()
	{
		return subject;
	}
	
	public void setSubject(String subject)
	{
		this.subject = subject;
	}
	
	public String getBody()
	{
		return body;
	}
	
	public void setBody(String body)
	{
		this.body = body;
	}
	
	public boolean isSent()
	{
		return sent;
	}
	
	public void setSent(boolean sent)
	{
		this.sent = sent;
	}
	
	public LocalDateTime getSendDate()
	{
		return sendDate;
	}
	
	public void setSendDate(LocalDateTime sendDate)
	{
		this.sendDate = sendDate;
	}
	
	public boolean isDeleted()
	{
		return deleted;
	}
	
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
	
	public MessageType getMessageType()
	{
		return messageType;
	}
	
	public void setMessageType(MessageType messageType)
	{
		this.messageType = messageType;
	}
	
	public Long getSumAttachements() {
		return sumAttachements;
	}

	public Long getSumReplies() {
		return sumReplies;
	}
}
