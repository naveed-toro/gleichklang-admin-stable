package de.binaerebauten.gleichklang.adminweb.service.matching;

import de.binaerebauten.gleichklang.adminweb.config.AdminTestConfig;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.locatable.Continent;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.locatable.Region;
import de.binaerebauten.gleichklang.core.model.locatable.Zip;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.matching.MatchStatistic.MatchingScope;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.matching.MatchRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchStatisticRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;

@ContextConfiguration(classes = { AdminTestConfig.class })
public class MatchingServiceTest extends BasePersistenceTest
{
	private static final Logger LOG = LoggerFactory.getLogger(MatchingServiceTest.class);
	
	@Rule
	public final ExpectedException thrown = ExpectedException.none();
	
	@Autowired
	private MatchingService matchingService;
	
	@Autowired
	private DefaultEntityFactory entityFactory;
	
	@Autowired
	private PaymentEntityFactory paymentEntityFactory;
	
	@Autowired
	private SubscriptionRepository subscriptionRepository;
	
	@Autowired
	private AnswerRepository answerRepository;
	
	@Autowired
	private MatchRepository matchRepository;
	
	@Autowired
	private MatrixRepository matrixRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private LocatableRepository locatableRepository;
	
	@Autowired
	private QuestionMappingRepository questionMappingRepository;
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private AvatarRepository avatarRepository;
	
	@Autowired
	private MatchStatisticRepository matchStatisticRepository;
	
	private User sourceUser;
	private User targetUser;
	
	@Before
	public void setup()
	{
		sourceUser = entityFactory.persistDefaultUser("source",
				RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);
		targetUser = entityFactory.persistDefaultUser("target",
				RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);
		
		final SubscriptionOffer offer = paymentEntityFactory.persistInitialSubscriptionOffer("test",
				LocalDateTime.now(), RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);
		paymentEntityFactory.persistSubscription(sourceUser, offer, LocalDateTime.now());
		paymentEntityFactory.persistSubscription(targetUser, offer, LocalDateTime.now());
	}
	
	@After
	public void teardown()
	{
		answerRepository.deleteAll();
		entityFactory.reset();
	}
	
	@Test
	public void testNoSubscription()
	{
		Subscription subscription;
		
		// Ensure matches table is empty
		checkEmpty();
		
		// with subscriptions
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		// target without subscription
		subscription = subscriptionRepository.findCurrentSubscription(targetUser).get();
		subscription.setCurrent(null);
		subscription = subscriptionRepository.save(subscription);
		
		matchingService.startMatching();
		checkStrictness(RecommendationCategory.FRIENDSHIP, Strictness.lastStrictness);
		checkStrictness(RecommendationCategory.PARTNERSHIP, Strictness.lastStrictness);
		
		// with subscription
		subscription.setCurrent(true);
		subscriptionRepository.save(subscription);
		
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		// source without subscription
		subscription = subscriptionRepository.findCurrentSubscription(sourceUser).get();
		subscription.setCurrent(null);
		subscriptionRepository.save(subscription);
		
		matchingService.startMatching();
		checkStrictness(RecommendationCategory.FRIENDSHIP, Strictness.lastStrictness);
		checkStrictness(RecommendationCategory.PARTNERSHIP, Strictness.lastStrictness);
		
		// source + target without subscription
		subscription = subscriptionRepository.findCurrentSubscription(targetUser).get();
		subscription.setCurrent(null);
		subscriptionRepository.save(subscription);
		
		matchingService.startMatching();
		checkStrictness(RecommendationCategory.FRIENDSHIP, Strictness.lastStrictness);
		checkStrictness(RecommendationCategory.PARTNERSHIP, Strictness.lastStrictness);
	}
	
	@Test
	public void testInvalidChoiceQuestionMappings()
	{
		final List<I18NEntity> choiceI18N = entityFactory.persistDefaultI18NEntries(2);
		final Questionnaire questionnaire1 = entityFactory.persistDefaultQuestionnaire(RecommendationCategory.PARTNERSHIP);
		final Questionnaire questionnaire2 = entityFactory.persistDefaultQuestionnaire(RecommendationCategory.FRIENDSHIP);
		final QuestionGroup questionGroup1 = entityFactory.persistDefaultQuestionGroup(questionnaire1);
		final QuestionGroup questionGroup2 = entityFactory.persistDefaultQuestionGroup(questionnaire2);
		final ChoiceQuestion sourceQuestion = entityFactory.persistDefaultChoiceQuestion(questionGroup1, choiceI18N.get(0));
		final ChoiceQuestion targetQuestion = entityFactory.persistDefaultChoiceQuestion(questionGroup2, choiceI18N.get(1));
		
		final ChoiceQuestionsMapping mapping1 = entityFactory.persistDefaultChoiceQuestionsMapping(null);
		final ChoiceQuestionsMapping mapping2 = entityFactory.persistDefaultChoiceQuestionsMapping(sourceQuestion, targetQuestion);
		
		createChoiceAnswers(mapping1);
		setMatrixValue(mapping1, Strictness.lastStrictness);
		
		createChoiceAnswers(mapping2);
		setMatrixValue(mapping2, Strictness.lastStrictness);
		
		// check empty
		checkEmpty();
		
		matchingService.startMatching();
		
		// check after run
		checkStrictness(Strictness.firstStrictness);
	}
	
