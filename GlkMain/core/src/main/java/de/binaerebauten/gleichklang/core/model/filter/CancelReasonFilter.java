package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.user.CancelReason;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class CancelReasonFilter extends EnumFilter<CancelReason>
{
    @XmlAttribute
	@Column(name = "enum_value")
	@Enumerated(EnumType.STRING)
	@NotNull
	private CancelReason enumValue;

	@Override
	public CancelReason getEnumValue()
	{
		return enumValue;
	}

	@Override
	public void setEnumValue(CancelReason enumValue)
	{
		this.enumValue = enumValue;
	}

	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
}
