package de.binaerebauten.gleichklang.core.repository;

import com.google.common.collect.Iterables;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.model.locatable.ProximitySearchRequest;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.RegionSearchRequest;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DatabaseResourceBundleControl;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link AnswerRepository}.
 */
public class AnswerRepositoryTest extends BasePersistenceTest
{
	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private I18NRepository i18NRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	private Questionnaire questionnaire;
	private User user;
	private List<Question> questions;
	private QuestionGroup questionGroup;
	
	@Before
	public void setup()
	{
		LocalizedEntity.setControl(new DatabaseResourceBundleControl(i18NRepository));

		defaultEntityFactory.persistDefaultI18NEntries("answer", 2);
		user = defaultEntityFactory.persistDefaultUser("testUser");
		questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);
		questions = defaultEntityFactory.persistDefaultRequiredQuestions(questionGroup);
		defaultEntityFactory.persistDefaultAnswers(questions, user);
	}

	@After
	public void teardown()
	{
		defaultEntityFactory.reset();
	}

	@Test
	public void testFindByQuestionAndUser() throws Exception
	{
		Question question = questions.get(0);
		Answer answer = answerRepository.findByUserAndQuestion(user, question);
		assertEquals(answer.getQuestion(), question);
	}

	@Test
	public void testRegionAnswer() throws Exception
	{
		final Region defaultRegion = defaultEntityFactory.persistDefaultRegion(defaultEntityFactory.persistDefaultCountry());
		final Zip defaultZip = defaultEntityFactory.persistDefaultZip(defaultEntityFactory.persistDefaultCountry());
		
		final RegionAnswer regionAnswerWithRegionSearchRequest = defaultEntityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(user, defaultRegion);
		final RegionAnswer regionAnswerWithProximitySearchRequest = defaultEntityFactory.persistDefaultRegionAnswerWithProximitySearchRequest(user, defaultZip);

		final RegionAnswer foundRegionAnswer1 = (RegionAnswer) answerRepository.findByUserAndQuestion(user, regionAnswerWithRegionSearchRequest.getQuestion());
		assertThat(foundRegionAnswer1, equalTo(regionAnswerWithRegionSearchRequest));
		
		final RegionAnswer foundRegionAnswer2 = (RegionAnswer) answerRepository.findByUserAndQuestion(user, regionAnswerWithProximitySearchRequest.getQuestion());
		assertThat(foundRegionAnswer2, equalTo(regionAnswerWithProximitySearchRequest));
		
		final List<RegionSearchRequest> regionSearchRequests = foundRegionAnswer1.getRegionSearchRequests();
		assertThat(regionSearchRequests.size(), equalTo(1));
		assertThat(regionSearchRequests.get(0).getRestrictions().iterator().next(), equalTo(defaultRegion));
		assertThat(foundRegionAnswer2.getRegionSearchRequests().size(), equalTo(0));
		
		final List<ProximitySearchRequest> proximitySearchRequests = foundRegionAnswer2.getProximitySearchRequests();
		assertThat(proximitySearchRequests.size(), equalTo(1));
		assertThat(proximitySearchRequests.get(0).getCenter(), equalTo(defaultZip));
		assertThat(foundRegionAnswer1.getProximitySearchRequests().size(), equalTo(0));
	}

	@Test
	public void testDeleteRegionAnswer() throws Exception
	{
		final Region defaultRegion = defaultEntityFactory.persistDefaultRegion(defaultEntityFactory.persistDefaultCountry());
		final Zip defaultZip = defaultEntityFactory.persistDefaultZip(defaultEntityFactory.persistDefaultCountry());
		
		final RegionAnswer regionAnswer1 = defaultEntityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(user, defaultRegion);
		final RegionAnswer regionAnswer2 = defaultEntityFactory.persistDefaultRegionAnswerWithProximitySearchRequest(user, defaultZip);

		assertThat(answerRepository.findOne(regionAnswer1.getId()), equalTo(regionAnswer1));
		assertThat(answerRepository.findOne(regionAnswer2.getId()), equalTo(regionAnswer2));
		
		answerRepository.delete(regionAnswer1);
		answerRepository.delete(regionAnswer2);

		assertThat(answerRepository.findOne(regionAnswer1.getId()), equalTo(null));
		assertThat(answerRepository.findOne(regionAnswer2.getId()), equalTo(null));
	}

	@Test
	public void testDeleteLocatableRequests() throws Exception
	{
		final Region defaultRegion = defaultEntityFactory.persistDefaultRegion(defaultEntityFactory.persistDefaultCountry());
		final Zip defaultZip = defaultEntityFactory.persistDefaultZip(defaultEntityFactory.persistDefaultCountry());
		
		final RegionAnswer regionAnswer1 = defaultEntityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(user, defaultRegion);
		final RegionAnswer regionAnswer2 = defaultEntityFactory.persistDefaultRegionAnswerWithProximitySearchRequest(user, defaultZip);
		
		assertThat(((RegionAnswer)answerRepository.findOne(regionAnswer1.getId())).getRegionSearchRequests().isEmpty(), equalTo(false));
		assertThat(((RegionAnswer)answerRepository.findOne(regionAnswer2.getId())).getProximitySearchRequests().isEmpty(), equalTo(false));
		
		regionAnswer1.getRegionSearchRequests().clear();
		regionAnswer2.getProximitySearchRequests().clear();

		answerRepository.save(regionAnswer1);
		answerRepository.save(regionAnswer2);
		
		assertThat(((RegionAnswer)answerRepository.findOne(regionAnswer1.getId())).getRegionSearchRequests().isEmpty(), equalTo(true));
		assertThat(((RegionAnswer)answerRepository.findOne(regionAnswer2.getId())).getProximitySearchRequests().isEmpty(), equalTo(true));
	}

	@Test
	public void testFindAnswersForUser() throws Exception
	{
		user = defaultEntityFactory.persistDefaultUser("testUser2");
		questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);
		questions = defaultEntityFactory.persistDefaultRequiredQuestions(questionGroup);
		defaultEntityFactory.persistDefaultAnswers(questions, user);

		List<Answer> result = answerRepository.findAnswersForUser(user, questions);

		defaultEntityFactory.persistDefaultTextQuestion(questionGroup,
				defaultEntityFactory.persistDefaultI18NEntry("text"));
		assertThat(result.size(), equalTo(questions.size()));

		User anotherUser = defaultEntityFactory.persistDefaultUser("user3");
		assertThat(answerRepository.findAnswersForUser(anotherUser, questions).size(), equalTo(0));
	}

	@Test
	public void testAllRequiredAnswersFilled() throws Exception
	{
		user = defaultEntityFactory.persistDefaultUser("testUser2");
		questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);
		questions = defaultEntityFactory.persistDefaultRequiredQuestions(questionGroup);
		defaultEntityFactory.persistDefaultAnswers(questions, user);

		List<? extends Question> requiredQuestions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, Collections.singleton(Requirement.REQUIRED));
		List<Answer> requiredAnswers = answerRepository.findAnswersForUser(user, requiredQuestions);

		assertThat(requiredAnswers.size(), is(requiredQuestions.size()));

		TextQuestion textQuestion = defaultEntityFactory.persistDefaultTextQuestion(questionGroup,
				defaultEntityFactory.persistDefaultI18NEntry());
		questionRepository.save(textQuestion);

		requiredQuestions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, Collections.singleton(Requirement.REQUIRED));
		requiredAnswers = answerRepository.findAnswersForUser(user, requiredQuestions);

		assertThat(requiredAnswers.size(), is(requiredQuestions.size() - 1));
	}

	/**
	 * This tests the methods:
	 * {@link AnswerRepository#getUnansweredQuestions(User, List)}
	 * {@link AnswerRepository#getUnansweredQuestionnaires(User, List)}
	 * {@link AnswerRepository#countIncompleteAndInvalidAnswers(User, List)}
	 * @throws Exception
	 */
	@Test
	public void testIncompleteAndInvalidAnswers() throws Exception
	{
		answerRepository.deleteAll();

		List<Answer> persistedEmptyAnswers = persistEmptyAnswers();
		List<Question> activeQuestions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, Collections.singleton(Requirement.REQUIRED));

		List<Answer> incompleteAndInvalidAnswers = answerRepository.getIncompleteAndInvalidAnswers(user, activeQuestions);
		List<Questionnaire> incompleteAndInvalidQuestionnaires = answerRepository.getIncompleteAndInvalidQuestionnaires(user, activeQuestions);
		int countIncompleteAndInvalidAnswers = answerRepository.countIncompleteAndInvalidAnswers(user, activeQuestions);
		assertThat(incompleteAndInvalidAnswers.size(), equalTo(persistedEmptyAnswers.size()));
		assertThat(incompleteAndInvalidQuestionnaires.size(), equalTo(1));
		assertThat(countIncompleteAndInvalidAnswers, equalTo(persistedEmptyAnswers.size()));

		TextAnswer textAnswer = Iterables.filter(persistedEmptyAnswers, TextAnswer.class).iterator().next();
		textAnswer.setTextValue("Test");
		answerRepository.save(textAnswer);

		incompleteAndInvalidAnswers = answerRepository.getIncompleteAndInvalidAnswers(user, activeQuestions);
		incompleteAndInvalidQuestionnaires = answerRepository.getIncompleteAndInvalidQuestionnaires(user, activeQuestions);
		countIncompleteAndInvalidAnswers = answerRepository.countIncompleteAndInvalidAnswers(user, activeQuestions);
		assertThat(incompleteAndInvalidAnswers.size(), equalTo(persistedEmptyAnswers.size() - 1));
		assertThat(incompleteAndInvalidQuestionnaires.size(), equalTo(1));
		assertThat(countIncompleteAndInvalidAnswers, equalTo(persistedEmptyAnswers.size() - 1));

		NumberAnswer numberAnswer = Iterables.filter(persistedEmptyAnswers, NumberAnswer.class).iterator().next();
		numberAnswer.setNumberValue(50);
		answerRepository.save(numberAnswer);

		incompleteAndInvalidAnswers = answerRepository.getIncompleteAndInvalidAnswers(user, activeQuestions);
		countIncompleteAndInvalidAnswers = answerRepository.countIncompleteAndInvalidAnswers(user, activeQuestions);
		assertThat(incompleteAndInvalidAnswers.size(), equalTo(persistedEmptyAnswers.size() - 2));
		assertThat(incompleteAndInvalidQuestionnaires.size(), equalTo(1));
		assertThat(countIncompleteAndInvalidAnswers, equalTo(persistedEmptyAnswers.size() - 2));

		numberAnswer.setNumberValue(0);
		answerRepository.save(numberAnswer);

		incompleteAndInvalidAnswers = answerRepository.getIncompleteAndInvalidAnswers(user, activeQuestions);
		countIncompleteAndInvalidAnswers = answerRepository.countIncompleteAndInvalidAnswers(user, activeQuestions);
		assertThat(incompleteAndInvalidAnswers.size(), equalTo(persistedEmptyAnswers.size() - 1));
		assertThat(incompleteAndInvalidQuestionnaires.size(), equalTo(1));
		assertThat(countIncompleteAndInvalidAnswers, equalTo(persistedEmptyAnswers.size() - 1));
	}

	/**
	 * This tests the methods:
	 *
	 * {@link AnswerRepository#getUnansweredQuestions(User, List)}
	 * {@link AnswerRepository#getUnansweredQuestionnaires(User, List)}
	 * {@link AnswerRepository#countUnansweredQuestions(User, List)}
	 */
	@Test
	public void testCheckUnansweredQuestions()
	{
		answerRepository.deleteAll();
		questionRepository.deleteAll();
		List<Question> activeQuestions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, Collections.singleton(Requirement.REQUIRED));

		List<Question> unansweredQuestions = answerRepository.getUnansweredQuestions(user, activeQuestions);
		List<Questionnaire> unansweredQuestionnaires = answerRepository.getUnansweredQuestionnaires(user, activeQuestions);
		int countUnansweredQuestions = answerRepository.countUnansweredQuestions(user, activeQuestions);

		assertThat(unansweredQuestions.size(), equalTo(0));
		assertThat(unansweredQuestionnaires.size(), equalTo(0));
		assertThat(countUnansweredQuestions, equalTo(0));

		TextQuestion unansweredQuestion = defaultEntityFactory.persistDefaultTextQuestion(questionGroup,
				defaultEntityFactory.persistDefaultI18NEntry());
		activeQuestions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, Collections.singleton(Requirement.REQUIRED));

		unansweredQuestions = answerRepository.getUnansweredQuestions(user, activeQuestions);
		unansweredQuestionnaires = answerRepository.getUnansweredQuestionnaires(user, activeQuestions);
		countUnansweredQuestions = answerRepository.countUnansweredQuestions(user, activeQuestions);

		assertThat(unansweredQuestions.size(), equalTo(1));
		assertThat(unansweredQuestionnaires.size(), equalTo(1));
		assertThat(countUnansweredQuestions, equalTo(1));

		assertThat(unansweredQuestions.get(0), equalTo(unansweredQuestion));
		assertThat(unansweredQuestionnaires.get(0), equalTo(questionnaire));
	}
	
	@Test
	public void testCountCompleteAnswers()
	{
		int countAnsweredQuestions;
		
		answerRepository.deleteAll();
		
		final List<Answer> persistedEmptyAnswers = persistEmptyAnswers();
		final List<Question> activeQuestions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, Collections.singleton(Requirement.REQUIRED));
		
		countAnsweredQuestions = answerRepository.countCompleteAnswers(user.getId(), activeQuestions);
		assertThat(countAnsweredQuestions, equalTo(0));
		
		final TextAnswer textAnswer = (TextAnswer) persistedEmptyAnswers.stream().filter((TextAnswer.class)::isInstance).findFirst().orElse(null);
		textAnswer.setTextValue("Test");
		answerRepository.save(textAnswer);
		
		countAnsweredQuestions = answerRepository.countCompleteAnswers(user.getId(), activeQuestions);
		assertThat(countAnsweredQuestions, equalTo(1));
		
		final NumberAnswer numberAnswer = (NumberAnswer) persistedEmptyAnswers.stream().filter((NumberAnswer.class)::isInstance).findFirst().orElse(null);
		numberAnswer.setNumberValue(50);
		answerRepository.save(numberAnswer);
		
		countAnsweredQuestions = answerRepository.countCompleteAnswers(user.getId(), activeQuestions);
		assertThat(countAnsweredQuestions, equalTo(2));
		
		final ChoiceAnswer choiceAnswer = (ChoiceAnswer) persistedEmptyAnswers.stream().filter((ChoiceAnswer.class)::isInstance).findFirst().orElse(null);
		choiceAnswer.setChoices(Collections.singleton(((ChoiceQuestion)choiceAnswer.getQuestion()).getChoiceGroup().getChoices().iterator().next()));
		answerRepository.save(choiceAnswer);
		
		countAnsweredQuestions = answerRepository.countCompleteAnswers(user.getId(), activeQuestions);
		assertThat(countAnsweredQuestions, equalTo(3));
	}

    @Test
    public void testAffinityResultFormat() {
        answerRepository.deleteAll();
        questionRepository.deleteAll();

        persistChoicesAndAnswers();

        List<Object[]> affinityResults = answerRepository.getAggregatedAffinityResultsForKey(questionnaire.getI18nKey());
        assertThat("Number of QuestionGroups and results" ,affinityResults.size(), is(questionnaire.getQuestionGroups().size()));

		for (Object[] item : affinityResults)
		{
			assertThat("Number of query values", item.length, is(3));
			assertThat("Type of first result value", item[0].getClass(), equalTo(BigDecimal.class));
			assertThat("Type of second result value", item[1].getClass(), equalTo(BigDecimal.class));
			assertThat("Type of third result value", item[2].getClass(), equalTo(String.class));
		}
    }

    @Test
    public void testAffinityUserResultFormat() {
        answerRepository.deleteAll();
        questionRepository.deleteAll();

        persistChoicesAndAnswers();

        List<Object[]> affinityUserResult = answerRepository.getAggregatedAffinityUserResultsForKey(user.getId(), questionnaire.getI18nKey());
        assertThat("Number of QuestionGroups and results", affinityUserResult.size(), is(questionnaire.getQuestionGroups().size()));

		for (Object[] item : affinityUserResult)
		{
            assertThat("Number of query values", item.length, is(2));
            assertThat("Type of first result value", item[0].getClass(), equalTo(BigDecimal.class));
            assertThat("Type of second result value", item[1].getClass(), equalTo(String.class));
        }
    }

    private List<ChoiceQuestion> persistChoicesAndAnswers() {
        List<ChoiceQuestion> choiceQuestions = new ArrayList<>();

        for (int i=0; i<2; i++) {
            ChoiceQuestion choiceQuestion = defaultEntityFactory.persistDefaultChoiceQuestion(this.questionGroup, defaultEntityFactory.persistDefaultI18NEntry("choice" + i));
            choiceQuestions.add(choiceQuestion);
			choiceQuestion.setRepresentationType(ChoiceQuestion.RepresentationType.AFFINITY);
            choiceQuestion.setSelectionType(ChoiceQuestion.SelectionType.SINGLE);
            ChoiceAnswer choiceAnswer = new ChoiceAnswer();
            choiceAnswer.setQuestion(choiceQuestion);
            choiceAnswer.setUser(user);
            questionRepository.save(choiceQuestion);

            final Set<Choice> choices = new HashSet<>();
            choices.add(choiceQuestion.getChoiceGroup().getChoices().get(i == 0 ? 0 : 4));
            choiceAnswer.setChoices(choices);
            answerRepository.save(choiceAnswer);
        }

        return choiceQuestions;
    }

	private List<Answer> persistEmptyAnswers()
	{
		List<Answer> unansweredAnswers = new ArrayList<>();

		TextQuestion textQuestion = defaultEntityFactory.persistDefaultTextQuestion(questionGroup, defaultEntityFactory.persistDefaultI18NEntry("text"));
		TextAnswer textAnswer = new TextAnswer();
		textAnswer.setQuestion(textQuestion);
		textAnswer.setUser(user);
		unansweredAnswers.add(answerRepository.save(textAnswer));

		NumberQuestion numberQuestion = defaultEntityFactory.persistDefaultNumberQuestion(this.questionGroup, defaultEntityFactory.persistDefaultI18NEntry("number"));
		numberQuestion.setRequirement(Requirement.REQUIRED);
		questionRepository.save(numberQuestion);
		
		NumberAnswer numberAnswer = new NumberAnswer();
		numberAnswer.setUser(user);
		numberAnswer.setQuestion(numberQuestion);
		unansweredAnswers.add(answerRepository.save(numberAnswer));

		ChoiceQuestion choiceQuestion = defaultEntityFactory.persistDefaultChoiceQuestion(this.questionGroup, defaultEntityFactory.persistDefaultI18NEntry("choice"));
		choiceQuestion.setRequirement(Requirement.REQUIRED);
		questionRepository.save(choiceQuestion);
		
		ChoiceAnswer choiceAnswer = new ChoiceAnswer();
		choiceAnswer.setQuestion(choiceQuestion);
		choiceAnswer.setUser(user);
		unansweredAnswers.add(answerRepository.save(choiceAnswer));

		return unansweredAnswers;
	}
	
	@Test
	public void testFindByUser()
	{
		final List<Answer> answers = answerRepository.findByUser(user);
		assertThat(answers.size(), equalTo(5));
	}
}