	private void checkStrictness(Strictness strictness)
	{
		checkStrictness(RecommendationCategory.PARTNERSHIP, strictness);
		checkStrictness(RecommendationCategory.FRIENDSHIP, Strictness.firstStrictness);
	}
	
	private void checkStrictness(RecommendationCategory recommendationCategory, Strictness strictness)
	{
		final Match result = matchRepository.findBySourceUserAndTargetUserAndCategory(sourceUser.getId(), targetUser.getId(), recommendationCategory);
		
		if (!Strictness.lastStrictness.equals(strictness))
		{
			Assert.assertThat(result, notNullValue());
		}
		
		if (result != null)
		{
			Assert.assertThat(result.getStrictness(), is(strictness));
		}
	}
	
	private void checkEmpty()
	{
		Assert.assertThat(matchRepository.count(), equalTo(0L));
	}
	
	private void createInvalidAffinityMappings()
	{
		final List<I18NEntity> choiceI18N = entityFactory.persistDefaultI18NEntries(2);
		final Questionnaire questionnaire1 = entityFactory.persistDefaultQuestionnaire(RecommendationCategory.PARTNERSHIP);
		final Questionnaire questionnaire2 = entityFactory.persistDefaultQuestionnaire(RecommendationCategory.FRIENDSHIP);
		final QuestionGroup questionGroup1 = entityFactory.persistDefaultQuestionGroup(questionnaire1);
		final QuestionGroup questionGroup2 = entityFactory.persistDefaultQuestionGroup(questionnaire2);
		final ChoiceQuestion choiceQuestion1 = entityFactory.persistDefaultChoiceQuestion(questionGroup1, choiceI18N.get(0));
		final ChoiceQuestion choiceQuestion2 = entityFactory.persistDefaultChoiceQuestion(questionGroup2, choiceI18N.get(1));
		
		final AffinityMapping mapping = entityFactory.persistDefaultAffinityMapping(choiceQuestion1, choiceQuestion2);
		
		createChoiceAnswers(mapping);
		
		for (ChoiceQuestion choiceQuestion : mapping.getQuestions())
		{
			final ChoiceAnswer targetAnswer = (ChoiceAnswer) answerRepository.findByUserAndQuestion(targetUser, choiceQuestion);
			targetAnswer.getChoices().clear();
			final List<Choice> choices = choiceQuestion.getChoiceGroup().getChoices();
			targetAnswer.getChoices().add(choices.get(choices.size() - 1));
			answerRepository.save(targetAnswer);
		}
	}
	
	@Test
	public void testInvalidNumberQuestionsMappings()
	{
		Match result1, result2, result3;
		NumberQuestion factQuestion, minQuestion, maxQuestion;
		
		final List<I18NEntity> choiceI18N = entityFactory.persistDefaultI18NEntries(6);
		final Questionnaire questionnaire1 = entityFactory.persistDefaultQuestionnaire(RecommendationCategory.PARTNERSHIP);
		final Questionnaire questionnaire2 = entityFactory.persistDefaultQuestionnaire(RecommendationCategory.FRIENDSHIP);
		final Questionnaire questionnaire3 = entityFactory.persistDefaultQuestionnaire();
		final QuestionGroup questionGroup1 = entityFactory.persistDefaultQuestionGroup(questionnaire1);
		final QuestionGroup questionGroup2 = entityFactory.persistDefaultQuestionGroup(questionnaire2);
		final QuestionGroup questionGroup3 = entityFactory.persistDefaultQuestionGroup(questionnaire3);
		
		// general
		final NumberQuestionsMapping mapping1 = entityFactory.persistDefaultNumberQuestionsMapping(null);
		
		// different categories
		factQuestion = entityFactory.persistDefaultNumberQuestion(questionGroup1, choiceI18N.get(0));
		minQuestion = entityFactory.persistDefaultNumberQuestion(questionGroup2, choiceI18N.get(1));
		maxQuestion = entityFactory.persistDefaultNumberQuestion(questionGroup2, choiceI18N.get(2));
		final NumberQuestionsMapping mapping2 = entityFactory.persistDefaultNumberQuestionsMapping(factQuestion, minQuestion, maxQuestion);
		
		// min max different
		factQuestion = entityFactory.persistDefaultNumberQuestion(questionGroup1, choiceI18N.get(3));
		minQuestion = entityFactory.persistDefaultNumberQuestion(questionGroup1, choiceI18N.get(4));
		maxQuestion = entityFactory.persistDefaultNumberQuestion(questionGroup3, choiceI18N.get(5));
		final NumberQuestionsMapping mapping3 = entityFactory.persistDefaultNumberQuestionsMapping(factQuestion, minQuestion, maxQuestion);
		
		setNumberAnswers(mapping1, 10, 11, 15);
		setNumberAnswers(mapping2, 10, 11, 15);
		setNumberAnswers(mapping3, 10, 11, 15);
		
		// Ensure matches table is empty
		checkEmpty();
		
		// check after run
		matchingService.startMatching();
		
		checkStrictness(Strictness.firstStrictness);
	}
	
