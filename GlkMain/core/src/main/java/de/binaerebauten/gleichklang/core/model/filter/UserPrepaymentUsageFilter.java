package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Filters users based on their {@link de.binaerebauten.gleichklang.core.model.payment.Prepayment#externalReferenceId}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class UserPrepaymentUsageFilter extends StringFilter
{
	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
}
