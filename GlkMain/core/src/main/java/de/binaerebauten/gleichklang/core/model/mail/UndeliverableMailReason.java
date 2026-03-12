package de.binaerebauten.gleichklang.core.model.mail;

/**
 * Enumerates all reasons that an email couldn't be delivered.
 * <p>
 * Taken unchanged from the old system (UndelivarableMailEntry.Reasons).
 */
public enum UndeliverableMailReason
{
	NONE(-1),
	UNKNOWN(4),
	DOMAIN_NOT_FOUND(0),
	INVALID_ADDRESS(0),
	UNROUTABLE_ADDRESS(0),
	RELAY_ACCESS_DENIED(0),
	SPAM_FILTER(0),
	MAILBOX_FULL(4),
	MANUAL(0),
	MAILER_LITE(0);
	
	private final int maxIncidents;
	
	UndeliverableMailReason(int maxIncidents)
	{
		this.maxIncidents = maxIncidents;
	}
	
	public int getMaxIncidents()
	{
		return maxIncidents;
	}
}
