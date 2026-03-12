package de.binaerebauten.gleichklang.core.service;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.binaerebauten.gleichklang.core.model.matching.Activator;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ActivatorRepository;
import de.binaerebauten.gleichklang.core.repository.AnswerRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionnaireRepository;
import de.binaerebauten.gleichklang.core.view.filter.QuestionnaireFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement.requiredRequirements;

/**
 * This service provides operations for questionnaires, question groups and questions.
 */
@Service
public class QuestionnaireService
{
	@Autowired
	private AnswerRepository answerRepository;
	
	@Autowired
	private QuestionnaireRepository questionnaireRepository;
	
	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private ActivatorRepository activatorRepository;
	
	/**
	 * Returns the active questionnaires for the given recommendation categories.
	 *
	 * @param recommendationCategories the non-null recommendation groups, can include a null value to retrieve questionnaires
	 *                                 without a recommendation category
	 * @return a list of questionnaires sorted by {@link Questionnaire#COMPARE_BY_CATEGORY_AND_SORTORDER}
	 */
	@Transactional
	public List<Questionnaire> getQuestionnaires(Collection<RecommendationCategory> recommendationCategories, boolean withAdminVisible)
	{
		Objects.requireNonNull(recommendationCategories, "recommendationCategories == null");

		final QuestionnaireFilter filter = new QuestionnaireFilter(recommendationCategories, withAdminVisible);
		final List<Questionnaire> questionnaires = questionnaireRepository.findAll(filter);

		questionnaires.sort(Questionnaire.COMPARE_BY_CATEGORY_AND_SORTORDER);

		return questionnaires;
	}

	/**
	 * Returns all questionnaire for the given user.
	 *
	 * @param user the non-null user
	 * @return a list of questionnaires sorted by {@link Questionnaire#COMPARE_BY_CATEGORY_AND_SORTORDER}
	 */
	@Transactional
	public List<Questionnaire> getQuestionnairesForUser(User user)
	{
		Objects.requireNonNull(user, "user == null");

		final Set<RecommendationCategory> categoriesWithDefault = user.getOrderedCategoriesWithDefault();
		final List<Questionnaire> questionnaires = getQuestionnaires(categoriesWithDefault, false);

		return questionnaires;
	}

	/**
	 * Returns true if the given user completed the given questionnaire with the given activation.
	 *
	 * @param user          the non-null user
	 * @param questionnaire the non-null questionnaire
	 * @param activation    the non-null activation
	 * @return true iff. the user validly answered all required questions of the questionnaire
	 */
	@Transactional
	public boolean isCompletedQuestionnaire(User user, Questionnaire questionnaire, QuestionnaireActivation activation)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(questionnaire, "questionnaire == null");
		Objects.requireNonNull(activation, "activation == null");

		final List<Question> questions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, requiredRequirements);
		final List<Question> enabledQuestions = activation.getEnabledQuestions(questions);

		final int countUnansweredQuestions = enabledQuestions.isEmpty() ?
				0 : answerRepository.countUnansweredQuestions(user, enabledQuestions);
		final int countIncompleteAndInvalidAnswers = enabledQuestions.isEmpty() ?
				0 : answerRepository.countIncompleteAndInvalidAnswers(user, enabledQuestions);

		return countUnansweredQuestions + countIncompleteAndInvalidAnswers == 0;
	}

	/**
	 * Returns true if the given user has answered all required questions of all active questionnaires.
	 *
	 * @param user the non-null user
	 * @return true iff. all required questions are answered
	 */
	@Transactional
	public boolean isAllQuestionnairesAnswered(User user)
	{
		Objects.requireNonNull(user, "user == null");

		final QuestionnaireActivation activation = getActivation(user);
		final List<Questionnaire> questionnaires = getQuestionnairesForUser(user);
		final List<Questionnaire> enabledQuestionnaires = activation.getEnabledQuestionnaires(questionnaires);
		final List<Question> questions = enabledQuestionnaires.isEmpty() ?
				Collections.emptyList() :
				questionRepository.findActiveQuestionsForQuestionnaires(enabledQuestionnaires, requiredRequirements);

		final int countUnansweredQuestions = questions.isEmpty() ?
				0 : answerRepository.countUnansweredQuestions(user, questions);

		return countUnansweredQuestions == 0;
	}

	/**
	 * Returns a map with the given users incomplete questionnaires grouped by their
	 * questions required flag.
	 *
	 * @param user the non-null user
	 * @return map containing the incomplete questionnaires with the required flag {@link Question#requirement}
	 */
	@Transactional
	public Multimap<Questionnaire, Requirement> getIncompleteQuestionnairesWithRequired(User user)
	{
		Objects.requireNonNull(user, "user == null");

		final Multimap<Questionnaire, Requirement> result = MultimapBuilder.hashKeys().hashSetValues().build();

		final QuestionnaireActivation activation = getActivation(user);
		final List<Questionnaire> questionnaires = getQuestionnairesForUser(user);

		final List<Question> allQuestions = questionRepository.findActiveQuestionsForQuestionnaires(questionnaires);
		final List<Question> allEnabledQuestions = activation.getEnabledQuestions(allQuestions);

		if (!questionnaires.isEmpty())
		{
			for (Requirement requirement : Requirement.values())
			{
				final List<Question> questions = allEnabledQuestions.stream()
						.filter(q -> requirement.equals(q.getRequirement()))
						.collect(Collectors.toList());

				if (!questions.isEmpty())
				{
					final List<Questionnaire> unansweredQuestionnaires =
							answerRepository.getUnansweredQuestionnaires(user, questions);
					unansweredQuestionnaires.forEach(qn -> result.put(qn, requirement));

					final List<Questionnaire> incompleteAndInvalidQuestionnaires =
							answerRepository.getIncompleteAndInvalidQuestionnaires(user, questions);
					incompleteAndInvalidQuestionnaires.forEach(qn -> result.put(qn, requirement));
				}
			}
		}

		return result;
	}

	/**
	 * Returns the questionnaire activation of the given user.
	 *
	 * @param user the non-null user
	 * @return the non-null questionnaire activation for the given user
	 */
	public QuestionnaireActivation getActivation(User user)
	{
		Objects.requireNonNull(user, "user == null");
		final List<Activator> activators = activatorRepository.findAll();

		final QuestionnaireActivation activation = new QuestionnaireActivation(user, activators);
		final List<ChoiceQuestion> activatingQuestions = activators.stream()
				.map(Activator::getActivatingQuestion)
				.collect(Collectors.toList());

		if (!activatingQuestions.isEmpty())
		{
			final List<Answer> activatingAnswers = answerRepository.findAnswersForUser(user, activatingQuestions);

			activation.updateActivatingAnswers(activatingAnswers);
		}

		return activation;
	}
}
