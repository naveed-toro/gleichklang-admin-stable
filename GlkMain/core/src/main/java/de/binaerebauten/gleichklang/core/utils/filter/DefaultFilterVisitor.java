package de.binaerebauten.gleichklang.core.utils.filter;

import de.binaerebauten.gleichklang.core.model.filter.*;

/**
 * Simplification of the {@link FilterVisitor}-Interface. All UserFilter are
 * mapped to {@link #visit(UserFilter)}.
 *
 * @param <T>
 */
public interface DefaultFilterVisitor<T> extends DefaultUserFilterVisitor<T>
{
	T visit(UserFilter userFilter);
	
	@Override
	default T visit(RegionFilter regionFilter)
	{
		return visit((UserFilter) regionFilter);
	}
	
	@Override
	default T visit(ProximityFilter proximityFilter)
	{
		return visit((UserFilter) proximityFilter);
	}
	
	@Override
	default T visit(ChoiceQuestionFilter choiceQuestionFilter)
	{
		return visit((UserFilter) choiceQuestionFilter);
	}
	
	@Override
	default T visit(NumberQuestionFilter numberQuestionFilter)
	{
		return visit((UserFilter) numberQuestionFilter);
	}
	
	@Override
	default T visit(AgeFilter ageFilter)
	{
		return visit((UserFilter) ageFilter);
	}

	@Override
	default T visit(DateFilter dateFilter)
	{
		return visit((UserFilter) dateFilter);
	}

	@Override
	default T visit(SubscriptionEndFilter subscriptionEndFilter)
	{
		return visit((UserFilter) subscriptionEndFilter);
	}

	@Override
	default T visit(ActiveSubscriptionFilter activeSubscriptionFilter)
	{
		return visit((UserFilter) activeSubscriptionFilter);
	}
	@Override
	default T visit(DeactivatedProlongationsFilter deactivatedProlongationsFilter)
	{
		return visit((UserFilter) deactivatedProlongationsFilter);
	}
	@Override
	default T visit(ActivatedProlongationsFilter activatedProlongationsFilter)
	{
		return visit((UserFilter) activatedProlongationsFilter);
	}
	@Override
	default T visit(LastLoginFilter lastLoginFilter)
	{
		return visit((UserFilter) lastLoginFilter);
	}

	@Override
	default T visit(TextQuestionFilter textQuestionFilter)
	{
		return visit((UserFilter) textQuestionFilter);
	}
	
	@Override
	default T visit(BooleanFilter booleanFilter)
	{
		return visit((UserFilter) booleanFilter);
	}
	
	@Override
	default T visit(NumberFilter numberFilter)
	{
		return visit((UserFilter) numberFilter);
	}
	
	@Override
	default <E extends Enum<E>> T visit(EnumFilter<E> enumFilter, Class<E> enumType)
	{
		return visit((UserFilter) enumFilter);
	}

	@Override
	default <E extends Enum<E>> T visit(EnumFilterMobile<E> enumFilter, Class<E> enumType)
	{
		return visit((UserFilter) enumFilter);
	}
	
	@Override
	default T visit(StringFilter stringFilter)
	{
		return visit((UserFilter) stringFilter);
	}
	
	@Override
	default T visit(UserActivityFilter userActivityFilter)
	{
		return visit((UserFilter) userActivityFilter);
	}
	
	@Override
	default T visit(ExternalReferenceIdFilter externalReferenceIdFilter)
	{
		return visit((UserFilter) externalReferenceIdFilter);
	}
	
	@Override
	default T visit(SexChoiceQuestionFilter sexChoiceQuestionFilter)
	{
		return visit((UserFilter) sexChoiceQuestionFilter);
	}
	
	@Override
	default T visit(MatchFilter matchFilter)
	{
		return visit((UserFilter) matchFilter);
	}
	
	@Override
	default T visit(RelationshipFilter relationshipFilter)
	{
		return visit((UserFilter) relationshipFilter);
	}
	
	@Override
	default T visit(MessageFilter messageFilter)
	{
		return visit((UserFilter) messageFilter);
	}
	
	@Override
	default T visit(MissingAnswersRatioFilter missingAnswersRatioFilter)
	{
		return visit((UserFilter) missingAnswersRatioFilter);
	}
}
