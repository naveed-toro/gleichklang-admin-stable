package de.binaerebauten.gleichklang.core.service;

import com.google.common.collect.Iterables;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.LocalizedEntity;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AnswerRepository;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionRepository;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.utils.DatabaseResourceBundleControl;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@see AnswerService}.
 */
public class AnswerServiceTest extends BasePersistenceTest
{
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private AnswerRepository answerRepository;

	@Autowired
	private AnswerService answerService;

	@Autowired
	private I18NRepository i18NRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	private User user;
	private QuestionnaireActivation activation;
	private Questionnaire questionnaire;
	private QuestionGroup questionGroup;
	private List<Question> questions;
	private ChoiceQuestion sexQuestion;

	@Before
	public void setUp() throws Exception
	{
		LocalizedEntity.setControl(new DatabaseResourceBundleControl(i18NRepository));
		user = defaultEntityFactory.persistDefaultUser("testUser3");
		activation = new QuestionnaireActivation(user, Collections.emptyList());

		questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);
		questions = defaultEntityFactory.persistDefaultRequiredQuestions(questionGroup);
		I18NEntity sex = defaultEntityFactory
				.persistDefaultI18NEntry(I18NEntity.BaseName.QUESTION_NAME,
						I18NEntity.Language.DE,
						NaturalKeyEntity.NaturalKey.SEX.naturalKey, "Geschlecht");
		sexQuestion = defaultEntityFactory.persistDefaultChoiceQuestion(questionGroup, sex);
		sexQuestion.setRequirement(Requirement.REQUIRED);
		questionRepository.save(sexQuestion);
		questions.add(sexQuestion);
	}

	@After
	public void teardown()
	{
		defaultEntityFactory.reset();
	}

	@Test
	public void testGetIncompleteAndMissingAnswers() throws Exception
	{
		List<Answer> incompleteAndMissingAnswers = answerService.getIncompleteAndMissingAnswers(user, questionnaire, activation, Collections.singleton(Requirement.REQUIRED));
		assertThat(incompleteAndMissingAnswers.size(), equalTo(questions.size()));

		TextQuestion textQuestion = Iterables.filter(questions, TextQuestion.class).iterator().next();
		TextAnswer answer = AnswerFactory.get().createNewAnswer(textQuestion, user);
		answerRepository.save(answer);

		incompleteAndMissingAnswers = answerService.getIncompleteAndMissingAnswers(user, questionnaire, activation, Collections.singleton(Requirement.REQUIRED));
		assertThat(incompleteAndMissingAnswers.size(), equalTo(questions.size()));

		answer.setTextValue("Test");
		answerRepository.save(answer);

		incompleteAndMissingAnswers = answerService.getIncompleteAndMissingAnswers(user, questionnaire, activation, Collections.singleton(Requirement.REQUIRED));
		assertThat(incompleteAndMissingAnswers.size(), equalTo(questions.size() - 1));
	}

	@Test
	public void testGetAnswerValue()
	{
		ChoiceAnswer answer = AnswerFactory.get().createNewAnswer(sexQuestion, user);
		answerRepository.save(answer);

		String answerValue = answerService.getAnswerValue(user, NaturalKeyEntity.NaturalKey.SEX);
		assertThat(answerValue, is(""));

		Set<Choice> singleton = Collections.singleton(sexQuestion.getChoiceGroup().getChoices().get(0));
		answer.setChoices(singleton);
		answerRepository.save(answer);

		answerValue = answerService.getAnswerValue(user, NaturalKeyEntity.NaturalKey.SEX);
		assertThat(answerValue, is(answer.getValue()));
	}

	@Test
	public void testGetAnswersVisibleToOtherUsers()
	{
		List<Answer> answersVisibleToOtherUsers = answerService.getAnswersVisibleToOtherUsers(user, NaturalKeyEntity.NaturalKey.SEX);

		assertThat(answersVisibleToOtherUsers.size(), is(0));

		ChoiceAnswer answer = AnswerFactory.get().createNewAnswer(sexQuestion, user);
		answer.setChoices(Collections.singleton(sexQuestion.getChoiceGroup().getChoices().iterator().next()));

		for (boolean adjustableRelationshipVisible : Arrays.asList(true, false))
		{
			sexQuestion.setAdjustableRelationshipVisibility(adjustableRelationshipVisible);
			questionRepository.save(sexQuestion);

			for (boolean relationshipVisible : Arrays.asList(true, false))
			{
				answer.setRelationshipVisible(relationshipVisible);
				answerRepository.save(answer);

				int expectedSize = answer.isVisibleToOtherUsers() ? 1 : 0;

				answersVisibleToOtherUsers = answerService.getAnswersVisibleToOtherUsers(user, NaturalKeyEntity.NaturalKey.SEX);
				assertThat(answersVisibleToOtherUsers.size(), is(expectedSize));
			}
		}
	}

	@Test
	public void testSave() throws UniqueValidationException
	{
		TextQuestion textQuestion = Iterables.filter(questions, TextQuestion.class).iterator().next();
		TextAnswer answer = AnswerFactory.get().createNewAnswer(textQuestion, user);

		answerService.save(answer);

		// TODO @FH: should throw UniqueValidationException
		TextAnswer answer2 = AnswerFactory.get().createNewAnswer(textQuestion, user);
		answerService.save(answer2);
	}
}
