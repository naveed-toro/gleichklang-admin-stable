package de.binaerebauten.gleichklang.adminweb.service.matching;

import com.google.common.annotations.VisibleForTesting;
import de.binaerebauten.gleichklang.adminweb.service.matching.MatchCacheService.CacheContent;
import de.binaerebauten.gleichklang.adminweb.service.matching.MatchCacheService.MatchCache;
import de.binaerebauten.gleichklang.core.model.locatable.LocatableEntity;
import de.binaerebauten.gleichklang.core.model.locatable.ProximitySearchRequest;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.matching.AreaMatchStatisticEntry.MatchArea;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic.MatchingScope;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry.LogArea;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.repository.matching.MatchRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchStatisticRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.concurrent.ListenableFuture;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MatchingService
{
	private static final int BATCH_SIZE = 40;
	private static final int PAGE_SIZE = 50000;
	private static final Logger LOG = LoggerFactory.getLogger(MatchingService.class);
	
	private final boolean autoMatchingEnabled;
	
	@Autowired
	private MatchingRepository matchingRepository;
	
	@Autowired
	private MatchRepository matchRepository;
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	@Autowired
	private MatchCacheService matchCacheService;
	
	@Autowired
	private MatchStatisticRepository matchStatisticRepository;
	
	@Autowired
	public MatchingService(Environment environment)
	{
		autoMatchingEnabled = environment.getProperty("matching.auto.enabled", Boolean.class, false);
	}
	
	public boolean isAutoMatchingEnabled()
	{
		return autoMatchingEnabled;
	}
	
	@Async
	public ListenableFuture<Void> startMatching()
	{
		final PerformanceLog performanceLog = new PerformanceLog(matchStatisticRepository, MatchingScope.MATCHING);
		
		final MatchCache matchCache = matchCacheService.createCache(performanceLog);
		
		deleteCorruptedMatches(performanceLog);
		deleteUnnecessaryMatches(performanceLog);
		updateMatches(matchCache, performanceLog);
		createNewMatches(performanceLog, matchCache);
		
		performanceLog.finish();
		
		return new AsyncResult<>(null);
	}
	
	public void startMatching(Collection<Long> userIds, ExplanationLog explanationLog)
	{
		final MatchCache matchCache = matchCacheService.createCache(userIds, explanationLog);
		
		for (RecommendationCategory category : RecommendationCategory.values())
		{
			for (Long sourceUserId : matchCache.getCachedUserIds())
			{
				if (matchCache.getContent(sourceUserId).hasSubscription(category))
				{
					final Set<Long> relationshipUsers = matchingRepository.findAllTargetUserRelationship(sourceUserId, category);
					
					for (Long targetUserId : matchCache.getCachedUserIds())
					{
						if (targetUserId > sourceUserId && matchCache.getContent(targetUserId).hasSubscription(category))
						{
							explanationLog.addDebugMessage("\n");
							explanationLog.addDebugMessage("# start for sourceUserId " + sourceUserId + " and targetUserId " + targetUserId + " in " + category.toString());
							
							if (!relationshipUsers.contains(targetUserId))
							{
								final Match match = new Match(sourceUserId, targetUserId, category);
								updateMatch(match, explanationLog, matchCache);
								
								explanationLog.addDebugMessage("final match strictness is " + match.getStrictness());
							}
							else
							{
								explanationLog.addDebugMessage("relationship already exists");
							}
						}
					}
				}
				else
				{
					explanationLog.addDebugMessage("user id " + sourceUserId + " no subscription in " + category);
				}
			}
		}
		
		explanationLog.finish();
	}
	
	// TODO @VK Warum testest du es nicht über die Haupteinstiegsmethode "startMatching()", wo wie es vorher auch war, dann kann diese Methode wieder private sein!
	@VisibleForTesting
	@Transactional
	void deleteCorruptedMatches(PerformanceLog performanceLog)
	{
		final Pageable pageable = new PageRequest(0, PAGE_SIZE);
		boolean hasContent;
		do
		{
			final LocalDateTime startTime = LocalDateTime.now();
			LOG.info("Start Delete Corrupted Matches");
			hasContent = transactionTemplate.execute(status ->
			{
				final Page<Long> corruptedSingleMatches = matchingRepository.findAllCorruptedMatches(pageable);
				LOG.info("Delete Corrupted Matches - Pages left: {} - Total Elements: {}",
						corruptedSingleMatches.getTotalPages(), corruptedSingleMatches.getTotalElements());
				List<Long> ids = corruptedSingleMatches.getContent();
				if (!ids.isEmpty()) matchRepository.deleteByIdIn(ids);
				
				return corruptedSingleMatches.hasContent();
			});
			performanceLog.add(LogArea.DELETE_CORRUPTED, Duration.between(startTime, LocalDateTime.now()));
			
		} while (hasContent);
	}
	
	// TODO @VK Warum testest du es nicht über die Haupteinstiegsmethode "startMatching()", wo wie es vorher auch war, dann kann diese Methode wieder private sein!
	@VisibleForTesting
	@Transactional
	void deleteUnnecessaryMatches(PerformanceLog performanceLog)
	{
		final Pageable pageable = new PageRequest(0, PAGE_SIZE);
		boolean hasContent;
		do
		{
			final LocalDateTime startTime = LocalDateTime.now();
			LOG.info("Start Delete Unnecessary Matches");
			hasContent = transactionTemplate.execute(status ->
			{
				final Page<Long> unnecessaryMatches = matchingRepository.findAllUnnecessaryMatches(LocalDateTime.now(), pageable);
				LOG.info("Delete Unnecessary Matches - Pages left: {} - Total Elements: {}",
						unnecessaryMatches.getTotalPages(), unnecessaryMatches.getTotalElements());
				List<Long> ids = unnecessaryMatches.getContent();
				if (!ids.isEmpty()) matchRepository.deleteByIdIn(ids);
				
				return unnecessaryMatches.hasContent();
			});
			performanceLog.add(LogArea.DELETE_UNNECESSARY, Duration.between(startTime, LocalDateTime.now()));
			
		} while (hasContent);
	}

	@Transactional
	private void updateMatches(MatchCache matchCache, PerformanceLog performanceLog)
	{
		LOG.info("Start Update");
		
		Pageable pageable = new PageRequest(0, PAGE_SIZE, Direction.ASC, "sourceUserId", "targetUserId");
		while (pageable != null)
		{
			LOG.info("Get Matches");
			final Page<Match> matches = matchingRepository.findAll(pageable);
			
			LOG.info("Start Update Matches {} / {} - Total Elements: {}", pageable.getPageNumber() + 1, matches.getTotalPages(), matches.getTotalElements());
			int index = 0;
			while (index < matches.getNumberOfElements())
			{
				final int start_i = index;
				index = transactionTemplate.execute(status ->
				{
					int updates = 0;
					
					for (int i = start_i; i < matches.getNumberOfElements(); i++)
					{
						final Match match = matches.getContent().get(i);
						final Strictness strictness = match.getStrictness();
						final int count = match.getNumber();
						final RecommendationCategory category = match.getCategory();
						
						/* check cached user has category because of gap in synchronization */
						if(!matchCache.getContent(match.getSourceUserId()).hasSubscription(category) || !matchCache.getContent(match.getTargetUserId()).hasSubscription(category))
						{
							match.setStrictness(Strictness.lastStrictness);
						}
						else
						{
							updateMatch(match, performanceLog, matchCache);
						}
						
						if (!match.getStrictness().equals(strictness) || match.getNumber() != count)
						{
							final LocalDateTime startTime = LocalDateTime.now();
							matchingRepository.updateMatch(match.getNumber(), match.getStrictness(), match.getId());
							performanceLog.add(LogArea.SAVE_UPDATE, Duration.between(startTime, LocalDateTime.now()));
							
							if (++updates >= BATCH_SIZE) return i + 1;
						}
					}
					return matches.getNumberOfElements();
				});
			}
			
			LOG.info("End Update Matches {} / {}", pageable.getPageNumber() + 1, matches.getTotalPages());
			
			pageable = matches.nextPageable();
		}
		
		LOG.info("End Update");
	}

	@Transactional
	private void createNewMatches(PerformanceLog performanceLog, MatchCache matchCache)
	{
		final int infoStep = 100;
		final List<Match> collectedMatches = new ArrayList<>(BATCH_SIZE);
		
		for (RecommendationCategory category : RecommendationCategory.values())
		{
			LOG.info("Create Matches for {}", category);
			
			long count = 0;
			for (Long sourceUserId : matchCache.getCachedUserIds())
			{
				if (matchCache.getContent(sourceUserId).hasSubscription(category))
				{
					final LocalDateTime startTime = LocalDateTime.now();
					final Set<Long> matchUsers = matchingRepository.findAllTargetUserMatch(sourceUserId, category);
					final Set<Long> relationshipUsers = matchingRepository.findAllTargetUserRelationship(sourceUserId, category);
					performanceLog.add(LogArea.GET_USERS, Duration.between(startTime, LocalDateTime.now()));
					
					for (Long targetUserId : matchCache.getCachedUserIds())
					{
						if (targetUserId > sourceUserId &&
								matchCache.getContent(targetUserId).hasSubscription(category) &&
								!matchUsers.contains(targetUserId) &&
								!relationshipUsers.contains(targetUserId))
						{
							final Match match = new Match(sourceUserId, targetUserId, category);
							updateMatch(match, performanceLog, matchCache);
							collectMatch(performanceLog, collectedMatches, match);
						}
					}
				}
				
				if (++count % infoStep == 0)
				{
					LOG.info("{} / {} done", count, matchCache.getCachedUserIds().size());
				}
			}
		}
		
		insertMatches(collectedMatches, performanceLog);
	}
	
	private void collectMatch(PerformanceLog performanceLog, List<Match> collectedMatches, Match match)
	{
		if (!Strictness.lastStrictness.equals(match.getStrictness()))
		{
			collectedMatches.add(match);
			if (collectedMatches.size() >= BATCH_SIZE)
			{
				insertMatches(collectedMatches, performanceLog);
				collectedMatches.clear();
			}
		}
	}
	
	private void insertMatches(Collection<Match> matches, PerformanceLog performanceLog)
	{
		final LocalDateTime startInsert = LocalDateTime.now();
		transactionTemplate.execute(status ->
		{
			for (Match match : matches)
			{
				matchingRepository.insertMatch(match.getSourceUserId(), match.getTargetUserId(), match.getNumber(), match.getCategory().name(), match.getStrictness().ordinal());
			}
			return null;
		});
		performanceLog.add(LogArea.SAVE_INSERT, Duration.between(startInsert, LocalDateTime.now()));
	}
	
	private void updateMatch(Match match, MatchingLog matchingLog, MatchCache matchCache)
	{
		final RecommendationCategory category = match.getCategory();
		match.setStrictness(Strictness.firstStrictness);
		match.setNumber(0);
		
		/* check users has answered all required questions */
		if (matchCache.getContent(match.getSourceUserId()).isRequiredAnswersMissing(category) ||
				matchCache.getContent(match.getTargetUserId()).isRequiredAnswersMissing(category))
		{
			matchingLog.requiredMissingAnswers(match, matchCache);
			match.setStrictness(Strictness.lastStrictness);
			return;
		}
		
		Arrays.stream(MatchArea.values()).forEach(area -> updateMatch(match, area, matchingLog, matchCache));
	}
	
	private void updateMatch(Match match, MatchArea matchArea, MatchingLog matchingLog, MatchCache matchCache)
	{
		if (match.getStrictness() == Strictness.lastStrictness) return;
		
		final LocalDateTime startTime = LocalDateTime.now();
		switch (matchArea)
		{
			case REGIONAL:
				matchRegionalRestrictions(match, matchCache, matchingLog);
				break;
			case NUMBER:
				matchNumberQuestions(match, matchCache, matchingLog);
				break;
			case CHOICES:
				matchChoiceQuestions(match, matchCache, matchingLog);
				break;
			case AFFINITY:
				matchAffinityQuestions(match, matchCache, matchingLog);
				break;
			case AGE:
				matchAgeQuestions(match, matchCache, matchingLog);
				break;
			case AVATAR:
				matchAvatarQuestions(match, matchCache, matchingLog);
				break;
		}
		matchingLog.add(matchArea, Duration.between(startTime, LocalDateTime.now()));
		matchingLog.addDebugMessage("strictness " + match.getStrictness() + " after " + matchArea.toString());
	}
	
	/**
	 * match if sourceUser match to targetUser and targetUser match to
	 * sourceUser or for partnership sourceUser match to targetUser and
	 * targetUser is ready to relocate and sourceUser search for relocatable or
	 * targetUser match to sourceUser and sourceUser is ready to relocate and
	 * targetUser search for relocatable
	 */
	private void matchRegionalRestrictions(Match match, MatchCache matchCache, MatchingLog matchingLog)
	{
		final CacheContent sourceContent = matchCache.getContent(match.getSourceUserId());
		final CacheContent targetContent = matchCache.getContent(match.getTargetUserId());
		
		final boolean isRelocatable1 = sourceContent.isRelocatable(match.getCategory()) && targetContent.isSearchRelocatable(match.getCategory());
		final boolean isRelocatable2 = sourceContent.isSearchRelocatable(match.getCategory()) && targetContent.isRelocatable(match.getCategory());
		
		final boolean isRegionalMatch1 = isRegionalMatch(match.getSourceUserId(), match.getTargetUserId(), match.getCategory(), matchCache);
		
		boolean resultMatch = isRegionalMatch1 && isRelocatable1;
		if (!resultMatch && (isRegionalMatch1 || isRelocatable2))
		{
			resultMatch = isRegionalMatch(match.getTargetUserId(), match.getSourceUserId(), match.getCategory(), matchCache);
		}
		
		final Strictness resultStrictness = resultMatch ? Strictness.firstStrictness : Strictness.lastStrictness;
		addResultToMatch(resultStrictness, match);
	}
	
	/**
	 * Regional question match if 1. sourceUser has regional restrictions and
	 * its conditions match 2. sourceUser has proximity restrictions and its
	 * conditions match 3. sourceUser hasn't any restrictions - everybody
	 * matchs
	 */
	private boolean isRegionalMatch(Long sourceUserId, Long targetUserId, RecommendationCategory category, MatchCache matchCache)
	{
		final CacheContent sourceContent = matchCache.getContent(sourceUserId);
		final CacheContent targetContent = matchCache.getContent(targetUserId);
		
		final Set<LocatableEntity> regionSearchRequests = sourceContent.getRegionSearchRequests(category);
		final boolean hasRegionRestriction = !regionSearchRequests.isEmpty();
		
		if (hasRegionRestriction)
		{
			// region match
			if (!Collections.disjoint(targetContent.getAddresses(), regionSearchRequests))
				return true;
		}
		
		final Collection<ProximitySearchRequest> proximitySearchRequests = sourceContent.getProximitySearchRequests(category);
		final boolean hasProximityQuestion = !proximitySearchRequests.isEmpty();
		
		if (hasProximityQuestion)
		{
			// proximity match
			for (ProximitySearchRequest plsr : proximitySearchRequests)
			{
				final Zip sourceZip = plsr.getCenter();
				
				for (Zip targetZip : targetContent.getZips())
				{
					if (!plsr.isRestrictCountry() || sourceZip.getParent().equals(targetZip.getParent()))
					{
						//second check proximity
						final double earthRadius = 6371; //km
						final double dLat = Math.toRadians(targetZip.getLatitude() - sourceZip.getLatitude());
						final double dLng = Math.toRadians(targetZip.getLongitude() - sourceZip.getLongitude());
						final double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLng / 2), 2) * Math.cos(Math.toRadians(sourceZip.getLatitude())) * Math.cos(Math.toRadians(targetZip.getLatitude()));
						final double c = 2 * Math.asin(Math.sqrt(a));
						final double dist = earthRadius * c;
						
						if (dist <= plsr.getDistance().getDistance())
							return true;
					}
				}
			}
		}
		
		// if no region restriction or proximity exists, return true
		// if region or proximity restriction exists and allowed it returned true before
		return (!hasRegionRestriction && !hasProximityQuestion);
	}
	
	// TODO @FH: please write comments for such methods.
	// What are affility questions and why you access _getChoiceAnswers_ here?
	private void matchAffinityQuestions(Match match, MatchCache matchCache, MatchingLog matchingLog)
	{
		final CacheContent sourceContent = matchCache.getContent(match.getSourceUserId());
		final CacheContent targetContent = matchCache.getContent(match.getTargetUserId());
		
		for (AffinityMapping affinityMapping : matchCache.getAffinityMapping(match.getCategory()))
		{
			final int sourceSum = affinityMapping.getQuestions().stream()
					.map(Question::getId)
					.map(sourceContent::getChoiceAnswers)
					.filter(Objects::nonNull)
					.map(value -> value.nextSetBit(0) + 1) // if bitSet is empty -1 + 1 = 0, if min position 0 + 1 = 1
					.filter(value -> value > 0) // was for empty bitSets
					.reduce(0, Integer::sum);
			
			final int targetSum = affinityMapping.getQuestions().stream()
					.map(Question::getId)
					.map(targetContent::getChoiceAnswers)
					.filter(Objects::nonNull)
					.map(value -> value.nextSetBit(0) + 1) // if bitSet is empty -1 + 1 = 0, if min position 0 + 1 = 1
					.filter(value -> value > 0) // was for empty bitSets
					.reduce(0, Integer::sum);
			
			final boolean result = Math.abs(sourceSum - targetSum) <= affinityMapping.getMaxDistance();
			addResultToMatch(result ? Strictness.firstStrictness : Strictness.lastStrictness, match);
			if (match.getStrictness() == Strictness.lastStrictness)
			{
				matchingLog.addDebugMessage("affinity lastStrictness after " + affinityMapping.getNaturalKey());
				return;
			}
		}
	}
	
	private void matchChoiceQuestions(Match match, MatchCache matchCache, MatchingLog matchingLog)
	{
		for (ChoiceQuestionsMapping mapping : matchCache.getChoiceQuestionsMapping(match.getCategory()))
		{
			addResultToMatch(getStrictnessChoiceQuestions(match.getSourceUserId(), match.getTargetUserId(), mapping, matchCache), match);
			if (match.getStrictness() == Strictness.lastStrictness)
			{
				matchingLog.addDebugMessage("from source to target lastStrictness after " + mapping.getNaturalKey() + " with matrix " + mapping.getMatrix().getName());
				return;
			}
			addResultToMatch(getStrictnessChoiceQuestions(match.getTargetUserId(), match.getSourceUserId(), mapping, matchCache), match);
			if (match.getStrictness() == Strictness.lastStrictness)
			{
				matchingLog.addDebugMessage("from target to source lastStrictness after " + mapping.getNaturalKey() + " with matrix " + mapping.getMatrix().getName());
				return;
			}
		}
	}
	
	private Strictness getStrictnessChoiceQuestions(Long sourceUserId, Long targetUserId, ChoiceQuestionsMapping mapping, MatchCache matchCache)
	{
		final CacheContent sourceContent = matchCache.getContent(sourceUserId);
		final CacheContent targetContent = matchCache.getContent(targetUserId);
		
		final BitSet sourceAnswer, targetAnswer;
		
		sourceAnswer = sourceContent.getChoiceAnswers(mapping.getSourceQuestion());
		targetAnswer = targetContent.getChoiceAnswers(mapping.getTargetQuestion());
		
		if (sourceAnswer == null || targetAnswer == null)
			return mapping.getDefaultEmptyStrictness();
		
		return mapping.getMatrix().getMatrixValues().stream()
				.filter(value -> sourceAnswer.get(value.getSourceChoice().getPosition()))
				.filter(value -> targetAnswer.get(value.getTargetChoice().getPosition()))
				.map(MatrixValue::getStrictness)
				.map(Strictness::ordinal)
				.reduce(Math::min)
				.map(value -> Strictness.values()[value])
				.orElse(mapping.getDefaultEmptyStrictness());
	}
	
	private void matchNumberQuestions(Match match, MatchCache matchCache, MatchingLog matchingLog)
	{
		final CacheContent sourceContent = matchCache.getContent(match.getSourceUserId());
		final CacheContent targetContent = matchCache.getContent(match.getTargetUserId());
		
		for (NumberQuestionsMapping mapping : matchCache.getNumberQuestionsMapping(match.getCategory()))
		{
			Integer factAnswer, minAnswer, maxAnswer;
			Strictness result;
			
			factAnswer = sourceContent.getNumberAnswers(mapping.getFactQuestion());
			minAnswer = targetContent.getNumberAnswers(mapping.getMinQuestion());
			maxAnswer = targetContent.getNumberAnswers(mapping.getMaxQuestion());
			
			if (factAnswer == null)
			{
				result = mapping.getDefaultEmptyStrictness();
			}
			else
			{
				result = (minAnswer == null || minAnswer <= factAnswer) && (maxAnswer == null || factAnswer <= maxAnswer) ? Strictness.firstStrictness : Strictness.lastStrictness;
			}
			addResultToMatch(result, match);
			if (match.getStrictness() == Strictness.lastStrictness)
			{
				matchingLog.addDebugMessage("from source (factAnswer) to target lastStrictness after " + mapping.getNaturalKey());
				return;
			}
			
			factAnswer = targetContent.getNumberAnswers(mapping.getFactQuestion());
			minAnswer = sourceContent.getNumberAnswers(mapping.getMinQuestion());
			maxAnswer = sourceContent.getNumberAnswers(mapping.getMaxQuestion());
			
			if (factAnswer == null)
			{
				result = mapping.getDefaultEmptyStrictness();
			}
			else
			{
				result = (minAnswer == null || minAnswer <= factAnswer) && (maxAnswer == null || factAnswer <= maxAnswer) ? Strictness.firstStrictness : Strictness.lastStrictness;
			}
			addResultToMatch(result, match);
			if (match.getStrictness() == Strictness.lastStrictness)
			{
				matchingLog.addDebugMessage("from target (factAnswer) to source lastStrictness after " + mapping.getNaturalKey());
				return;
			}
		}
	}
	
	private void matchAgeQuestions(Match match, MatchCache matchCache, MatchingLog matchingLog)
	{
		final CacheContent sourceContent = matchCache.getContent(match.getSourceUserId());
		final CacheContent targetContent = matchCache.getContent(match.getTargetUserId());
		
		for (AgeQuestionMapping ageQuestionMapping : matchCache.getAgeQuestionsMapping(match.getCategory()))
		{
			Integer minAnswer, maxAnswer, age;
			boolean result;
			
			age = sourceContent.getAge();
			minAnswer = targetContent.getNumberAnswers(ageQuestionMapping.getMinAgeQuestion());
			maxAnswer = targetContent.getNumberAnswers(ageQuestionMapping.getMaxAgeQuestion());
			
			result = (minAnswer == null || minAnswer <= age) && (maxAnswer == null || age <= maxAnswer);
			addResultToMatch(result ? Strictness.firstStrictness : Strictness.lastStrictness, match);
			if (match.getStrictness() == Strictness.lastStrictness) return;
			
			age = targetContent.getAge();
			minAnswer = sourceContent.getNumberAnswers(ageQuestionMapping.getMinAgeQuestion());
			maxAnswer = sourceContent.getNumberAnswers(ageQuestionMapping.getMaxAgeQuestion());
			
			result = (minAnswer == null || minAnswer <= age) && (maxAnswer == null || age <= maxAnswer);
			addResultToMatch(result ? Strictness.firstStrictness : Strictness.lastStrictness, match);
			if (match.getStrictness() == Strictness.lastStrictness) return;
		}
	}
	
	private void matchAvatarQuestions(Match match, MatchCache matchCache, MatchingLog matchingLog)
	{
		final CacheContent sourceContent = matchCache.getContent(match.getSourceUserId());
		final CacheContent targetContent = matchCache.getContent(match.getTargetUserId());
		
		for (AvatarQuestionMapping avatarQuestionMapping : matchCache.getAvatarQuestionsMapping(match.getCategory()))
		{
			BitSet answer;
			boolean onlyWithAvatar, result;
			
			answer = sourceContent.getChoiceAnswers(avatarQuestionMapping.getAvatarQuestion());
			onlyWithAvatar = answer != null && answer.get(avatarQuestionMapping.getTrueChoice().getPosition());
			result = !onlyWithAvatar || targetContent.hasAvatar(match.getCategory());
			
			addResultToMatch(result ? Strictness.firstStrictness : Strictness.lastStrictness, match);
			if (match.getStrictness() == Strictness.lastStrictness) return;
			
			answer = targetContent.getChoiceAnswers(avatarQuestionMapping.getAvatarQuestion());
			onlyWithAvatar = answer != null && answer.get(avatarQuestionMapping.getTrueChoice().getPosition());
			result = !onlyWithAvatar || sourceContent.hasAvatar(match.getCategory());
			
			addResultToMatch(result ? Strictness.firstStrictness : Strictness.lastStrictness, match);
			if (match.getStrictness() == Strictness.lastStrictness) return;
		}
	}
	
	private void addResultToMatch(Strictness result, Match match)
	{
		if (result == match.getStrictness())
		{
			match.setNumber(match.getNumber() + 1);
		}
		else if (result.ordinal() > match.getStrictness().ordinal())
		{
			match.setStrictness(result);
			match.setNumber(1);
		}
	}
}
