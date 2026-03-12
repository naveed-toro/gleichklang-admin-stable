package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class AgeFilter extends UserFilter
{
	@XmlAttribute
	@Column(name = "min_age_value")
	@Min(18)
	@Max(115)
	@NotNull
	private int minAge;
	
	@XmlAttribute
	@Column(name = "max_age_value")
	@Min(18)
	@Max(115)
	@NotNull
	private int maxAge;
	
	public int getMinAge()
	{
		return minAge;
	}
	
	public void setMinAge(int minAge)
	{
		this.minAge = minAge;
	}
	
	public int getMaxAge()
	{
		return maxAge;
	}
	
	public void setMaxAge(int maxAge)
	{
		this.maxAge = maxAge;
	}
	
	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}

	@Override
	public String getName()
	{
		return UserFilterType.AGE_FILTER.toString() + ": " + minAge + " - " + maxAge;
	}
}