	@Ignore
	@Test
	public void testDeletedNumberQuestion()
	{
		final NumberQuestionsMapping mapping = entityFactory.persistDefaultNumberQuestionsMapping(RecommendationCategory.PARTNERSHIP);
		
		final int factValue = 10;
		setNumberAnswers(mapping, factValue, factValue - 2, factValue - 1);
		
		checkEmpty();
		
		matchingService.startMatching();
		
		// check first fail run
		checkStrictness(Strictness.lastStrictness);
		
		mapping.getFactQuestion().setDeleted(true);
		mapping.getMaxQuestion().setDeleted(true);
		mapping.getMinQuestion().setDeleted(true);
		
		questionRepository.save(mapping.getFactQuestion());
		questionRepository.save(mapping.getMaxQuestion());
		questionRepository.save(mapping.getMinQuestion());
		
		// check deleted
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
	}
	
	private void setNumberAnswers(NumberQuestionsMapping mapping, Integer factValue, Integer minValue, Integer maxValue)
	{
		setNumberAnswers(mapping, factValue, minValue, maxValue, false, sourceUser, targetUser);
	}
	
	private void setNumberAnswers(NumberQuestionsMapping mapping, Integer factValue, Integer minValue, Integer maxValue, boolean deleteEmpty)
	{
		setNumberAnswers(mapping, factValue, minValue, maxValue, deleteEmpty, sourceUser, targetUser);
	}
	
	private void setNumberAnswers(NumberQuestionsMapping mapping, Integer factValue, Integer minValue, Integer maxValue, boolean deleteEmpty, User sourceUser, User targetUser)
	{
		setNumberAnswer(mapping.getFactQuestion(), factValue, sourceUser, deleteEmpty);
		setNumberAnswer(mapping.getMinQuestion(), minValue, targetUser, deleteEmpty);
		setNumberAnswer(mapping.getMaxQuestion(), maxValue, targetUser, deleteEmpty);
	}
	
	private NumberAnswer setNumberAnswer(NumberQuestion question, Integer value, User user, boolean deleteEmpty)
	{
		NumberAnswer answer = (NumberAnswer) answerRepository.findByUserAndQuestion(user, question);
		if (deleteEmpty && value == null)
		{
			if (answer != null)
			{
				answerRepository.delete(answer);
			}
			return null;
		}
		
		if (answer == null)
		{
			answer = new NumberAnswer();
			answer.setQuestion(question);
			answer.setUser(user);
		}
		answer.setNumberValue(value);
		return answerRepository.save(answer);
	}
	
	private void setMatrixValue(ChoiceQuestionsMapping mapping, Strictness strictness)
	{
		final Choice sourceChoice = mapping.getSourceQuestion().getChoiceGroup().getChoices().get(0);
		final Choice targetChoice = mapping.getTargetQuestion().getChoiceGroup().getChoices().get(0);
		
		final Set<MatrixValue> matrixValues = mapping.getMatrix().getMatrixValues();
		final MatrixValue matrixValue = matrixValues.stream()
				.filter(matrixValue1 -> matrixValue1.getSourceChoice().equals(sourceChoice) && matrixValue1.getTargetChoice().equals(targetChoice))
				.findFirst().get();
		matrixValue.setStrictness(strictness);
		
		matrixRepository.save(mapping.getMatrix());
	}
	
	private void createChoiceAnswers(ChoiceQuestionsMapping mapping)
	{
		final Choice sourceChoice = mapping.getSourceQuestion().getChoiceGroup().getChoices().get(0);
		final Choice targetChoice = mapping.getTargetQuestion().getChoiceGroup().getChoices().get(0);
		
		createChoiceAnswer(sourceChoice, sourceUser, mapping.getSourceQuestion());
		createChoiceAnswer(targetChoice, targetUser, mapping.getTargetQuestion());
	}
	
	private void createChoiceAnswer(Choice choice, User user, ChoiceQuestion question)
	{
		final ChoiceAnswer choiceAnswer = new ChoiceAnswer();
		choiceAnswer.setUser(user);
		choiceAnswer.setQuestion(question);
		choiceAnswer.getChoices().add(choice);
		answerRepository.save(choiceAnswer);
	}
	
