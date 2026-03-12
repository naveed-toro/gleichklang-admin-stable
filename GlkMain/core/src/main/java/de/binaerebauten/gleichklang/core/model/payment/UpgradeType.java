package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

/**
 * Enumerates the upgrade types
 */
public enum UpgradeType implements DefaultEnumI18N
{
	TARIFF_CHANGE,

	CATEGORY_EXTENSION,

	DONATION;

	@Override
	public String toString()
	{
		return msg();
	}
}
