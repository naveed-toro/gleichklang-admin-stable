package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType.MATCH_FILTER;

/**
 * Filter for the count in the MatchTable {@link de.binaerebauten.gleichklang.core.model.matching.Match}
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class MatchFilter extends UserFilter
{
	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
	
	@Override
	public String getName()
	{
		return MATCH_FILTER.toString();
	}
}
