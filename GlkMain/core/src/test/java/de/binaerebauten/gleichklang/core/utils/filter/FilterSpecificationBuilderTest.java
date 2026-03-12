package de.binaerebauten.gleichklang.core.utils.filter;

import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.filter.*;
import de.binaerebauten.gleichklang.core.model.filter.BinaryOperatorFilter.BinaryOperator;
import de.binaerebauten.gleichklang.core.model.filter.UnaryOperatorFilter.UnaryOperator;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMailReason;
import de.binaerebauten.gleichklang.core.model.matching.AffinityMapping;
import de.binaerebauten.gleichklang.core.model.matching.ChoiceQuestionsMapping;
import de.binaerebauten.gleichklang.core.model.matching.NumberQuestionsMapping;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment.PaymentType;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog.UserActivity;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.mail.UndeliverableMailRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRegistrationStateRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory.MessageState;
import de.binaerebauten.gleichklang.core.utils.FilterEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class FilterSpecificationBuilderTest extends BasePersistenceTest
{
	private class UserCreator implements FilterVisitor<Void>
	{
		private final User user;
		private boolean inverse;
		
		private UserCreator()
		{
			this(false);
		}
		
		private UserCreator(boolean inverse)
		{
			this.inverse = inverse;
			user = defaultEntityFactory.persistDefaultUser(UUID.randomUUID().toString());
		}
		
		@Override
		public Void visit(UnaryOperatorFilter unaryOperatorFilter)
		{
			inverse = !inverse;
			unaryOperatorFilter.getFilter().accept(this);
			inverse = !inverse;
			return null;
		}
		
		@Override
		public Void visit(BinaryOperatorFilter binaryOperatorFilter)
		{
			switch (binaryOperatorFilter.getBinaryOperator())
			{
				case UNION:
					binaryOperatorFilter.getLeftFilter().accept(this);
					break;
				case INTERSECTION:
					binaryOperatorFilter.getLeftFilter().accept(this);
					binaryOperatorFilter.getRightFilter().accept(this);
					break;
			}
			return null;
		}
		
		@Override
		public Void visit(TemplateFilter templateFilter)
		{
			templateFilter.getFilter().accept(this);
			return null;
		}
		
		@Override
		public Void visit(RegionFilter regionFilter)
		{
			if (inverse) return null;
			final Address address;
			try
			{
				address = user.getPaymentAddress();
				if (regionFilter.getLocatableEntity() instanceof Continent)
					address.setContinent((Continent) regionFilter.getLocatableEntity());
				if (regionFilter.getLocatableEntity() instanceof Country)
					address.setCountry((Country) regionFilter.getLocatableEntity());
				if (regionFilter.getLocatableEntity() instanceof Region)
					address.setRegion((Region) regionFilter.getLocatableEntity());
				if (regionFilter.getLocatableEntity() instanceof Zip)
					address.setZip((Zip) regionFilter.getLocatableEntity());
				
			}
			catch (AddressNotFoundException e)
			{
				e.printStackTrace();
			}
			
			userRepository.save(user);
			
			return null;
		}
		
		@Override
		public Void visit(CityFilter cityFilter)
		{
			user.getOptionalPaymentAddress().ifPresent(address -> address.setCity(getStringValue(cityFilter)));
			userRepository.save(user);
			
			return null;
		}
		
		@Override
		public Void visit(ProximityFilter proximityFilter)
		{
			return null;
		}
		
		private <T extends Enum<T>> T getEnumValue(EnumFilter<T> enumFilter, Class<T> type)
		{
			return inverse ? Arrays.stream(type.getEnumConstants()).filter(value -> !value.equals(enumFilter.getEnumValue())).findFirst().get() : enumFilter.getEnumValue();
		}
		
		private boolean getBooleanValue(BooleanFilter booleanFilter)
		{
			return booleanFilter.isValue() ^ inverse; //XOR
		}
		
		private long getNumberValue(NumberFilter numberFilter)
		{
			return numberFilter.getValue() + (inverse ? 1 : 0);
		}
		
		private String getStringValue(StringFilter stringFilter)
		{
			return getStringValue(stringFilter.getValue());
		}
		
		private String getStringValue(String value)
		{
			if (inverse)
			{
				final String randomString = UUID.randomUUID().toString();
				return randomString.substring(0, 5) + "@" + randomString.substring(5, 10);
			}
			
			return "pre@" + value + "post";
		}
		
		@Override
		public Void visit(RecommendationCategoryFilter recommendationCategoryFilter)
		{
			final SubscriptionOffer subscriptionOffer = paymentEntityFactory.persistInitialSubscriptionOffer(UUID.randomUUID().toString(), LocalDateTime.MIN, getEnumValue(recommendationCategoryFilter, RecommendationCategory.class));
			paymentEntityFactory.persistSubscription(user, subscriptionOffer, LocalDateTime.MIN);
			
			return null;
		}
		
		@Override
		public Void visit(MemberStatusFilter memberStatusFilter)
		{
			user.setMemberStatus(getEnumValue(memberStatusFilter, MemberStatus.class));
			userRepository.save(user);
			
			return null;
		}
		
		@Override
		public Void visit(ChoiceQuestionFilter choiceQuestionFilter)
		{
			final ChoiceAnswer answer = (ChoiceAnswer) defaultEntityFactory.persistDefaultAnswers(Collections.singletonList(choiceQuestionFilter.getChoiceQuestion()), user).iterator().next();
			
			final Set<Choice> inverseChoices = ((ChoiceQuestion) answer.getQuestion()).getChoiceGroup().getChoices().stream().filter(choice -> !choice.equals(choiceQuestionFilter.getChoice())).collect(Collectors.toSet());
			answer.setChoices(inverse ? inverseChoices : Collections.singleton(choiceQuestionFilter.getChoice()));
			answerRepository.save(answer);
			
			return null;
		}
		
		@Override
		public Void visit(NumberQuestionFilter numberQuestionFilter)
		{
			final NumberAnswer answer = (NumberAnswer) defaultEntityFactory.persistDefaultAnswers(Collections.singletonList(numberQuestionFilter.getNumberQuestion()), user).iterator().next();
			
			answer.setNumberValue(inverse ? numberQuestionFilter.getMin() - 1 : numberQuestionFilter.getMin());
			answerRepository.save(answer);
			
			return null;
		}
		
		@Override
		public Void visit(AgeFilter choiceQuestionFilter)
		{
			user.setBirthDate(LocalDate.now().minusYears(choiceQuestionFilter.getMinAge() - (inverse ? 1 : 0)));
			userRepository.save(user);
			
			return null;
		}

		@Override
		public Void visit(DateFilter dateFilter) {
			return null;
		}

		@Override
		public Void visit(LastLoginFilter lastLoginFilter) {
			return null;
		}

		@Override
		public Void visit(SubscriptionEndFilter subscriptionEndFilter) {
			return null;
		}

		@Override
		public Void visit(ActiveSubscriptionFilter activeSubscriptionFilter) {
			return null;
		}

		@Override
		public Void visit(DeactivatedProlongationsFilter deactivatedProlongationsFilter) {
			return null;
		}
		@Override
		public Void visit(ActivatedProlongationsFilter activatedProlongationsFilter) {
			return null;
		}
		@Override
		public Void visit(TextQuestionFilter textQuestionFilter)
		{
			final TextAnswer answer = (TextAnswer) defaultEntityFactory.persistDefaultAnswers(Collections.singletonList(textQuestionFilter.getTextQuestion()), user).iterator().next();
			
			answer.setTextValue(getStringValue(textQuestionFilter.getText()));
			answerRepository.save(answer);
			
			return null;
		}
		
		@Override
		public Void visit(LastNameFilter lastNameFilter)
		{
			user.setLastName(getStringValue(lastNameFilter));
			userRepository.save(user);
			return null;
		}
		
		@Override
		public Void visit(FirstNameFilter firstNameFilter)
		{
			user.setFirstName(getStringValue(firstNameFilter));
			userRepository.save(user);
			return null;
		}
		
		@Override
		public Void visit(AliasFilter aliasFilter)
		{
			user.setAlias(getStringValue(aliasFilter));
			userRepository.save(user);
			return null;
		}
		
		@Override
		public Void visit(MailFilter mailFilter)
		{
			user.setEmail(getStringValue(mailFilter));
			userRepository.save(user);
			return null;
		}
		
		@Override
		public Void visit(ConfirmationFilter confirmationFilter)
		{
			user.setEmailConfirmed(getBooleanValue(confirmationFilter));
			userRepository.save(user);
			return null;
		}
		
		@Override
		public Void visit(ActionCodeFilter actionCodeFilter)
		{
			final UserPaymentSettings userPaymentSettings = paymentEntityFactory.persistUserPaymentSettings(user, PaymentMethod.PREPAYMENT);
			userPaymentSettings.setActionCode(getStringValue(actionCodeFilter));
			userPaymentSettingsRepository.save(userPaymentSettings);
			return null;
		}
		
		@Override
		public Void visit(UserActivityFilter userActivityFilter)
		{
			final UserActivity userActivity = inverse ? Arrays.stream(UserActivity.values()).filter(value -> !value.equals(userActivityFilter.getUserActivity())).findFirst().get() : userActivityFilter.getUserActivity();
			
			defaultEntityFactory.persistDefaultUserActivityLog(user, userActivity);
			
			return null;
		}
		
		@Override
		public Void visit(PaymentStateFilter paymentStateFilter)
		{
			Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);
			
			AbstractPayment payment = paymentEntityFactory.persistExternalPayment(invoice, getEnumValue(paymentStateFilter, PaymentState.class));
			payment.setCurrent(true);
			paymentRepository.save(payment);
			
			return null;
		}
		
		@Override
		public Void visit(CancelReasonFilter cancelReasonFilter)
		{
			user.getCancelReasons().add(getEnumValue(cancelReasonFilter, CancelReason.class));
			userRepository.save(user);
			
			return null;
		}

		@Override
		public Void visit(LastSeenBrowserFilter lastSeenBrowserFilter) {
			return null;
		}

		@Override
		public Void visit(SubscriptionStateFilter subscriptionStateFilter)
		{
			final LocalDateTime now = LocalDateTime.now();
			final SubscriptionOffer subscriptionOffer = paymentEntityFactory.persistInitialSubscriptionOffer("FriendshipOffer", now, RecommendationCategory.FRIENDSHIP);
			final LocalDateTime end = now.plusDays(1);
			final LocalDateTime subscriptionBegin = end.minusMonths(subscriptionOffer.getDuration());
			
			final Subscription subscription = paymentEntityFactory.persistSubscription(user, subscriptionOffer, subscriptionBegin);
			subscription.setState(getEnumValue(subscriptionStateFilter, SubscriptionState.class));
			subscriptionRepository.save(subscription);
			
			return null;
		}
		
		@Override
		public Void visit(UserPrepaymentUsageFilter userPrepaymentUsageFilter)
		{
			Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);
			Country country = defaultEntityFactory.persistDefaultCountry();
			
			BankAccount bankAccount = paymentEntityFactory.persistDefaultBankAccount(country);
			
			String externalReferenceId = inverse ? "1" : userPrepaymentUsageFilter.getValue();
			paymentEntityFactory.persistPrepayment(user, bankAccount, invoice, externalReferenceId);
			
			return null;
		}
		
		@Override
		public Void visit(ExternalReferenceIdFilter externalReferenceIdFilter)
		{
			Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);
			Country country = defaultEntityFactory.persistDefaultCountry();
			
			BankAccount bankAccount = paymentEntityFactory.persistDefaultBankAccount(country);
			
			String externalReferenceId = inverse ? "1" : externalReferenceIdFilter.getValue();
			paymentEntityFactory.persistPrepayment(user, bankAccount, invoice, externalReferenceId);
			
			return null;
		}

		@Override
		public Void visit(SexChoiceQuestionFilter sexChoiceQuestionFilter)
		{
			final ChoiceAnswer answer = (ChoiceAnswer) defaultEntityFactory.persistDefaultAnswers(Collections.singletonList(sexChoiceQuestion), user).iterator().next();

			final Set<Choice> inverseChoices = ((ChoiceQuestion) answer.getQuestion()).getChoiceGroup().getChoices().stream().filter(choice -> !choice.equals(sexChoiceQuestionFilter.getChoice())).collect(Collectors.toSet());
			answer.setChoices(inverse ? inverseChoices : Collections.singleton(sexChoiceQuestionFilter.getChoice()));
			answerRepository.save(answer);

			return null;
		}
		
		@Override
		public Void visit(MatchFilter matchFilter)
		{
			if(inverse) defaultEntityFactory.persistDefaultMatch(user, user, RecommendationCategory.PARTNERSHIP);
			return null;
		}
		
		@Override
		public Void visit(RelationshipFilter relationshipFilter)
		{
			if(inverse) defaultEntityFactory.persistDefaultRelationship(user, user, RecommendationCategory.PARTNERSHIP);
			return null;
		}
		
		@Override
		public Void visit(MessageFilter messageFilter)
		{
			if(inverse) defaultEntityFactory.persistDefaultMessage(user, user, MessageState.SENT);
			return null;
		}
		
		@Override
		public Void visit(MissingAnswersRatioFilter missingAnswersRatioFilter)
		{
			if(inverse) defaultEntityFactory.persistDefaultAnswers(questions, user);
			
			return null;
		}
		
		@Override
		public Void visit(FreeTextFilter freeTextFilter)
		{
			final TextAnswer textAnswer = (TextAnswer) defaultEntityFactory.persistDefaultAnswers(Collections.singleton(freeTextQuestion), user).iterator().next();
			textAnswer.setTextValue(getStringValue(freeTextFilter));
			answerRepository.save(textAnswer);
			
			return null;
		}
		
		@Override
		public Void visit(DisableFootprintNotificationsFilter disableFootprintNotificationsFilter)
		{
			user.getUserSettings().setDisableFootprintNotifications(getBooleanValue(disableFootprintNotificationsFilter));
			userRepository.save(user);
			return null;
		}
		
		@Override
		public Void visit(DisableNewsNotificationsFilter disableNewsNotificationsFilter)
		{
			user.getUserSettings().setDisableNewsNotifications(getBooleanValue(disableNewsNotificationsFilter));
			userRepository.save(user);
			return null;
		}
		
		@Override
		public Void visit(EnableMarketingNotificationsFilter enableMarketingNotificationsFilter)
		{
			user.getUserSettings().setEnableMarketingNotifications(getBooleanValue(enableMarketingNotificationsFilter));
			userRepository.save(user);
			return null;
		}


		@Override
		public Void visit(DisableCipherMessageNotificationsFilter disableCipherMessageNotificationsFilter)
		{
			user.getUserSettings().setDisableCipherMessageNotifications(getBooleanValue(disableCipherMessageNotificationsFilter));
			userRepository.save(user);
			return null;
		}
		
		@Override
		public Void visit(DisablePositiveRankingNotificationsFilter disablePositiveRankingNotificationsFilter)
		{
			user.getUserSettings().setDisablePositiveRankingNotifications(getBooleanValue(disablePositiveRankingNotificationsFilter));
			userRepository.save(user);
			return null;
		}
		
		@Override
		public Void visit(DisableRecommendationNotificationsFilter disableRecommendationNotificationsFilter)
		{
			user.getUserSettings().setDisableRecommendationNotifications(getBooleanValue(disableRecommendationNotificationsFilter));
			userRepository.save(user);
			return null;
		}
		
		@Override
		public Void visit(MailBlockedFilter mailBlockedFilter)
		{
			final UndeliverableMail undeliverableMail = new UndeliverableMail();
			undeliverableMail.setRecipientEmail(user.getEmail());
			undeliverableMail.setUndeliverableMailReason(UndeliverableMailReason.UNKNOWN);
			undeliverableMail.setIncidents(inverse ? UndeliverableMailReason.UNKNOWN.getMaxIncidents() : UndeliverableMailReason.UNKNOWN.getMaxIncidents() + 1);
			undeliverableMailRepository.save(undeliverableMail);
			return null;
		}

		@Override
		public Void visit(BlockedFilter blockedFilter) {
			if(getBooleanValue(blockedFilter)) {
				user.setMemberStatus(MemberStatus.BLOCKED);
				user.setBlockedStatus(BlockedStatus.BLOCKED);
				userRepository.save(user);

			}
			return null;
		}

		@Override
		public Void visit(AdminBlockedFilter adminBlockedFilter) {
			if(getBooleanValue(adminBlockedFilter)) {
				user.setMemberStatus(MemberStatus.ADMIN_BLOCKED);
				user.setBlockedStatus(BlockedStatus.ADMIN_BLOCKED);
				userRepository.save(user);

			}
			return null;
		}

		@Override
		public Void visit(AudioPartnershipFilter audioPartnershipFilter) {
			return null;
		}

		@Override
		public Void visit(AudioFriendshipFilter audioFriendshipFilter) {
			return null;
		}

		@Override
		public Void visit(PaymentTypeFilter paymentTypeFilter)
		{
			final Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);
			final Country country = defaultEntityFactory.persistDefaultCountry();
			final BankAccount bankAccount = paymentEntityFactory.persistDefaultBankAccount(country);
			final AbstractPayment payment;
			
			switch(getEnumValue(paymentTypeFilter, PaymentType.class))
			{
				case EXTERNAL_PAYMENT:
					payment = paymentEntityFactory.persistExternalPayment(invoice, PaymentState.PAID);
					break;
				default:
					payment = paymentEntityFactory.persistPrepayment(user, bankAccount, invoice);
					break;
			}
			
			payment.setCurrent(true);
			paymentRepository.save(payment);
			
			return null;
		}
		
		@Override
		public Void visit(RecommendationBreakFilter recommendationBreakFilter)
		{
			final RecommendationBreak recommendationBreak = new RecommendationBreak();
			recommendationBreak.setUser(user);
			recommendationBreak.setCategory(recommendationBreakFilter.getEnumValue());
			if(inverse) recommendationBreak.setEndDate(LocalDate.now().minus(1, ChronoUnit.DAYS));
			recommendationBreakRepository.save(recommendationBreak);
			
			return null;
		}
		
		@Override
		public Void visit(UserRegistrationFilter userRegistrationFilter)
		{
			final UserRegistrationState userRegistrationState = new UserRegistrationState();
			userRegistrationState.setUser(user);
			userRegistrationState.setRegistrationState(getEnumValue(userRegistrationFilter, RegistrationState.class));
			userRegistrationStateRepository.save(userRegistrationState);
			
			return null;
		}
		
		@Override
		public Void visit(HasSubscriptionFilter hasSubscriptionFilter)
		{
			if(getBooleanValue(hasSubscriptionFilter))
				paymentEntityFactory.persistSubscription(user, paymentEntityFactory.persistInitialSubscriptionOffer("test", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP), LocalDateTime.now());
			
			return null;
		}
		
		@Override
		public Void visit(AutoRenewalFilter autoRenewalFilter)
		{
			final Subscription subscription = paymentEntityFactory.persistSubscription(user, paymentEntityFactory.persistInitialSubscriptionOffer("test", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP), LocalDateTime.now());
			subscription.setAutomaticRenewal(getBooleanValue(autoRenewalFilter));
			subscriptionRepository.save(subscription);
			
			return null;
		}
		
		@Override
		public Void visit(InvoicesProductFilter invoicesProductFilter)
		{
			final Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);
			final InitialSubscriptionOffer product = paymentEntityFactory.persistInitialSubscriptionOffer(getStringValue(invoicesProductFilter), LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);
			
			paymentEntityFactory.persistDefaultInvoiceItem(invoice, product);
			
			return null;
		}
		
		@Override
		public Void visit(SubscriptionsProductFilter subscriptionsProductFilter)
		{
			final InitialSubscriptionOffer offer = paymentEntityFactory.persistInitialSubscriptionOffer(getStringValue(subscriptionsProductFilter), LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);
			paymentEntityFactory.persistSubscription(user, offer, LocalDateTime.now());
			return null;
		}
		
		@Override
		public Void visit(CurrentProductFilter currentProductFilter)
		{
			final InitialSubscriptionOffer offer = paymentEntityFactory.persistInitialSubscriptionOffer(getStringValue(currentProductFilter), LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);
			paymentEntityFactory.persistSubscription(user, offer, LocalDateTime.now());
			return null;
		}
		
		@Override
		public Void visit(UserIdFilter userIdFilter)
		{
			// specified correctly in the filter
			return null;
		}
		
		@Override
		public Void visit(PaymentIdFilter paymentIdFilter)
		{
			// specified correctly in the filter
			return null;
		}
	}
	
	@Autowired
	private FilterEntityFactory filterEntityFactory;
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AnswerRepository answerRepository;
	
	@Autowired
	private FilterRepository filterRepository;
	
	@Autowired
	private UserPaymentSettingsRepository userPaymentSettingsRepository;
	
	@Autowired
	private PaymentEntityFactory paymentEntityFactory;
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private QuestionnaireRepository questionnaireRepository;
	
	@Autowired
	private FilterSpecificationBuilder filterSpecificationBuilder;
	
	@Autowired
	private RecommendationBreakRepository recommendationBreakRepository;
	
	@Autowired
	private UserRegistrationStateRepository userRegistrationStateRepository;
	
	@Autowired
	private UndeliverableMailRepository undeliverableMailRepository;
	
	@Autowired
	private SubscriptionRepository subscriptionRepository;
	
	@Autowired
	private PaymentRepository paymentRepository;
	
	private UserCreator userCreator;
	private UserCreator inverseUserCreator;
	
	private TextQuestion freeTextQuestion;
	private ChoiceQuestion sexChoiceQuestion;
	
	private final Collection<Question> questions = new HashSet<>();
	
	@Before
	public void setup()
	{
		userCreator = new UserCreator();
		inverseUserCreator = new UserCreator(true);
		
		createQuestions();
		
		/* for freeTextFilterTest */
		final Questionnaire questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		questionnaire.setI18nKey(NaturalKey.FREE_TEXT_FRIENDSHIP.naturalKey);
		questionnaireRepository.save(questionnaire);
		freeTextQuestion = defaultEntityFactory.persistDefaultTextQuestion(defaultEntityFactory.persistDefaultQuestionGroup(questionnaire), defaultEntityFactory.persistDefaultI18NEntry());
		
		/* for sexChoiceFilterTest */
		sexChoiceQuestion = defaultEntityFactory.persistDefaultChoiceQuestion(defaultEntityFactory.persistDefaultQuestionGroup(defaultEntityFactory.persistDefaultQuestionnaire()), defaultEntityFactory.createDefaultI18NEntry(NaturalKey.SEX));
	}
	
	private void createQuestions()
	{
		final NumberQuestionsMapping numberQuestionsMapping = defaultEntityFactory.persistDefaultNumberQuestionsMapping(RecommendationCategory.PARTNERSHIP);
		final AffinityMapping affinityMapping = defaultEntityFactory.persistDefaultAffinityMapping(RecommendationCategory.PARTNERSHIP);
		final ChoiceQuestionsMapping choiceQuestionsMapping = defaultEntityFactory.persistDefaultChoiceQuestionsMapping(RecommendationCategory.PARTNERSHIP);
		
		questions.add(numberQuestionsMapping.getFactQuestion());
		questions.add(numberQuestionsMapping.getMaxQuestion());
		questions.add(numberQuestionsMapping.getMinQuestion());
		questions.addAll(affinityMapping.getQuestions());
		questions.add(choiceQuestionsMapping.getSourceQuestion());
		questions.add(choiceQuestionsMapping.getTargetQuestion());
		
		questions.forEach(q -> q.setRequirement(Requirement.REQUIRED));
		
		questionRepository.save(questions);
	}
	
	@After
	public void tearDown()
	{
		filterEntityFactory.reset();
		defaultEntityFactory.reset();
	}
	
	private void runAsserts(AbstractFilter filter)
	{
		filter.accept(userCreator);
		filter.accept(inverseUserCreator);
		
		final Specification<User> specification = filterSpecificationBuilder.build(filter,"");
		
		Assert.assertThat(userRepository.count(), CoreMatchers.equalTo(2L));
		Assert.assertThat(userRepository.count(specification), CoreMatchers.equalTo(1L));
		Assert.assertThat(userRepository.findAll(specification), CoreMatchers.hasItem(userCreator.user));
	}
	
	@Test
	public void regionFilterTest()
	{
		final RegionFilter regionFilter = filterEntityFactory.persistDefaultRegionFilter(defaultEntityFactory.persistDefaultZip(defaultEntityFactory.persistDefaultCountry()));
		
		runAsserts(regionFilter);
	}
	
	@Test
	public void cityFilterTest()
	{
		final CityFilter cityFilter = filterEntityFactory.persistDefaultCityFilter();
		
		runAsserts(cityFilter);
	}
	
	@Test
	public void choiceQuestionFilterTest()
	{
		final ChoiceQuestionFilter choiceQuestionFilter = filterEntityFactory.persistDefaultChocieQuestionFilter(defaultEntityFactory.persistDefaultChoiceQuestion(defaultEntityFactory.persistDefaultQuestionGroup(defaultEntityFactory.persistDefaultQuestionnaire()), defaultEntityFactory.persistDefaultI18NEntry()));
		
		runAsserts(choiceQuestionFilter);
	}
	
	@Test
	public void numberQuestionFilterTest()
	{
		final NumberQuestionFilter numberQuestionFilter = filterEntityFactory.persistDefaultNumberQuestionFilter(defaultEntityFactory.persistDefaultNumberQuestion(defaultEntityFactory.persistDefaultQuestionGroup(defaultEntityFactory.persistDefaultQuestionnaire()), defaultEntityFactory.persistDefaultI18NEntry()));
		
		runAsserts(numberQuestionFilter);
	}
	
	@Test
	public void ageFilterTest()
	{
		final AgeFilter ageFilter = filterEntityFactory.persistDefaultAgeFilter();
		
		runAsserts(ageFilter);
	}
	
	@Test
	public void textQuestionFilterTest()
	{
		final TextQuestionFilter textQuestionFilter = filterEntityFactory.persistDefaultTextQuestionFilter(defaultEntityFactory.persistDefaultTextQuestion(defaultEntityFactory.persistDefaultQuestionGroup(defaultEntityFactory.persistDefaultQuestionnaire()), defaultEntityFactory.persistDefaultI18NEntry()));
		
		runAsserts(textQuestionFilter);
	}
	
	@Test
	public void recommendationCategoryFilterTest()
	{
		final RecommendationCategoryFilter recommendationCategoryFilter = filterEntityFactory.persistDefaultRecommendationCategoryFilter();
		
		runAsserts(recommendationCategoryFilter);
	}
	
	@Test
	public void memberStatusFilterTest()
	{
		final MemberStatusFilter memberStatusFilter = filterEntityFactory.persistDefaultMemberStatusFilter();
		
		runAsserts(memberStatusFilter);
	}
	
	@Test
	public void firstNameFilterTest()
	{
		final FirstNameFilter firstNameFilter = filterEntityFactory.persistDefaultFirstNameFilter();
		
		runAsserts(firstNameFilter);
	}
	
	@Test
	public void lastNameFilterTest()
	{
		final LastNameFilter lastNameFilter = filterEntityFactory.persistDefaultLastNameFilter();
		
		runAsserts(lastNameFilter);
	}
	
	@Test
	public void aliasFilterTest()
	{
		final AliasFilter aliasFilter = filterEntityFactory.persistDefaultAliasFilter();
		
		runAsserts(aliasFilter);
	}
	
	@Test
	public void mailFilterTest()
	{
		final MailFilter mailFilter = filterEntityFactory.persistDefaultMailFilter();
		
		runAsserts(mailFilter);
	}
	
	@Test
	public void confirmationFilterTest()
	{
		final ConfirmationFilter confirmationFilter = filterEntityFactory.persistDefaultConfirmationFilter();
		
		runAsserts(confirmationFilter);
	}
	
	@Test
	public void actionCodeFilterTest()
	{
		final ActionCodeFilter actionCodeFilter = filterEntityFactory.persistDefaultActionCodeFilter();
		
		runAsserts(actionCodeFilter);
	}
	
	@Test
	public void intersectionFilterTest()
	{
		final MemberStatusFilter memberStatusFilter = filterEntityFactory.persistDefaultMemberStatusFilter();
		final RecommendationCategoryFilter recommendationCategoryFilter = filterEntityFactory.persistDefaultRecommendationCategoryFilter();
		
		final BinaryOperatorFilter binaryOperatorFilter = filterEntityFactory.persistDefaultBinaryOperatorFilter(memberStatusFilter, recommendationCategoryFilter);
		binaryOperatorFilter.setBinaryOperator(BinaryOperator.INTERSECTION);
		filterRepository.save(binaryOperatorFilter);
		
		runAsserts(binaryOperatorFilter);
	}
	
	@Test
	public void unionFilterTest()
	{
		final MemberStatusFilter memberStatusFilter = filterEntityFactory.persistDefaultMemberStatusFilter();
		final RecommendationCategoryFilter recommendationCategoryFilter = filterEntityFactory.persistDefaultRecommendationCategoryFilter();
		
		final BinaryOperatorFilter binaryOperatorFilter = filterEntityFactory.persistDefaultBinaryOperatorFilter(memberStatusFilter, recommendationCategoryFilter);
		binaryOperatorFilter.setBinaryOperator(BinaryOperator.UNION);
		filterRepository.save(binaryOperatorFilter);
		
		runAsserts(binaryOperatorFilter);
	}
	
	@Test
	public void notFilterTest()
	{
		final MemberStatusFilter memberStatusFilter = filterEntityFactory.persistDefaultMemberStatusFilter();
		
		final UnaryOperatorFilter unaryOperatorFilter = filterEntityFactory.persistDefaultUnaryOperatorFilter(memberStatusFilter);
		unaryOperatorFilter.setUnaryOperator(UnaryOperator.NOT);
		filterRepository.save(unaryOperatorFilter);
		
		runAsserts(unaryOperatorFilter);
	}
	
	@Test
	public void templateFilterTest()
	{
		final MemberStatusFilter memberStatusFilter = filterEntityFactory.persistDefaultMemberStatusFilter();
		final TemplateFilter templateFilter = filterEntityFactory.persistDefaultTemplateFilter(memberStatusFilter);
		
		runAsserts(templateFilter);
	}
	
	@Test
	public void userActivityFilterTest()
	{
		final UserActivityFilter userActivityFilter = filterEntityFactory.persistDefaultUserActivityFilter();
		
		runAsserts(userActivityFilter);
	}
	
	@Test
	public void paymentStateFilterTest()
	{
		PaymentStateFilter paymentStateFilter = filterEntityFactory.persistDefaultPaymentStateFilter();
		
		runAsserts(paymentStateFilter);
	}
	
	@Test
	public void cancelReasonFilterTest()
	{
		final CancelReasonFilter cancelReasonFilter = filterEntityFactory.persistDefaultCancelReasonFilter();
		
		runAsserts(cancelReasonFilter);
	}
	
	@Test
	public void subscriptionStateFilterTest()
	{
		final SubscriptionStateFilter subscriptionStateFilter =
				filterEntityFactory.persistDefaultSubscriptionStateFilter();
		
		runAsserts(subscriptionStateFilter);
	}
	
	@Test
	public void userPrepaymentUsageFilterTest()
	{
		final UserPrepaymentUsageFilter userPrepaymentUsageFilter =
				filterEntityFactory.persistDefaultUserPrepaymentUsageFiliter();
		
		runAsserts(userPrepaymentUsageFilter);
	}

	@Test
	public void sexChoiceQuestionFilterTest()
	{
		final SexChoiceQuestionFilter sexChoiceQuestionFilter = filterEntityFactory.persistDefaultSexChocieQuestionFilter(sexChoiceQuestion);
		
		runAsserts(sexChoiceQuestionFilter);
	}
	
	@Test
	public void matchFilterTest()
	{
		final MatchFilter matchFilter = filterEntityFactory.persistDefaultMatchFilter();
		
		runAsserts(matchFilter);
	}
	
	@Test
	public void relationshipFilterTest()
	{
		final RelationshipFilter relationshipFilter = filterEntityFactory.persistDefaultRelationshipFilter();
		
		runAsserts(relationshipFilter);
	}
	
	@Test
	public void messageFilterTest()
	{
		final MessageFilter messageFilter = filterEntityFactory.persistDefaultMessageFilter();
		
		runAsserts(messageFilter);
	}
	
	@Test
	public void missingAnswersRatioFilterTest()
	{
		final MissingAnswersRatioFilter missingAnswersRatioFilter = filterEntityFactory.persistDefaultMissingAnswersRatioFilter();
		
		runAsserts(missingAnswersRatioFilter);
	}
	
	@Test
	public void freeTextFilterTest()
	{
		final FreeTextFilter freeTextFilter = filterEntityFactory.persistDefaultFreeTextFilter();
		
		runAsserts(freeTextFilter);
	}
	
	@Test
	public void disableFootprintNotificationsFilterTest()
	{
		final DisableFootprintNotificationsFilter disableFootprintNotificationsFilter = filterEntityFactory.persistDefaultDisableFootprintNotificationsFilter();
		
		runAsserts(disableFootprintNotificationsFilter);
	}
	
	@Test
	public void disableNewsNotificationsFilterTest()
	{
		final DisableNewsNotificationsFilter disableNewsNotificationsFilter = filterEntityFactory.persistDefaultDisableNewsNotificationsFilter();
		
		runAsserts(disableNewsNotificationsFilter);
	}
	
	@Test
	public void disableCipherMessageNotificationsFilterTest()
	{
		final DisableCipherMessageNotificationsFilter disableCipherMessageNotificationsFilter = filterEntityFactory.persistDefaultDisableCipherMessageNotificationsFilter();
		
		runAsserts(disableCipherMessageNotificationsFilter);
	}
	
	@Test
	public void disablePositiveRankingNotificationsFilterTest()
	{
		final DisablePositiveRankingNotificationsFilter disablePositiveRankingNotificationsFilter = filterEntityFactory.persistDefaultDisablePositiveRankingNotificationsFilter();
		
		runAsserts(disablePositiveRankingNotificationsFilter);
	}
	
	@Test
	public void disableRecommendationNotificationsFilterTest()
	{
		final DisableRecommendationNotificationsFilter disableRecommendationNotificationsFilter = filterEntityFactory.persistDefaultDisableRecommendationNotificationsFilter();
		
		runAsserts(disableRecommendationNotificationsFilter);
	}
	
	@Test
	public void enableMarketingNotificationsFilterTest()
	{
		final EnableMarketingNotificationsFilter enableMarketingNotificationsFilter = filterEntityFactory.persistDefaultEnableMarketingNotificationsFilter();
		
		runAsserts(enableMarketingNotificationsFilter);
	}
	
	@Test
	public void mailBlockedFilterTest()
	{
		final MailBlockedFilter mailBlockedFilter = filterEntityFactory.persistDefaultMailBlockedFilter();
		
		runAsserts(mailBlockedFilter);
	}
	
	@Test
	public void paymentTypeFilterTest()
	{
		final PaymentTypeFilter paymentTypeFilter = filterEntityFactory.persistDefaultPaymentTypeFilter();
		
		runAsserts(paymentTypeFilter);
	}
	
	@Test
	public void recomendationBreakFilterTest()
	{
		final RecommendationBreakFilter recommendationBreakFilter = filterEntityFactory.persistDefaultRecommendationBreakFilter();
		
		runAsserts(recommendationBreakFilter);
	}
	
	@Test
	public void userRegistrationFilterTest()
	{
		final UserRegistrationFilter userRegistrationFilter = filterEntityFactory.persistDefaultUserRegistrationFilter();
		
		runAsserts(userRegistrationFilter);
	}
	
	@Test
	public void hasSubscriptionFiterTest()
	{
		final HasSubscriptionFilter hasSubscriptionFilter = filterEntityFactory.persistDefaultHasSubscriptionFilter();
		
		runAsserts(hasSubscriptionFilter);
	}
	
	@Test
	public void autoRenewalFilterTest()
	{
		final AutoRenewalFilter autoRenewalFilter = filterEntityFactory.persistDefaultAutoRenewalFilter();
		
		runAsserts(autoRenewalFilter);
	}
	
	@Test
	public void currentProductFilterTest()
	{
		final CurrentProductFilter currentProductFilter = filterEntityFactory.persistDefaultCurrentProductFilter();
		
		runAsserts(currentProductFilter);
	}
	
	@Test
	public void subscriptionsProductFilterTest()
	{
		final SubscriptionsProductFilter subscriptionsProductFilter = filterEntityFactory.persistDefaultSubscriptionsProductFilter();
		
		runAsserts(subscriptionsProductFilter);
	}
	
	@Test
	public void invoicesProductFilterTest()
	{
		final InvoicesProductFilter invoicesProductFilter = filterEntityFactory.persistDefaultInvoicesProductFilter();
		
		runAsserts(invoicesProductFilter);
	}
	
	@Test
	public void userIdFilterTest()
	{
		final UserIdFilter userIdFilter = filterEntityFactory.persistDefaultUserIdFilter();
		userIdFilter.setValue(userCreator.user.getId());
		
		runAsserts(userIdFilter);
	}
	
	@Test
	public void paymentIdFilterTest()
	{
		final Invoice invoice = paymentEntityFactory.persistDefaultInvoice(userCreator.user);
		final Country country = defaultEntityFactory.persistDefaultCountry();
		
		final BankAccount bankAccount = paymentEntityFactory.persistDefaultBankAccount(country);
		
		final AbstractPayment payment = paymentEntityFactory.persistPrepayment(userCreator.user, bankAccount, invoice, "foobar");
		
		final PaymentIdFilter paymentIdFilter = filterEntityFactory.persistDefaultPaymentIdFilter();
		paymentIdFilter.setValue(payment.getId());
		
		runAsserts(paymentIdFilter);
	}
}
