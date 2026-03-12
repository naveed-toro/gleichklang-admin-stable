package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
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
public class RecommendationBreakFilter extends EnumFilter<RecommendationCategory>
{
	@XmlAttribute
	@Column(name = "enum_value")
	@Enumerated(EnumType.STRING)
	@NotNull
	private RecommendationCategory enumValue;
	
	@Override
	public RecommendationCategory getEnumValue()
	{
		return enumValue;
	}
	
	@Override
	public void setEnumValue(RecommendationCategory enumValue)
	{
		this.enumValue = enumValue;
	}
	
	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
}
