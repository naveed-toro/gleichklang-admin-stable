package de.binaerebauten.gleichklang.core.model.questionnaire;

import de.binaerebauten.gleichklang.core.model.matching.Activator;
import de.binaerebauten.gleichklang.core.model.matching.QuestionActivator;
import de.binaerebauten.gleichklang.core.model.matching.QuestionGroupActivator;
import de.binaerebauten.gleichklang.core.model.matching.QuestionnaireActivator;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents the activation of all questionnaires with their
 * question groups and their questions for an user.
 */
public class QuestionnaireActivation
{
	private static final Logger LOG = LoggerFactory.getLogger(QuestionnaireActivation.class);
	
	private final User user;
	private final Map<Question, Boolean> enableableQuestions = new HashMap<>();
	private final Map<QuestionGroup, Boolean> enableableQuestionGroups = new HashMap<>();
	private final Map<Questionnaire, Boolean> enableableQuestionnaires = new HashMap<>();
	private final Set<Activator> activators;
	
	public QuestionnaireActivation(User user, Collection<Activator> activators)
	{
		this.user = Objects.requireNonNull(user, "user == null");
		this.activators = new HashSet<>(Objects.requireNonNull(activators, "activators == null"));
	}
	
	public User getUser()
	{
		return user;
	}
	
	/**
	 * Returns the questionnaires from the given questionnaires that are enabled
	 * in this activation.
	 *
	 * @param questionnaires the non-null questionnaires
	 * @return questionnaires that are enabled {@link #isEnabledQuestionnaire(Questionnaire)}
	 */
	public List<Questionnaire> getEnabledQuestionnaires(List<Questionnaire> questionnaires)
	{
		return questionnaires.stream()
				.filter(this::isEnabledQuestionnaire)
				.collect(Collectors.toList());
	}
	
