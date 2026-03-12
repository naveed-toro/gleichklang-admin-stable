package de.binaerebauten.gleichklang.core.model.filter;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.utils.filter.FilterVisitor;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlSeeAlso({ ActionCodeFilter.class, AgeFilter.class, AliasFilter.class, AutoRenewalFilter.class, BinaryOperatorFilter.class,
				CancelReasonFilter.class, ChoiceQuestionFilter.class, CityFilter.class, ConfirmationFilter.class,
		CurrentProductFilter.class, DisableCipherMessageNotificationsFilter.class, DisableFootprintNotificationsFilter.class,
		DisableNewsNotificationsFilter.class, DisablePositiveRankingNotificationsFilter.class,
				DisableRecommendationNotificationsFilter.class, EnableMarketingNotificationsFilter.class,
		ExternalReferenceIdFilter.class, FirstNameFilter.class, FreeTextFilter.class, HasSubscriptionFilter.class,
		InvoicesProductFilter.class, LastNameFilter.class, MailBlockedFilter.class, MailFilter.class, MatchFilter.class,
				MemberStatusFilter.class, MessageFilter.class, MissingAnswersRatioFilter.class,
		NumberQuestionFilter.class, PaymentIdFilter.class, PaymentStateFilter.class, PaymentTypeFilter.class,
		ProximityFilter.class, RecommendationBreakFilter.class, RecommendationCategoryFilter.class, RegionFilter.class,
				RelationshipFilter.class, SexChoiceQuestionFilter.class, SubscriptionsProductFilter.class,
		SubscriptionStateFilter.class, TemplateFilter.class, TextQuestionFilter.class, UnaryOperatorFilter.class,
		UserActivityFilter.class, UserIdFilter.class, UserPrepaymentUsageFilter.class, UserRegistrationFilter.class })@Entity
@Table(name = "filter")
public abstract class AbstractFilter extends BaseEntity
{
	public enum FilterType implements DefaultEnumI18N
	{
		USER_FILTER,
		TEMPLATE_FILTER;

		@Override
		public String toString()
		{
			return msg();
		}
	}

	public abstract <T> T accept(FilterVisitor<T> filterVisitor);

	@XmlID
	@XmlAttribute
	public String getUniqueXmlKey()
	{
		return this.getClass().getSimpleName() + "_" + getId();
	}
}