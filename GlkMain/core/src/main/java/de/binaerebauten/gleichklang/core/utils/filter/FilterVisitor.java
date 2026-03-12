package de.binaerebauten.gleichklang.core.utils.filter;

import de.binaerebauten.gleichklang.core.model.filter.*;

public interface FilterVisitor<T>
{
	T visit(UnaryOperatorFilter unaryOperatorFilter);

	T visit(BinaryOperatorFilter binaryOperatorFilter);

	T visit(TemplateFilter templateFilter);

	T visit(RegionFilter regionFilter);

	T visit(ProximityFilter proximityFilter);

	T visit(RecommendationCategoryFilter recommendationCategoryFilter);

	T visit(MemberStatusFilter memberStatusFilter);

	T visit(ChoiceQuestionFilter choiceQuestionFilter);
	
	T visit(NumberQuestionFilter choiceQuestionFilter);
	
	T visit(AgeFilter choiceQuestionFilter);

	T visit(DateFilter dateFilter);

	T visit(LastLoginFilter lastLoginFilter);

	T visit(SubscriptionEndFilter subscriptionEndFilter);

	T visit(ActiveSubscriptionFilter activeSubscriptionFilter);

	T visit(DeactivatedProlongationsFilter deactivatedProlongationsFilter);

	T visit(ActivatedProlongationsFilter activatedProlongationsFilter);

	T visit(TextQuestionFilter textQuestionFilter);

	T visit(LastNameFilter lastNameFilter);

	T visit(FirstNameFilter firstNameFilter);

	T visit(AliasFilter aliasFilter);

	T visit(MailFilter mailFilter);

	T visit(ActionCodeFilter actionCodeFilter);

	T visit(UserActivityFilter userActivityFilter);

	T visit(PaymentStateFilter paymentStateFilter);

	T visit(CancelReasonFilter cancelReasonFilter);

	T visit(LastSeenBrowserFilter lastSeenBrowserFilter);

	T visit(SubscriptionStateFilter subscriptionStateFilter);

	T visit(UserPrepaymentUsageFilter userPrepaymentUsageFilter);

	T visit(ExternalReferenceIdFilter externalReferenceIdFilter);
	
	T visit(ConfirmationFilter confirmationFilter);
	
	T visit(CityFilter cityFilter);

	T visit(SexChoiceQuestionFilter sexChoiceQuestionFilter);

	T visit(MatchFilter matchFilter);
	
	T visit(RelationshipFilter relationshipFilter);
	
	T visit(MessageFilter messageFilter);
	
	T visit(MissingAnswersRatioFilter missingAnswersRatioFilter);
	
	T visit(FreeTextFilter freeTextFilter);
	
	T visit(DisableFootprintNotificationsFilter disableFootprintNotificationsFilter);
	
	T visit(DisableNewsNotificationsFilter disableNewsNotificationsFilter);
	
	T visit(DisableCipherMessageNotificationsFilter disableCipherMessageNotificationsFilter);
	
	T visit(DisablePositiveRankingNotificationsFilter disablePositiveRankingNotificationsFilter);
	
	T visit(DisableRecommendationNotificationsFilter disableRecommendationNotificationsFilter);
	
	T visit(BlockedFilter mailBlockedFilter);

	T visit(AdminBlockedFilter adminBlockedFilter);

	T visit(AudioFriendshipFilter audioFriendshipFilter);

	T visit(AudioPartnershipFilter audioPartnershipFilter);

	T visit(MailBlockedFilter mailBlockedFilter);
	
	T visit(PaymentTypeFilter paymentTypeFilter);
	
	T visit(RecommendationBreakFilter recommendationBreakFilter);
	
	T visit(UserRegistrationFilter userRegistrationFilter);
	
	T visit(HasSubscriptionFilter hasSubscriptionFilter);
	
	T visit(AutoRenewalFilter autoRenewalFilter);
	
	T visit(InvoicesProductFilter invoicesProductFilter);
	
	T visit(SubscriptionsProductFilter subscriptionsProductFilter);
	
	T visit(CurrentProductFilter currentProductFilter);
	
	T visit(UserIdFilter userIdFilter);
	
	T visit(PaymentIdFilter paymentIdFilter);
	
	T visit(EnableMarketingNotificationsFilter enableMarketingNotificationsFilter);

}
