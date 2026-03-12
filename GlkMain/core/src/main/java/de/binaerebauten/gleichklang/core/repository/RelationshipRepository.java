package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Long>, JpaSpecificationExecutor<Relationship>, DeleteRepository<Relationship, Long>
{
	@Query("FROM Relationship r WHERE r.sourceUser = ?1 AND deleted = false AND r.targetUser.blockedStatus <> 'ADMIN_BLOCKED' ORDER BY r.targetUser.alias")
	List<Relationship> findAllRelationshipsForUser(User user);

	@Query("FROM Relationship r WHERE r.sourceUser = ?1 AND r.deleted = false AND r.viewed = false ORDER BY r.targetUser.alias")
	List<Relationship> findAllUnviewedRelationshipsForUser(User user);

	@Query("FROM Relationship r WHERE r.sourceUser = ?1 AND r.deleted = false AND r.notified = false ORDER BY r.targetUser.alias")
	List<Relationship> findAllNotNotifiedRelationshipsForUser(User user);

	@Query("FROM Relationship r WHERE r.targetUser = ?1 AND r.deleted = false AND r.footprintNotified = false AND r.footprintViewed = false AND r.sourceUser.isBlocked = false AND r.targetUser.memberStatus='REGISTERED' ORDER BY r.sourceUser.alias")
	List<Relationship> findAllNotNotifiedNewFootprintsForUser(User user);
	
	@Query("SELECT DISTINCT u FROM Relationship r LEFT JOIN r.targetUser u LEFT JOIN u.userSettings us WHERE r.deleted = false AND r.footprintNotified = false AND r.footprintViewed = false AND us.disableFootprintNotifications = false AND r.targetUser.memberStatus='REGISTERED'")
	List<User> findAllUserWithNotNotifiedNewFootprints();

	@EntityGraph("RelationshipLazy")
	Relationship findRelationshipBySourceUserIdAndTargetUserId(Long sourceUserId, Long targetUserId);
	
	Relationship findRelationshipBySourceUserAndTargetUser(User sourceUser, User targetUser);
	
	@Query("FROM Relationship r WHERE r.targetUser = ?1 AND deleted = false AND footprintViewed = false and r.sourceUser.isBlocked = false and r.targetUser.isBlocked = false AND footprint IS NOT NULL")
	List<Relationship> findAllRelationshipsWithNewFootprint(User currentUser);

	@Query("SELECT COUNT(*) FROM Relationship WHERE sourceUser = ?1 AND ?2 MEMBER OF categories")
	Long countBySourceUserAndCategory(User user, RecommendationCategory category);

	@Query("SELECT COUNT(*) FROM Relationship WHERE sourceUser = ?1 AND targetUser.memberStatus = 'REGISTERED' AND ?2 MEMBER OF categories AND ?2 MEMBER OF targetUser.categories AND viewed = false AND deleted = false")
	Long countUnviewedBySourceUserAndCategory(User user, RecommendationCategory category);

	//@Query("SELECT COUNT(*) FROM Relationship WHERE sourceUser = ?1 AND targetUser.memberStatus = 'REGISTERED' AND viewed = false AND deleted = false and targetUser.isBlocked=false")
	@Query(value = "SELECT count(distinct r.id) FROM relationship r " +
			"INNER JOIN user_ us ON r.source_user_id=us.id " +
			"INNER JOIN user_ ut ON r.target_user_id=ut.id " +
			"INNER JOIN user_recommendation_category urct ON r.target_user_id = urct.user_id " +
			"INNER JOIN user_recommendation_category urcs ON r.source_user_id = urcs.user_id " +
			"INNER JOIN relationship_category rc on rc.relationship_id=r.id  AND r.source_user_id=urcs.user_id " +
			"AND urcs.categories = rc.category " +
			"AND urct.categories = rc.category " +
			"AND r.source_user_id = ?1 AND us.member_status = 'REGISTERED' AND ut.member_status = 'REGISTERED' AND r.viewed = 0  AND r.deleted = 0 AND ut.isBlocked = 0;",
			nativeQuery = true)
	Long countUnviewedBySourceUser(User user);

	@Query("SELECT COUNT(*) FROM Relationship WHERE sourceUserId = :user AND :category MEMBER OF categories AND createDate > :startDate")
	Long countRelationshipsSinceDate(@Param("user") Long userId, @Param("category") RecommendationCategory category, @Param("startDate") LocalDateTime startDate);
	
	@Query("SELECT COUNT(*) > 0 FROM Relationship WHERE sourceUserId = :user AND :category MEMBER OF categories")
	Boolean hasRelationships(@Param("user") Long userId, @Param("category") RecommendationCategory category);
	
	Long countBySourceUser(User user);
	
	Long countBySourceUserAndViewedTrue(User user);

	long countByCreateDateBetween(LocalDateTime startDate, LocalDateTime endDate);

	@Query("SELECT COUNT(DISTINCT r.sourceUserId) FROM Relationship r WHERE r.createDate BETWEEN ?1 AND ?2")
	long countAffectedUsers(LocalDateTime startDate, LocalDateTime endDate);

    // TODO Changes for Task 3

    // Query to check how many profiles has the current logged in user seen
    @Query("SELECT count(*) FROM Relationship r WHERE sourceUser = :user AND :category MEMBER OF categories AND viewed = true AND deleted = false")
    Long findProfileViewsCountByUser(@Param("user") User user, @Param("category") RecommendationCategory category);

    // Query to check how many users have seen the profile of current logged in user
    @Query("SELECT count(*) FROM Relationship r WHERE targetUser = :user AND :category MEMBER OF categories AND viewed = true AND deleted = false")
    Long findProfileViewsCountOfUser(@Param("user") User user, @Param("category") RecommendationCategory category);

    @Query("SELECT count(*) From Relationship r WHERE r.sourceUser = ?1 AND r.viewed = true AND r.lastViewedDate > ?2")
	Long findNumberOfViewedSuggestions(User user, LocalDateTime localDateTime);

//	@Transactional
//	@Modifying
//	@Query("UPDATE Relationship r  SET r.deleteDate = ?1 where r = ?2)")
//	public void deletedDate(LocalDateTime date, Relationship relationship);


}
