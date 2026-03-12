package de.binaerebauten.gleichklang.adminweb.service.matching;

import de.binaerebauten.gleichklang.adminweb.config.AdminTestConfig;
import de.binaerebauten.gleichklang.core.model.matching.Match;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.matching.MatchRepository;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchStatisticRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.UserActivityService;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@ContextConfiguration(classes = { AdminTestConfig.class })
public class GenerateSuggestionServiceTest extends BasePersistenceTest
{
	private GenerateSuggestionService generateSuggestionService;
	
	@Autowired
	private DefaultEntityFactory entityFactory;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	@Autowired
	private MatchRepository matchRepository;

	@Autowired
	private RelationshipRepository relationshipRepository;
	
	@Autowired
	private UserActivityService userActivityService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	@Autowired
	private MatchStatisticRepository matchStatisticRepository;

	@Mock
	private MailQueueService mailQueueService;

	private User sourceUser;
	private User targetUser;
	private Random random;

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		
		generateSuggestionService = new GenerateSuggestionService(relationshipRepository, matchStatisticRepository,
				userActivityService, transactionTemplate, matchRepository,
				mailQueueService, userRepository);
		
		random = new Random(0);

		sourceUser = entityFactory.persistDefaultUser("source", RecommendationCategory.PARTNERSHIP);
		targetUser = entityFactory.persistDefaultUser("target", RecommendationCategory.PARTNERSHIP);

