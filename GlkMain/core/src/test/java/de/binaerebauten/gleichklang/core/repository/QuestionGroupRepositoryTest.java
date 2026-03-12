package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class QuestionGroupRepositoryTest extends AbstractRepositoryTest<QuestionGroup>
{
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private QuestionGroupRepository questionGroupRepository;
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	private Questionnaire questionnaire;
	private QuestionGroup questionGroup;
	
	@Test
	public void testFindActiveByQuestionnaire() throws Exception
	{
		initQuestionGroup();
		List<QuestionGroup> questionGroups = questionGroupRepository.findActiveByQuestionnaire(questionnaire);
		assertThat(questionGroups.isEmpty(), is(false));
	}
	
	@Override
	protected Collection<QuestionGroup> getPersistedEntities()
	{
		initQuestionGroup();
		return Collections.singletonList(questionGroup);
	}
	
	private void initQuestionGroup()
	{
		questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);
	}
	
	@Override
	protected JpaRepository<QuestionGroup, Long> getRepository()
	{
		return questionGroupRepository;
	}
	
	@Test
	public void testFindAllOptionalQuestionGroups()
	{
		defaultEntityFactory.reset();
		
		createQuestion(false, Requirement.OPTIONAL, true);
		createQuestion(false, Requirement.REQUIRED, false);
		createQuestion(true, Requirement.OPTIONAL, false);
		final Question optionalQuestion = createQuestion(false, Requirement.OPTIONAL, false);
		
		final List<QuestionGroup> optionalQuestionGroups = questionGroupRepository.findAllQuestionGroupsForRequirement(Collections.singleton(Requirement.OPTIONAL));
		assertThat(optionalQuestionGroups.size(), equalTo(1));
		assertThat(optionalQuestionGroups.get(0), equalTo(optionalQuestion.getQuestionGroup()));
	}
	
	private Question createQuestion(boolean deleted, Requirement requirement, boolean onlyAdminVisible)
	{
		final Question q = defaultEntityFactory.persistDefaultTextQuestion();
		q.setDeleted(deleted);
		q.setRequirement(requirement);
		q.setOnlyAdminVisible(onlyAdminVisible);
		
		return questionRepository.save(q);
	}
}