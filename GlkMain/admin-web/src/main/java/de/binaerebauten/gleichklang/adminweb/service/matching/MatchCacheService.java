package de.binaerebauten.gleichklang.adminweb.service.matching;

import de.binaerebauten.gleichklang.core.model.locatable.*;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry.LogArea;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.SubscriptionRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchingRepository;
import de.binaerebauten.gleichklang.core.repository.user.AddressRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static de.binaerebauten.gleichklang.core.utils.MatchingUtils.ALLOWED_MISSING_ANSWERS_RATIO;

@Service
public class MatchCacheService
{
	public class CacheContent
	{
		private final int age;
		private final Set<RecommendationCategory> subscriptions;
		private final Set<RecommendationCategory> missingRequiredAnswers = EnumSet.noneOf(RecommendationCategory.class);
		private final Set<LocatableEntity> addresses = new HashSet<>();
		private final Set<Zip> zips = new HashSet<>();
		private final Map<RecommendationCategory, Set<LocatableEntity>> regionSearchRequests = new HashMap<>();
		private final Map<RecommendationCategory, Collection<ProximitySearchRequest>> proximitySearchRequests = new HashMap<>();
		private final Map<RecommendationCategory, Boolean> searchRelocatables = new HashMap<>();
		private final Map<RecommendationCategory, Boolean> relocatables = new HashMap<>();
		private final Map<RecommendationCategory, Boolean> withAvatar = new HashMap<>();
		private final Map<Long, Integer> numberAnswers = new HashMap<>();
		private final Map<Long, BitSet> choiceAnswers = new HashMap<>();
		
		public CacheContent(Long userId, Set<NumberQuestion> numberQuestions, Set<ChoiceQuestion> choiceQuestions)
		{
			if(userRepository.findBirthDateById(userId)!=null)
			{
				age = (int) userRepository.findBirthDateById(userId).until(LocalDate.now(), ChronoUnit.YEARS);
			}
			else
			{
				age=0;
			}
			subscriptions = subscriptionRepository.findCurrentSubscriptionOfferCategories(userId, LocalDateTime.now());
			
			for (Address address : addressRepository.findByUserId(userId))
			{
				if (address.getRegion() != null)
					addresses.add(address.getRegion());
				if (address.getCountry() != null)
					addresses.add(address.getCountry());
				if (address.getContinent() != null)
					addresses.add(address.getContinent());
				if (address.getZip() != null) zips.add(address.getZip());
			}
			
			for (RecommendationCategory category : subscriptions)
			{
				final RegionAnswer regionAnswer = matchingRepository.getRegionAnswer(userId, category);
				
				final List<RegionSearchRequest> regionSearchRequests = regionAnswer != null ? regionAnswer.getRegionSearchRequests() : new ArrayList<>();
				final List<ProximitySearchRequest> proximitySearchRequests = regionAnswer != null ? regionAnswer.getProximitySearchRequests() : new ArrayList<>();
				
				final Set<LocatableEntity> locatableEntities = new HashSet<>();
				for (RegionSearchRequest regionSearchRequest : regionSearchRequests)
				{
					locatableEntities.addAll(regionSearchRequest.getRestrictions());
					if (regionSearchRequest.getRestrictions().isEmpty())
					{
						final Country country = regionSearchRequest.getCountry();
						final Continent continent = regionSearchRequest.getContinent();
						
						locatableEntities.add(country != null ? country : continent);
					}
				}
				
				this.proximitySearchRequests.put(category, proximitySearchRequests);
				searchRelocatables.put(category, regionAnswer != null && regionAnswer.isSearchRelocatable());
				relocatables.put(category, regionAnswer != null && regionAnswer.isRelocatable());
				this.regionSearchRequests.put(category, locatableEntities);
				withAvatar.put(category, matchingRepository.isWithAvatar(userId, category));
			}
			
			if (!numberQuestions.isEmpty())
			{
				for (NumberAnswer numberAnswer : matchingRepository.getNumberAnswers(userId, numberQuestions))
				{
					numberAnswers.put(numberAnswer.getQuestion().getId(), numberAnswer.getNumberValue());
				}
			}
			
			if (!choiceQuestions.isEmpty())
			{
				for (ChoiceAnswer choiceAnswer : matchingRepository.getChoiceAnswers(userId, choiceQuestions))
				{
					if (!choiceAnswer.getChoices().isEmpty())
						choiceAnswers.put(choiceAnswer.getQuestion().getId(), choiceAnswerToBitSet(choiceAnswer));
				}
			}
			
			final List<Question> questions = new ArrayList<>();
			questions.addAll(numberQuestions);
			questions.addAll(choiceQuestions);
			
			for (RecommendationCategory category : RecommendationCategory.values())
			{
				if (getMissingRatio(questions, category) > ALLOWED_MISSING_ANSWERS_RATIO)
				{
					this.missingRequiredAnswers.add(category);
				}
			}
		}
		
