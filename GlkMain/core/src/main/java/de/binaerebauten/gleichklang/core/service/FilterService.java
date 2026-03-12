package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.repository.FilterRepository;
import de.binaerebauten.gleichklang.core.utils.filter.DefaultFilterVisitor;
import de.binaerebauten.gleichklang.core.utils.filter.FilterHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class FilterService
{
	private class FilterSaveVisitor implements DefaultFilterVisitor<Collection<AbstractFilter>>
	{
		private final List<AbstractFilter> oldFilters;

		private FilterSaveVisitor(List<AbstractFilter> oldFilters)
		{
			this.oldFilters = new ArrayList<>(oldFilters);
		}

		@Override
		public Collection<AbstractFilter> visit(UserFilter userFilter)
		{
			oldFilters.remove(userFilter);
			filterRepository.save(userFilter);
			return oldFilters;
		}

		@Override
		public Collection<AbstractFilter> visit(UnaryOperatorFilter unaryOperatorFilter)
		{
			oldFilters.remove(unaryOperatorFilter);
			unaryOperatorFilter.getFilter().accept(this);
			filterRepository.save(unaryOperatorFilter);
			return oldFilters;
		}

		@Override
		public Collection<AbstractFilter> visit(BinaryOperatorFilter binaryOperatorFilter)
		{
			oldFilters.remove(binaryOperatorFilter);
			binaryOperatorFilter.getLeftFilter().accept(this);
			binaryOperatorFilter.getRightFilter().accept(this);
			filterRepository.save(binaryOperatorFilter);
			return oldFilters;
		}

		@Override
		public Collection<AbstractFilter> visit(TemplateFilter templateFilter)
		{
			return oldFilters;
		}
	}

	@Autowired
	private FilterRepository filterRepository;

	/**
	 * Save the template filter and all his children. To delete the previous and
	 * obsolet filters it is necessary to commit the oldFilters as list. It is
	 * possible to create this list with {@link #getFilterList(Filterable)}
	 *
	 * @param filter
	 * @param oldFilters
	 */
	@Transactional
	public void saveFilter(TemplateFilter filter, List<AbstractFilter> oldFilters)
	{
		Objects.requireNonNull(filter);
		Objects.requireNonNull(oldFilters);

		final Collection<AbstractFilter> removeFilters = saveFilterChain(filter, oldFilters);
		filterRepository.save(filter);
		deleteFilters(removeFilters);
	}

	@Transactional
	public Collection<AbstractFilter> saveFilterChain(Filterable filterable, List<AbstractFilter> oldFilters)
	{
		Objects.requireNonNull(filterable);
		Objects.requireNonNull(oldFilters);

		if(filterable.getFilter() == null) return oldFilters;
		return filterable.getFilter().accept(new FilterSaveVisitor(oldFilters));
	}

	@Transactional
	public void deleteFilters(Collection<AbstractFilter> removeFilters)
	{
		Objects.requireNonNull(removeFilters);

		filterRepository.delete(removeFilters);
	}

	/**
	 * Create a list with all filters from a filterable (i.e. template-filter or news). Flatten the tree
	 *
	 * @param filterable
	 * @return
	 */
	public List<AbstractFilter> getFilterList(Filterable filterable)
	{
		Objects.requireNonNull(filterable);

		if (filterable.getFilter() == null) return Collections.emptyList();
		return FilterHelper.getFilterList(filterable.getFilter());
	}
}
