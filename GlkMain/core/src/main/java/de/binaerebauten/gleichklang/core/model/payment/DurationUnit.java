package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

import java.time.temporal.ChronoUnit;

/**
 * Enumeration for the possible duration unit of a {@link SubscriptionOffer}.
 */
public enum DurationUnit implements DefaultEnumI18N
{
	/**
	 * This should just be used for testing.
	 */
	HOURS(ChronoUnit.HOURS),
	MONTHS(ChronoUnit.MONTHS);

	/**
	 * The chrono unit of this duration.
	 */
	public final ChronoUnit chronoUnit;

	DurationUnit(ChronoUnit chronoUnit)
	{
		this.chronoUnit = chronoUnit;
	}

	@Override
	public String toString()
	{
		return msg();
	}
}
