package de.binaerebauten.gleichklang.core.repository.matching;

import de.binaerebauten.gleichklang.core.model.matching.Match;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match>
{
	@Query("FROM Match match WHERE "
			+ "(match.sourceUserId = ?1 AND match.targetUserId = ?2 OR "
			+ "match.sourceUserId = ?2 AND match.targetUserId = ?1) "
			+ "AND match.category = ?3")
	Match findBySourceUserAndTargetUserAndCategory(Long sourceUserId, Long targetUserId, RecommendationCategory category);

	@Query("SELECT DISTINCT user.id FROM User user WHERE "
			+ " user.isBlocked <> TRUE AND (user.id IN (SELECT DISTINCT match.sourceUserId FROM Match match WHERE match.category = ?1) OR "
			+ "user.id IN (SELECT DISTINCT match.targetUserId FROM Match match WHERE match.category = ?1)) AND "
			+ "?1 MEMBER OF user.categories AND "
			+ "NOT EXISTS (FROM RecommendationBreak rb WHERE rb.category = ?1 AND rb.user = user AND (rb.endDate IS NULL OR rb.endDate >= CURRENT_DATE))"
			+ "ORDER BY user.id")
	List<Long> findUsersForRelationship(RecommendationCategory category);

	@Query("SELECT match FROM Match match WHERE "
			+ "(match.sourceUserId = :userId OR match.targetUserId = :userId) AND "
			+ "match.strictness < :maxStrictness AND "
			+ "match.category = :category AND "
			+ ":category MEMBER OF match.targetUser.categories AND "
			+ ":category MEMBER OF match.sourceUser.categories AND "
			+ "NOT EXISTS (FROM RecommendationBreak rb WHERE rb.category = match.category AND (rb.user.id = match.targetUserId OR rb.user.id = match.sourceUserId) AND (rb.endDate IS NULL OR rb.endDate >= CURRENT_DATE))"
			+ "ORDER BY match.strictness, match.number")
	List<Match> findMatchesForUser(@Param("userId") Long userId, @Param("maxStrictness") Strictness maxStrictness, @Param("category") RecommendationCategory category);
	
	@Query("SELECT "
			+ "COUNT(m1) "
			+ "+"
			+ "(SELECT COUNT(m2) FROM Match m2 WHERE m2.targetUserId = :userId AND m2.strictness = :strictness AND m2.category = :category) "
			+ "FROM Match m1 WHERE m1.sourceUserId = :userId AND m1.strictness = :strictness AND m1.category = :category")
	long countByStrictnessAndCategory(@Param("userId") Long userId, @Param("strictness") Strictness strictness, @Param("category") RecommendationCategory category);
	
	@Transactional
	@Modifying
	void deleteByIdIn(@Param("ids") Iterable<Long> ids);
}
