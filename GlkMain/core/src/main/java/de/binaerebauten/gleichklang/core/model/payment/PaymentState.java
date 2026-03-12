package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

/**
 * All possible payment states.
 */
public enum PaymentState implements DefaultEnumI18N
{
	PENDING,
	PAID,
	CANCELED,
	FAILED,
	UNKNOWN;

	@Override
	public String toString() {
		return msg();
	}
}
