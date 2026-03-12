package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.model.matching.QuestionActivator;
import de.binaerebauten.gleichklang.core.model.matching.QuestionnaireActivator;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.utils.DatabaseResourceBundleControl;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@see QuestionnaireService}.
 */
public class QuestionnaireServiceTest extends BasePersistenceTest
{
	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private QuestionnaireService questionnaireService;

	@Autowired
	private I18NRepository i18NRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private QuestionnaireRepository questionnaireRepository;
	
	@Before
	public void setUp() throws Exception
	{
		LocalizedEntity.setControl(new DatabaseResourceBundleControl(i18NRepository));
	}

	@After
	public void teardown()
	{
		defaultEntityFactory.reset();
	}

	@Test
	public void testGetQuestionnaires() throws Exception
	{
		List<I18NEntity> i18NEntities = defaultEntityFactory.persistDefaultI18NEntries("questionnaire", 8);

		defaultEntityFactory.persistDefaultQuestionnaireWithQuestionGroup(RecommendationCategory.FRIENDSHIP, i18NEntities.get(0));

		defaultEntityFactory.persistDefaultQuestionnaireWithQuestionGroup(RecommendationCategory.PARTNERSHIP, i18NEntities.get(1));
		defaultEntityFactory.persistDefaultQuestionnaireWithQuestionGroup(RecommendationCategory.PARTNERSHIP, i18NEntities.get(2));

		defaultEntityFactory.persistDefaultQuestionnaireWithQuestionGroup(null, i18NEntities.get(3))
		;
		List<Questionnaire> friendshipQuestionnaires = questionnaireService.getQuestionnaires
				(Arrays.asList(RecommendationCategory.FRIENDSHIP), true);
		assertThat(friendshipQuestionnaires.size(), equalTo(1));

		List<Questionnaire> partnershipQuestionnaires = questionnaireService.getQuestionnaires
				(Arrays.asList(RecommendationCategory.PARTNERSHIP), true);
		assertThat(partnershipQuestionnaires.size(), equalTo(2));

		List<Questionnaire> allQuestionnaires = questionnaireService.getQuestionnaires
				(Arrays.asList(null, RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP), true);
		assertThat(allQuestionnaires.size(), equalTo(4));
	}

	@Test
	public void testGetNextStep() throws Exception
	{
		User user = defaultEntityFactory.persistDefaultUser("testUser");
		QuestionnaireActivation activation = new QuestionnaireActivation(user, Collections.emptyList());

		List<Questionnaire> questionnaires = questionnaireRepository.findAll();
		assertThat(questionnaires.isEmpty(), equalTo(true));

		Questionnaire questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		QuestionGroup questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);

		List<Question> questions = defaultEntityFactory.persistDefaultRequiredQuestions(questionGroup);
		defaultEntityFactory.persistDefaultAnswers(questions, user);

		questionnaireRepository.findAll();

		TextQuestion textQuestion = defaultEntityFactory.persistDefaultTextQuestion(questionGroup, defaultEntityFactory.persistDefaultI18NEntry());
		textQuestion.setRequirement(Requirement.REQUIRED);
		questionRepository.save(textQuestion);

		final TextAnswer textAnswer = new TextAnswer();
		textAnswer.setQuestion(textQuestion);
		textAnswer.setUser(user);
		answerRepository.save(textAnswer);