		protected BitSet choiceAnswerToBitSet(Choice choice)
		{
			if (Objects.isNull(choice)) return null;
			
			final BitSet bitSet = new BitSet(choice.getPosition() + 1);
			bitSet.set(choice.getPosition());
			
			return bitSet;
		}
		
		private BitSet choiceAnswerToBitSet(ChoiceAnswer choiceAnswer)
		{
			if (Objects.isNull(choiceAnswer)) return null;
			
			final BitSet bitSet = new BitSet(choiceAnswer.getChoices().stream()
					.map(Choice::getPosition)
					.reduce(Math::max)
					.orElse(-1) + 1);
			choiceAnswer.getChoices().stream().map(Choice::getPosition).forEach(bitSet::set);
			
			return bitSet;
		}
		
		private double getMissingRatio(Collection<? extends Question> questions, RecommendationCategory category)
		{
			long requiredCount = 0L;
			long missingCount = 0L;
			
			for (Question question : questions)
			{
				final RecommendationCategory questionCategory = question.getQuestionGroup().getQuestionnaire().getRecommendationCategory();
				final boolean isRequiredCategory = questionCategory == null || questionCategory.equals(category);
				
				if (isRequiredCategory && question.isRequired())
				{
					requiredCount++;
					
					final Integer numberAnswer = numberAnswers.get(question.getId());
					final BitSet choiceAnswer = choiceAnswers.get(question.getId());
					
					if (numberAnswer == null && (choiceAnswer == null || choiceAnswer.isEmpty()))
					{
						missingCount++;
					}
				}
			}
			
			if (requiredCount == 0L) return 0d;
			return (double) missingCount / requiredCount;
		}
		
		public int getAge()
		{
			return age;
		}
		
		public boolean hasSubscription(RecommendationCategory category)
		{
			return subscriptions.contains(category);
		}
		
		public boolean isRequiredAnswersMissing(RecommendationCategory category)
		{
			return missingRequiredAnswers.contains(category);
		}
		
		public Set<LocatableEntity> getAddresses()
		{
			return addresses;
		}
		
		public Set<Zip> getZips()
		{
			return zips;
		}
		
		public Set<LocatableEntity> getRegionSearchRequests(RecommendationCategory category)
		{
			return regionSearchRequests.get(category);
		}
		
		public Collection<ProximitySearchRequest> getProximitySearchRequests(RecommendationCategory category)
		{
			return proximitySearchRequests.get(category);
		}
		
		public boolean isSearchRelocatable(RecommendationCategory category)
		{
			return searchRelocatables.get(category);
		}
		
		public boolean isRelocatable(RecommendationCategory category)
		{
			return relocatables.get(category);
		}
		
		public Boolean hasAvatar(RecommendationCategory category)
		{
			return withAvatar.get(category);
		}
		
		private Integer getNumberAnswers(Long questionId)
		{
			return numberAnswers.get(questionId);
		}
		
		public Integer getNumberAnswers(NumberQuestion question)
		{
			return getNumberAnswers(question.getId());
		}
		
