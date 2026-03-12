package de.binaerebauten.gleichklang.core.utils.filter;

import de.binaerebauten.gleichklang.core.model.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.model.filter.BinaryOperatorFilter;
import de.binaerebauten.gleichklang.core.model.filter.BinaryOperatorFilter.BinaryOperator;
import de.binaerebauten.gleichklang.core.model.filter.UnaryOperatorFilter;

public class FilterBuilder
{
	private AbstractFilter rootFilter;

	public FilterBuilder()
	{
		this(null);
	}

	public FilterBuilder(AbstractFilter rootFilter)
	{
		this.rootFilter = rootFilter;
	}

	/**
	 * Add filters to the rootFilter as intersection. The filter among each
	 * other will be an union. So that addFilter(a, b) is not the same as
	 * addFilter(a); addFilter(b)!!!
	 *
	 * @param filters
	 */
	public void addFilter(AbstractFilter... filters)
	{
		if (filters == null) return;

		AbstractFilter filterUnion = null;

		for (AbstractFilter filter : filters)
		{
			filterUnion = mergeFilter(filterUnion, filter, BinaryOperator.UNION);
		}

		rootFilter = mergeFilter(rootFilter, filterUnion, BinaryOperator.INTERSECTION);
	}

	private AbstractFilter mergeFilter(AbstractFilter rootFilter, AbstractFilter filter, BinaryOperator binaryOperator)
	{
		if (filter == null) return rootFilter;
		if (rootFilter == null) return filter;

		final BinaryOperatorFilter binaryOperatorFilter = new BinaryOperatorFilter();
		binaryOperatorFilter.setBinaryOperator(binaryOperator);
		binaryOperatorFilter.setLeftFilter(filter);
		binaryOperatorFilter.setRightFilter(rootFilter);

		return binaryOperatorFilter;
	}

	/**
	 * Remove the filter from the rootFilter-tree.
	 *
	 * @param filter
	 */
	public void removeFilter(AbstractFilter filter)
	{
		rootFilter = removeFromFilter(rootFilter, filter);
	}

	private AbstractFilter removeFromFilter(AbstractFilter filter, AbstractFilter filterToRemove)
	{
		if (filter == filterToRemove) return null;

		if (filter instanceof BinaryOperatorFilter)
		{
			final BinaryOperatorFilter binaryOperatorFilter = (BinaryOperatorFilter) filter;

			final AbstractFilter newLeftFilter = removeFromFilter(binaryOperatorFilter.getLeftFilter(), filterToRemove);
			final AbstractFilter newRightFilter = removeFromFilter(binaryOperatorFilter.getRightFilter(), filterToRemove);

			if (newLeftFilter == null)
			{
				return newRightFilter;
			}
			else if (newRightFilter == null)
			{
				return newLeftFilter;
			}

			binaryOperatorFilter.setRightFilter(newRightFilter);
			binaryOperatorFilter.setLeftFilter(newLeftFilter);
		}
		else if (filter instanceof UnaryOperatorFilter)
		{
			final UnaryOperatorFilter unaryOperatorFilter = (UnaryOperatorFilter) filter;

			final AbstractFilter newFilter = removeFromFilter(unaryOperatorFilter.getFilter(), filterToRemove);
			if (newFilter == null) return null;
			unaryOperatorFilter.setFilter(newFilter);
		}

		return filter;
	}

	public void reset()
	{
		rootFilter = null;
	}

	public AbstractFilter getFilter()
	{
		return rootFilter;
	}
}
