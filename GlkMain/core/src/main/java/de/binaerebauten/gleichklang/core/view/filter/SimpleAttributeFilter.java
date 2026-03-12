package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.Collections;

public class SimpleAttributeFilter<T extends BaseEntity, A> extends AbstractFilter<T, A, A>
{
	/**
	 *
	 * The NestedAttributeFilter are very special. If nobody use it, they can be deleted.
	 * Maybe it could be easier to inherit from {@link SimpleAttributeFilter} and override
	 * the {@link SimpleAttributeFilter#getSimplePath(Root)} manually for the concrete case.
	 *
	 * An alternative could be to set a {@link Path} with the help of the {@link javax.persistence.EntityManager}.
	 *
	 */
	
	public static class NestedAttributeFilter1<T extends BaseEntity, A1, A2> extends SimpleAttributeFilter<T, A2>
	{
		private final SingularAttribute<? super T, A1> singularAttribute1;
		private final SingularAttribute<? super A1, A2> singularAttribute2;
		
		public NestedAttributeFilter1(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, A2> singularAttribute2)
		{
			this(singularAttribute1, singularAttribute2, false);
		}
		
		public NestedAttributeFilter1(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, A2> singularAttribute2, boolean invert)
		{
			super(invert);
			
			this.singularAttribute1 = singularAttribute1;
			this.singularAttribute2 = singularAttribute2;
		}
		
		@Override
		protected Collection<Path<A2>> getSimplePath(Root<T> root)
		{
			return Collections.singletonList(root.get(singularAttribute1).get(singularAttribute2));
		}
	}
	
	public static class NestedAttributeFilter2<T extends BaseEntity, A1, A2, A3> extends SimpleAttributeFilter<T, A3>
	{
		private final SingularAttribute<? super T, A1> singularAttribute1;
		private final SingularAttribute<? super A1, A2> singularAttribute2;
		private final SingularAttribute<? super A2, A3> singularAttribute3;
		
		public NestedAttributeFilter2(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, A2> singularAttribute2, SingularAttribute<? super A2, A3> singularAttribute3)
		{
			this(singularAttribute1, singularAttribute2, singularAttribute3, false);
		}
		
		public NestedAttributeFilter2(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, A2> singularAttribute2, SingularAttribute<? super A2, A3> singularAttribute3, boolean invert)
		{
			super(invert);
			
			this.singularAttribute1 = singularAttribute1;
			this.singularAttribute2 = singularAttribute2;
			this.singularAttribute3 = singularAttribute3;
		}
		
		@Override
		protected Collection<Path<A3>> getSimplePath(Root<T> root)
		{
			return Collections.singletonList(root.get(singularAttribute1).get(singularAttribute2).get(singularAttribute3));
		}
	}
	
	public static class NestedAttributeFilter3<T extends BaseEntity, A1, A2, A3, A4> extends SimpleAttributeFilter<T, A4>
	{
		private final SingularAttribute<? super T, A1> singularAttribute1;
		private final SingularAttribute<? super A1, A2> singularAttribute2;
		private final SingularAttribute<? super A2, A3> singularAttribute3;
		private final SingularAttribute<? super A3, A4> singularAttribute4;
		
		public NestedAttributeFilter3(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, A2> singularAttribute2, SingularAttribute<? super A2, A3> singularAttribute3, SingularAttribute<? super A3, A4> singularAttribute4)
		{
			this(singularAttribute1, singularAttribute2, singularAttribute3, singularAttribute4, false);
		}
		
		public NestedAttributeFilter3(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, A2> singularAttribute2, SingularAttribute<? super A2, A3> singularAttribute3, SingularAttribute<? super A3, A4> singularAttribute4, boolean invert)
		{
			super(invert);
			
			this.singularAttribute1 = singularAttribute1;
			this.singularAttribute2 = singularAttribute2;
			this.singularAttribute3 = singularAttribute3;
			this.singularAttribute4 = singularAttribute4;
		}
		
		@Override
		protected Collection<Path<A4>> getSimplePath(Root<T> root)
		{
			return Collections.singletonList(root.get(singularAttribute1).get(singularAttribute2).get(singularAttribute3).get(singularAttribute4));
		}
	}
	
	private final boolean invert;
	
	public SimpleAttributeFilter(SingularAttribute<? super T, A> attribute)
	{
		this(attribute, false);
	}
	
	public SimpleAttributeFilter(SingularAttribute<? super T, A> attribute, boolean invert)
	{
		super(attribute);
		
		this.invert = invert;
	}
	
	protected SimpleAttributeFilter(boolean invert)
	{
		super();
		
		this.invert = invert;
	}
	
	@Override
	protected Predicate toPredicate(Path<A> path, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		final Predicate equal = cb.equal(path, getValue());
		return invert ? cb.not(equal) : equal;
	}
}