	private void createChoiceAnswers(AffinityMapping mapping)
	{
		for (ChoiceQuestion choiceQuestion : mapping.getQuestions())
		{
			final Choice choice = choiceQuestion.getChoiceGroup().getChoices().get(0);
			
			createChoiceAnswer(choice, sourceUser, choiceQuestion);
			createChoiceAnswer(choice, targetUser, choiceQuestion);
		}
	}
	
	@Test
	public void choiceQuestionsTest()
	{
		final ChoiceQuestionsMapping mapping = entityFactory.persistDefaultChoiceQuestionsMapping(RecommendationCategory.PARTNERSHIP);
		
		createChoiceAnswers(mapping);
		
		checkEmpty();
		
		for (Strictness strictness : Strictness.values())
		{
			setMatrixValue(mapping, strictness);
			
			matchingService.startMatching();
			
			checkStrictness(strictness);
		}
	}
	
	@Test
	public void choiceQuestionsWithDefaultEmptyStrictnessTest()
	{
		final Strictness defaultEmptyStrictness = Strictness._2;
		final ChoiceQuestionsMapping mapping = entityFactory.persistDefaultChoiceQuestionsMapping(RecommendationCategory.PARTNERSHIP);
		mapping.setDefaultEmptyStrictness(defaultEmptyStrictness);
		questionMappingRepository.save(mapping);
		setMatrixValue(mapping, Strictness.firstStrictness);
		
		final Choice sourceChoice = mapping.getSourceQuestion().getChoiceGroup().getChoices().get(0);
		final Choice targetChoice = mapping.getTargetQuestion().getChoiceGroup().getChoices().get(0);
		
		checkEmpty();
		
		matchingService.startMatching();
		checkStrictness(defaultEmptyStrictness);
		
		createChoiceAnswer(sourceChoice, sourceUser, mapping.getSourceQuestion());
		
		matchingService.startMatching();
		checkStrictness(defaultEmptyStrictness);
		
		createChoiceAnswer(targetChoice, targetUser, mapping.getTargetQuestion());
		
		matchingService.startMatching();
		checkStrictness(defaultEmptyStrictness);
		
		createChoiceAnswer(targetChoice, sourceUser, mapping.getTargetQuestion());
		
		matchingService.startMatching();
		checkStrictness(defaultEmptyStrictness);
		
		createChoiceAnswer(sourceChoice, targetUser, mapping.getSourceQuestion());
		
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
	}
	
	@Test
	public void affinityQuestionsTest()
	{
		final AffinityMapping mapping = entityFactory.persistDefaultAffinityMapping(RecommendationCategory.PARTNERSHIP);
		
		createInvalidAffinityMappings();
		
		createChoiceAnswers(mapping);
		
		checkEmpty();
		
		matchingService.startMatching();
		
		// check first success run
		checkStrictness(Strictness.firstStrictness);
		
		for (ChoiceQuestion choiceQuestion : mapping.getQuestions())
		{
			final ChoiceAnswer targetAnswer = (ChoiceAnswer) answerRepository.findByUserAndQuestion(targetUser, choiceQuestion);
			targetAnswer.getChoices().clear();
			final List<Choice> choices = choiceQuestion.getChoiceGroup().getChoices();
			targetAnswer.getChoices().add(choices.get(choices.size() - 1));
			answerRepository.save(targetAnswer);
		}
		matchingService.startMatching();
		
		// check failing
		checkStrictness(Strictness.lastStrictness);
	}
	
	@Test
	public void generalAffinityQuestionsTest()
	{
		final AffinityMapping mapping = entityFactory.persistDefaultAffinityMapping();
		
		final List<ChoiceAnswer> choiceAnswers = new ArrayList<>();
		
		for (ChoiceQuestion choiceQuestion : mapping.getQuestions())
		{
			final ChoiceAnswer sourceChoiceAnswer = new ChoiceAnswer();
			sourceChoiceAnswer.setUser(sourceUser);
			sourceChoiceAnswer.setQuestion(choiceQuestion);
			sourceChoiceAnswer.getChoices().add(choiceQuestion.getChoiceGroup().getChoices().get(0));
			
			final ChoiceAnswer targetChoiceAnswer = new ChoiceAnswer();
			targetChoiceAnswer.setUser(targetUser);
			targetChoiceAnswer.setQuestion(choiceQuestion);
			targetChoiceAnswer.getChoices().add(choiceQuestion.getChoiceGroup().getChoices().get(0));
			
			choiceAnswers.add(sourceChoiceAnswer);
			choiceAnswers.add(targetChoiceAnswer);
		}
		
		answerRepository.save(choiceAnswers);
		
		checkEmpty();
		
		matchingService.startMatching();
		
		// check first success run
		checkStrictness(Strictness.firstStrictness);
		;
		
		// check failing
		for (ChoiceQuestion choiceQuestion : mapping.getQuestions())
		{
			final ChoiceAnswer targetAnswer = (ChoiceAnswer) answerRepository.findByUserAndQuestion(targetUser, choiceQuestion);
			targetAnswer.getChoices().clear();
			final List<Choice> choices = choiceQuestion.getChoiceGroup().getChoices();
			targetAnswer.getChoices().add(choices.get(choices.size() - 1));
			answerRepository.save(targetAnswer);
		}
		
		matchingService.startMatching();
		checkStrictness(RecommendationCategory.PARTNERSHIP, Strictness.lastStrictness);
		checkStrictness(RecommendationCategory.FRIENDSHIP, Strictness.lastStrictness);
	}
	
