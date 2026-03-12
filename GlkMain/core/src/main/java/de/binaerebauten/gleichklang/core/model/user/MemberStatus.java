package de.binaerebauten.gleichklang.core.model.user;

/**
 * Represents the possible states of a member {@link User}.
 */
public enum MemberStatus
{
	/**
	 * Member can login, but hasn't filled out all necessary questionaires.
	 */
	REGISTRATION,
	/**
	 * Member can login and gets new recommendation. He can use the full functionality of his
	 * subscription.
	 */
	REGISTERED,
	CANCELED,
	DELETED,
	// Adding two new enum to find out who's deleted or cancelled the user.
	ADMIN_CANCELED,
	ADMIN_DELETED,
	ADMIN_BLOCKED,
	BLOCKED
}
