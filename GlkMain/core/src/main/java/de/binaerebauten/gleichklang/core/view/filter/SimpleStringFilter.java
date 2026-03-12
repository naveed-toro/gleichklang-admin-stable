package de.binaerebauten.gleichklang.core.view.filter;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.security.EntityContentSanitizer;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.Collections;

public class SimpleStringFilter<T extends BaseEntity> extends AbstractFilter<T, String, String>
{
	/**
	 *
	 * The NestedStringFilter are very special. If nobody use it, they can be deleted.
	 * Maybe it could be easier to inherit from {@link SimpleStringFilter} and override
	 * the {@link SimpleStringFilter#getSimplePath(Root)} manually for the concrete case.
	 *
	 * An alternative could be to set a {@link Path} with the help of the {@link javax.persistence.EntityManager}.
	 *
	 */
	
	public static class NestedStringFilter1<T extends BaseEntity, A1> extends SimpleStringFilter<T>
	{
		private final SingularAttribute<? super T, A1> singularAttribute1;
		private final SingularAttribute<? super A1, String> singularAttribute2;

		public NestedStringFilter1(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, String> singularAttribute2, boolean sanitizeStrings)
		{
			super();
			
			this.singularAttribute1 = singularAttribute1;
			this.singularAttribute2 = singularAttribute2;
			this.setSanitizeStringBeforeFiltering(sanitizeStrings);
		}
		
		@Override
		protected Collection<Path<String>> getSimplePath(Root<T> root)
		{
			return Collections.singletonList(root.get(singularAttribute1).get(singularAttribute2));
		}
	}
	
	public static class NestedStringFilter2<T extends BaseEntity, A1, A2> extends SimpleStringFilter<T>
	{
		private final SingularAttribute<? super T, A1> singularAttribute1;
		private final SingularAttribute<? super A1, A2> singularAttribute2;
		private final SingularAttribute<? super A2, String> singularAttribute3;
		
		public NestedStringFilter2(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, A2> singularAttribute2, SingularAttribute<? super A2, String> singularAttribute3)
		{
			super();
			
			this.singularAttribute1 = singularAttribute1;
			this.singularAttribute2 = singularAttribute2;
			this.singularAttribute3 = singularAttribute3;
		}
		
		@Override
		protected Collection<Path<String>> getSimplePath(Root<T> root)
		{
			return Collections.singletonList(root.get(singularAttribute1).get(singularAttribute2).get(singularAttribute3));
		}
	}
	
	public static class NestedStringFilter3<T extends BaseEntity, A1, A2, A3> extends SimpleStringFilter<T>
	{
		private final SingularAttribute<? super T, A1> singularAttribute1;
		private final SingularAttribute<? super A1, A2> singularAttribute2;
		private final SingularAttribute<? super A2, A3> singularAttribute3;
		private final SingularAttribute<? super A3, String> singularAttribute4;
		
		public NestedStringFilter3(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, A2> singularAttribute2, SingularAttribute<? super A2, A3> singularAttribute3, SingularAttribute<? super A3, String> singularAttribute4)
		{
			super();
			
			this.singularAttribute1 = singularAttribute1;
			this.singularAttribute2 = singularAttribute2;
			this.singularAttribute3 = singularAttribute3;
			this.singularAttribute4 = singularAttribute4;
		}
		
		@Override
		protected Collection<Path<String>> getSimplePath(Root<T> root)
		{
			return Collections.singletonList(root.get(singularAttribute1).get(singularAttribute2).get(singularAttribute3).get(singularAttribute4));
		}
	}
	
	public enum FilteringMode
	{
		EQUAL,
		CONTAINS,
		STARTS_WITH
	}
	
	private FilteringMode filteringMode = FilteringMode.CONTAINS;
	private boolean sanitizeStringBeforeFiltering = false;

	public SimpleStringFilter(SingularAttribute<T, String> attribute, boolean sanitizeStringBefore)
	{
		super(attribute);
		this.sanitizeStringBeforeFiltering = sanitizeStringBefore;
	}

	public SimpleStringFilter(SingularAttribute<? super T, String> attribute, FilteringMode filteringMode)
	{
		super(attribute);
		this.filteringMode = filteringMode;
	}
	
	public SimpleStringFilter(SingularAttribute<? super T, String> attribute)
	{
		super(attribute);
	}
	
	protected SimpleStringFilter()
	{
		super();
	}
	
	@Override
	protected Predicate toPredicate(Path<String> path, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		if (filteringMode == null || Strings.isNullOrEmpty(getValue()))
			return null;

		String value = getValue();

		if (sanitizeStringBeforeFiltering)
			value = EntityContentSanitizer.SANITIZER.sanitize(value);



		switch (filteringMode)
		{
			case CONTAINS:
				return getLikeFilter(value,cb,path);
			case STARTS_WITH:
				System.out.println("in starts with  adding criteria"+ value);
				return cb.like(path, value + "%");
			default:
				return cb.equal(path, value);
		}

	}


	// Changes for GR3-56, allow sibling emails to be searched when filtering
	private Predicate getLikeFilter(String value, CriteriaBuilder cb, Path<String> path)
	{
		try {
			if (value != null && value.indexOf(',') != -1) {
				String[] valArr = value.split(",");
				Predicate filter = cb.like(path, "%" + valArr[0] + "%");
				for (String s : valArr) {
					filter = cb.or(filter, cb.like(path, "%" + s + "%"));
				}
				return filter;
			} else
				return cb.like(path, "%" + value + "%");
		}

		catch (Exception ex)
		{
			return cb.like(path, "%" + value + "%");
		}
	}
	
	public void setFilteringMode(FilteringMode filteringMode)
	{
		this.filteringMode = filteringMode;
	}

	public void setSanitizeStringBeforeFiltering(boolean sanitizeString)
	{
		this.sanitizeStringBeforeFiltering = sanitizeString;
	}
}