	@Test
	public void numberQuestionsTest()
	{
		final NumberQuestionsMapping mapping = entityFactory.persistDefaultNumberQuestionsMapping(RecommendationCategory.PARTNERSHIP);
		
		final int factValue = 10;
		setNumberAnswers(mapping, factValue, factValue - 1, factValue + 1);
		
		checkEmpty();
		
		matchingService.startMatching();
		
		// check first success run
		checkStrictness(Strictness.firstStrictness);
		
		// check fact answer null
		setNumberAnswers(mapping, null, factValue - 1, factValue + 1);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		setNumberAnswers(mapping, null, factValue - 1, factValue + 1, true);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		// check open interval
		setNumberAnswers(mapping, factValue, factValue - 1, null);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		setNumberAnswers(mapping, factValue, null, factValue + 1);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		setNumberAnswers(mapping, factValue, factValue - 1, null, true);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		setNumberAnswers(mapping, factValue, null, factValue + 1, true);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		// check min to high
		setNumberAnswers(mapping, factValue, factValue + 1, factValue + 2);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		setNumberAnswers(mapping, factValue, factValue + 1, null);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		setNumberAnswers(mapping, factValue, factValue + 1, null, true);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		// check max to low
		setNumberAnswers(mapping, factValue, factValue - 2, factValue - 1);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		setNumberAnswers(mapping, factValue, null, factValue - 1);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		setNumberAnswers(mapping, factValue, null, factValue - 1, true);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
	}
	
	@Test
	public void numberQuestionsTestWithDefaultEmptyStrictness()
	{
		final Strictness defaultEmptyStrictness = Strictness._2;
		final NumberQuestionsMapping mapping = entityFactory.persistDefaultNumberQuestionsMapping(RecommendationCategory.PARTNERSHIP);
		mapping.setDefaultEmptyStrictness(defaultEmptyStrictness);
		questionMappingRepository.save(mapping);
		
		final int factValue = 10;
		
		checkEmpty();
		
		// check success
		
		setNumberAnswers(mapping, factValue, factValue - 1, factValue + 1, true, sourceUser, targetUser);
		setNumberAnswers(mapping, factValue, factValue - 1, factValue + 1, true, targetUser, sourceUser);
		
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		setNumberAnswers(mapping, factValue, null, factValue + 1, true, sourceUser, targetUser);
		setNumberAnswers(mapping, factValue, factValue - 1, null, true, targetUser, sourceUser);
		
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		// default empty strictness
		
		setNumberAnswers(mapping, factValue, factValue - 1, factValue + 1, true, sourceUser, targetUser);
		setNumberAnswers(mapping, null, factValue - 1, factValue + 1, true, targetUser, sourceUser);
		
		matchingService.startMatching();
		checkStrictness(defaultEmptyStrictness);
		
		setNumberAnswers(mapping, null, factValue - 1, factValue + 1, true, sourceUser, targetUser);
		setNumberAnswers(mapping, factValue, factValue - 1, factValue + 1, true, targetUser, sourceUser);
		
		matchingService.startMatching();
		checkStrictness(defaultEmptyStrictness);
		
		// failing
		
		setNumberAnswers(mapping, factValue, factValue + 1, factValue + 2, true, sourceUser, targetUser);
		setNumberAnswers(mapping, null, factValue - 1, factValue + 1, true, targetUser, sourceUser);
		
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		setNumberAnswers(mapping, null, factValue - 1, factValue + 1, true, sourceUser, targetUser);
		setNumberAnswers(mapping, factValue, factValue + 1, factValue + 2, true, targetUser, sourceUser);
		
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
	}
	
	private void setAgeAnswer(AgeQuestionMapping mapping, int minAge, int maxAge)
	{
		NumberAnswer minAnswer = (NumberAnswer) answerRepository.findByUserAndQuestion(targetUser, mapping.getMinAgeQuestion());
		if (minAnswer == null)
		{
			minAnswer = new NumberAnswer();
			minAnswer.setQuestion(mapping.getMinAgeQuestion());
			minAnswer.setUser(targetUser);
		}
		minAnswer.setNumberValue(minAge);
		
		NumberAnswer maxAnswer = (NumberAnswer) answerRepository.findByUserAndQuestion(targetUser, mapping.getMaxAgeQuestion());
		if (maxAnswer == null)
		{
			maxAnswer = new NumberAnswer();
			maxAnswer.setQuestion(mapping.getMaxAgeQuestion());
			maxAnswer.setUser(targetUser);
		}
		maxAnswer.setNumberValue(maxAge);
		
		answerRepository.save(Arrays.asList(minAnswer, maxAnswer));
	}
	
