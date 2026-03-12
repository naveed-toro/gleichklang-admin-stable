package de.binaerebauten.gleichklang.core.repository.matching;

import de.binaerebauten.gleichklang.core.model.matching.Match;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.repository.LocatableRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class MatchRepositoryTest extends AbstractRepositoryTest<Match>
{
	@Autowired
	private MatchRepository matchRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private DefaultEntityFactory entityFactory;
	
	@Autowired
	private LocatableRepository locatableRepository;
	
	private User sourceUser;
	private User targetUser;
	
	public MatchRepositoryTest()
	{
	}
	
	@Override
	protected Collection<Match> getPersistedEntities()
	{
		sourceUser = entityFactory.persistDefaultUser("source", RecommendationCategory.PARTNERSHIP);
		targetUser = entityFactory.persistDefaultUser("target", RecommendationCategory.PARTNERSHIP);
		
		return Collections.singletonList(entityFactory.persistDefaultMatch(sourceUser, targetUser, RecommendationCategory.PARTNERSHIP));
	}
	
	@Override
	protected JpaRepository<Match, Long> getRepository()
	{
		return matchRepository;
	}
	
	@Test
	public void findBySourceUserAndTargetUserTest()
	{
		final Match match = matchRepository.findBySourceUserAndTargetUserAndCategory(sourceUser.getId(), targetUser.getId(), RecommendationCategory.PARTNERSHIP);
		assertThat(match, notNullValue());
		assertThat(match.getSourceUserId(), is(sourceUser.getId()));
		assertThat(match.getTargetUserId(), is(targetUser.getId()));
	}
	
	@Test
	public void findUsersForRelationshipTest()
	{
		entityFactory.reset();
		
		final int additionalUsers = 6;
		for (int i = 0; i < additionalUsers; i++)
			entityFactory.persistDefaultUser("User " + i, RecommendationCategory.PARTNERSHIP);
		
		final List<User> allUser = userRepository.findAll();
		
		final List<Long> emptyResults = matchRepository.findUsersForRelationship(RecommendationCategory.PARTNERSHIP);
		assertThat(emptyResults.size(), is(0));
		
		for (User sourceUser : allUser)
		{
			for (User targetUser : allUser)
			{
				if (sourceUser.getId() < targetUser.getId())
				{
					entityFactory.persistDefaultMatch(sourceUser, targetUser, RecommendationCategory.PARTNERSHIP);
				}
			}
		}
		
		final List<Long> relationshipUsers = matchRepository.findUsersForRelationship(RecommendationCategory.PARTNERSHIP);
		assertThat(relationshipUsers.size(), is(allUser.size()));
		for (User user : allUser)
		{
			assertThat(relationshipUsers, hasItem(user.getId()));
		}
	}
	
	@Test
	public void findMatchesForUserTest()
	{
		entityFactory.reset();
		
		final int additionalUsers = 6;
		for (int i = 0; i < additionalUsers; i++)
			entityFactory.persistDefaultUser("User " + i, RecommendationCategory.PARTNERSHIP);
		
		final List<User> allUser = userRepository.findAll();
		
		for (User user : allUser)
		{
			final List<Match> emptyResult = matchRepository.findMatchesForUser(user.getId(), Strictness.lastStrictness, RecommendationCategory.PARTNERSHIP);
			assertThat(emptyResult.size(), is(0));
		}
		
		for (User sourceUser : allUser)
		{
			for (User targetUser : allUser)
			{
				if (!sourceUser.equals(targetUser))
				{
					final Match match = matchRepository.findBySourceUserAndTargetUserAndCategory(sourceUser.getId(), targetUser.getId(), RecommendationCategory.PARTNERSHIP);
					if (match == null)
					{
						entityFactory.persistDefaultMatch(sourceUser, targetUser, RecommendationCategory.PARTNERSHIP);
					}
				}
			}
		}
		
		for (User user : allUser)
		{
			final List<Match> emptyResult = matchRepository.findMatchesForUser(user.getId(), Strictness.firstStrictness, RecommendationCategory.PARTNERSHIP);
			assertThat(emptyResult.size(), is(0));
		}
		
		for (User user : allUser)
		{
			final List<Match> matchPairObj = matchRepository.findMatchesForUser(user.getId(), Strictness.lastStrictness, RecommendationCategory.PARTNERSHIP);
			
			assertThat(matchPairObj.size(), is(allUser.size() - 1));
			assertThat(matchPairObj.stream().allMatch(
					matchPair -> Stream.of(matchPair).allMatch(Objects::nonNull)),
					is(true));
		}
	}
	
	@Test
	public void countByStrictnessAndCategoryTest()
	{
		entityFactory.reset();
		
		final int additionalUsers = 6;
		for (int i = 0; i < additionalUsers; i++)
			entityFactory.persistDefaultUser("User " + i, RecommendationCategory.PARTNERSHIP);
		
		final List<User> allUser = userRepository.findAll();
		
		for (User user : allUser)
		{
			for (Strictness strictness : Strictness.values())
			{
				final long emptyResult = matchRepository.countByStrictnessAndCategory(user.getId(), strictness, RecommendationCategory.PARTNERSHIP);
				assertThat(emptyResult, is(0L));
			}
		}
		
		for (User sourceUser : allUser)
		{
			for (User targetUser : allUser)
			{
				if (!sourceUser.equals(targetUser))
				{
					Match match = matchRepository.findBySourceUserAndTargetUserAndCategory(sourceUser.getId(), targetUser.getId(), RecommendationCategory.PARTNERSHIP);
					if (match == null)
					{
						match = entityFactory.persistDefaultMatch(sourceUser, targetUser, RecommendationCategory.PARTNERSHIP);
						match.setStrictness(Strictness._2);
						matchRepository.save(match);
					}
				}
			}
		}
		
		for (User user : allUser)
		{
			for (Strictness strictness : Strictness.values())
			{
				final long result = matchRepository.countByStrictnessAndCategory(user.getId(), strictness, RecommendationCategory.PARTNERSHIP);
				if (Strictness._2.equals(strictness))
				{
					assertThat(result, is(allUser.size() - 1L));
				}
				else
				{
					assertThat(result, is(0L));
				}
			}
		}
	}
	
	@Test
	public void deleteByIdInTest()
	{
		final Match match = matchRepository.findBySourceUserAndTargetUserAndCategory(sourceUser.getId(), targetUser.getId(), RecommendationCategory.PARTNERSHIP);
		List<Long> ids = Arrays.asList(match.getSourceUserId(), match.getTargetUserId());
		matchRepository.deleteByIdIn(ids);
		
		final List<Match> emptyResultUser1 = matchRepository.findMatchesForUser(sourceUser.getId(), Strictness.firstStrictness, RecommendationCategory.PARTNERSHIP);
		assertThat(emptyResultUser1.size(), is(0));
		
		final List<Match> emptyResultUser2 = matchRepository.findMatchesForUser(targetUser.getId(), Strictness.firstStrictness, RecommendationCategory.PARTNERSHIP);
		assertThat(emptyResultUser2.size(), is(0));
	}
	
}