	public List<Questionnaire> getActivatableQuestionnaires()
	{
		return activators.stream()
				.filter(activator -> activator instanceof QuestionnaireActivator)
				.map(activator -> ((QuestionnaireActivator)activator).getEnablesQuestionnaire())
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns the question groups from the given question groups that are
	 * enabled in this activation.
	 *
	 * @param questionGroups the non-null question groups
	 * @return question groups that are enabled {@link #isEnabledQuestionGroup(QuestionGroup)}
	 */
	public List<QuestionGroup> getEnabledQuestionGroups(List<QuestionGroup> questionGroups)
	{
		Objects.requireNonNull(questionGroups, "questionGroups == null");
		
		return questionGroups.stream()
				.filter(this::isEnabledQuestionGroup)
				.collect(Collectors.toList());
	}
	
	public List<QuestionGroup> getActivatableQuestionGroups()
	{
		return activators.stream()
				.filter(activator -> activator instanceof QuestionGroupActivator)
				.map(activator -> ((QuestionGroupActivator)activator).getEnablesQuestionGroup())
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns the questions from the given questions that are enabled in this
	 * activation.
	 *
	 * @param questions the non-null list of questions
	 * @return questions that are enabled {@link #isEnabledQuestion(Question)}
	 */
	public List<Question> getEnabledQuestions(List<Question> questions)
	{
		Objects.requireNonNull(questions, "questions == null");
		
		return questions.stream()
				.filter(this::isEnabledQuestion)
				.collect(Collectors.toList());
	}
	
	public List<Question> getActivatableQuestions()
	{
		return activators.stream()
				.filter(activator -> activator instanceof QuestionActivator)
				.map(activator -> ((QuestionActivator)activator).getEnablesQuestion())
				.collect(Collectors.toList());
	}
	
	public List<Question> getActivatableQuestionsByQuestions(Collection<Question> questions)
	{
		return activators.stream()
				.filter(activator -> activator instanceof QuestionActivator)
				.filter(activator -> questions.contains(activator.getActivatingQuestion()))
				.map(activator -> ((QuestionActivator)activator).getEnablesQuestion())
				.collect(Collectors.toList());
	}
	
	public boolean isEnabledQuestionnaire(Questionnaire questionnaire)
	{
		Objects.requireNonNull(questionnaire, "questionnaire == null");
		
		return isEnabled(enableableQuestionnaires, questionnaire);
	}
	
	public boolean isEnabledQuestionGroup(QuestionGroup questionGroup)
	{
		Objects.requireNonNull(questionGroup, "questionGroup == null");
		
		return isEnabledQuestionnaire(questionGroup.getQuestionnaire())
				&& isEnabled(enableableQuestionGroups, questionGroup);
	}
	
	public boolean isEnabledQuestion(Question question)
	{
		Objects.requireNonNull(question, "question == null");
		
		return isEnabledQuestionGroup(question.getQuestionGroup())
				&& isEnabled(enableableQuestions, question);
	}
	
	public void updateActivatingAnswers(List<Answer> activatingAnswers)
	{
		for (Activator activator : activators)
		{
			final Question activatingQuestion = activator.getActivatingQuestion();
			final Answer activatingAnswer = activatingAnswers.stream().filter(answer -> answer.getQuestion().equals(activatingQuestion)).findFirst().orElse(null);
			
//			LOG.info("Update Answer {}", activatingQuestion.getName());
			final boolean isActivating = activatingAnswer != null && isActivating(((ChoiceAnswer) activatingAnswer).getChoices(), activator);
			setActivation(activator, isActivating);
		}
	}
	
	public Set<Activator> updateActivatingAnswer(ChoiceAnswer choiceAnswer, Set<Choice> choices)
	{
		Set<Activator> changedActivators = new HashSet<>();
		
		ChoiceQuestion choiceQuestion = (ChoiceQuestion) choiceAnswer.getQuestion();
		Stream<Activator> activators = getActivetableBy(choiceQuestion);
		activators.forEach(
				activator ->
				{
					LOG.info("Update Answer for question {}", choiceQuestion.getName());
					boolean activating = isActivating(choices, activator);
					setActivation(activator, activating);
					changedActivators.add(activator);
				});
		return changedActivators;
	}
	
	private <T> boolean isEnabled(Map<T, Boolean> enableableMap, T arg)
	{
		return !enableableMap.containsKey(arg) || enableableMap.get(arg);
	}
	
	private Stream<Activator> getActivetableBy(ChoiceQuestion activatingQuestion)
	{
		return activators.stream().filter(a -> a.getActivatingQuestion().equals(activatingQuestion));
	}
	
	private boolean isActivating(Set<Choice> choices, Activator activator)
	{
		return !choices.isEmpty() && !Collections.disjoint(activator.getActivatingChoices(), choices);
	}
	
	private void setActivation(Activator activator, boolean activation)
	{
		if (activator instanceof QuestionActivator)
		{
			Question enablesQuestion = ((QuestionActivator) activator).getEnablesQuestion();
			this.enableableQuestions.put(enablesQuestion, activation);
//			LOG.info(" set activation {} for question {} ", activation, enablesQuestion.getName());
		}
		else if (activator instanceof QuestionGroupActivator)
		{
			QuestionGroup enablesQuestionGroup = ((QuestionGroupActivator) activator).getEnablesQuestionGroup();
			this.enableableQuestionGroups.put(enablesQuestionGroup, activation);
//			LOG.info(" set activation {} for question group {}", activation, enablesQuestionGroup.getName());
		}
		else if (activator instanceof QuestionnaireActivator)
		{
			Questionnaire enablesQuestionnaire = ((QuestionnaireActivator) activator).getEnablesQuestionnaire();
			this.enableableQuestionnaires.put(enablesQuestionnaire, activation);
//			LOG.info(" set activation {} for questionnaire {} ", activation, enablesQuestionnaire.getName());
		}
	}
}