		private BitSet getChoiceAnswers(Long questionId, BitSet defaultAnswersValues)
		{
			return choiceAnswers.getOrDefault(questionId, defaultAnswersValues);
		}
		
		/**
		 * Returns an answered result only.
		 *
		 * @param questionId
		 * @return
		 */
		public BitSet getChoiceAnswers(Long questionId)
		{
			return choiceAnswers.get(questionId);
		}
		
		/**
		 * Returns an answered result or a fallback result, if such is saved as
		 * a default empty answer in the question.
		 *
		 * @param question
		 * @return
		 */
		public BitSet getChoiceAnswers(ChoiceQuestion question)
		{
			Choice defaultChoice = question.getDefaultChoice();
			BitSet defaultAnswersValues = choiceAnswerToBitSet(defaultChoice);
			
			return getChoiceAnswers(question.getId(), defaultAnswersValues);
		}
	}
	
	public class MatchCache
	{
		private final SortedMap<Long, CacheContent> cachedUser = new TreeMap<>();
		private final Map<RecommendationCategory, Set<NumberQuestionsMapping>> numberQuestionsMapping = new HashMap<>();
		private final Map<RecommendationCategory, Set<ChoiceQuestionsMapping>> choiceQuestionsMapping = new HashMap<>();
		private final Map<RecommendationCategory, Set<AffinityMapping>> affinityMapping = new HashMap<>();
		private final Map<RecommendationCategory, Set<AgeQuestionMapping>> ageQuestionsMapping = new HashMap<>();
		private final Map<RecommendationCategory, Set<AvatarQuestionMapping>> avatarQuestionsMapping = new HashMap<>();
		private final Set<NumberQuestion> numberQuestions = new HashSet<>();
		private final Set<ChoiceQuestion> choiceQuestions = new HashSet<>();
		
		public MatchCache()
		{
			for (RecommendationCategory category : RecommendationCategory.values())
			{
				final Set<NumberQuestionsMapping> numberQuestionMappings = matchingRepository.getNumberQuestionMappings(category);
				final Set<ChoiceQuestionsMapping> choiceQuestionsMappings = matchingRepository.getChoiceQuestionMappings(category);
				final Set<AffinityMapping> affinityMappings = matchingRepository.getAffinityMappings(category);
				final Set<AgeQuestionMapping> ageQuestionsMappings = matchingRepository.getAgeQuestionMappings(category);
				final Set<AvatarQuestionMapping> avatarQuestionsMappings = matchingRepository.getAvatarQuestionMappings(category);
				
				numberQuestionsMapping.put(category, numberQuestionMappings);
				choiceQuestionsMapping.put(category, choiceQuestionsMappings);
				affinityMapping.put(category, affinityMappings);
				ageQuestionsMapping.put(category, ageQuestionsMappings);
				avatarQuestionsMapping.put(category, avatarQuestionsMappings);
				
				numberQuestionMappings.stream().map(NumberQuestionsMapping::getFactQuestion).forEach(numberQuestions::add);
				numberQuestionMappings.stream().map(NumberQuestionsMapping::getMinQuestion).forEach(numberQuestions::add);
				numberQuestionMappings.stream().map(NumberQuestionsMapping::getMaxQuestion).forEach(numberQuestions::add);
				
				choiceQuestionsMappings.stream().map(ChoiceQuestionsMapping::getSourceQuestion).forEach(choiceQuestions::add);
				choiceQuestionsMappings.stream().map(ChoiceQuestionsMapping::getTargetQuestion).forEach(choiceQuestions::add);
				
				affinityMappings.stream().map(AffinityMapping::getQuestions).forEach(choiceQuestions::addAll);
				
				ageQuestionsMappings.stream().map(AgeQuestionMapping::getMaxAgeQuestion).forEach(numberQuestions::add);
				ageQuestionsMappings.stream().map(AgeQuestionMapping::getMinAgeQuestion).forEach(numberQuestions::add);
				
				avatarQuestionsMappings.stream().map(AvatarQuestionMapping::getAvatarQuestion).forEach(choiceQuestions::add);
			}
		}
		
