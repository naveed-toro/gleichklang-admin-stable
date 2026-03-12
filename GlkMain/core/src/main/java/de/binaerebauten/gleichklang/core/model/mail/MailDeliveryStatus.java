package de.binaerebauten.gleichklang.core.model.mail;

/**
 * Enumerates the possible states of a {@link MailQueueEntry}.
 */
public enum MailDeliveryStatus
{
	SCHEDULED,
	DESCHEDULED,
	PENDING,
	SENT,
	DISCARDED,
	UNDELIVERABLE
}
