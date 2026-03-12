package de.binaerebauten.gleichklang.core.model.filter;

public interface Filterable
{
	AbstractFilter getFilter();

	void setFilter(AbstractFilter filter);
}
