package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;

public class SimpleUserFilter<T extends BaseEntity, A> extends AbstractFilter<T, Specification<User>, A>
{
	public static class SimpleUserIdFilter<T extends BaseEntity> extends SimpleUserFilter<T, Long>
	{
		public SimpleUserIdFilter(Collection<SingularAttribute<? super T, Long>> attributes)
		{
			super(attributes, User_.id);
		}
		
		public SimpleUserIdFilter(SingularAttribute<? super T, Long> attribute)
		{
			super(attribute, User_.id);
		}
	}
	
	public static class SimpleUserMailFilter<T extends BaseEntity> extends SimpleUserFilter<T, String>
	{
		public SimpleUserMailFilter(Collection<SingularAttribute<? super T, String>> attributes)
		{
			super(attributes, User_.email);
		}
		
		public SimpleUserMailFilter(SingularAttribute<? super T, String> attribute)
		{
			super(attribute, User_.email);
		}
	}
	
	public static class DirectUserFilter<T extends BaseEntity> extends AbstractFilter<T, Specification<User>, User>
	{
		public DirectUserFilter(SingularAttribute<? super T, User> userAttribute)
		{
			super(userAttribute);
		}
		
		@Override
		protected Predicate toPredicate(Path<User> path, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Subquery<User> sq = query.subquery(User.class);
			final Root<User> user = sq.from(User.class);
			sq.select(user);
			
			final Predicate restriction1 = getValue().toPredicate(user, query, cb);
			final Predicate restriction2 = cb.equal(path, user);
			
			return cb.exists(sq.where(restriction1, restriction2));
		}
	}
	
	private final SingularAttribute<? super User, A> userAttribute;
	
	public SimpleUserFilter(SingularAttribute<? super T, A> fromAttribute, SingularAttribute<? super User, A> toAttribute)
	{
		super(fromAttribute);
		this.userAttribute = toAttribute;
	}
	
	public SimpleUserFilter(Collection<SingularAttribute<? super T, A>> fromAttributes, SingularAttribute<? super User, A> toAttribute)
	{
		super(fromAttributes);
		this.userAttribute = toAttribute;
	}
	
	@Override
	protected Predicate toPredicate(Path<A> path, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		final Subquery<User> sq = query.subquery(User.class);
		final Root<User> user = sq.from(User.class);
		sq.select(user);
		
		final Predicate restriction1 = getValue().toPredicate(user, query, cb);
		final Predicate restriction2 = cb.equal(path, user.get(userAttribute));
		
		return cb.exists(sq.where(restriction1, restriction2));
	}
}
