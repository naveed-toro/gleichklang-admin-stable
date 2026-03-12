package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.IdEntity;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.SortableEntity;
import de.binaerebauten.gleichklang.core.view.component.TableControl.Direction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BaseRepositoryImpl<T extends SortableEntity<ID> & NaturalKeyEntity<ID>, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements SortRepository<T, ID>, NaturalKeyRepository<T, ID>, SpareEntityRepository<T, ID>
{
	private final EntityManager em;

	public BaseRepositoryImpl(Class<T> domainClass, EntityManager em)
	{
		super(domainClass, em);
		this.em = em;
	}

	public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager em)
	{
		super(entityInformation, em);
		this.em = em;
	}

	private T findTargetEntity(int sortOrder, Direction direction, Specification<T> specification)
	{
		Specification<T> sortOrderSpecification = null;
		Sort sort = null;
		switch (direction)
		{
			case UP:
				sortOrderSpecification = (root, query, cb) -> cb.lessThan(root.get(SortableEntity.SORT_ORDER), sortOrder);
				sort = new Sort(Sort.Direction.DESC, SortableEntity.SORT_ORDER, IdEntity.ID);
				break;
			case DOWN:
				sortOrderSpecification = (root, query, cb) -> cb.greaterThan(root.get(SortableEntity.SORT_ORDER), sortOrder);
				sort = new Sort(Sort.Direction.ASC, SortableEntity.SORT_ORDER, IdEntity.ID);
				break;
		}

		return getQuery(Specifications.where(sortOrderSpecification).and(specification), sort)
				.setMaxResults(1)
				.getSingleResult();
	}

	@Override
	public int findMinSortOrder(Specification<T> specification)
	{
		try
		{
			return getQuery(specification, new Sort(Sort.Direction.ASC, SortableEntity.SORT_ORDER)).setMaxResults(1).getSingleResult().getSortOrder();
		}
		catch (NoResultException ex)
		{
			return -1;
		}
	}

	@Override
	public int findMaxSortOrder(Specification<T> specification)
	{
		try
		{
			return getQuery(specification, new Sort(Sort.Direction.DESC, SortableEntity.SORT_ORDER)).setMaxResults(1).getSingleResult().getSortOrder();
		}
		catch (NoResultException ex)
		{
			return -1;
		}
	}

	@Transactional
	public Map<Integer, T> moveItems(Map<Integer, T> items, Direction
			direction, Specification<T> specification)
	{
		final Map<T, Integer> itemsBiMap = items.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey));
		final Map<Integer, T> result = new HashMap<>();

		final int borderSortOrder = direction == Direction.UP ? findMinSortOrder(specification) : findMaxSortOrder(specification);
		int prevSortOrder = -1;
		final List<T> sortedList = items.values().stream()
				.sorted((o1, o2) -> direction == Direction.UP ? Integer.compare(o1.getSortOrder(), o2.getSortOrder()) : Integer.compare(o2.getSortOrder(), o1.getSortOrder()))
				.collect(Collectors.toList());

		for (final T entity : sortedList)
		{
			final int sortOrder = entity.getSortOrder();
			int index = itemsBiMap.get(entity);
			final boolean borderItem = sortOrder == borderSortOrder;

			if (!borderItem)
			{
				final T targetEntity = findTargetEntity(sortOrder, direction, specification);
				final int targetSortOrder = targetEntity.getSortOrder();

				if (targetSortOrder != prevSortOrder)
				{
					entity.setSortOrder(-1);
					save(entity);
					flush();

					targetEntity.setSortOrder(sortOrder);
					save(targetEntity);
					flush();

					index += direction == Direction.UP ? -1 : 1;
					entity.setSortOrder(targetSortOrder);
					save(entity);
				}
			}

			prevSortOrder = entity.getSortOrder();
			result.put(index, entity);
		}

		return result;
	}

	@Override
	public T findByNaturalKey(NaturalKey naturalKey)
	{
		if(naturalKey == null) return null;

		final Specification<T> specification = (root, query, cb) -> cb.equal(root.get(NaturalKeyEntity.NATURAL_KEY), naturalKey.naturalKey);
		for(T item : getQuery(specification, (Sort) null).getResultList())
		{
			if(item.getClass().equals(naturalKey.naturalKeyClass)) return item;
		}

		return null;
	}
	
	@Override
	public List<ID> findAllWithIdsOnly()
	{
		return findAllWithIdsOnly(null);
	}
	
	@Override
	public List<ID> findAllWithIdsOnly(Specification<T> spec)
	{
		final List<ID> ids = new ArrayList<>();
		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		final CriteriaQuery<Tuple> tupleQuery = criteriaBuilder.createTupleQuery();
		final Root<T> root = tupleQuery.from(getDomainClass());

		if (spec != null)
		{
			tupleQuery.where(spec.toPredicate(root, tupleQuery, criteriaBuilder));
		}

		tupleQuery.select(criteriaBuilder.tuple(root.get(IdEntity.ID)));

		final List<Tuple> results = em.createQuery(tupleQuery).getResultList();
		for (Tuple tuple : results)
		{
			ids.add((ID) tuple.get(0));
		}

		return ids;
	}

}
