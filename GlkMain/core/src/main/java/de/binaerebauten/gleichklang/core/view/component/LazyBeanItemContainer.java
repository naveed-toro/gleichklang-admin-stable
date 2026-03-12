package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.collect.ImmutableSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.vaadin.addons.lazyquerycontainer.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LazyBeanQueryContainer provides lazy loading of bean-items from business
 * services. It is easier to use instead the {@link LazyQueryContainer}, because
 * the most configurations are set to a default value.
 *
 * @param <BEANTYPE>
 * @author fhessel
 */
@SuppressWarnings("serial")
public class LazyBeanItemContainer<BEANTYPE> extends LazyQueryContainer
{
	public interface SizeChangeListener
	{
		void onSizeChanged(int size);
	}
	
	public interface LazyBeanItemsHandler<BEANTYPE>
	{
		Page<BEANTYPE> getItems(Pageable pageable);
	}

	public interface LazyBeanFilteredItemsHandler<BEANTYPE>
	{
		Page<BEANTYPE> getItems(Specification<BEANTYPE> specification, Pageable pageable);
	}

	private static class BeanQuery<BEANTYPE> extends AbstractBeanQuery<BEANTYPE>
	{
		private final LazyBeanFilteredItemsHandler<BEANTYPE> filteredHandler;
		private final Collection<Specification<BEANTYPE>> specifications;
		private final SizeChangeListener sizeChangeListener;

		private Page<BEANTYPE> cachedQuery = null;
		private Pageable cachedPageable = null;
		private Integer size = null;

		public BeanQuery(QueryDefinition queryDefinition, LazyBeanFilteredItemsHandler<BEANTYPE> filteredHandler, Collection<Specification<BEANTYPE>> specifications, SizeChangeListener sizeChangeListener)
		{
			super(queryDefinition, null, queryDefinition.getSortPropertyIds(), queryDefinition.getSortPropertyAscendingStates());
			this.filteredHandler = filteredHandler;
			this.specifications = specifications;
			this.sizeChangeListener = sizeChangeListener;
		}

		@Override
		protected BEANTYPE constructBean()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * The total size of all elements. This function retrieve the first page
		 * and save this first page. The spring.data implementation of
		 * {@link Pageable} and {@link Page} included the total elements, so it
		 * is not necessary to call this separately
		 */
		@Override
		public int size()
		{
			cachedPageable = getPageable(0);
			cachedQuery = filteredHandler.getItems(getSpecification(specifications), cachedPageable);
			final int newSize = cachedQuery != null ? (int) cachedQuery.getTotalElements() : 0;
			
			if(size == null || !size.equals(newSize))
			{
				size = newSize;
				sizeChangeListener.onSizeChanged(size);
			}
			
			return size;
		}

		/**
		 * Creates the {@link Pageable}-Object for the database request includes
		 * the column-order.
		 *
		 * @param startIndex
		 * @return
		 */
		private Pageable getPageable(int startIndex)
		{
			Sort sort = null;
			final List<Order> orders = new ArrayList<>();
			for (int i = 0; i < getSortPropertyIds().length; i++)
			{
				orders.add(new Order(getSortStates()[i] ? Direction.ASC : Direction.DESC, (String) getSortPropertyIds()[i]));
			}
			if (!orders.isEmpty()) sort = new Sort(orders);

			return new PageRequest(startIndex / getQueryDefinition().getBatchSize(), getQueryDefinition().getBatchSize(), sort);
		}

		/**
		 * Returns a sub-list of the requested section. The count property will
		 * be ignored and replaced with the
		 * {@link QueryDefinition#getBatchSize()}. The {@link #getPageable(int)}
		 * convert the startIndex to the corresponding {@link Pageable}. The
		 * result will be cached and reused, if the {@link Pageable} equals. So
		 * the saved first page from {@link #size()} can be reused.
		 */
		@Override
		protected List<BEANTYPE> loadBeans(int startIndex, int count)
		{
			final Pageable pageable = getPageable(startIndex);
			final Specification<BEANTYPE> specification = getSpecification(specifications);
			if (!pageable.equals(cachedPageable))
			{
				cachedPageable = pageable;
				cachedQuery = filteredHandler.getItems(specification, pageable);
			}
			return cachedQuery != null ? cachedQuery.getContent() : new ArrayList<>(0);
		}

		@Override
		protected void saveBeans(List<BEANTYPE> addedBeans, List<BEANTYPE> modifiedBeans, List<BEANTYPE> removedBeans)
		{
			throw new UnsupportedOperationException();
		}
	}