	@Test
	public void testInvalidAgeQuestions()
	{
		final List<I18NEntity> choiceI18N = entityFactory.persistDefaultI18NEntries(2);
		final Questionnaire questionnaire1 = entityFactory.persistDefaultQuestionnaire(RecommendationCategory.PARTNERSHIP);
		final Questionnaire questionnaire2 = entityFactory.persistDefaultQuestionnaire(RecommendationCategory.FRIENDSHIP);
		final QuestionGroup questionGroup1 = entityFactory.persistDefaultQuestionGroup(questionnaire1);
		final QuestionGroup questionGroup2 = entityFactory.persistDefaultQuestionGroup(questionnaire2);
		final NumberQuestion minQuestion = entityFactory.persistDefaultNumberQuestion(questionGroup1, choiceI18N.get(0));
		final NumberQuestion maxQuestion = entityFactory.persistDefaultNumberQuestion(questionGroup2, choiceI18N.get(1));
		
		final AgeQuestionMapping mapping1 = entityFactory.persistDefaultAgeQuestionMapping(null);
		final AgeQuestionMapping mapping2 = entityFactory.persistDefaultAgeQuestionMapping(minQuestion, maxQuestion);
		
		final int age = 20;
		final LocalDate birthday = LocalDate.now().minus(age, ChronoUnit.YEARS);
		
		sourceUser.setBirthDate(birthday);
		userRepository.save(sourceUser);
		
		setAgeAnswer(mapping1, age + 1, age + 2);
		setAgeAnswer(mapping2, age + 1, age + 2);
		
		// Ensure matches table is empty
		checkEmpty();
		
		// check after run
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
	}
	
	@Test
	public void ageQuestionsTest()
	{
		final AgeQuestionMapping mapping = entityFactory.persistDefaultAgeQuestionMapping(RecommendationCategory.PARTNERSHIP);
		
		final int age = 20;
		final LocalDate birthday = LocalDate.now().minus(age, ChronoUnit.YEARS);
		
		setAgeAnswer(mapping, age - 1, age + 1);
		
		sourceUser.setBirthDate(birthday);
		userRepository.save(sourceUser);
		
		// check empty
		checkEmpty();
		
		// check first success run
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		// check min to high
		setAgeAnswer(mapping, age + 1, age + 2);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		// check max to low
		setAgeAnswer(mapping, age - 2, age - 1);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
	}
	
	@Test
	public void testInvalidAvatarQuestions()
	{
		final AvatarQuestionMapping mapping = entityFactory.persistDefaultAvatarQuestionMapping(null);
		
		// Ensure matches table is empty
		checkEmpty();
		
		// check after success run
		setAvatarAnswer(mapping);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
	}
	
	@Test
	public void avatarQuestionsTest()
	{
		final AvatarQuestionMapping mapping = entityFactory.persistDefaultAvatarQuestionMapping(RecommendationCategory.PARTNERSHIP);
		
		// Ensure matches table is empty
		checkEmpty();
		
		// check first success run
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		// check fail
		setAvatarAnswer(mapping);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		// check fail
		setAvatar(RecommendationCategory.FRIENDSHIP);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		// check true
		setAvatar(RecommendationCategory.PARTNERSHIP);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
	}
	
	private void setAvatarAnswer(AvatarQuestionMapping mapping)
	{
		createChoiceAnswer(mapping.getTrueChoice(), sourceUser, mapping.getAvatarQuestion());
	}
	
	private void setAvatar(RecommendationCategory recommendationCategory)
	{
		final Avatar avatar = new Avatar();
		avatar.setFile(entityFactory.persistDefaultFileEntity());
		avatar.setCategory(recommendationCategory);
		avatar.setUser(targetUser);
		
		avatarRepository.save(avatar);
	}
	
	@Test
	public void regionQuestionTest() throws Exception
	{
		final Continent defaultContinent = entityFactory.persistDefaultContinent();
		final Country defaultCountry = entityFactory.persistDefaultCountry(defaultContinent);
		final Region defaultRegion = entityFactory.persistDefaultRegion(defaultCountry);
		final Zip defaultZip = entityFactory.persistDefaultZip(defaultRegion);
		
		// check empty
		checkEmpty();
		
		// check first success run
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		//check fail
		entityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(sourceUser, defaultRegion);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		targetUser.addAddress(DefaultStaticEntityFactory.createDefaultAddress(defaultZip));
		userRepository.save(targetUser);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
	}
	