		assertThat("Expected that questionnaire has not filled required answers",
				questionnaireService.isCompletedQuestionnaire(user, questionnaire, activation), equalTo(false));
	}

	@Test
	public void testIsCompletedQuestionnaire() throws Exception
	{
		User user = defaultEntityFactory.persistDefaultUser("testUser3");
		QuestionnaireActivation activation = new QuestionnaireActivation(user, Collections.emptyList());

		Questionnaire questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		QuestionGroup questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);
		List<Question> questions = defaultEntityFactory.persistDefaultRequiredQuestions(questionGroup);

		defaultEntityFactory.persistDefaultAnswers(questions, user);

		assertThat("Questionnaire is completed",
				questionnaireService.isCompletedQuestionnaire(user, questionnaire, activation), equalTo(true));

		TextQuestion textQuestion = defaultEntityFactory.persistDefaultTextQuestion(questionGroup,
				defaultEntityFactory.persistDefaultI18NEntry("not_answered_question"));

		TextAnswer textAnswer = new TextAnswer();
		textAnswer.setQuestion(textQuestion);
		textAnswer.setUser(user);
		textAnswer = answerRepository.save(textAnswer);

		assertThat(questionnaireService.isCompletedQuestionnaire(user, questionnaire, activation), is(false));

		textAnswer.setTextValue("hh");
		answerRepository.save(textAnswer);

		assertThat(questionnaireService.isCompletedQuestionnaire(user, questionnaire, activation), is(true));
	}

	@Test
	public void testIsAllQuestionnairesAnswered() throws Exception
	{
		questionRepository.deleteAll();
		User user = defaultEntityFactory.persistDefaultUser("testUser3");
		Questionnaire questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		QuestionGroup questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);
		List<Question> questions = defaultEntityFactory.persistDefaultRequiredQuestions(questionGroup);
		
		assertThat(questionnaireService.isAllQuestionnairesAnswered(user), is(false));
		
		defaultEntityFactory.persistDefaultAnswers(questions, user);
		
		assertThat(questionnaireService.isAllQuestionnairesAnswered(user), is(true));
	}

	@Test
	public void testGetActivation() throws Exception
	{
		QuestionActivator questionActivator = defaultEntityFactory.persistDefaultQuestionActivator();
		defaultEntityFactory.persistDefaultQuestionGroupActivator();
		QuestionnaireActivator questionnaireActivator = defaultEntityFactory.persistDefaultQuestionnaireActivator();
		
		User user = defaultEntityFactory.persistDefaultUser("test user");
		
		QuestionnaireActivation activation = questionnaireService.getActivation(user);
		
		ChoiceAnswer choiceAnswer = AnswerFactory.get().createNewAnswer(questionActivator.getActivatingQuestion(), user);
		choiceAnswer.setChoices(questionActivator.getActivatingChoices());
		choiceAnswer = answerRepository.save(choiceAnswer);
		
		activation.updateActivatingAnswer(choiceAnswer, choiceAnswer.getChoices());
		
		List<Question> enabledQuestions = Arrays.asList(questionActivator.getEnablesQuestion());
		assertThat("User answered the activation question with the activation choices enabled questions should have one value and map it to true",
				activation.getEnabledQuestions(enabledQuestions), equalTo(enabledQuestions));
		
		choiceAnswer.setChoices(new HashSet<>());
		choiceAnswer = answerRepository.save(choiceAnswer);
		
		activation.updateActivatingAnswer(choiceAnswer, choiceAnswer.getChoices());
		
		enabledQuestions = Arrays.asList(questionActivator.getEnablesQuestion());
		assertThat("User answered the activation question with no activation choices enabled questions should have one value and map it to false",
				activation.getEnabledQuestions(enabledQuestions), equalTo(Collections.emptyList()));
		
		ChoiceAnswer choiceAnswer1 = AnswerFactory.get().createNewAnswer(questionnaireActivator.getActivatingQuestion(), user);
		choiceAnswer1.setChoices(questionnaireActivator.getActivatingChoices());
		choiceAnswer1 = answerRepository.save(choiceAnswer1);
		
		List<Answer> activatingAnswers = new ArrayList<>();
		activatingAnswers.add(choiceAnswer);
		activatingAnswers.add(choiceAnswer1);
		
		activation.updateActivatingAnswers(activatingAnswers);
		
		enabledQuestions = Arrays.asList(questionActivator.getEnablesQuestion());
		assertThat("User answered the activation question for enabled question with no activating choices",
				activation.getEnabledQuestions(enabledQuestions), equalTo(Collections.emptyList()));
		List<Questionnaire> enabledQuestionnaires = Arrays.asList(questionnaireActivator.getEnablesQuestionnaire());
		assertThat("User answered the activation question for enabled questionnaire with activating choices",
				activation.getEnabledQuestionnaires(enabledQuestionnaires), equalTo(enabledQuestionnaires));
	}
}
