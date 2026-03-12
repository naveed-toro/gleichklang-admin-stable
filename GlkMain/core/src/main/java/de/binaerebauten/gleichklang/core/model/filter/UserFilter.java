package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

@XmlTransient
@Entity
public abstract class UserFilter extends AbstractFilter
{
	public enum UserFilterType implements DefaultEnumI18N
	{
		MAIL_FILTER(MailFilter.class),
		ALIAS_FILTER(AliasFilter.class, true),
		LAST_NAME_FILTER(LastNameFilter.class),
		FIRST_NAME_FILTER(FirstNameFilter.class),
		CATEGORY_FILTER(RecommendationCategoryFilter.class),
		REGION_FILTER(RegionFilter.class, true),
		CITY_FILTER(CityFilter.class),
		SUBSCRIPTION_STATE_FILTER(SubscriptionStateFilter.class),
		PAYMENT_STATE_FILTER(PaymentStateFilter.class),
		USER_ACTIVITY_FILTER(UserActivityFilter.class),
		MATCH_FILTER(MatchFilter.class),
		RELATIONSHIP_FILTER(RelationshipFilter.class),
		MESSAGE_FILTER(MessageFilter.class),
		MISSING_ANSWERS_RATIO_FILTER(MissingAnswersRatioFilter.class),
		//		PROXIMITY_FILTER(ProximityFilter.class),
		CHOICE_QUESTION_FILTER(ChoiceQuestionFilter.class),
		NUMBER_QUESTION_FILTER(NumberQuestionFilter.class),
		AGE_FILTER(AgeFilter.class, true),
		DATE_FILTER(DateFilter.class,true),
		SUBSCRIPTION_END_DATE_RANGE_FILTER(SubscriptionEndFilter.class,true),
		ACTIVE_SUBSCRIPTION_DATE_RANGE_FILTER(ActiveSubscriptionFilter.class,true),
		LAST_LOGIN_FILTER(LastLoginFilter.class,true),
		TEXT_QUESTION_FILTER(TextQuestionFilter.class),
		MEMBER_STATUS_FILTER(MemberStatusFilter.class),
		ACTION_CODE_FILTER(ActionCodeFilter.class),
		CANCEL_REASON_FILTER(CancelReasonFilter.class),
		LAST_SEEN_BROWSER_FILTER(LastSeenBrowserFilter.class),
		USER_PREPAYMENT_USAGE_FILTER(UserPrepaymentUsageFilter.class),
		EXTERNAL_REFERENCE_ID_FILTER(ExternalReferenceIdFilter.class),
		CONFIRMATION_FILTER(ConfirmationFilter.class),
		SEX_CHOICE_QUESTION_FILTER(SexChoiceQuestionFilter.class, true),
		FREE_TEXT_FILTER(FreeTextFilter.class),
		DISABLE_FOOTPRINT_NOTIFICATIONS_FILTER(DisableFootprintNotificationsFilter.class),
		DISABLE_NEWS_NOTIFICATIONS_FILTER(DisableNewsNotificationsFilter.class),
		DISABLE_CIPHER_MESSAGE_NOTIFICATIONS_FILTER(DisableCipherMessageNotificationsFilter.class),
		DISABLE_POSITIVE_RANKING_NOTIFICATIONS_FILTER(DisablePositiveRankingNotificationsFilter.class),
		DISABLE_RECOMMENDATION_NOTIFICATIONS_FILTER(DisableRecommendationNotificationsFilter.class),
		ENABLE_MARKETING_NOTIFICATIONS_FILTER(EnableMarketingNotificationsFilter.class),
		MAIL_BLOCKED_FILTER(MailBlockedFilter.class),
		PAYMENT_TYPE_FILTER(PaymentTypeFilter.class),
		RECOMMENDATION_BREAK_FILTER(RecommendationBreakFilter.class),
		USER_REGISTRATION_FILTER(UserRegistrationFilter.class),
		HAS_SUBSCRIPTION_FILTER(HasSubscriptionFilter.class),
		AUTO_RENEWAL_FILTER(AutoRenewalFilter.class),
		CURRENT_PRODUCT_FILTER(CurrentProductFilter.class),
		SUBSCRIPTIONS_PRODUCT_FILTER(SubscriptionsProductFilter.class),
		INVOICES_PRODUCT_FILTER(InvoicesProductFilter.class),
		USER_ID_FILTER(UserIdFilter.class),
		PAYMENT_ID_FILTER(PaymentIdFilter.class),
		BLOCKED_FILTER(BlockedFilter.class),
		ADMIN_BLOCKED_FILTER(AdminBlockedFilter.class),
		AUDIO_PARTNERSHIP_FILTER(AudioPartnershipFilter.class),
		AUDIO_FRIENDSHIP_FILTER(AudioFriendshipFilter.class),
		DEACTIVATED_PROLONGATION_DATE_RANGE_FILTER(DeactivatedProlongationsFilter.class,true),
		ACTIVATED_PROLONGATION_DATE_RANGE_FILTER(ActivatedProlongationsFilter.class,true);
		private static final Map<Class<? extends AbstractFilter>, UserFilterType> reversMap = EnumSet.allOf(UserFilterType.class)
				.stream()
				.collect(Collectors.toMap(UserFilterType::getFilterClass, l -> l));
		private final Class<? extends AbstractFilter> filterClass;
		private final boolean onlySingleUsage;

		UserFilterType(Class<? extends AbstractFilter> filterClass)
		{
			this(filterClass, false);
		}
		
		UserFilterType(Class<? extends AbstractFilter> filterClass, boolean onlySingleUsage)
		{
			this.filterClass = filterClass;
			this.onlySingleUsage = onlySingleUsage;
		}

		public Class<? extends AbstractFilter> getFilterClass()
		{
			return filterClass;
		}

		public static UserFilterType valueOf(Class<? extends AbstractFilter> filterClass)
		{
			return reversMap.get(filterClass);
		}

		public AbstractFilter createFilter()
		{
			try
			{
				return getFilterClass().newInstance();
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				return null;
			}
		}
		
		public boolean isOnlySingleUsage()
		{
			return onlySingleUsage;
		}
		
		@Override
		public String toString()
		{
			return msg();
		}
	}

	public abstract String getName();
}
