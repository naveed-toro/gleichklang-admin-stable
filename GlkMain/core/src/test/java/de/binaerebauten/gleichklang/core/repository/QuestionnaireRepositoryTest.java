package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.view.filter.QuestionnaireFilter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class QuestionnaireRepositoryTest extends AbstractRepositoryTest<Questionnaire>
{
	@Autowired
	private QuestionnaireRepository questionnaireRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Override
	protected Collection<Questionnaire> getPersistedEntities()
	{
		List<Questionnaire> questionnaires = new ArrayList<>();
		List<RecommendationCategory> orderedRecommendationCategories = Arrays.asList(RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);

		Questionnaire questionnaire = new Questionnaire();
		questionnaire.setRecommendationCategory(orderedRecommendationCategories.get(1));
		questionnaire.setI18nKey(defaultEntityFactory.persistDefaultI18NEntry("1").getKey());
		questionnaire.setSortOrder(1);

		Questionnaire questionnaire1 = new Questionnaire();
		questionnaire1.setRecommendationCategory(orderedRecommendationCategories.get(0));
		questionnaire1.setI18nKey(defaultEntityFactory.persistDefaultI18NEntry("0").getKey());
		questionnaire1.setSortOrder(0);

		questionnaire = questionnaireRepository.save(questionnaire);
		questionnaire1 = questionnaireRepository.save(questionnaire1);

		questionnaires.add(questionnaire);
		questionnaires.add(questionnaire1);

		return questionnaires;
	}

	@Override
	protected JpaRepository<Questionnaire, Long> getRepository()
	{
		return questionnaireRepository;
	}

	@Test
	public void testFindAllWithSpec() throws Exception
	{
		defaultEntityFactory.reset();

		List<I18NEntity> i18NEntities = defaultEntityFactory.persistDefaultI18NEntries("questionnaire", 8);

		Questionnaire deletedQuestionnaire = defaultEntityFactory.persistDefaultQuestionnaireWithQuestionGroup
				(RecommendationCategory.FRIENDSHIP, i18NEntities.get(0));
		deletedQuestionnaire.setDeleted(true);
		questionnaireRepository.save(deletedQuestionnaire);

		List<Questionnaire> questionnairesResult = questionnaireRepository.findAll(
				new QuestionnaireFilter(EnumSet.of(RecommendationCategory.FRIENDSHIP), true));
		assertThat("no questionnaires will be found because questionnaire is deleted ", questionnairesResult
				.isEmpty(), is(true));

		defaultEntityFactory.persistDefaultQuestionnaireWithQuestionGroup
				(RecommendationCategory.FRIENDSHIP, i18NEntities.get(1));

		defaultEntityFactory.persistDefaultQuestionnaireWithQuestionGroup
				(RecommendationCategory.PARTNERSHIP, i18NEntities.get(3));
		defaultEntityFactory.persistDefaultQuestionnaireWithQuestionGroup
				(RecommendationCategory.PARTNERSHIP, i18NEntities.get(4));

		defaultEntityFactory.persistDefaultQuestionnaireWithQuestionGroup
				(null, i18NEntities.get(5));

		questionnairesResult = questionnaireRepository.findAll(
				new QuestionnaireFilter(Arrays.asList(null, RecommendationCategory.FRIENDSHIP), true));
		assertThat("all active with friendship and null category", questionnairesResult.size(),
				equalTo(2));

		questionnairesResult = questionnaireRepository.findAll(
				new QuestionnaireFilter(Collections.singleton(null), true));
		assertThat("all active with null category", questionnairesResult.size(), equalTo(1));

		questionnairesResult = questionnaireRepository.findAll(
				new QuestionnaireFilter(Arrays.asList(RecommendationCategory.PARTNERSHIP), true));
		assertThat("all active with partnership", questionnairesResult.size(), equalTo(2));

		questionnairesResult = questionnaireRepository.findAll(
				new QuestionnaireFilter(Arrays.asList(null, RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP), true));
		assertThat("all active", questionnairesResult.size(), equalTo(4));
	}
	
	@Test
	public void testFindAllOptionalQuestionnaires()
	{
		defaultEntityFactory.reset();
		
		createQuestion(false, Requirement.OPTIONAL, true);
		createQuestion(false, Requirement.REQUIRED, false);
		createQuestion(true, Requirement.OPTIONAL, false);
		final Question optionalQuestion = createQuestion(false, Requirement.OPTIONAL, false);
		
		final List<Questionnaire> optionalQuestionnaire = questionnaireRepository.findAllQuestionnaireForRequirement(Collections.singleton(Requirement.OPTIONAL));
		assertThat(optionalQuestionnaire.size(), equalTo(1));
		assertThat(optionalQuestionnaire.get(0), equalTo(optionalQuestion.getQuestionGroup().getQuestionnaire()));
	}
	
	private Question createQuestion(boolean deleted, Requirement requirement, boolean onlyAdminVisible)
	{
		final Question q = defaultEntityFactory.persistDefaultTextQuestion();
		q.setDeleted(deleted);
		q.setRequirement(requirement);
		q.setOnlyAdminVisible(onlyAdminVisible);
		
		return questionRepository.save(q);
	}

	@Test
	public void testSaveQuestionWithDefaultEmptyAnswer()
	{
		final Questionnaire questionnaire = defaultEntityFactory.persistDefaultQuestionnaire();
		final QuestionGroup questionGroup = defaultEntityFactory.persistDefaultQuestionGroup(questionnaire);
		final ChoiceQuestion question = defaultEntityFactory.persistDefaultChoiceQuestion(questionGroup, defaultEntityFactory.persistDefaultI18NEntry());
		final Optional<Choice> choice = defaultEntityFactory.persistDefaultChoiceGroup().getChoices().stream().findFirst();
		
		if (choice.isPresent())
		{
			Choice defaultChoice = choice.get();

			question.setDeleted(false);
			question.setRequirement(Requirement.OPTIONAL);
			question.setOnlyAdminVisible(false);
			question.setDefaultChoice(defaultChoice);
			
			questionRepository.save(question);
		}
		else
		{
			assert false;
		}
	}

}