	@Test
	public void testDeletedRegionQuestion() throws Exception
	{
		final Continent defaultContinent = entityFactory.persistDefaultContinent();
		final Country defaultCountry = entityFactory.persistDefaultCountry(defaultContinent);
		final Region defaultRegion = entityFactory.persistDefaultRegion(defaultCountry);
		final Zip defaultZip = entityFactory.persistDefaultZip(defaultRegion);
		
		final RegionAnswer regionAnswer = entityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(sourceUser, defaultRegion);
		
		// Ensure matches table is empty
		checkEmpty();
		
		// check first fail run
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		// check deleted question
		regionAnswer.getQuestion().setDeleted(true);
		questionRepository.save(regionAnswer.getQuestion());
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
	}
	
	@Test
	public void proximityQuestionTest() throws Exception
	{
		final Country defaultCountry = entityFactory.persistDefaultCountry();
		
		final Zip sourceZip = entityFactory.persistDefaultZip(defaultCountry);
		sourceZip.setLatitude(41.12);
		sourceZip.setLongitude(12.12);
		locatableRepository.save(sourceZip);
		
		final Zip targetZipFail = entityFactory.persistDefaultZip(defaultCountry);
		targetZipFail.setLatitude(51.12);
		targetZipFail.setLongitude(10.12);
		locatableRepository.save(targetZipFail);
		
		final Zip targetZipSuccess = entityFactory.persistDefaultZip(defaultCountry);
		targetZipSuccess.setLatitude(42.52);
		targetZipSuccess.setLongitude(10.12);
		locatableRepository.save(targetZipSuccess);
		
		// Ensure matches table is empty
		checkEmpty();
		
		// check first success run
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		entityFactory.persistDefaultRegionAnswerWithProximitySearchRequest(sourceUser, sourceZip);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		targetUser.addAddress(DefaultStaticEntityFactory.createDefaultAddress(targetZipFail));
		userRepository.save(targetUser);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		targetUser.addAddress(DefaultStaticEntityFactory.createDefaultAddress(targetZipSuccess));
		userRepository.save(targetUser);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
	}
	
	@Test
	public void matchWithRelocationOnly() throws Exception
	{
		final Continent defaultContinent = entityFactory.persistDefaultContinent();
		final Country defaultCountry = entityFactory.persistDefaultCountry(defaultContinent);
		final Zip defaultZip = entityFactory.persistDefaultZip(defaultCountry);
		
		final Continent defaultContinent2 = entityFactory.persistDefaultContinent();
		final Country defaultCountry2 = entityFactory.persistDefaultCountry(defaultContinent2);
		final Zip defaultZip2 = entityFactory.persistDefaultZip(defaultCountry2);
		
		sourceUser.addAddress(DefaultStaticEntityFactory.createDefaultAddress(defaultZip));
		userRepository.save(sourceUser);
		
		targetUser.addAddress(DefaultStaticEntityFactory.createDefaultAddress(defaultZip2));
		userRepository.save(targetUser);
		
		// Ensure matches table is empty
		checkEmpty();
		
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		RegionAnswer partnerRegionAnswerSrc, partnerRegionAnswerTrgt;
		
		partnerRegionAnswerSrc = entityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(sourceUser, defaultZip.getRegion());
		partnerRegionAnswerTrgt = entityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(targetUser, defaultZip2.getRegion());
		partnerRegionAnswerSrc.setSearchRelocatable(true);
		partnerRegionAnswerTrgt.setRelocatable(true);
		answerRepository.save(partnerRegionAnswerSrc);
		answerRepository.save(partnerRegionAnswerTrgt);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		answerRepository.delete(partnerRegionAnswerSrc);
		answerRepository.delete(partnerRegionAnswerTrgt);
		partnerRegionAnswerSrc = entityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(sourceUser, defaultZip2.getRegion());
		partnerRegionAnswerTrgt = entityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(targetUser, defaultZip2.getRegion());
		partnerRegionAnswerSrc.setSearchRelocatable(true);
		partnerRegionAnswerTrgt.setRelocatable(true);
		answerRepository.save(partnerRegionAnswerSrc);
		answerRepository.save(partnerRegionAnswerTrgt);
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		answerRepository.delete(partnerRegionAnswerSrc);
		answerRepository.delete(partnerRegionAnswerTrgt);
		partnerRegionAnswerSrc = entityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(sourceUser, defaultZip.getRegion());
		partnerRegionAnswerTrgt = entityFactory.persistDefaultRegionAnswerWithRegionSearchRequest(targetUser, defaultZip.getRegion());
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		partnerRegionAnswerSrc.setSearchRelocatable(true);
		partnerRegionAnswerTrgt.setRelocatable(true);
		answerRepository.save(partnerRegionAnswerSrc);
		answerRepository.save(partnerRegionAnswerTrgt);
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
	}
	
