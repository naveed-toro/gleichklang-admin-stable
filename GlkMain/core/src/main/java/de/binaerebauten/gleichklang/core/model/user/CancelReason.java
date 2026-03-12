package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

public enum CancelReason implements DefaultEnumI18N
{
	SUCCESS_THROW_GK,
	NO_TIME,
	UNSUITABLE_RECOMMENDATIONS,
	SUCCESS_ELSEWHERE,
	OTHER,
	TOO_LESS_RECOMMENDATIONS,
	UNHAPPY_WITH_SERVICE;

	@Override
	public String toString()
	{
		return msg();
	}
}
