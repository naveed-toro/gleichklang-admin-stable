package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

/**
 * Created by michael on 09/04/15.
 */
public enum AdminRole implements DefaultEnumI18N
{
	ADMIN_MANAGEMENT,
	QUESTIONNAIRE,
	MATCHING,
	STATISTICS,
	PRODUCTS,
	SUBSCRIPTIONS,
	HEIDELPAY,
	BANK_ACCOUNTS,
	INVOICE,
	FILTER,
	USER_CONTROL,
	TRANSLATION,
	NEWS,
	ADMIN_MESSAGES,
	SERIALIZATION,
	SECURITY,
	BUSINESS_STATISTICS,
	SYSTEM_CONFIGURATION;
	@Override
	public String toString()
	{
		return msg();
	}
}