	@Test
	public void testDeleteCorruptedMatches()
	{
		// Ensure matches table is empty
		checkEmpty();

		// Switch source and target users to get an inverse ids order
		Match match = new Match();
		match.setSourceUserId(targetUser.getId());
		match.setTargetUserId(sourceUser.getId());
		match.setStrictness(Strictness.firstStrictness);
		match.setNumber(1);
		match.setCategory(RecommendationCategory.PARTNERSHIP);
		matchRepository.save(match);

		// Ensure that the match with inverse ids order was saved
		Assert.assertThat(matchRepository.count(), equalTo(1L));

		matchingService.deleteCorruptedMatches(new PerformanceLog(matchStatisticRepository, MatchingScope.MATCHING));

		Assert.assertThat(matchRepository.count(), equalTo(0L));
	}
	
	@Test
	public void testDeleteUnnecessaryMatches()
	{
		// Ensure matches table is empty
		checkEmpty();

		final User userWithoutSubscription = entityFactory.persistDefaultUser("userWithoutSubscription",
				RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);
		Match userWithoutSubscriptionMatch = new Match(sourceUser, userWithoutSubscription, RecommendationCategory.PARTNERSHIP);
		userWithoutSubscriptionMatch.setStrictness(Strictness.firstStrictness);
		userWithoutSubscriptionMatch.setNumber(1);
		matchRepository.save(userWithoutSubscriptionMatch);

		final User deletedUser = entityFactory.persistDefaultUser("deletedUser",
				RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);
		userRepository.deleteUser(MemberStatus.DELETED ,deletedUser);
		Match deletedUserMatch = new Match(sourceUser, deletedUser, RecommendationCategory.PARTNERSHIP);
		deletedUserMatch.setStrictness(Strictness.firstStrictness);
		deletedUserMatch.setNumber(1);
		matchRepository.save(deletedUserMatch);

		Match matchWithStrictnessFive = new Match(sourceUser, targetUser, RecommendationCategory.PARTNERSHIP);
		matchWithStrictnessFive.setStrictness(Strictness.lastStrictness);
		matchWithStrictnessFive.setNumber(1);
		matchRepository.save(matchWithStrictnessFive);

		final SubscriptionOffer friendshipOffer = paymentEntityFactory.persistInitialSubscriptionOffer("friendshipOffer",
				LocalDateTime.now(), RecommendationCategory.FRIENDSHIP);
		final User normalUserWithFriendshipOnly = entityFactory.persistDefaultUser("normalUserWithFriendshipOnly",
				RecommendationCategory.FRIENDSHIP);
		paymentEntityFactory.persistSubscription(normalUserWithFriendshipOnly, friendshipOffer, LocalDateTime.now());
		Match matchPartnership = new Match(sourceUser, normalUserWithFriendshipOnly, RecommendationCategory.PARTNERSHIP);
		matchPartnership.setStrictness(Strictness.firstStrictness);
		matchPartnership.setNumber(1);
		matchRepository.save(matchPartnership);

		final SubscriptionOffer offer = paymentEntityFactory.persistInitialSubscriptionOffer("offer",
				LocalDateTime.now(), RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);
		final User normalUser = entityFactory.persistDefaultUser("normalUser",
				RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);
		paymentEntityFactory.persistSubscription(normalUser, offer, LocalDateTime.now());
		Match match = new Match(sourceUser, normalUser, RecommendationCategory.PARTNERSHIP);
		match.setStrictness(Strictness.firstStrictness);
		match.setNumber(1);
		match = matchRepository.save(match);

		/*
		* Ensure we stored unnecessary matches with:
		*  - not relevant recommendation categories
		*  - strictness 4 or more
		*  - deleted users
		*  - users without subscription
		*
		* and one normal match that shouldn't be deleted.
		*/
		Assert.assertThat(matchRepository.count(), equalTo(5L));

		matchingService.deleteUnnecessaryMatches(new PerformanceLog(matchStatisticRepository, MatchingScope.MATCHING));

		// The normal match should stay
		Assert.assertThat(matchRepository.count(), equalTo(1L));
		Assert.assertThat(matchRepository.findAll().iterator().next(), equalTo(match));
	}
	
	@Test
	public void testMissingRequiredAnswers()
	{
		// Ensure matches table is empty
		checkEmpty();
		
		// check first success run
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		final NumberQuestionsMapping numberQuestionsMapping = entityFactory.persistDefaultNumberQuestionsMapping(RecommendationCategory.PARTNERSHIP);
		
		// check run not yet required
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
		
		final NumberQuestion question = numberQuestionsMapping.getFactQuestion();
		question.setRequirement(Requirement.REQUIRED);
		questionRepository.save(question);
		
		// check run with required now
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		setNumberAnswer(question, 1, sourceUser, true);
		
		// check run with required now and sourceUser with answer
		matchingService.startMatching();
		checkStrictness(Strictness.lastStrictness);
		
		setNumberAnswer(question, 1, targetUser, true);
		
		// check run with required now and both with answer
		matchingService.startMatching();
		checkStrictness(Strictness.firstStrictness);
	}
}
