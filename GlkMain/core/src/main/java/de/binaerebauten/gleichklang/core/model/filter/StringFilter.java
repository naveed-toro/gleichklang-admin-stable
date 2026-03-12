package de.binaerebauten.gleichklang.core.model.filter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlTransient
@MappedSuperclass
public abstract class StringFilter extends UserFilter
{
	@XmlValue
	@Column(name = "string_value")
	@NotNull
	private String value;

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public String getName()
	{
		final UserFilterType userFilterType = UserFilterType.valueOf(getClass());
		final String userFilterTypeStr = userFilterType == null ? getClass().getSimpleName() : userFilterType.toString();

		return userFilterTypeStr + ": " + value;
	}
}
