package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link QuestionRepository}.
 */
public class QuestionRepositoryTest extends AbstractRepositoryTest<Question>
{
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	private Questionnaire questionnaire;
	private List<Question> questions = new ArrayList<>();
	private QuestionGroup questionGroup;
	
	@Test
	public void testFindActiveQuestionsForQuestionGroupsWithAdminVisible()
	{
		Question adminVisibleQuestion = questions.get(0);
		adminVisibleQuestion.setOnlyAdminVisible(true);
		questionRepository.save(adminVisibleQuestion);
		
		List<Question> activeQuestionsWithAdminVisible = questionRepository.findActiveQuestionsForQuestionGroupsWithAdminVisible(Collections.singleton(questionGroup));
		assertThat(activeQuestionsWithAdminVisible.size(), is(questions.size()));
		
		Question deletedQuestion = questions.get(1);
		deletedQuestion.setDeleted(true);
		questionRepository.save(deletedQuestion);
		
		activeQuestionsWithAdminVisible = questionRepository.findActiveQuestionsForQuestionGroupsWithAdminVisible(Collections.singleton(questionGroup));
		assertThat(activeQuestionsWithAdminVisible.size(), is(questions.size() - 1));
	}
	
	@Test
	public void testFindActiveQuestionsForQuestionGroups()
	{
		Question adminVisibleQuestion = questions.get(0);
		adminVisibleQuestion.setOnlyAdminVisible(true);
		questionRepository.save(adminVisibleQuestion);
		
		List<Question> activeQuestions = questionRepository.findActiveQuestionsForQuestionGroups(Collections.singleton(questionGroup));
		assertThat(activeQuestions.size(), is(questions.size() - 1));
		
		Question deletedQuestion = questions.get(1);
		deletedQuestion.setDeleted(true);
		questionRepository.save(deletedQuestion);
		
		activeQuestions = questionRepository.findActiveQuestionsForQuestionGroups(Collections.singleton(questionGroup));
		assertThat(activeQuestions.size(), is(questions.size() - 2));
	}
	
	@Test
	public void testFindActiveQuestionsForQuestionnaire()
	{
		Question adminVisibleQuestion = questions.get(0);
		adminVisibleQuestion.setOnlyAdminVisible(true);
		questionRepository.save(adminVisibleQuestion);
		
		List<Question> activeQuestions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, Collections.singleton(Requirement.REQUIRED));
		assertThat(activeQuestions.size(), is(questions.size() - 1));
		
		Question deletedQuestion = questions.get(1);
		deletedQuestion.setDeleted(true);
		questionRepository.save(deletedQuestion);
		
		activeQuestions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, Collections.singleton(Requirement.REQUIRED));
		assertThat(activeQuestions.size(), is(questions.size() - 2));
		
		Question optionalQuestion = questions.get(2);
		optionalQuestion.setRequirement(Requirement.OPTIONAL);
		questionRepository.save(optionalQuestion);
		
		activeQuestions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, Collections.singleton(Requirement.REQUIRED));
		assertThat(activeQuestions.size(), is(questions.size() - 3));
		
		activeQuestions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, Collections.singleton(Requirement.OPTIONAL));
		assertThat(activeQuestions, is(Collections.singletonList(optionalQuestion)));
	}
	
	private <T extends Question> T createQuestion(boolean deleted, Requirement requirement, boolean onlyAdminVisible, Class<T> questionType)
	{
		final QuestionGroup questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(defaultEntityFactory.persistDefaultQuestionnaire());
		final T q;
		if (questionType == TextQuestion.class)
			q = (T) defaultEntityFactory.persistDefaultTextQuestion(questionGroup, defaultEntityFactory.persistDefaultI18NEntry());
		else if (questionType == ChoiceQuestion.class)
			q = (T) defaultEntityFactory.persistDefaultChoiceQuestion(questionGroup, defaultEntityFactory.persistDefaultI18NEntry());
		else if (questionType == NumberQuestion.class)
			q = (T) defaultEntityFactory.persistDefaultNumberQuestion(questionGroup, defaultEntityFactory.persistDefaultI18NEntry());
		else return null;
		
		q.setDeleted(deleted);
		q.setRequirement(requirement);
		q.setOnlyAdminVisible(onlyAdminVisible);
		
		return questionRepository.save(q);
	}
	
	private Question createQuestion(boolean deleted, Requirement requirement, boolean onlyAdminVisible)
	{
		return createQuestion(deleted, requirement, onlyAdminVisible, TextQuestion.class);
	}
	
	@Test
	public void testFindAllOptionalQuestions()
	{
		defaultEntityFactory.reset();
		
		createQuestion(false, Requirement.OPTIONAL, true);
		createQuestion(false, Requirement.REQUIRED, false);
		createQuestion(true, Requirement.OPTIONAL, false);
		final Question optionalQuestion = createQuestion(false, Requirement.OPTIONAL, false);
		
		final List<Question> optionalQuestions = questionRepository.findAllQuestionsForRequirement(Collections.singleton(Requirement.OPTIONAL));
		assertThat(optionalQuestions.size(), equalTo(1));
		assertThat(optionalQuestions.get(0), equalTo(optionalQuestion));
	}
	
	@Test
	public void testFindAllActivatingQuestions()
	{
		defaultEntityFactory.reset();
		
		createQuestion(false, Requirement.REQUIRED, false, TextQuestion.class);
		createQuestion(false, Requirement.OPTIONAL, false, TextQuestion.class);
		
		createQuestion(false, Requirement.REQUIRED, false, NumberQuestion.class);
		createQuestion(false, Requirement.OPTIONAL, false, NumberQuestion.class);
		
		createQuestion(false, Requirement.REQUIRED, true, ChoiceQuestion.class);
		createQuestion(false, Requirement.OPTIONAL, true, ChoiceQuestion.class);
		
		createQuestion(true, Requirement.REQUIRED, false, ChoiceQuestion.class);
		createQuestion(true, Requirement.OPTIONAL, false, ChoiceQuestion.class);
		
		final ChoiceQuestion aq1 = createQuestion(false, Requirement.REQUIRED, false, ChoiceQuestion.class);
		final ChoiceQuestion aq2 = createQuestion(false, Requirement.OPTIONAL, false, ChoiceQuestion.class);
		
		final List<ChoiceQuestion> activatingQuestions = questionRepository.findAllActivatingQuestions();
		assertThat(activatingQuestions.size(), equalTo(2));
		assertThat(activatingQuestions, hasItems(aq1, aq2));
	}
	
	@Override
	protected Collection<Question> getPersistedEntities()
	{
		questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);
		questions = defaultEntityFactory.persistDefaultRequiredQuestions(questionGroup);
		return questions;
	}
	
	@Override
	protected JpaRepository<Question, Long> getRepository()
	{
		return questionRepository;
	}
}
