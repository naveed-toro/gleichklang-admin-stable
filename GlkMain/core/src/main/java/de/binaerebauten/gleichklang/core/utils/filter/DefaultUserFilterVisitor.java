package de.binaerebauten.gleichklang.core.utils.filter;

import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment.PaymentType;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.user.*;

/**
 * Simplification of the {@link FilterVisitor}-Interface. Generic UserFilter
 * (i.e. {@link StringFilter, EnumFilter, BooleanFilter} are mapped to there
 * analog visit-method.
 *
 * @param <T>
 */
public interface DefaultUserFilterVisitor<T> extends FilterVisitor<T>
{
	<E extends Enum<E>> T visit(EnumFilter<E> enumFilter, Class<E> enumType);

	<E extends Enum<E>> T visit(EnumFilterMobile<E> enumFilter, Class<E> enumType);
	
	T visit(StringFilter stringFilter);
	
	T visit(BooleanFilter booleanFilter);
	
	T visit(NumberFilter numberFilter);
	
	@Override
	default T visit(RecommendationCategoryFilter recommendationCategoryFilter)
	{
		return visit(recommendationCategoryFilter, RecommendationCategory.class);
	}
	
	@Override
	default T visit(MemberStatusFilter memberStatusFilter)
	{
		return visit(memberStatusFilter, MemberStatus.class);
	}
	
	@Override
	default T visit(LastNameFilter lastNameFilter)
	{
		return visit((StringFilter) lastNameFilter);
	}
	
	@Override
	default T visit(FirstNameFilter firstNameFilter)
	{
		return visit((StringFilter) firstNameFilter);
	}
	
	@Override
	default T visit(AliasFilter aliasFilter)
	{
		return visit((StringFilter) aliasFilter);
	}
	
	@Override
	default T visit(MailFilter mailFilter)
	{
		return visit((StringFilter) mailFilter);
	}
	
	@Override
	default T visit(ActionCodeFilter actionCodeFilter)
	{
		return visit((StringFilter) actionCodeFilter);
	}
	
	@Override
	default T visit(PaymentStateFilter paymentStateFilter)
	{
		return visit(paymentStateFilter, PaymentState.class);
	}
	
	@Override
	default T visit(CancelReasonFilter cancelReasonFilter)
	{
		return visit(cancelReasonFilter, CancelReason.class);
	}

	@Override
	default T visit(LastSeenBrowserFilter lastSeenBrowserFilter)
	{
		return visit(lastSeenBrowserFilter, ClientInformation.Browser.class);
	}
	
	@Override
	default T visit(SubscriptionStateFilter subscriptionStateFilter)
	{
		return visit(subscriptionStateFilter, SubscriptionState.class);
	}
	
	@Override
	default T visit(UserPrepaymentUsageFilter userPrepaymentUsageFilter)
	{
		return visit((StringFilter) userPrepaymentUsageFilter);
	}
	
	@Override
	default T visit(ExternalReferenceIdFilter externalReferenceIdFilter)
	{
		return visit((StringFilter) externalReferenceIdFilter);
	}
	
	@Override
	default T visit(ConfirmationFilter confirmationFilter)
	{
		return visit((BooleanFilter) confirmationFilter);
	}
	
	@Override
	default T visit(CityFilter cityFilter)
	{
		return visit((StringFilter) cityFilter);
	}
	
	@Override
	default T visit(FreeTextFilter freeTextFilter)
	{
		return visit((StringFilter) freeTextFilter);
	}
	
	@Override
	default T visit(DisableFootprintNotificationsFilter disableFootprintNotificationsFilter)
	{
		return visit((BooleanFilter) disableFootprintNotificationsFilter);
	}
	
	@Override
	default T visit(DisableNewsNotificationsFilter disableNewsNotificationsFilter)
	{
		return visit((BooleanFilter) disableNewsNotificationsFilter);
	}
	
	@Override
	default T visit(DisableCipherMessageNotificationsFilter disableCipherMessageNotificationsFilter)
	{
		return visit((BooleanFilter) disableCipherMessageNotificationsFilter);
	}
	
	@Override
	default T visit(DisablePositiveRankingNotificationsFilter disablePositiveRankingNotificationsFilter)
	{
		return visit((BooleanFilter) disablePositiveRankingNotificationsFilter);
	}
	
	@Override
	default T visit(DisableRecommendationNotificationsFilter disableRecommendationNotificationsFilter)
	{
		return visit((BooleanFilter) disableRecommendationNotificationsFilter);
	}
	
	@Override
	default T visit(EnableMarketingNotificationsFilter enableMarketingNotificationsFilter)
	{
		return visit((BooleanFilter) enableMarketingNotificationsFilter);
	}
	
	@Override
	default T visit(MailBlockedFilter mailBlockedFilter)
	{
		return visit((BooleanFilter) mailBlockedFilter);
	}

	@Override
	default T visit(BlockedFilter blockedFilter)
	{
		return visit((BooleanFilter) blockedFilter);
	}
	@Override
	default T visit(AdminBlockedFilter adminBlockedFilter)
	{
		return visit((BooleanFilter) adminBlockedFilter);
	}
	@Override
	default T visit(AudioFriendshipFilter audioFriendshipFilter)
	{
		return visit((BooleanFilter) audioFriendshipFilter);
	}
	@Override
	default T visit(AudioPartnershipFilter audioPartnershipFilter) { return visit((BooleanFilter) audioPartnershipFilter); }

	@Override
	default T visit(PaymentTypeFilter paymentTypeFilter)
	{
		return visit(paymentTypeFilter, PaymentType.class);
	}
	
	@Override
	default T visit(RecommendationBreakFilter recommendationBreakFilter)
	{
		return visit(recommendationBreakFilter, RecommendationCategory.class);
	}
	
	@Override
	default T visit(UserRegistrationFilter userRegistrationFilter)
	{
		return visit(userRegistrationFilter, RegistrationState.class);
	}
	
	@Override
	default T visit(HasSubscriptionFilter hasSubscriptionFilter)
	{
		return visit((BooleanFilter) hasSubscriptionFilter);
	}
	
	@Override
	default T visit(AutoRenewalFilter autoRenewalFilter)
	{
		return visit((BooleanFilter) autoRenewalFilter);
	}
	
	@Override
	default T visit(InvoicesProductFilter invoicesProductFilter)
	{
		return visit((StringFilter) invoicesProductFilter);
	}
	
	@Override
	default T visit(SubscriptionsProductFilter subscriptionsProductFilter)
	{
		return visit((StringFilter) subscriptionsProductFilter);
	}
	
	@Override
	default T visit(CurrentProductFilter currentProductFilter)
	{
		return visit((StringFilter) currentProductFilter);
	}
	
	@Override
	default T visit(UserIdFilter userIdFilter)
	{
		return visit((NumberFilter) userIdFilter);
	}
	
	@Override
	default T visit(PaymentIdFilter paymentIdFilter)
	{
		return visit((NumberFilter) paymentIdFilter);
	}

}