	private static class LazyBeanFactory<BEANTYPE> implements QueryFactory
	{
		private final LazyBeanFilteredItemsHandler<BEANTYPE> filteredHandler;
		private final Collection<Specification<BEANTYPE>> specifications;
		private final SizeChangeListener sizeChangeListener;

		public LazyBeanFactory(LazyBeanFilteredItemsHandler<BEANTYPE> filteredHandler, Collection<Specification<BEANTYPE>> specifications, SizeChangeListener sizeChangeListener)
		{
			this.filteredHandler = filteredHandler;
			this.specifications = specifications;
			this.sizeChangeListener = sizeChangeListener;
		}

		@Override
		public Query constructQuery(QueryDefinition queryDefinition)
		{
			return new BeanQuery<>(queryDefinition, filteredHandler, specifications, sizeChangeListener);
		}
	}
	
	public LazyBeanItemContainer(LazyBeanFilteredItemsHandler<BEANTYPE> filteredHandler, Collection<Specification<BEANTYPE>> specifications, SizeChangeListener sizeChangeListener)
	{
		super(new LazyBeanFactory<>(filteredHandler, specifications, sizeChangeListener), null, 100, true);
	}

	public LazyBeanItemContainer(LazyBeanItemsHandler<BEANTYPE> handler, SizeChangeListener sizeChangeListener)
	{
		this((specification, pageable) -> handler.getItems(pageable), new HashSet<>(), sizeChangeListener);
	}
	
	/**
	 * Returns a Specification-Object from the specification collection ANDed.
	 *
	 * @return
	 */
	public static <T> Specification<T> getSpecification(Collection<Specification<T>> specifications)
	{
		Specifications<T> result = null;
		for (Specification<T> specification : specifications)
		{
			if (result == null)
			{
				result = Specifications.where(specification);
			}
			else
			{
				result = result.and(specification);
			}
		}
		return result;
	}
	
	public void setMaxNestedPropertyDepth(int depth)
	{
		getQueryView().getQueryDefinition().setMaxNestedPropertyDepth(depth);
	}
	
	/**
	 * Convert the itemId to the concrete bean.
	 *
	 * @param itemId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public BEANTYPE getBean(Object itemId)
	{
		if (itemId == null) return null;
		
		if (itemId instanceof Set<?>)
		{
			final Set<BEANTYPE> beans = getBeans((Set<Object>) itemId);
			if(beans.size() == 1) return beans.iterator().next();
			return null;
		}
		
		final CompositeItem compositeItem = (CompositeItem) getItem(itemId);
		if(compositeItem == null) return null;

		final NestingBeanItem<BEANTYPE> nestingBeanItem = (NestingBeanItem<BEANTYPE>) compositeItem.getItem("bean");
		if(nestingBeanItem == null) return null;

		return nestingBeanItem.getBean();
	}
	
	/**
	 * Convert the itemIds to the concrete beans.
	 *
	 * @param itemIds
	 * @return
	 */
	public Set<BEANTYPE> getBeans(Set<Object> itemIds)
	{
		if (itemIds == null) return Collections.emptySet();

		return itemIds.stream().map(this::getBean).filter(Objects::nonNull).collect(Collectors.toSet());
	}
	/**
	 * Returns beans.
	 *
	 * @return
	 */
	public Set<BEANTYPE> getBeans()
	{

		return this.getItemIds().stream().map(this::getBean).filter(Objects::nonNull).collect(Collectors.toSet());
	}
	/**
	 * Convert the itemId to the concrete bean. If the itemIds contains a single
	 * element, a set with one element will returned. If the itemIds contains a
	 * set of items, every object will convert to the bean and returned as set.
	 *
	 * @param itemIds
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<BEANTYPE> getBeans(Object itemIds)
	{
		if (itemIds == null) return Collections.emptySet();

		if (itemIds instanceof Set<?>)
		{
			return getBeans((Set<Object>) itemIds);
		}
		else
		{
			final BEANTYPE bean = getBean(itemIds);
			return bean == null ? Collections.emptySet() : Collections.singleton(bean);
		}
	}

	public Set<Integer> indexOfIds(Set<Object> itemIds)
	{
		if (itemIds == null) return new HashSet<>(0);

		final Set<Integer> result = new HashSet<>(itemIds.size());

		itemIds.forEach(itemId -> result.add(indexOfId(itemId)));

		return result;
	}

	@SuppressWarnings("unchecked")
	public Set<Integer> indexOfIds(Object itemIds)
	{
		if (itemIds == null) return new HashSet<>(0);

		if (itemIds instanceof Set<?>)
		{
			return indexOfIds((Set<Object>) itemIds);
		}
		else
		{
			return ImmutableSet.of(indexOfId(itemIds));
		}
	}
}
