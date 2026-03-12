package de.binaerebauten.gleichklang.core.model.affiliate;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

/**
 * All connected affiliate partners.
 */
public enum AffiliatePartner implements DefaultEnumI18N
{
	SUPERCLIX,
	ADCELL;
	
	@Override
	public String toString() {
		return msg();
	}
}
