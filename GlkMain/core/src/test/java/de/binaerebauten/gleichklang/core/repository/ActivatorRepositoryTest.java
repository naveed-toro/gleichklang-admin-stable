package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.matching.Activator;
import de.binaerebauten.gleichklang.core.model.matching.QuestionActivator;
import de.binaerebauten.gleichklang.core.model.matching.QuestionGroupActivator;
import de.binaerebauten.gleichklang.core.model.matching.QuestionnaireActivator;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ActivatorRepositoryTest extends AbstractRepositoryTest<Activator>
{
	@Autowired
	private ActivatorRepository activatorRepository;
	
	@Autowired
	private DefaultEntityFactory entityFactory;
	private QuestionActivator questionActivator;
	private QuestionnaireActivator questionnaireActivator;
	private QuestionGroupActivator questionGroupActivator;
	
	public ActivatorRepositoryTest()
	{
	}

	@Override
	protected Collection<Activator> getPersistedEntities()
	{
		questionActivator = entityFactory.persistDefaultQuestionActivator();
		questionnaireActivator = entityFactory.persistDefaultQuestionnaireActivator();
		questionGroupActivator = entityFactory.persistDefaultQuestionGroupActivator();
		
		return Arrays.asList(questionActivator, questionnaireActivator, questionGroupActivator);
	}

	@Override
	protected JpaRepository<Activator, Long> getRepository()
	{
		return activatorRepository;
	}
	
	@Test
	public void testExistsEnablesQuestion()
	{
		final Question question = entityFactory.persistDefaultTextQuestion();
		
		assertThat(activatorRepository.existsEnablesQuestion(questionActivator.getEnablesQuestion()), equalTo(true));
		assertThat(activatorRepository.existsEnablesQuestion(question), equalTo(false));
	}
	
	@Test
	public void testExistsEnablesQuestionGroup()
	{
		final QuestionGroup questionGroup = entityFactory.persistDefaultQuestionGroup(entityFactory.persistDefaultQuestionnaire());
		
		assertThat(activatorRepository.existsEnablesQuestionGroup(questionGroupActivator.getEnablesQuestionGroup()), equalTo(true));
		assertThat(activatorRepository.existsEnablesQuestionGroup(questionGroup), equalTo(false));
	}
	
	@Test
	public void testExistsEnablesQuestionnaire()
	{
		final Questionnaire questionnaire = entityFactory.persistDefaultQuestionnaire();
		
		assertThat(activatorRepository.existsEnablesQuestionnaire(questionnaireActivator.getEnablesQuestionnaire()), equalTo(true));
		assertThat(activatorRepository.existsEnablesQuestionnaire(questionnaire), equalTo(false));
	}
}
