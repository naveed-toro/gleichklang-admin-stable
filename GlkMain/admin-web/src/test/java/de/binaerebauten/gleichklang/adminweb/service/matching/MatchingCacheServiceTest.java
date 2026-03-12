package de.binaerebauten.gleichklang.adminweb.service.matching;

import de.binaerebauten.gleichklang.adminweb.config.AdminTestConfig;
import de.binaerebauten.gleichklang.adminweb.service.matching.MatchCacheService.CacheContent;
import de.binaerebauten.gleichklang.adminweb.service.matching.MatchCacheService.MatchCache;
import de.binaerebauten.gleichklang.core.model.matching.ChoiceQuestionsMapping;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic.MatchingScope;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.QuestionRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchStatisticRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.BitSet;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;

@ContextConfiguration(classes = { AdminTestConfig.class })
public class MatchingCacheServiceTest extends BasePersistenceTest
{
	@Autowired
	private DefaultEntityFactory entityFactory;
	
	@Autowired
	private MatchCacheService matchCacheService;
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private MatchStatisticRepository matchStatisticRepository;
	
	private User user;

	private CacheContent cacheContent;

	@Before
	public void setup()
	{
		user = entityFactory.persistDefaultUser("user");
		
		final PerformanceLog performanceLog = new PerformanceLog(matchStatisticRepository, MatchingScope.MATCHING);
		final MatchCache matchCache = matchCacheService.createCache(performanceLog);
		cacheContent = matchCache.getContent(user.getId());
	}

	@After
	public void teardown()
	{
		entityFactory.reset();
	}
	
	@Test
	public void testGetDefaultEmptyChoiceAnswer()
	{
		final ChoiceQuestionsMapping choiceQuestionsMapping = entityFactory.persistDefaultChoiceQuestionsMapping(RecommendationCategory.PARTNERSHIP);
		final ChoiceQuestion question = choiceQuestionsMapping.getSourceQuestion();
		question.setRequirement(Requirement.OPTIONAL);
		
		Optional<Choice> choice = entityFactory.persistDefaultChoiceGroup().getChoices().stream().findAny();
		if (choice.isPresent())
		{
			Choice defaultChoice = choice.get();

			question.setDefaultChoice(defaultChoice);
			questionRepository.save(question);

			BitSet value = cacheContent.getChoiceAnswers(question);
			Assert.assertThat(value, equalTo(cacheContent.choiceAnswerToBitSet(defaultChoice)));
		}
		else
		{
			assert false;
		}
	}

}
