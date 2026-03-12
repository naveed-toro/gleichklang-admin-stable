package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RelationshipRepositoryTest extends AbstractRepositoryTest<Relationship>
{
	@Autowired
	private RelationshipRepository relationshipRepository;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	private User sourceUser;
	private User targetUser;
	private RecommendationCategory category;

	public RelationshipRepositoryTest()
	{
	}

	@Override
	protected Collection<Relationship> getPersistedEntities()
	{
		sourceUser = entityFactory.persistDefaultUser("source");
		targetUser = entityFactory.persistDefaultUser("target");
		category = RecommendationCategory.FRIENDSHIP;

		return Collections.singletonList(entityFactory.persistDefaultRelationship(sourceUser, targetUser, category));
	}

	@Override
	protected JpaRepository<Relationship, Long> getRepository()
	{
		return relationshipRepository;
	}

	@Test
	public void testFindAllRelationshipsForUser()
	{
		final List<Relationship> relationships = relationshipRepository.findAllRelationshipsForUser(sourceUser);
		assertThat(relationships.size(), is(1));
		
		final Relationship relationship = relationships.iterator().next();
		assertThat(relationship.isDeleted(), equalTo(false));
		
		relationship.setDeleted(true);
		relationshipRepository.save(relationship);
		assertThat(relationshipRepository.findAllRelationshipsForUser(sourceUser).size(), is(0));
	}

	@Test
	public void testCountRelationshipByUser()
	{
		final RecommendationCategory categoryFail = Arrays.stream(RecommendationCategory.values()).filter(value -> !value.equals(category)).findFirst().get();

		final Long count1 = relationshipRepository.countBySourceUserAndCategory(sourceUser, category);
		assertThat(count1, equalTo(1L));

		final Long count2 = relationshipRepository.countBySourceUserAndCategory(sourceUser, categoryFail);
		assertThat(count2, equalTo(0L));
	}
	
	@Test
	public void testCountRelationshipsSinceDate()
	{
		final LocalDateTime successDate = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
		final LocalDateTime failDate = LocalDateTime.now();
		
		final Long count1 = relationshipRepository.countRelationshipsSinceDate(sourceUser.getId(), category, successDate);
		assertThat(count1, equalTo(1L));
		
		final Long count2 = relationshipRepository.countRelationshipsSinceDate(sourceUser.getId(), category, failDate);
		assertThat(count2, equalTo(0L));
	}
	
	@Test
	public void testHasRelationships()
	{
		final RecommendationCategory categoryFail = Arrays.stream(RecommendationCategory.values()).filter(value -> !value.equals(category)).findFirst().get();
		
		final Boolean count1 = relationshipRepository.hasRelationships(sourceUser.getId(), category);
		assertThat(count1, equalTo(true));
		
		final Boolean count2 = relationshipRepository.hasRelationships(sourceUser.getId(), categoryFail);
		assertThat(count2, equalTo(false));
	}
	
	@Test
	public void testFindAllNotNotifiedRelationshipsForUser()
	{
		final Relationship relationship = relationshipRepository.findAll().iterator().next();
		
		final List<Relationship> relationships = relationshipRepository.findAllNotNotifiedRelationshipsForUser(sourceUser);
		assertThat(relationships.size(), equalTo(1));
		assertThat(relationships.iterator().next(), equalTo(relationship));
		
		relationship.setNotified(true);
		relationshipRepository.save(relationship);
		
		assertThat(relationshipRepository.findAllNotNotifiedRelationshipsForUser(sourceUser).size(), equalTo(0));
	}
	
	@Test
	public void testFindAllNotNotifiedNewFootprintsForUser()
	{
		final Relationship relationship = relationshipRepository.findAll().iterator().next();
		
		assertThat(relationshipRepository.findAllNotNotifiedNewFootprintsForUser(targetUser).size(), equalTo(0));
		
		relationship.setFootprintNotified(false);
		relationship.setFootprintViewed(false);
		relationshipRepository.save(relationship);
		
		final List<Relationship> relationships = relationshipRepository.findAllNotNotifiedNewFootprintsForUser(targetUser);
		assertThat(relationships.size(), equalTo(1));
		assertThat(relationships.iterator().next(), equalTo(relationship));
	}
	
	@Test
	public void testFindAllUserWithNotNotifiedNewFootprints()
	{
		final Relationship relationship = relationshipRepository.findAll().iterator().next();
		
		assertThat(relationshipRepository.findAllUserWithNotNotifiedNewFootprints().size(), equalTo(0));
		
		relationship.setFootprintNotified(false);
		relationship.setFootprintViewed(false);
		relationshipRepository.save(relationship);
		
		final List<User> users = relationshipRepository.findAllUserWithNotNotifiedNewFootprints();
		assertThat(users.size(), equalTo(1));
		assertThat(users.iterator().next(), equalTo(targetUser));
	}
	
	@Test
	public void testFindBySourceUserAndViewedTrue()
	{
		final Relationship relationship = relationshipRepository.findAll().iterator().next();
		
		assertThat(relationshipRepository.countBySourceUserAndViewedTrue(sourceUser), equalTo(0L));
		
		relationship.setViewed(true);
		relationshipRepository.save(relationship);
		
		assertThat(relationshipRepository.countBySourceUserAndViewedTrue(sourceUser), equalTo(1L));
	}
	
	@Test
	public void testCountByCreateDateBetween()
	{
		final LocalDateTime now = LocalDateTime.now();
		assertThat(relationshipRepository.countByCreateDateBetween(now.minusDays(1), now.plusDays(1)), equalTo(1L));
		assertThat(relationshipRepository.countByCreateDateBetween(now.minusDays(2), now.minusDays(1)), equalTo(0L));
		assertThat(relationshipRepository.countByCreateDateBetween(now.plusDays(1), now.plusDays(2)), equalTo(0L));
	}
	
	@Test
	public void testCountAffectedUsers()
	{
		final LocalDateTime now = LocalDateTime.now();
		assertThat(relationshipRepository.countAffectedUsers(now.minusDays(1), now.plusDays(1)), equalTo(1L));
		assertThat(relationshipRepository.countAffectedUsers(now.minusDays(2), now.minusDays(1)), equalTo(0L));
		assertThat(relationshipRepository.countAffectedUsers(now.plusDays(1), now.plusDays(2)), equalTo(0L));
		
		entityFactory.persistDefaultRelationship(sourceUser, entityFactory.persistDefaultUser("test1"), RecommendationCategory.PARTNERSHIP);
		assertThat(relationshipRepository.countAffectedUsers(now.minusDays(1), now.plusDays(1)), equalTo(1L));
		
		entityFactory.persistDefaultRelationship(entityFactory.persistDefaultUser("test2"), entityFactory.persistDefaultUser("test3"), RecommendationCategory.PARTNERSHIP);
		assertThat(relationshipRepository.countAffectedUsers(now.minusDays(1), now.plusDays(1)), equalTo(2L));
	}
	
	//@Test
	public void testCountUnviewedBySourceUserAndCategory()
	{
		final Relationship relationship = relationshipRepository.findAll().iterator().next();
		relationship.setViewed(true);
		relationshipRepository.save(relationship);
		
		assertThat(relationshipRepository.countUnviewedBySourceUserAndCategory(sourceUser, RecommendationCategory.FRIENDSHIP), equalTo(0L));
		assertThat(relationshipRepository.countUnviewedBySourceUserAndCategory(sourceUser, RecommendationCategory.PARTNERSHIP), equalTo(0L));
		
		relationship.setViewed(false);
		relationshipRepository.save(relationship);
		
		assertThat(relationshipRepository.countUnviewedBySourceUserAndCategory(sourceUser, RecommendationCategory.FRIENDSHIP), equalTo(1L));
		
		targetUser.setMemberStatus(MemberStatus.CANCELED);
		userRepository.save(targetUser);
		
		assertThat(relationshipRepository.countUnviewedBySourceUserAndCategory(sourceUser, RecommendationCategory.FRIENDSHIP), equalTo(0L));
	}
}
