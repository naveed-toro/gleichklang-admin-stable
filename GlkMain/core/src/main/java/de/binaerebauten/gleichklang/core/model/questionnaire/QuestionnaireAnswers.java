package de.binaerebauten.gleichklang.core.model.questionnaire;

import com.google.common.collect.Lists;
import de.binaerebauten.gleichklang.core.model.user.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class encapsulates a questionnaire with the answers of an user.
 */
public class QuestionnaireAnswers
{
	private final User user;

	private final QuestionnaireActivation activation;

	private final Questionnaire questionnaire;

	private final List<QuestionGroup> questionGroups;

	private final Map<Question, Answer> questionsWithAnswers;

	public QuestionnaireAnswers(QuestionnaireActivation activation, Questionnaire questionnaire,
			List<QuestionGroup> questionGroups, Map<Question, Answer> questionsWithAnswers)
	{
		this.user = Objects.requireNonNull(activation.getUser(), "activation.user == null");
		this.activation = Objects.requireNonNull(activation, "activation == null");
		this.questionnaire = Objects.requireNonNull(questionnaire, "questionnaire == null");
		this.questionGroups = Objects.requireNonNull(questionGroups, "questionGroups == null");
		this.questionsWithAnswers = Objects.requireNonNull(questionsWithAnswers, "questionsWithAnswers == null");
	}

	public QuestionnaireAnswers(Questionnaire questionnaire,
								List<QuestionGroup> questionGroups, Map<Question, Answer> questionsWithAnswers)
	{
		this.user = null;
		this.activation = null;
		this.questionnaire = Objects.requireNonNull(questionnaire, "questionnaire == null");
		this.questionGroups = Objects.requireNonNull(questionGroups, "questionGroups == null");
		this.questionsWithAnswers = Objects.requireNonNull(questionsWithAnswers, "questionsWithAnswers == null");
	}

	/**
	 * Returns the user.
	 * @return the non-null user
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * Returns the activation of this questionnaire.
	 *
	 * @return the non-null activation
	 */
	public QuestionnaireActivation getActivation()
	{
		return activation;
	}

	/**
	 * Returns the questionnaire represented by this object.
	 *
	 * @return the non-null questionnaire
	 */
	public Questionnaire getQuestionnaire()
	{
		return questionnaire;
	}

	/**
	 * Returns all question groups.
	 *
	 * @return all question groups
	 */
	public List<QuestionGroup> getQuestionGroups()
	{
		return questionGroups;
	}

	/**
	 * Returns the questions of the given question group.
	 *
	 * @param questionGroup the non-null question group
	 * @return the questions for the given question group
	 */
	public List<Question> getQuestions(QuestionGroup questionGroup) {
		Objects.requireNonNull(questionGroup, "questionGroup == null");

		return questionsWithAnswers.keySet().stream()
				.filter(q -> q.getQuestionGroup().equals(questionGroup))
				.sorted(QuestionGroup.COMPARATOR)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the answer for the given question.
	 *
	 * @param question the non-null question
	 *
	 * @return the answer for the given question or null
	 */
	public Answer getAnswer(Question question) {
		Objects.requireNonNull(question, "question == null");

		return questionsWithAnswers.get(question);
	}
	
	public List<Answer> getAnswers(QuestionGroup questionGroup)
	{
		return getQuestions(questionGroup).stream().map(this::getAnswer).collect(Collectors.toList());
	}

	/**
	 * Returns the answers of this questionnaire.
	 *
	 * @return the answers
	 */
	public List<Answer> getAnswers()
	{
		return Lists.newArrayList(questionsWithAnswers.values());
	}
	
	public void updateAnswers(Collection<Answer> answers)
	{
		answers.forEach(answer -> questionsWithAnswers.put(answer.getQuestion(), answer));
	}
}
