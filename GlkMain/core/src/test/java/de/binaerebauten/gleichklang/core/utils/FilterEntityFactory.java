package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.model.filter.BinaryOperatorFilter.BinaryOperator;
import de.binaerebauten.gleichklang.core.model.filter.MessageFilter.Directory;
import de.binaerebauten.gleichklang.core.model.filter.UnaryOperatorFilter.UnaryOperator;
import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment.PaymentType;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.NumberQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.TextQuestion;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog.UserActivity;
import de.binaerebauten.gleichklang.core.repository.FilterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
public class FilterEntityFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(FilterEntityFactory.class);

	@Autowired
	private FilterRepository filterRepository;

	public FilterEntityFactory()
	{
	}

	@Transactional
	public BinaryOperatorFilter persistDefaultBinaryOperatorFilter(AbstractFilter leftFilter, AbstractFilter rightFilter)
	{
		Objects.requireNonNull(leftFilter);
		Objects.requireNonNull(rightFilter);

		final BinaryOperatorFilter binaryOperatorFilter = new BinaryOperatorFilter();
		binaryOperatorFilter.setRightFilter(rightFilter);
		binaryOperatorFilter.setLeftFilter(leftFilter);
		binaryOperatorFilter.setBinaryOperator(BinaryOperator.INTERSECTION);

		return filterRepository.save(binaryOperatorFilter);
	}
	
	public UnaryOperatorFilter persistDefaultUnaryOperatorFilter(AbstractFilter filter)
	{
		Objects.requireNonNull(filter);

		final UnaryOperatorFilter unaryOperatorFilter = new UnaryOperatorFilter();
		unaryOperatorFilter.setFilter(filter);
		unaryOperatorFilter.setUnaryOperator(UnaryOperator.NOT);

		return filterRepository.save(unaryOperatorFilter);
	}
	
	public TemplateFilter persistDefaultTemplateFilter(AbstractFilter filter)
	{
		Objects.requireNonNull(filter);

		final TemplateFilter templateFilter = new TemplateFilter();
		templateFilter.setFilter(filter);
		templateFilter.setName("template");

		return filterRepository.save(templateFilter);
	}
	
	public RegionFilter persistDefaultRegionFilter(LocatableEntity locatableEntity)
	{
		Objects.requireNonNull(locatableEntity);

		final RegionFilter regionFilter = new RegionFilter();
		regionFilter.setLocatableEntity(locatableEntity);

		return filterRepository.save(regionFilter);
	}
	
	public ProximityFilter persistDefaultProximityFilter(Zip zip)
	{
		Objects.requireNonNull(zip);

		final ProximityFilter proximityFilter = new ProximityFilter();
		proximityFilter.setRadius(500);
		proximityFilter.setZip(zip);

		return filterRepository.save(proximityFilter);
	}
	
	public RecommendationCategoryFilter persistDefaultRecommendationCategoryFilter()
	{
		final RecommendationCategoryFilter recommendationCategoryFilter = new RecommendationCategoryFilter();
		recommendationCategoryFilter.setEnumValue(RecommendationCategory.FRIENDSHIP);

		return filterRepository.save(recommendationCategoryFilter);
	}
	
	public MemberStatusFilter persistDefaultMemberStatusFilter()
	{
		final MemberStatusFilter memberStatusFilter = new MemberStatusFilter();
		memberStatusFilter.setEnumValue(MemberStatus.REGISTERED);

		return filterRepository.save(memberStatusFilter);
	}

	public LastNameFilter persistDefaultLastNameFilter()
	{
		final LastNameFilter lastNameFilter = new LastNameFilter();
		lastNameFilter.setValue("lastNameFilter");

		return filterRepository.save(lastNameFilter);
	}

	public FirstNameFilter persistDefaultFirstNameFilter()
	{
		final FirstNameFilter firstNameFilter = new FirstNameFilter();
		firstNameFilter.setValue("firstNameFilter");

		return filterRepository.save(firstNameFilter);
	}

	public AliasFilter persistDefaultAliasFilter()
	{
		final AliasFilter aliasFilter = new AliasFilter();
		aliasFilter.setValue("aliasFilter");

		return filterRepository.save(aliasFilter);
	}

	public MailFilter persistDefaultMailFilter()
	{
		final MailFilter mailFilter = new MailFilter();
		mailFilter.setValue("mailFilter");

		return filterRepository.save(mailFilter);
	}

	public ActionCodeFilter persistDefaultActionCodeFilter()
	{
		final ActionCodeFilter actionCodeFilter = new ActionCodeFilter();
		actionCodeFilter.setValue("actionCodeFilter");

		return filterRepository.save(actionCodeFilter);
	}
	
	public ChoiceQuestionFilter persistDefaultChocieQuestionFilter(ChoiceQuestion choiceQuestion)
	{
		Objects.requireNonNull(choiceQuestion);
		Assert.notEmpty(choiceQuestion.getChoiceGroup().getChoices());

		final ChoiceQuestionFilter choiceQuestionFilter = new ChoiceQuestionFilter();
		choiceQuestionFilter.setChoiceQuestion(choiceQuestion);
		choiceQuestionFilter.setChoice(choiceQuestion.getChoiceGroup().getChoices().get(0));

		return filterRepository.save(choiceQuestionFilter);
	}
	
	public NumberQuestionFilter persistDefaultNumberQuestionFilter(NumberQuestion numberQuestion)
	{
		Objects.requireNonNull(numberQuestion);
		
		final NumberQuestionFilter numberQuestionFilter = new NumberQuestionFilter();
		numberQuestionFilter.setNumberQuestion(numberQuestion);
		numberQuestionFilter.setMin(10);
		numberQuestionFilter.setMax(20);
		
		return filterRepository.save(numberQuestionFilter);
	}
	
	public AgeFilter persistDefaultAgeFilter()
	{
		final AgeFilter ageFilter = new AgeFilter();
		ageFilter.setMinAge(20);
		ageFilter.setMaxAge(30);
		
		return filterRepository.save(ageFilter);
	}

	public TextQuestionFilter persistDefaultTextQuestionFilter(TextQuestion textQuestion)
	{
		Objects.requireNonNull(textQuestion);

		final TextQuestionFilter textQuestionFilter = new TextQuestionFilter();
		textQuestionFilter.setTextQuestion(textQuestion);
		textQuestionFilter.setText("textValue");

		return filterRepository.save(textQuestionFilter);
	}

	public UserActivityFilter persistDefaultUserActivityFilter()
	{
		final UserActivityFilter userActivityFilter = new UserActivityFilter();
		userActivityFilter.setUserActivity(UserActivity.LOGIN);
		userActivityFilter.setFromDate(LocalDate.now().minusDays(3));
		userActivityFilter.setToDate(LocalDate.now().plusDays(3));

		return filterRepository.save(userActivityFilter);
	}

	public PaymentStateFilter persistDefaultPaymentStateFilter()
	{
		final PaymentStateFilter paymentStateFilter = new PaymentStateFilter();
		paymentStateFilter.setEnumValue(PaymentState.PENDING);

		return filterRepository.save(paymentStateFilter);
	}

	@Transactional
	public void reset()
	{
		try
		{
			filterRepository.deleteAll();
		}
		catch (ConstraintViolationException ex)
		{
			LOG.error("Cleaning failed", ex);
		}
	}

	public CancelReasonFilter persistDefaultCancelReasonFilter()
	{
		final CancelReasonFilter cancelReasonFilter = new CancelReasonFilter();
		cancelReasonFilter.setEnumValue(CancelReason.NO_TIME);

		return filterRepository.save(cancelReasonFilter);
	}

	public SubscriptionStateFilter persistDefaultSubscriptionStateFilter()
	{
		final SubscriptionStateFilter subscriptionStateFilter = new SubscriptionStateFilter();
		subscriptionStateFilter.setEnumValue(SubscriptionState.ACTIVE);

		return filterRepository.save(subscriptionStateFilter);
	}

	public UserPrepaymentUsageFilter persistDefaultUserPrepaymentUsageFiliter()
	{
		final UserPrepaymentUsageFilter userPrepaymentUsageFilter = new UserPrepaymentUsageFilter();
		userPrepaymentUsageFilter.setValue("0000-0000-0001");

		return filterRepository.save(userPrepaymentUsageFilter);
	}
	
	public ConfirmationFilter persistDefaultConfirmationFilter()
	{
		final ConfirmationFilter confirmationFilter = new ConfirmationFilter();
		confirmationFilter.setValue(true);
		
		return filterRepository.save(confirmationFilter);
	}
	
	public CityFilter persistDefaultCityFilter()
	{
		final CityFilter cityFilter = new CityFilter();
		cityFilter.setValue("textValue");
		
		return filterRepository.save(cityFilter);
	}

	public SexChoiceQuestionFilter persistDefaultSexChocieQuestionFilter(ChoiceQuestion choiceQuestion)
	{
		Objects.requireNonNull(choiceQuestion);
		Assert.notEmpty(choiceQuestion.getChoiceGroup().getChoices());

		final SexChoiceQuestionFilter choiceQuestionFilter = new SexChoiceQuestionFilter();
		choiceQuestionFilter.setChoice(choiceQuestion.getChoiceGroup().getChoices().get(0));

		return filterRepository.save(choiceQuestionFilter);
	}
	
	public MatchFilter persistDefaultMatchFilter()
	{
		final MatchFilter matchFilter = new MatchFilter();
		
		return filterRepository.save(matchFilter);
	}
	
	public RelationshipFilter persistDefaultRelationshipFilter()
	{
		final RelationshipFilter relationshipFilter = new RelationshipFilter();
		relationshipFilter.setTo(LocalDateTime.now().plus(1, ChronoUnit.MINUTES));
		relationshipFilter.setFrom(LocalDateTime.now().minus(1, ChronoUnit.MINUTES));
		
		return filterRepository.save(relationshipFilter);
	}
	
	public MessageFilter persistDefaultMessageFilter()
	{
		final MessageFilter messageFilter = new MessageFilter();
		messageFilter.setDirectory(Directory.INCOMING);
		
		return filterRepository.save(messageFilter);
	}
	
	public MissingAnswersRatioFilter persistDefaultMissingAnswersRatioFilter()
	{
		final MissingAnswersRatioFilter missingAnswersRatioFilter = new MissingAnswersRatioFilter();
		missingAnswersRatioFilter.setCategory(RecommendationCategory.PARTNERSHIP);
		
		return filterRepository.save(missingAnswersRatioFilter);
	}
	
	public FreeTextFilter persistDefaultFreeTextFilter()
	{
		final FreeTextFilter freeTextFilter = new FreeTextFilter();
		freeTextFilter.setValue("textValue");
		
		return filterRepository.save(freeTextFilter);
	}
	
	public DisableFootprintNotificationsFilter persistDefaultDisableFootprintNotificationsFilter()
	{
		final DisableFootprintNotificationsFilter disableFootprintNotificationsFilter = new DisableFootprintNotificationsFilter();
		disableFootprintNotificationsFilter.setValue(true);
		
		return filterRepository.save(disableFootprintNotificationsFilter);
	}
	
	public DisableNewsNotificationsFilter persistDefaultDisableNewsNotificationsFilter()
	{
		final DisableNewsNotificationsFilter disableNewsNotificationsFilter = new DisableNewsNotificationsFilter();
		disableNewsNotificationsFilter.setValue(true);
		
		return filterRepository.save(disableNewsNotificationsFilter);
	}
	
	public DisableCipherMessageNotificationsFilter persistDefaultDisableCipherMessageNotificationsFilter()
	{
		final DisableCipherMessageNotificationsFilter disableCipherMessageNotificationsFilter = new DisableCipherMessageNotificationsFilter();
		disableCipherMessageNotificationsFilter.setValue(true);
		
		return filterRepository.save(disableCipherMessageNotificationsFilter);
	}
	
	public DisablePositiveRankingNotificationsFilter persistDefaultDisablePositiveRankingNotificationsFilter()
	{
		final DisablePositiveRankingNotificationsFilter disablePositiveRankingNotificationsFilter = new DisablePositiveRankingNotificationsFilter();
		disablePositiveRankingNotificationsFilter.setValue(true);
		
		return filterRepository.save(disablePositiveRankingNotificationsFilter);
	}
	
	public DisableRecommendationNotificationsFilter persistDefaultDisableRecommendationNotificationsFilter()
	{
		final DisableRecommendationNotificationsFilter disableRecommendationNotificationsFilter = new DisableRecommendationNotificationsFilter();
		disableRecommendationNotificationsFilter.setValue(true);
		
		return filterRepository.save(disableRecommendationNotificationsFilter);
	}
	
	public EnableMarketingNotificationsFilter persistDefaultEnableMarketingNotificationsFilter()
	{
		final EnableMarketingNotificationsFilter enableMarketingNotificationsFilter = new EnableMarketingNotificationsFilter();
		enableMarketingNotificationsFilter.setValue(true);
		
		return filterRepository.save(enableMarketingNotificationsFilter);
	}
	
	public MailBlockedFilter persistDefaultMailBlockedFilter()
	{
		final MailBlockedFilter mailBlockedFilter = new MailBlockedFilter();
		mailBlockedFilter.setValue(true);
		
		return filterRepository.save(mailBlockedFilter);
	}
	
	public PaymentTypeFilter persistDefaultPaymentTypeFilter()
	{
		final PaymentTypeFilter paymentTypeFilter = new PaymentTypeFilter();
		paymentTypeFilter.setEnumValue(PaymentType.EXTERNAL_PAYMENT);
		
		return filterRepository.save(paymentTypeFilter);
	}
	
	public RecommendationBreakFilter persistDefaultRecommendationBreakFilter()
	{
		final RecommendationBreakFilter recommendationBreakFilter = new RecommendationBreakFilter();
		recommendationBreakFilter.setEnumValue(RecommendationCategory.PARTNERSHIP);
		
		return filterRepository.save(recommendationBreakFilter);
	}
	
	public UserRegistrationFilter persistDefaultUserRegistrationFilter()
	{
		final UserRegistrationFilter userRegistrationFilter = new UserRegistrationFilter();
		userRegistrationFilter.setEnumValue(RegistrationState.PAYMENT);
		
		return filterRepository.save(userRegistrationFilter);
	}
	
	public HasSubscriptionFilter persistDefaultHasSubscriptionFilter()
	{
		final HasSubscriptionFilter hasSubscriptionFilter = new HasSubscriptionFilter();
		hasSubscriptionFilter.setValue(true);
		
		return filterRepository.save(hasSubscriptionFilter);
	}
	
	public AutoRenewalFilter persistDefaultAutoRenewalFilter()
	{
		final AutoRenewalFilter autoRenewalFilter = new AutoRenewalFilter();
		autoRenewalFilter.setValue(true);
		
		return filterRepository.save(autoRenewalFilter);
	}
	
	public CurrentProductFilter persistDefaultCurrentProductFilter()
	{
		final CurrentProductFilter currentProductFilter = new CurrentProductFilter();
		currentProductFilter.setValue("testValue");
		
		return filterRepository.save(currentProductFilter);
	}
	
	public SubscriptionsProductFilter persistDefaultSubscriptionsProductFilter()
	{
		final SubscriptionsProductFilter subscriptionsProductFilter = new SubscriptionsProductFilter();
		subscriptionsProductFilter.setValue("testValue");
		
		return filterRepository.save(subscriptionsProductFilter);
	}
	
	public InvoicesProductFilter persistDefaultInvoicesProductFilter()
	{
		final InvoicesProductFilter invoicesProductFilter = new InvoicesProductFilter();
		invoicesProductFilter.setValue("testValue");
		
		return filterRepository.save(invoicesProductFilter);
	}
	
	public UserIdFilter persistDefaultUserIdFilter()
	{
		final UserIdFilter userIdFilter = new UserIdFilter();
		userIdFilter.setValue(42L);
		
		return filterRepository.save(userIdFilter);
	}
	
	public PaymentIdFilter persistDefaultPaymentIdFilter()
	{
		final PaymentIdFilter paymentIdFilter = new PaymentIdFilter();
		paymentIdFilter.setValue(42L);
		
		return filterRepository.save(paymentIdFilter);
	}
}
