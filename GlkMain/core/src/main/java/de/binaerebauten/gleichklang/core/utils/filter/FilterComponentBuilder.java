package de.binaerebauten.gleichklang.core.utils.filter;

import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.model.filter.UserFilter.UserFilterType;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent;
import de.binaerebauten.gleichklang.core.view.component.FilterComponent.FilterComponentHandler;
import de.binaerebauten.gleichklang.core.view.component.filter.*;
import de.binaerebauten.gleichklang.core.view.component.filter.LabelFilterComponent.FilterRemoveListener;

import java.util.Objects;

public class FilterComponentBuilder
{
	/**
	 * Visitor to build {@link FilterComponent} for the filter creation.
	 */
	private class NewFilterComponentBuilderVisitor implements DefaultUserFilterVisitor<FilterComponent>
	{
		@Override
		public FilterComponent visit(UnaryOperatorFilter unaryOperatorFilter)
		{
			return new LayoutFilterComponent(unaryOperatorFilter, unaryOperatorFilter.getFilter().accept(this));
		}

		@Override
		public FilterComponent visit(BinaryOperatorFilter binaryOperatorFilter)
		{
			return createBinaryOperatorComponent(binaryOperatorFilter, this);
		}

		@Override
		public FilterComponent visit(TemplateFilter templateFilter)
		{
			return new LabelFilterComponent("", templateFilter);
		}

		@Override
		public FilterComponent visit(RegionFilter regionFilter)
		{
			return new RegionFilterComponent(regionFilter, filterComponentHandler);
		}

		@Override
		public FilterComponent visit(ProximityFilter proximityFilter)
		{
			return new ProximityFilterComponent(proximityFilter);
		}

		@Override
		public FilterComponent visit(ChoiceQuestionFilter choiceQuestionFilter)
		{
			return new ChoiceQuestionFilterComponent(choiceQuestionFilter, filterComponentHandler);
		}

		
		@Override
		public FilterComponent visit(NumberQuestionFilter numberQuestionFilter)
		{
			return new NumberQuestionFilterComponent(numberQuestionFilter, filterComponentHandler);
		}
		
		@Override
		public FilterComponent visit(AgeFilter ageFilter)
		{
			return new AgeFilterComponent(ageFilter, filterComponentHandler);
		}

		@Override
		public FilterComponent visit(DateFilter dateFilter)
		{
			return new DateFilterComponent(dateFilter, filterComponentHandler);
		}

		@Override
		public FilterComponent visit(SubscriptionEndFilter subscriptionEndFilter)
		{
			return new SubscriptionEndDateRangeFilterComponent(subscriptionEndFilter, filterComponentHandler);
		}

		@Override
		public FilterComponent visit(ActiveSubscriptionFilter activeSubscriptionFilter)
		{
			return new ActiveSubscriptionDateRangeFilterComponent(activeSubscriptionFilter, filterComponentHandler);
		}

		@Override
		public FilterComponent visit(DeactivatedProlongationsFilter deactivatedProlongationsFilter)
		{
			return new DeactivatedProlongationsFilterComponent(deactivatedProlongationsFilter, filterComponentHandler);
		}
		@Override
		public FilterComponent visit(ActivatedProlongationsFilter activatedProlongationsFilter)
		{
			return new AtivatedProlongationsFilterComponent(activatedProlongationsFilter, filterComponentHandler);
		}
		@Override
		public FilterComponent visit(LastLoginFilter lastLoginFilter)
		{
			return new LastLoginDateRangeFilterComponent(lastLoginFilter, filterComponentHandler);
		}

		@Override
		public FilterComponent visit(TextQuestionFilter textQuestionFilter)
		{
			return new TextQuestionFilterComponent(textQuestionFilter, filterComponentHandler);
		}

		@Override
		public <E extends Enum<E>> FilterComponent visit(EnumFilter<E> enumFilter, Class<E> enumType)
		{
			return new EnumFilterComponent<>(enumFilter, enumType);
		}

		@Override
		public <E extends Enum<E>> FilterComponent visit(EnumFilterMobile<E> enumFilter, Class<E> enumType)
		{
			return new EnumFilterComponentMobileCheck<>(enumFilter, enumType);
		}

		@Override
		public FilterComponent visit(StringFilter stringFilter)
		{
			return new StringFilterComponent(stringFilter);
		}

