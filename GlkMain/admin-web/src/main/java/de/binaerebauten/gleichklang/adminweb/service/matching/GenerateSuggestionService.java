package de.binaerebauten.gleichklang.adminweb.service.matching;

import de.binaerebauten.gleichklang.core.model.matching.Match;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic.MatchingScope;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatisticEntry.LogArea;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog.UserActivity;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchStatisticRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.UserActivityService;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate.NEW_MATCH;

@Service
public class GenerateSuggestionService
{
	private static class MatchWithCount
	{
		private final Match match;
		private final boolean matchCount;

		private MatchWithCount(Match match, boolean matchCount)
		{
			this.match = match;
			this.matchCount = matchCount;
		}
	}
	
	public static final Duration CHECK_PERIOD = Duration.of(2, ChronoUnit.DAYS);
	public static final int MIN_SUGGESTIONS = 1;
	public static final int MIN_SUGGESTIONS_INITIAL = 5;
	
	private static final Logger LOG = LoggerFactory.getLogger(GenerateSuggestionService.class);

	private final RelationshipRepository relationshipRepository;
	private final MatchRepository matchRepository;
	private final MatchStatisticRepository matchStatisticRepository;
	private final UserActivityService userActivityService;
	private final MailQueueService mailQueueService;
	private final TransactionTemplate transactionTemplate;
	private final UserRepository userRepository;

	/**
	 * Constructor init for test purposes.
	 * @param relationshipRepository
	 * @param userActivityService
	 * @param transactionTemplate
	 * @param matchRepository
	 * @param mailQueueService
	 */
	@Autowired
	public GenerateSuggestionService(RelationshipRepository relationshipRepository,
			MatchStatisticRepository matchStatisticRepository,
			UserActivityService userActivityService, TransactionTemplate transactionTemplate,
			MatchRepository matchRepository,
			MailQueueService mailQueueService, UserRepository userRepository)
	{
		this.relationshipRepository = relationshipRepository;
		this.matchStatisticRepository = matchStatisticRepository;
		this.userActivityService = userActivityService;
		this.transactionTemplate = transactionTemplate;
		this.matchRepository = matchRepository;
		this.mailQueueService = mailQueueService;
		this.userRepository  = userRepository;
	}

	public void generateSuggestion()
	{
		final PerformanceLog performanceLog = new PerformanceLog(matchStatisticRepository, MatchingScope.SUGGESTION);
		final int infoStep = 100;
		
		for (RecommendationCategory category : RecommendationCategory.values())
		{
			LOG.info("Create Relationships for {}", category);
			
			long count = 0;
			
			final Map<Long, Set<MatchWithCount>> userRelations;
			final Map<Long, Set<Relationship>> newRelationships;
			
			final LocalDateTime getUserStartTime = LocalDateTime.now();
			{
				// block for garbage collecting usersForRelationship
				
				final List<Long> usersForRelationship = matchRepository.findUsersForRelationship(category);
				userRelations = usersForRelationship.stream().collect(Collectors.toMap(user1 -> user1, user -> new HashSet<MatchWithCount>()));
				newRelationships = usersForRelationship.stream().collect(Collectors.toMap(user1 -> user1, user -> new HashSet<Relationship>()));
			}
			performanceLog.add(LogArea.GET_USERS, Duration.between(getUserStartTime, LocalDateTime.now()));
			
			for (Long userId : userRelations.keySet()) {
				User user = userRepository.findById(userId);
				if (!user.isBlocked()) {
					transactionTemplate.execute(transactionStatus ->
					{
						final boolean initial = isInitial(userId, category);
						int hit = (int) getRelationshipsSinceCheckPeriod(userId, category);
						final LocalDateTime getMatchesStartTime = LocalDateTime.now();
						final List<Match> matches = matchRepository.findMatchesForUser(userId, Strictness.lastStrictness, category);
						performanceLog.add(LogArea.GET_MATCHES, Duration.between(getMatchesStartTime, LocalDateTime.now()));

						for (Match match : matches) {
							final Long targetUserId = match.getSourceUserId().equals(userId) ? match.getTargetUserId() : match.getSourceUserId();
							if (newRelationships.containsKey(targetUserId)) {
								hit += removeBetterMatches(match, userRelations.get(userId));

								if (match.getStrictness() != Strictness.firstStrictness && (hit >= MIN_SUGGESTIONS_INITIAL || !initial && hit >= MIN_SUGGESTIONS))
									break;

								final LocalDateTime createStartTime = LocalDateTime.now();
								if (createRelationship(userId, targetUserId, match.getCategory(), newRelationships.get(userId)))
									hit++;

								final boolean matchCount = createRelationship(targetUserId, userId, match.getCategory(), newRelationships.get(targetUserId));
								userRelations.get(targetUserId).add(new MatchWithCount(match, matchCount));
								performanceLog.add(LogArea.SAVE_RELATIONSHIP, Duration.between(createStartTime, LocalDateTime.now()));

								final LocalDateTime deleteStartTime = LocalDateTime.now();
								matchRepository.delete(match);
								performanceLog.add(LogArea.DELETE_MATCH, Duration.between(deleteStartTime, LocalDateTime.now()));
							}
						}
						userRelations.get(userId).clear();
						return null;
					});

					if (++count % infoStep == 0) {
						LOG.info("{} / {} done", count, userRelations.size());
					}
				}
			}
			LOG.info("Sending Mails for {}", category);
			final LocalDateTime sendMailStartTime = LocalDateTime.now();

			/*Iterator<Long> it1 = newRelationships.keySet().iterator();

			while (it1.hasNext()) {
				Long key = it1.next();
				User user = userRepository.findById(key);
				if(user.isBlocked()){
					newRelationships.remove(key);
				}
			} */


			newRelationships.keySet().stream().filter(userId -> !newRelationships.get(userId).isEmpty()).forEach(userId -> mailQueueService.enqueue(userId, NEW_MATCH));
			performanceLog.add(LogArea.SEND_MAILS, Duration.between(sendMailStartTime, LocalDateTime.now()));
		}
		
		performanceLog.finish();
	}
	
