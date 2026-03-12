package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import static de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType.MISSING_ANSWERS_RATIO_FILTER;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class MissingAnswersRatioFilter extends UserFilter
{
	@XmlAttribute
	@Enumerated(EnumType.STRING)
	@NotNull
	private RecommendationCategory category;
	
	public RecommendationCategory getCategory()
	{
		return category;
	}
	
	public void setCategory(RecommendationCategory category)
	{
		this.category = category;
	}
	
	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
	
	@Override
	public String getName()
	{
		return MISSING_ANSWERS_RATIO_FILTER.toString();
	}
}