		@Override
		public FilterComponent visit(BooleanFilter booleanFilter)
		{
			return new BooleanFilterComponent(booleanFilter);
		}
		
		@Override
		public FilterComponent visit(NumberFilter numberFilter)
		{
			return new NumberFilterComponent(numberFilter);
		}
		
		@Override
		public FilterComponent visit(UserActivityFilter userActivityFilter)
		{
			return new UserActivityFilterComponent(userActivityFilter);
		}

		@Override
		public FilterComponent visit(SexChoiceQuestionFilter sexChoiceQuestionFilter)
		{
			return new SexChoiceQuestionFilterComponent(sexChoiceQuestionFilter, filterComponentHandler);
		}
		
		@Override
		public FilterComponent visit(MatchFilter matchFilter)
		{
			return new LabelFilterComponent(UserFilterType.MATCH_FILTER.toString(), matchFilter);
		}
		
		@Override
		public FilterComponent visit(RelationshipFilter relationshipFilter)
		{
			return new RelationshipFilterComponent(relationshipFilter);
		}
		
		@Override
		public FilterComponent visit(MessageFilter messageFilter)
		{
			return new MessageFilterComponent(messageFilter);
		}
		
		@Override
		public FilterComponent visit(MissingAnswersRatioFilter missingAnswersRatioFilter)
		{
			return new MissingAnswersRatioFilterComponent(missingAnswersRatioFilter);
		}
	}

	/**
	 * Visitor to build {@link FilterComponent} for view only. Not editable.
	 */
	private class ViewFilterComponentBuilderVisitor implements DefaultFilterVisitor<FilterComponent>
	{
		@Override
		public FilterComponent visit(UserFilter userFilter)
		{
			return new LabelFilterComponent(userFilter.getName(), userFilter, filterRemoveHandler);
		}

		@Override
		public FilterComponent visit(UnaryOperatorFilter unaryOperatorFilter)
		{
			final LayoutFilterComponent layoutFilterComponent = new LayoutFilterComponent(unaryOperatorFilter, true);
			layoutFilterComponent.addFilterComponent(unaryOperatorFilter.getFilter().accept(this));

			return layoutFilterComponent;
		}

		@Override
		public FilterComponent visit(BinaryOperatorFilter binaryOperatorFilter)
		{
			return createBinaryOperatorComponent(binaryOperatorFilter, this);
		}

		@Override
		public FilterComponent visit(TemplateFilter templateFilter)
		{
			return new LabelFilterComponent(templateFilter.getName(), templateFilter, filterRemoveHandler);
		}
	}

	private final FilterComponentHandler filterComponentHandler;
	private FilterRemoveListener filterRemoveHandler = null;

	public FilterComponentBuilder(FilterComponentHandler filterComponentHandler)
	{
		Objects.requireNonNull(filterComponentHandler);
		this.filterComponentHandler = filterComponentHandler;
	}

	/**
	 * RemoveHandler for the view.
	 *
	 * @param filterRemoveHandler
	 */
	public void setRemoveHandler(FilterRemoveListener filterRemoveHandler)
	{
		this.filterRemoveHandler = filterRemoveHandler;
	}

	private FilterComponent createBinaryOperatorComponent(BinaryOperatorFilter binaryOperatorFilter, FilterVisitor<FilterComponent> visitor)
	{
		final LayoutFilterComponent layoutFilterComponent = new LayoutFilterComponent(binaryOperatorFilter);
		layoutFilterComponent.addFilterComponent(binaryOperatorFilter.getRightFilter().accept(visitor));
		layoutFilterComponent.addFilterComponent(binaryOperatorFilter.getLeftFilter().accept(visitor));

		return layoutFilterComponent;
	}

	/**
	 * Build for creation. Not suitable for editing, because the filter state
	 * will not be transferred to the FilterComponent
	 *
	 * @param filter
	 * @return
	 */
	public FilterComponent buildNewComponent(AbstractFilter filter)
	{
		if (filter == null) return new EmptyFilterComponent();

		return filter.accept(new NewFilterComponentBuilderVisitor());
	}

	/**
	 * Build for readonly view. With remove-button if {@link
	 * #setRemoveHandler(FilterRemoveListener)} is set.
	 *
	 * @param filter
	 * @return
	 */
	public FilterComponent buildViewComponent(AbstractFilter filter)
	{
		if (filter == null) return new EmptyFilterComponent();

		return filter.accept(new ViewFilterComponentBuilderVisitor());
	}
}