	public long getRelationshipsSinceCheckPeriod(Long userId, RecommendationCategory category)
	{
		return relationshipRepository.countRelationshipsSinceDate(userId, category, LocalDateTime.now().minus(CHECK_PERIOD));
	}
	
	public boolean isInitial(Long userId, RecommendationCategory category)
	{
		return !relationshipRepository.hasRelationships(userId, category);
	}
	
	/**
	 * Compare a match to existing matches. The existing matches are from
	 * reciprocity user. All better matches will be deleted from the set.
	 *
	 * @param compareMatch the new match
	 * @param fromMatches  existing matches
	 * @return number of better counting matches
	 */
	private int removeBetterMatches(Match compareMatch, Set<MatchWithCount> fromMatches)
	{
		int hit = 0;
		final Set<MatchWithCount> deleteList = new HashSet<>();
		for (MatchWithCount existingMatch : fromMatches)
		{
			if (existingMatch.match.getStrictness() == Strictness.firstStrictness ||
					existingMatch.match.getStrictness().ordinal() <= compareMatch.getStrictness().ordinal() &&
							existingMatch.match.getNumber() <= compareMatch.getNumber())
			{
				if (existingMatch.matchCount) hit++;
				deleteList.add(existingMatch);
			}
		}
		fromMatches.removeAll(deleteList);
		return hit;
	}

	/**
	 * Create the corresponding relationship for a match. The relationship will
	 * be persisted and the match will be deleted. If a relationship already
	 * exist the category will be added to the relationship.
	 *
	 * @return true, if the new relationship count as new suggestion, otherwise
	 * false
	 */
	private boolean createRelationship(Long sourceUserId, Long targetUserId, RecommendationCategory category, Set<Relationship> countRelationships)
	{
		Relationship relationship = relationshipRepository.findRelationshipBySourceUserIdAndTargetUserId(sourceUserId, targetUserId);
		if (relationship == null)
		{
			relationship = new Relationship();
			relationship.setSourceUserId(sourceUserId);
			relationship.setTargetUserId(targetUserId);
			relationship.setAffiliation(Affiliation.NEUTRAL);
		}
		relationship.setViewed(false);

		final boolean greaterExist = relationship.getCategories().stream().anyMatch(value -> value.ordinal() > category.ordinal());
		final boolean alreadyExist = !relationship.getCategories().add(category);

		relationshipRepository.save(relationship);

		final boolean count = !relationship.isDeleted() && !greaterExist && !alreadyExist;

		if (count)
		{
			userActivityService.createActivity(sourceUserId, UserActivity.NEW_MATCH, category);
			countRelationships.add(relationship);
		}

		return count;
	}

	public void createRelationship(Relationship relationship) throws ValidationException
	{
		if (relationship == null) return;

		final Relationship inverseRelationship = new Relationship();
		inverseRelationship.setAffiliation(relationship.getAffiliation());
		inverseRelationship.getCategories().addAll(relationship.getCategories());
		inverseRelationship.setDeleted(relationship.isDeleted());
		inverseRelationship.setMemo(relationship.getMemo());
		inverseRelationship.setViewed(relationship.isViewed());
		inverseRelationship.setSourceUserId(relationship.getTargetUserId());
		inverseRelationship.setTargetUserId(relationship.getSourceUserId());

		try
		{
			transactionTemplate.execute(transactionStatus -> relationshipRepository.save(Arrays.asList(relationship, inverseRelationship)));
		}
		catch (NonTransientDataAccessException e)
		{
			throw new ValidationException(I18N.GENERATESUGGESTIONSERVICE_VALIDATION_SAVEERROR.msg());
		}
	}
	
	public long countRelationships(LocalDateTime startDate, LocalDateTime endDate)
	{
		if(startDate == null || endDate == null) return 0;
		return relationshipRepository.countByCreateDateBetween(startDate, endDate);
	}
	
	public long countAffectedUsers(LocalDateTime startDate, LocalDateTime endDate)
	{
		if(startDate == null || endDate == null) return 0;
		return relationshipRepository.countAffectedUsers(startDate, endDate);
	}
}
