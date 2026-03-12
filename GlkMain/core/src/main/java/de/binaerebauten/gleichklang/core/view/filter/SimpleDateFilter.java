package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.popup.I18N;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.SingularAttribute;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class SimpleDateFilter<T extends BaseEntity> extends AbstractFilter<T, LocalDateTime, LocalDateTime>
{
	/**
	 * The NestedDateFilter are very special. If nobody use it, they can be deleted.
	 * Maybe it could be easier to inherit from {@link SimpleDateFilter} and override
	 * the {@link SimpleDateFilter#getSimplePath(Root)} manually for the concrete case.
	 * <p>
	 * An alternative could be to set a {@link Path} with the help of the {@link javax.persistence.EntityManager}.
	 */
	
	public static class NestedDateFilter1<T extends BaseEntity, A1> extends SimpleDateFilter<T>
	{
		private final SingularAttribute<? super T, A1> singularAttribute1;
		private final SingularAttribute<? super A1, LocalDateTime> singularAttribute2;
		
		public NestedDateFilter1(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, LocalDateTime> singularAttribute2)
		{
			super();
			
			this.singularAttribute1 = singularAttribute1;
			this.singularAttribute2 = singularAttribute2;
		}
		
		@Override
		protected Collection<Path<LocalDateTime>> getSimplePath(Root<T> root)
		{
			return Collections.singletonList(root.get(singularAttribute1).get(singularAttribute2));
		}
	}
	
	public static class NestedDateFilter2<T extends BaseEntity, A1, A2> extends SimpleDateFilter<T>
	{
		private final SingularAttribute<? super T, A1> singularAttribute1;
		private final SingularAttribute<? super A1, A2> singularAttribute2;
		private final SingularAttribute<? super A2, LocalDateTime> singularAttribute3;
		
		public NestedDateFilter2(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, A2> singularAttribute2, SingularAttribute<? super A2, LocalDateTime> singularAttribute3)
		{
			super();
			
			this.singularAttribute1 = singularAttribute1;
			this.singularAttribute2 = singularAttribute2;
			this.singularAttribute3 = singularAttribute3;
		}
		
		@Override
		protected Collection<Path<LocalDateTime>> getSimplePath(Root<T> root)
		{
			return Collections.singletonList(root.get(singularAttribute1).get(singularAttribute2).get(singularAttribute3));
		}
	}
	
	public static class NestedDateFilter3<T extends BaseEntity, A1, A2, A3> extends SimpleDateFilter<T>
	{
		private final SingularAttribute<? super T, A1> singularAttribute1;
		private final SingularAttribute<? super A1, A2> singularAttribute2;
		private final SingularAttribute<? super A2, A3> singularAttribute3;
		private final SingularAttribute<? super A3, LocalDateTime> singularAttribute4;
		
		public NestedDateFilter3(SingularAttribute<? super T, A1> singularAttribute1, SingularAttribute<? super A1, A2> singularAttribute2, SingularAttribute<? super A2, A3> singularAttribute3, SingularAttribute<? super A3, LocalDateTime> singularAttribute4)
		{
			super();
			
			this.singularAttribute1 = singularAttribute1;
			this.singularAttribute2 = singularAttribute2;
			this.singularAttribute3 = singularAttribute3;
			this.singularAttribute4 = singularAttribute4;
		}
		
		@Override
		protected Collection<Path<LocalDateTime>> getSimplePath(Root<T> root)
		{
			return Collections.singletonList(root.get(singularAttribute1).get(singularAttribute2).get(singularAttribute3).get(singularAttribute4));
		}
	}
	
	private LocalDateTime startDate = null;
	private LocalDateTime endDate = null;
	
	public SimpleDateFilter(SingularAttribute<? super T, LocalDateTime> attribute)
	{
		super(attribute);
	}
	
	protected SimpleDateFilter()
	{
		super();
	}
	
	@Override
	protected Predicate toPredicate(Path<LocalDateTime> path, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		if (startDate == null || endDate == null) return null;
		
		return cb.between(path, startDate, endDate);
	}
	
	public void setValue(LocalDate date)
	{
		setValue(date, date);
	}
	
	public void setValue(LocalDate startDate, LocalDate endDate)
	{
		if (startDate == null || endDate == null)
		{
			setValue((LocalDateTime) null, null);
		}
		else
		{
			setValue(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
		}
	}

	public void setValue(LocalDate startDateTime, LocalDate endDateTime,boolean isFromTo)
	{
		final boolean dirty = !Objects.equals(this.startDate, startDateTime) || !Objects.equals(this.endDate, endDateTime);

		if ((startDateTime != null && endDateTime == null) || (startDateTime == null && endDateTime != null))
		{
			MessageBox.show(I18N.USERMANAGEPOPUP_CAPTION_CREATEDATEFILTER_VALIDATION_FROM_TO_REQUIRED.msg());
		}
		else if (startDateTime != null && endDateTime != null)
		{
			if (startDateTime.compareTo(endDateTime) > 0)
			{
				MessageBox.show(I18N.USERMANAGEPOPUP_CAPTION_CREATEDATEFILTER_VALIDATION_FROM_GREATER_THAN_TO.msg());
			}
			else
			{
				setValue(startDateTime.atStartOfDay(), endDateTime.plusDays(1).atStartOfDay());
			}
		}
		else
		{
			setValue((LocalDateTime) null, null);
		}
		if (dirty)
		{
			onValueChanged();
		}
	}

	public void setValue(LocalDateTime startDateTime, LocalDateTime endDateTime)
	{
		final boolean dirty = !Objects.equals(this.startDate, startDateTime) || !Objects.equals(this.endDate, endDateTime);
		
		if (startDateTime == null || endDateTime == null)
		{
			this.startDate = null;
			this.endDate = null;
		}
		else
		{
			this.startDate = startDateTime;
			this.endDate = endDateTime;
		}
		
		if (dirty)
		{
			onValueChanged();
		}
	}
	
	@Override
	public LocalDateTime getValue()
	{
		return startDate;
	}
	
	@Override
	public void setValue(LocalDateTime value)
	{
		setValue(value, value);
	}
}
