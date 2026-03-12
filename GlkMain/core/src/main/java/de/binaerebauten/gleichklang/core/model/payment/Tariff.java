package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

/**
 * Enumeration for the tariffs of a {@link SubscriptionOffer}.
 */
public enum Tariff implements DefaultEnumI18N
{
	/**
	 * A social tariff has always an action code with the prefix "SZ-".
	 */
	SOCIAL,
	/**
	 * A standard tariff can have an action code which is then used to
	 * provide discount subscription offers.
	 */
	STANDARD;

	@Override
	public String toString()
	{
		return msg();
	}
}