		public CacheContent getContent(Long userId)
		{
			CacheContent cacheContent = cachedUser.get(userId);
			if (cacheContent == null)
			{
				LOG.warn("User {} not in cache!", userId);
				cacheContent = new CacheContent(userId, numberQuestions, choiceQuestions);
				cachedUser.put(userId, cacheContent);
			}
			return cacheContent;
		}
		
		public Collection<Long> getCachedUserIds()
		{
			return cachedUser.keySet();
		}
		
		private void preInit(Long userId, MatchingLog matchingLog)
		{
			if (cachedUser.containsKey(userId)) return;
			
			final LocalDateTime startTime = LocalDateTime.now();
			cachedUser.put(userId, new CacheContent(userId, numberQuestions, choiceQuestions));
			matchingLog.add(LogArea.CACHE, Duration.between(startTime, LocalDateTime.now()));
		}
		
		public Set<NumberQuestionsMapping> getNumberQuestionsMapping(RecommendationCategory category)
		{
			return numberQuestionsMapping.get(category);
		}
		
		public Set<ChoiceQuestionsMapping> getChoiceQuestionsMapping(RecommendationCategory category)
		{
			return choiceQuestionsMapping.get(category);
		}
		
		public Set<AffinityMapping> getAffinityMapping(RecommendationCategory category)
		{
			return affinityMapping.get(category);
		}
		
		public Set<AgeQuestionMapping> getAgeQuestionsMapping(RecommendationCategory category)
		{
			return ageQuestionsMapping.get(category);
		}
		
		public Set<AvatarQuestionMapping> getAvatarQuestionsMapping(RecommendationCategory category)
		{
			return avatarQuestionsMapping.get(category);
		}
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(MatchCacheService.class);
	
	@Autowired
	private MatchingRepository matchingRepository;
	
	@Autowired
	private AddressRepository addressRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private SubscriptionRepository subscriptionRepository;
	
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MatchCache createCache(MatchingLog matchingLog)
	{
		LOG.info("Start creating cache");
		
		final int infoStep = 100;
		final MatchCache matchCache = new MatchCache();
		
		final List<Long> users = matchingRepository.findAllMatchUser(LocalDateTime.now());
		long count = 0;
		for (Long user : users)
		{
			matchCache.preInit(user, matchingLog);
			
			if (++count % infoStep == 0)
			{
				LOG.info("{} / {} done", count, users.size());
			}
		}
		
		LOG.info("Finished creating cache");
		
		return matchCache;
	}
	
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MatchCache createCache(Collection<Long> userIds, ExplanationLog explanationLog)
	{
		final MatchCache matchCache = new MatchCache();
		
		final LocalDateTime now = LocalDateTime.now();
		final List<Long> users = matchingRepository.findAllMatchUser(now, userIds);
		
		userIds.stream().filter(id -> !users.contains(id)).forEach(id ->
		{
			final User user = userRepository.findById(id);
			final Optional<Subscription> subscription = subscriptionRepository.findCurrentSubscription(id);
			if (user != null && user.getEmail() == null)
			{
				explanationLog.addDebugMessage(id + " is no matchable user - Email is null!");
			}
			else if (!subscription.isPresent())
			{
				explanationLog.addDebugMessage(id + " is no matchable user - No current subscription!");
			}
			else if (!now.isAfter(subscription.get().getBegin()) || !now.isBefore(subscription.get().getEnd()))
			{
				explanationLog.addDebugMessage(id + " is no matchable user - Current subscription begin and end is not between now");
			}
			else
			{
				explanationLog.addDebugMessage(id + " is no matchable user - Reason unknown; Bug?");
			}
		});
		
		for (Long user : users)
		{
			matchCache.preInit(user, explanationLog);
		}
		
		return matchCache;
	}
}
