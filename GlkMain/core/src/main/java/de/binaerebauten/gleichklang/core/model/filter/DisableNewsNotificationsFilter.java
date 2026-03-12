package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
public class DisableNewsNotificationsFilter extends BooleanFilter
{
	@Override
	public <T> T accept(FilterVisitor<T> filterVisitor)
	{
		return filterVisitor.visit(this);
	}
}
