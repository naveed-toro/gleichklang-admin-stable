package de.binaerebauten.gleichklang.core.repository.matching;

import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface MatchingRepository extends JpaRepository<Match, Long>
{
	@Query("SELECT match.targetUserId FROM Match match WHERE "
			+ "match.sourceUserId = ?1 AND "
			+ "match.category = ?2")
	Set<Long> findAllTargetUserMatch(Long sourceUserId, RecommendationCategory category);
	
	@Query("SELECT r.targetUser.id FROM Relationship r WHERE "
			+ "r.sourceUser.id = ?1 AND "
			+ "?2 MEMBER OF r.categories")
	Set<Long> findAllTargetUserRelationship(Long sourceUserId, RecommendationCategory category);
	
	@Query("SELECT user.id FROM User user "
			+ "LEFT JOIN user.subscriptions s WITH s.current = TRUE AND :date BETWEEN s.begin AND s.end "
			+ "WHERE user.email IS NOT NULL AND s IS NOT NULL "
			+ "ORDER BY user.id")
	List<Long> findAllMatchUser(@Param("date") LocalDateTime date);
	
	@Query("SELECT user.id FROM User user "
			+ "LEFT JOIN user.subscriptions s WITH s.current = TRUE AND :date BETWEEN s.begin AND s.end "
			+ "WHERE user.email IS NOT NULL AND s IS NOT NULL AND user.id IN :users "
			+ "ORDER BY user.id")
	List<Long> findAllMatchUser(@Param("date") LocalDateTime date, @Param("users") Collection<Long> users);
	
	/**
	 * Returns matches with inverse user ids order.
	 *
	 * @param pageable
	 * @return
	 */
	@Query("SELECT match.id FROM Match AS match WHERE match.sourceUserId > match.targetUserId")
	Page<Long> findAllCorruptedMatches(Pageable pageable);
	
	/**
	 * Returns matches for:
	 *  - not relevant recommendation categories
	 *  - strictness 4 or more
	 *  - deleted users
	 * 	- users without active subscription
	 *
	 * @param date
	 * @param pageable
	 * @return
	 */
	@Query("SELECT DISTINCT m.id "
			+ "FROM Match m "
			+ "  JOIN m.sourceUser u1 "
			+ "  LEFT JOIN u1.subscriptions s1 WITH s1.current = TRUE AND :date BETWEEN s1.begin AND s1.end "
			+ "  JOIN m.targetUser u2 "
			+ "  LEFT JOIN u2.subscriptions s2 WITH s2.current = TRUE AND :date BETWEEN s2.begin AND s2.end "
			+ "WHERE "
			+ "  m.category NOT MEMBER OF u1.categories OR "
			+ "  m.category NOT MEMBER OF u2.categories OR "
			+ "  m.strictness >= 4 OR "
			+ "  u1.email IS NULL OR "
			+ "  u2.email IS NULL OR "
			+ "  s1 IS NULL OR "
			+ "  s2 IS NULL")
	Page<Long> findAllUnnecessaryMatches(@Param("date") LocalDateTime date, Pageable pageable);
	
	@Query(value = "SELECT answer FROM NumberAnswer answer WHERE "
			+ "answer.user.id = ?1 AND "
			+ "answer.question IN ?2")
	Set<NumberAnswer> getNumberAnswers(Long userId, Set<NumberQuestion> numberQuestions);
	
	@Query(value = "SELECT answer FROM ChoiceAnswer answer WHERE "
			+ "answer.user.id = ?1 AND "
			+ "answer.question IN ?2")
	Set<ChoiceAnswer> getChoiceAnswers(Long userId, Set<ChoiceQuestion> choiceQuestions);
	
	@Query(value = "SELECT mapping FROM NumberQuestionsMapping mapping "
			+ "LEFT JOIN mapping.factQuestion.questionGroup.questionnaire factQuestionnaire "
			+ "LEFT JOIN mapping.minQuestion.questionGroup.questionnaire minQuestionnaire "
			+ "LEFT JOIN mapping.maxQuestion.questionGroup.questionnaire maxQuestionnaire WHERE "
			+ "(factQuestionnaire.recommendationCategory = ?1 OR factQuestionnaire.recommendationCategory IS NULL) AND "
			+ "(minQuestionnaire.recommendationCategory = ?1 OR minQuestionnaire.recommendationCategory IS NULL) AND "
			+ "(maxQuestionnaire.recommendationCategory = ?1 OR maxQuestionnaire.recommendationCategory IS NULL) AND "
			+ "(minQuestionnaire.recommendationCategory = maxQuestionnaire.recommendationCategory) AND "
			+ "(factQuestionnaire.recommendationCategory = ?1 OR minQuestionnaire.recommendationCategory = ?1)")
	Set<NumberQuestionsMapping> getNumberQuestionMappings(RecommendationCategory category);
	
	@Query(value = "SELECT mapping FROM ChoiceQuestionsMapping mapping "
			+ "LEFT JOIN mapping.sourceQuestion.questionGroup.questionnaire sourceQuestionnaire "
			+ "LEFT JOIN mapping.targetQuestion.questionGroup.questionnaire targetQuestionnaire WHERE "
			+ "(sourceQuestionnaire.recommendationCategory = ?1 OR sourceQuestionnaire.recommendationCategory IS NULL) AND "
			+ "(targetQuestionnaire.recommendationCategory = ?1 OR targetQuestionnaire.recommendationCategory IS NULL) AND "
			+ "(sourceQuestionnaire.recommendationCategory = ?1 OR targetQuestionnaire.recommendationCategory = ?1)")
	Set<ChoiceQuestionsMapping> getChoiceQuestionMappings(RecommendationCategory category);
	
	@Query(value = "SELECT mapping FROM AffinityMapping mapping, ChoiceQuestion choiceQuestion WHERE "
			+ "choiceQuestion MEMBER OF mapping.questions AND "
			+ "(choiceQuestion.questionGroup.questionnaire.recommendationCategory = ?1 OR choiceQuestion.questionGroup.questionnaire.recommendationCategory IS NULL) AND "
			+ "1 >= (SELECT COUNT(DISTINCT question.questionGroup.questionnaire.recommendationCategory) FROM ChoiceQuestion question WHERE question MEMBER OF mapping.questions)")
	Set<AffinityMapping> getAffinityMappings(RecommendationCategory category);
	
	@Query(value = "SELECT mapping FROM AvatarQuestionMapping mapping "
			+ "LEFT JOIN mapping.avatarQuestion.questionGroup.questionnaire avatarQuestionnaire WHERE "
			+ "avatarQuestionnaire.recommendationCategory = ?1 AND "
			+ "mapping.trueChoice IS NOT NULL")
	Set<AvatarQuestionMapping> getAvatarQuestionMappings(RecommendationCategory category);
	
	@Query("SELECT mapping FROM AgeQuestionMapping mapping "
			+ "LEFT JOIN mapping.minAgeQuestion.questionGroup.questionnaire minQuestionnaire "
			+ "LEFT JOIN mapping.maxAgeQuestion.questionGroup.questionnaire maxQuestionnaire WHERE "
			+ "minQuestionnaire.recommendationCategory = ?1 AND "
			+ "maxQuestionnaire.recommendationCategory = ?1")
	Set<AgeQuestionMapping> getAgeQuestionMappings(RecommendationCategory category);
	
	@Modifying
	@Transactional
	@Query("UPDATE Match m SET m.number = :count, m.strictness = :strictness, m.changeDate = CURRENT_DATE WHERE m.id = :id")
	void updateMatch(@Param("count") int count, @Param("strictness") Strictness strictness, @Param("id") long id);
	
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO match_ (source_user_id, target_user_id, count, category, strictness, create_date, change_date ) "
			+ "VALUES(?1, ?2, ?3, ?4, ?5, CURRENT_DATE, CURRENT_DATE)",
			nativeQuery = true)
	void insertMatch(Long sourceUserId, Long targetUserId, int count, String category, int strictness);
	
	@Query("SELECT answer FROM RegionAnswer answer "
			+ "LEFT JOIN answer.question question "
			+ "LEFT JOIN question.questionGroup questionGroup "
			+ "LEFT JOIN questionGroup.questionnaire questionnaire WHERE "
			+ "answer.user.id = ?1 AND "
			+ "questionnaire.recommendationCategory = ?2 AND "
			+ "question.deleted = false AND "
			+ "questionGroup.deleted = false AND "
			+ "questionnaire.deleted = false")
	RegionAnswer getRegionAnswer(Long userId, RecommendationCategory category);
	
	@Query("SELECT COUNT(avatar) > 0 FROM Avatar avatar WHERE avatar.user.id = ?1 AND avatar.category = ?2")
	Boolean isWithAvatar(Long userId, RecommendationCategory category);
}
