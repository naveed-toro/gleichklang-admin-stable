package de.binaerebauten.gleichklang.core.utils.filter;

import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType;

import java.util.*;
import java.util.stream.Collectors;

public class FilterHelper
{
	private static class FilterSetVisitor implements DefaultFilterVisitor<List<AbstractFilter>>
	{
		@Override
		public List<AbstractFilter> visit(UserFilter userFilter)
		{
			return Collections.singletonList(userFilter);
		}
		
		@Override
		public List<AbstractFilter> visit(UnaryOperatorFilter unaryOperatorFilter)
		{
			final List<AbstractFilter> result = new ArrayList<>();
			result.add(unaryOperatorFilter);
			result.addAll(unaryOperatorFilter.getFilter().accept(this));
			return result;
		}
		
		@Override
		public List<AbstractFilter> visit(BinaryOperatorFilter binaryOperatorFilter)
		{
			final List<AbstractFilter> result = new ArrayList<>();
			result.add(binaryOperatorFilter);
			result.addAll(binaryOperatorFilter.getLeftFilter().accept(this));
			result.addAll(binaryOperatorFilter.getRightFilter().accept(this));
			return result;
		}
		
		@Override
		public List<AbstractFilter> visit(TemplateFilter templateFilter)
		{
			return Collections.emptyList();
		}
	}
	
	public static Collection<UserFilterType> getUserFilterTypes(AbstractFilter filter)
	{
		final List<AbstractFilter> filters = getFilterList(filter);
		return filters.stream().map(f -> UserFilterType.valueOf(f.getClass())).filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	/**
	 * Create a list with all filters. Flatten the tree
	 *
	 * @return
	 */
	public static List<AbstractFilter> getFilterList(AbstractFilter filter)
	{
		if(filter == null) return Collections.emptyList();
		return filter.accept(new FilterSetVisitor());
	}
}
