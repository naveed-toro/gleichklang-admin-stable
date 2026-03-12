package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.DeletableEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class DeletedFilter<T extends DeletableEntity<?>> implements Specification<T>
{
	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		return cb.equal(root.get(DeletableEntity.DELETED), false);
	}
}
