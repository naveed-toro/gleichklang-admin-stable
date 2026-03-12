package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

public abstract class AbstractCategoryFilter<T> implements Specification<T>
{
	private final Map<RecommendationCategory, Specification<T>> filterMap;

	private final boolean initialActivated;
	private final Collection<RecommendationCategory> visibleCategories;

	public AbstractCategoryFilter()
	{
		this(true);
	}

	public AbstractCategoryFilter(boolean initialActivated)
	{
		this(initialActivated, Arrays.asList(RecommendationCategory.values()));
	}
	
	public AbstractCategoryFilter(Collection<RecommendationCategory> visibleCategories)
	{
		this(true, visibleCategories);
	}
	
	public AbstractCategoryFilter(boolean initialActivated, Collection<RecommendationCategory> visibleCategories)
	{
		Objects.requireNonNull(visibleCategories);
		
		this.visibleCategories = visibleCategories;
		this.initialActivated = initialActivated;
		filterMap = new HashMap<>();
		
		reset();
	}
	
	public void reset()
	{
		filterMap.clear();
	}
	
	public void setCategoryEnabled(RecommendationCategory category)
	{
		filterMap.clear();
		setCategoryEnabled(category, true);
	}
	
	public void setCategoryEnabled(RecommendationCategory category, boolean enabled)
	{
		if (!visibleCategories.contains(category)) return;
		
		if (enabled)
		{
			filterMap.put(category, createSingleFilter(category));
		}
		else
		{
			filterMap.remove(category);
		}
	}

	protected abstract Specification<T> createSingleFilter(RecommendationCategory category);

	private Specification<T> orSpecifications(Specification<T> specification1, Specification<T> specification2)
	{
		return Specifications.where(specification1).or(specification2);
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		return filterMap.values().stream()
				.reduce(this::orSpecifications)
				.map(specification -> specification.toPredicate(root, query, cb))
				.orElse(cb.and()); // always true if filter is empty
	}

	public boolean isInitialActivated()
	{
		return initialActivated;
	}
}
