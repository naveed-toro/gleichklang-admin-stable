package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

/**
 * Describes payment methods.
 * The order of items appears same in the UI.
 *
 * @author matthias.koester@binaere-bauten.de
 */
public enum PaymentMethod implements DefaultEnumI18N
{
	DIRECT_DEBIT(true), // in german "Bankeinzug"
	CREDIT_CARD(true),
	PREPAYMENT(false);

	private final boolean external;

	PaymentMethod(boolean external)
	{
		this.external = external;
	}

	public boolean isExternal()
	{
		return external;
	}

	@Override
	public String toString() {
		return msg();
	}
}