		final SubscriptionOffer offer = paymentEntityFactory.persistInitialSubscriptionOffer("test", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);
		paymentEntityFactory.persistSubscription(sourceUser, offer, LocalDateTime.now());
		paymentEntityFactory.persistSubscription(targetUser, offer, LocalDateTime.now());
	}

	@After
	public void teardown()
	{
		entityFactory.reset();
	}

	@Test
	public void generateSuggestionTestForRecommendationBreak()
	{
		Assert.assertThat(matchRepository.findAll().size(), is(0));

		final int firstStrictnessMatches = 10;

		final User firstStrictnessUser = createRandomUser();
		for (int i = 0; i < firstStrictnessMatches; i++)
		{
			createMatches(Strictness._1, 1, firstStrictnessUser);
		}

		entityFactory.persistDefaultRecommendationBreak(firstStrictnessUser);
		preTestSuggestion(firstStrictnessMatches, 0, firstStrictnessUser);
		generateSuggestionService.generateSuggestion();
		postTestSuggestion(firstStrictnessMatches, 0, 0, firstStrictnessUser);
		resetSuggestion();
	}
	
	@Test
	public void generateSuggestionTestForDeactivatedCategories()
	{
		Assert.assertThat(matchRepository.findAll().size(), is(0));
		
		final int firstStrictnessMatches = 10;
		
		final User firstStrictnessUser = createRandomUser();
		for (int i = 0; i < firstStrictnessMatches; i++)
		{
			createMatches(Strictness._1, 1, firstStrictnessUser);
		}
		
		firstStrictnessUser.getCategories().clear();
		userRepository.save(firstStrictnessUser);
		
		preTestSuggestion(firstStrictnessMatches, 0, firstStrictnessUser);
		generateSuggestionService.generateSuggestion();
		postTestSuggestion(firstStrictnessMatches, 0, 0, firstStrictnessUser);
		resetSuggestion();
	}

	private void generateSuggestionAndTest(long sumMatches, long sumSuggestions, long sumRelationships, User sourceUser)
	{
		preTestSuggestion(sumMatches, sumRelationships, sourceUser);
		generateSuggestionService.generateSuggestion();
		postTestSuggestion(sumMatches, sumSuggestions, sumRelationships, sourceUser);
	}

	//@Test
	public void generateSuggestionTestForFirstStrictness()
	{
		Assert.assertThat(matchRepository.findAll().size(), is(0));

		final int firstStrictnessMatches = 10;

		final User firstStrictnessUser = createRandomUser();
		for (int i = 0; i < firstStrictnessMatches; i++)
		{
			createMatches(Strictness._1, 1, firstStrictnessUser);
		}

		generateSuggestionAndTest(firstStrictnessMatches, firstStrictnessMatches, 0, firstStrictnessUser);
		resetSuggestion();
	}

	@Test
	public void generateSuggestionTestForExclusion()
	{
		Assert.assertThat(matchRepository.findAll().size(), is(0));

		final int exclusionMatches = 10;

		final User exclusionUser = createRandomUser();
		for (int i = 0; i < exclusionMatches; i++)
		{
			createMatches(Strictness.EXCLUSION, 1, exclusionUser);

		}

		generateSuggestionAndTest(exclusionMatches, 0, 0, exclusionUser);
		resetSuggestion();
	}

	//@Test
	public void generateSuggestionTestForInitial()
	{
		Assert.assertThat(matchRepository.findAll().size(), is(0));

		final int secondStrictnessMatches = 10;
		Assert.assertThat(secondStrictnessMatches, greaterThanOrEqualTo(GenerateSuggestionService.MIN_SUGGESTIONS_INITIAL));

		final User secondStrictnessUser = createRandomUser();
		for (int i = 0; i < secondStrictnessMatches; i++)
		{
			createMatches(Strictness._2, i + 1, secondStrictnessUser);
		}

		generateSuggestionAndTest(secondStrictnessMatches, GenerateSuggestionService.MIN_SUGGESTIONS_INITIAL, 0, secondStrictnessUser);
		resetSuggestion();
	}
	
	@Test
	public void generateSuggestionTestForRestockNotNecessary()
	{
		Assert.assertThat(matchRepository.findAll().size(), is(0));
		
		final int secondStrictnessMatches = 10;
		Assert.assertThat(secondStrictnessMatches, greaterThanOrEqualTo(GenerateSuggestionService.MIN_SUGGESTIONS));
		
		final User secondStrictnessUser = createRandomUser();
		for (int i = 0; i < secondStrictnessMatches; i++)
		{
			createMatches(Strictness._2, i + 1, secondStrictnessUser);
		}
		
		for(int i = 0; i < GenerateSuggestionService.MIN_SUGGESTIONS; i++)
		{
			entityFactory.persistDefaultRelationship(secondStrictnessUser, createRandomUser(), RecommendationCategory.PARTNERSHIP);
		}
		
		generateSuggestionAndTest(secondStrictnessMatches, 0, GenerateSuggestionService.MIN_SUGGESTIONS, secondStrictnessUser);
		resetSuggestion();
	}
	
	//@Test
	public void generateSuggestionTestForRestock()
	{
		Assert.assertThat(matchRepository.findAll().size(), is(0));
		
		final int secondStrictnessMatches = 10;
		Assert.assertThat(secondStrictnessMatches, greaterThanOrEqualTo(GenerateSuggestionService.MIN_SUGGESTIONS));
		
		final User secondStrictnessUser = createRandomUser();
		for (int i = 0; i < secondStrictnessMatches; i++)
		{
			createMatches(Strictness._2, i + 1, secondStrictnessUser);
		}
		
		for(int i = 0; i < GenerateSuggestionService.MIN_SUGGESTIONS; i++)
		{
			final Relationship relationship = entityFactory.persistDefaultRelationship(secondStrictnessUser, createRandomUser(), RecommendationCategory.PARTNERSHIP);
			relationship.setCreateDate(LocalDateTime.now().minus(GenerateSuggestionService.CHECK_PERIOD));
			relationshipRepository.save(relationship);
		}
		
		generateSuggestionAndTest(secondStrictnessMatches, GenerateSuggestionService.MIN_SUGGESTIONS, GenerateSuggestionService.MIN_SUGGESTIONS, secondStrictnessUser);
		resetSuggestion();
	}

	private void postTestSuggestion(long sumMatches, long sumSuggestions, long sumRelationships, User sourceUser)
	{
		final long sourceMatchesAfter = matchRepository.findAll().stream().filter(match -> match.getSourceUserId().equals(sourceUser.getId())).count();
		final long sourceRelationshipsAfter = relationshipRepository.findAll().stream().filter(relationship -> relationship.getSourceUserId().equals(sourceUser.getId())).count();

		Assert.assertThat(sourceRelationshipsAfter, is(sumSuggestions + sumRelationships));
		Assert.assertThat(sourceMatchesAfter, is(sumMatches - sumSuggestions));
	}

	private void preTestSuggestion(long sumMatches, long sumRelationships, User sourceUser)
	{
		final long sourceMatchesBefore = matchRepository.findAll().stream().filter(match -> match.getSourceUserId().equals(sourceUser.getId())).count();
		final long sourceRelationshipsBefore = relationshipRepository.findAll().stream().filter(relationship -> relationship.getSourceUserId().equals(sourceUser.getId())).count();

		Assert.assertThat(sourceMatchesBefore, is(sumMatches));
		Assert.assertThat(sourceRelationshipsBefore, is(sumRelationships));
	}

	private void resetSuggestion()
	{
		relationshipRepository.deleteAll();
		relationshipRepository.deleteAll();
	}

	private void createMatches(Strictness strictness, int count, User sourceUser)
	{
		final User targetUser = createRandomUser();

		createMatches(strictness, count, sourceUser, targetUser);

		/* prevention for reverse relationship from targetUser to sourceUser */
		for (int i = 0; i < GenerateSuggestionService.MIN_SUGGESTIONS; i++)
		{
			entityFactory.persistDefaultRelationship(targetUser, createRandomUser(), RecommendationCategory.PARTNERSHIP);
		}
	}

	private void createMatches(Strictness strictness, int count, User sourceUser, User targetUser)
	{
		final Match match1 = new Match(sourceUser, targetUser, RecommendationCategory.PARTNERSHIP);
		match1.setStrictness(strictness);
		match1.setNumber(count);
		matchRepository.save(match1);
	}

	private User createRandomUser()
	{
		return entityFactory.persistDefaultUser("source_" + random.nextInt(), RecommendationCategory.PARTNERSHIP);
	}
}
