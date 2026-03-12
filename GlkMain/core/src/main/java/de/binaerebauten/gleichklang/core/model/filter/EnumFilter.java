package de.binaerebauten.gleichklang.core.model.filter;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@MappedSuperclass
public abstract class EnumFilter<E extends Enum<E>> extends UserFilter
{
	public abstract E getEnumValue();

	public abstract void setEnumValue(E enumValue);

	@Override
	public String getName()
	{
		final UserFilterType userFilterType = UserFilterType.valueOf(getClass());
		final String userFilterTypeStr = userFilterType == null ? getClass().getSimpleName() : userFilterType.toString();

		return userFilterTypeStr + ": " + getEnumValue().toString();
	}
}
