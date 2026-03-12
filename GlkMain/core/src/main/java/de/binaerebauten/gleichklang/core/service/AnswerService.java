package de.binaerebauten.gleichklang.core.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.binaerebauten.gleichklang.core.model.FreeTextElement;
import de.binaerebauten.gleichklang.core.model.NaturalKeyEntity.NaturalKey;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AnswerRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionGroupRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionRepository;
import de.binaerebauten.gleichklang.core.repository.QuestionnaireRepository;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnswerService
{
	@Autowired
	private QuestionnaireRepository questionnaireRepository;

	@Autowired
	private QuestionGroupRepository questionGroupRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private AnswerRepository answerRepository;
	
	/**
	 * Reurns the incomplete and missing answers for the requirement or optional questions
	 * for the given user, questionnaire and activation
	 *
	 * @param user          the non-null user
	 * @param questionnaire the non-null questionnaire
	 * @param activation    the non-null activation
	 * @param requirements  the requirement flag {@see Question#requirement}
	 * @return the ordered missing or incomplete answers
	 */
	@Transactional
	public List<Answer> getIncompleteAndMissingAnswers(User user, Questionnaire questionnaire, QuestionnaireActivation activation, Collection<Requirement> requirements)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(questionnaire, "questionnaire == null");
		Objects.requireNonNull(activation, "activation == null");
		Objects.requireNonNull(requirements);

		final List<Question> questions = questionRepository.findActiveQuestionsForQuestionnaire(questionnaire, requirements);
		final List<Question> enabledQuestions = activation.getEnabledQuestions(questions);

		if (enabledQuestions.isEmpty())
		{
			return new ArrayList<>();
		}

		final List<Question> unansweredQuestions = answerRepository.getUnansweredQuestions(user, enabledQuestions);
		final AnswerFactory answerFactory = AnswerFactory.get();
		final List<Answer> missingAnswers = unansweredQuestions.stream()
				.map(q -> (Answer) answerFactory.createNewAnswer(q, user))
				.collect(Collectors.toList());
		final List<Answer> incompleteAnswers = answerRepository.getIncompleteAndInvalidAnswers(user, enabledQuestions);
		
		final List<Question> activatableQuestions = activation.getActivatableQuestionsByQuestions(enabledQuestions);
		activatableQuestions.removeAll(enabledQuestions);
		
		final List<Answer> activatableAnswers;
		if (activatableQuestions.isEmpty())
		{
			activatableAnswers = Collections.emptyList();
		}
		else
		{
			activatableAnswers = createMissingAnswers(answerRepository.findAnswersForUser(user, activatableQuestions), activatableQuestions, user);
		}

		final List<Answer> result = Lists.newArrayList(Iterables.concat(missingAnswers, incompleteAnswers, activatableAnswers));
		result.sort(Answer.COMPARATOR);

		return result;
	}

	public static NaturalKey getFreeTextNaturalKey(RecommendationCategory recommendationCategory)
	{
		NaturalKey freeTextNaturalKey = null;
		switch (recommendationCategory)
		{
			case FRIENDSHIP:
				freeTextNaturalKey = NaturalKey.FREE_TEXT_FRIENDSHIP;
				break;
			case PARTNERSHIP:
				freeTextNaturalKey = NaturalKey.FREE_TEXT_PARTNER;
				break;
		}
		return freeTextNaturalKey;
	}

	/**
	 * Returns the value of the given question natural key represented as string.
	 *
	 * @param user       the non-null user
	 * @param naturalKey the non-null natural key of a question
	 * @return the string representation of the given question or the empty string if the question couldn't be found
	 */
	@Transactional
	public String getAnswerValue(User user, NaturalKey naturalKey)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(naturalKey, "naturalKey == null");
		Preconditions.checkArgument(Question.class.isAssignableFrom(naturalKey.naturalKeyClass),
				"natural key " + naturalKey + " type not of type question");

		final List<Answer> answers = getAnswers(user, naturalKey, false);
		return answers.stream().findFirst().map(Answer::getValue).orElse("");
	}
	
	/**
	 * Returns the questionnaire with answers for the given questionnaire and activation.
	 *
	 * @param questionnaire    the non-null questionnaire
	 * @param activation       the non-null activation
	 * @param withAdminVisible iff. true, answers with questions only visible for admins {@link Question#onlyAdminVisible} will be returned too
	 * @return questionnaire with answers
	 */
	@Transactional
	public QuestionnaireAnswers getQuestionnaireWithAnswers(Questionnaire questionnaire, QuestionnaireActivation activation, boolean withAdminVisible)
	{
		Objects.requireNonNull(questionnaire, "questionnaire == null");
		Objects.requireNonNull(activation, "activation == null");
		User user = Objects.requireNonNull(activation.getUser(), "activation.user == null");

		List<QuestionGroup> activeQuestionGroups = questionGroupRepository.findActiveByQuestionnaire(questionnaire);
		List<Question> activeQuestions = withAdminVisible ?
				questionRepository.findActiveQuestionsForQuestionGroupsWithAdminVisible(activeQuestionGroups) :
				questionRepository.findActiveQuestionsForQuestionGroups(activeQuestionGroups);
		List<Answer> answers = answerRepository.findAnswersForUser(user, activeQuestions);

		Map<Question, Answer> questionAnswerMap = new HashMap<>();
		for (Question question : activeQuestions)
		{
			Answer answer = getOrCreateAnswerForQuestion(answers, question, user);
			questionAnswerMap.put(question, answer);
		}

		return new QuestionnaireAnswers(activation, questionnaire, activeQuestionGroups, questionAnswerMap);
	}
	/**
	 * Returns the questionnaire with answers for the given questionnaire and activation.
	 *
	 * @param questionnaire    the non-null questionnaire
	 * @return questionnaire with answers
	 */
	@Transactional
	public QuestionnaireAnswers getQuestionnaireWithAnswers(User user, Questionnaire questionnaire)
	{
		Objects.requireNonNull(questionnaire, "questionnaire == null");

		List<QuestionGroup> activeQuestionGroups = questionGroupRepository.findActiveByQuestionnaire(questionnaire);
		List<Question> activeQuestions = questionRepository.findActiveQuestionsForQuestionGroupsWithAdminVisible(activeQuestionGroups);
		List<Answer> answers = answerRepository.findAnswersForUser(user, activeQuestions);

		Map<Question, Answer> questionAnswerMap = new HashMap<>();
		for (Question question : activeQuestions)
		{
			Answer answer = getOrCreateAnswerForQuestion(answers, question, user);
			questionAnswerMap.put(question, answer);
		}

		return new QuestionnaireAnswers(questionnaire, activeQuestionGroups, questionAnswerMap);
	}
	/**
	 * Returns the answers for the given user and the given natural key
	 * visible to other users {@link Answer#isVisibleToOtherUsers()}.
	 *
	 * @param user       the non-null user
	 * @param naturalKey the non-null natural key of a questionnaire
	 * @return the answers of the given user visible to other users
	 */
	@Transactional
	public List<Answer> getAnswersVisibleToOtherUsers(User user, NaturalKey naturalKey)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(naturalKey, "naturalKey == null");
		
		return getAnswersVisibleToOtherUsers(user, naturalKey.asSet(), null);
	}
	
	/**
	 * Returns the answers for the given user and the given natural key
	 * visible to other users {@link Answer#isVisibleToOtherUsers()}.
	 *
	 * @param user       the non-null user
	 * @param naturalKeys the non-null natural key of a questionnaire
	 * @return the answers of the given user visible to other users
	 */
	@Transactional
	public List<Answer> getAnswersVisibleToOtherUsers(User user, Collection<NaturalKey> naturalKeys, Collection<NaturalKey> excludedNaturalKeys)
	{
		Objects.requireNonNull(user, "user == null");
		
		final List<Answer> answers = getAnswers(user, naturalKeys, excludedNaturalKeys);

		return answers.stream()
				.filter(Answer::isVisibleToOtherUsers)
				.filter(Answer::isAnswered)
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns all answers for the given user and  and the given natural key.
	 *
	 * @param user       the non-null user
	 * @param naturalKey the non-null natural key
	 * @return
	 */
	@Transactional
	public List<Answer> getAnswers(User user, NaturalKey naturalKey, boolean createMissing)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(naturalKey, "naturalKey == null");
		
		
		final List<Answer> answers;
		if (naturalKey.naturalKeyClass.equals(Questionnaire.class))
		{
			answers = extractResultForQuestionnaire(user, naturalKey, createMissing);
		}
		else if (naturalKey.naturalKeyClass.equals(QuestionGroup.class))
		{
			answers = extractResultForQuestionGroup(user, naturalKey, createMissing);
		}
		else if (Question.class.isAssignableFrom(naturalKey.naturalKeyClass))
		{
			answers = extractResultForQuestion(user, naturalKey, createMissing);
		}
		else
		{
			throw new IllegalArgumentException("Unsupported natural key " + naturalKey);
		}
		
		answers.sort(Answer.COMPARATOR);
		return answers;
	}
	
	/**
	 * Returns all answers for the given user and  and the given natural key.
	 *
	 * @param user       the non-null user
	 * @param naturalKey the non-null natural key
	 * @return
	 */
	@Transactional
	public List<Answer> getAnswers(User user, NaturalKey naturalKey)
	{
		return getAnswers(user, naturalKey, false);
	}

	/**
	 * Returns all answers for the given user and  and the given natural key.
	 *
	 * @param user       the non-null user
	 * @param naturalKeys the non-null natural key
	 * @return
	 */
	@Transactional
	public List<Answer> getAnswers(User user, Collection<NaturalKey> naturalKeys, Collection<NaturalKey> excludedNaturalKeys)
	{
		Objects.requireNonNull(user, "user == null");
		
		final List<Answer> answers = new ArrayList<>();
		
		if(naturalKeys != null)
		{
			for (NaturalKey naturalKey : naturalKeys)
			{
				answers.addAll(getAnswers(user, naturalKey));
			}
		}
		
		if(excludedNaturalKeys != null)
		{
			for(NaturalKey naturalKey : excludedNaturalKeys)
			{
				if(naturalKey.naturalKeyClass.equals(Questionnaire.class))
				{
					answers.removeIf(answer -> naturalKey.equals(answer.getQuestion().getQuestionGroup().getQuestionnaire().getNaturalKey(Questionnaire.class)));
				}
				else if(naturalKey.naturalKeyClass.equals(QuestionGroup.class))
				{
					answers.removeIf(answer -> naturalKey.equals(answer.getQuestion().getQuestionGroup().getNaturalKey(QuestionGroup.class)));
				}
				else if(naturalKey.naturalKeyClass.equals(Question.class))
				{
					answers.removeIf(answer -> naturalKey.equals(answer.getQuestion().getNaturalKey(Question.class)));
				}
			}
		}
		
		return answers;
	}

	@Transactional
	public List<FreeTextElement> getFreeText(User user, RecommendationCategory recommendationCategory)
	{
		final NaturalKey freeTextNaturalKey = getFreeTextNaturalKey(recommendationCategory);

		final List<FreeTextElement> result = new ArrayList<>();
		final List<Answer> answers = getAnswersVisibleToOtherUsers(user, freeTextNaturalKey);

		boolean headlineMissing = true;

		for (Answer answer : answers)
		{
			final FreeTextElement freeTextElement = new FreeTextElement();

			if (answer instanceof ChoiceAnswer)
			{
			    if(((ChoiceAnswer) answer).getChoices().size()!=0) {
					freeTextElement.setHeader(answer.getValue());
//				result.add("<h1>" + answer.getValue() + "</h1>");
					result.add(freeTextElement);
					headlineMissing = false;
				}
			}
			else if (answer instanceof TextAnswer && answer.getValue() != null)
			{
				if (headlineMissing)
				{
				    freeTextElement.setHeader(answer.getQuestion().getName() + ":");
//					result.add("<h1>" + answer.getQuestion().getName() + ":</h1>");
				}
				freeTextElement.setContent(answer.getValue());
				result.add(freeTextElement);
//				result.add(answer.getValue());
				headlineMissing = true;
			}
		}

		return result;
	}

	@CheckedTransactional
	// TODO @MW: Saving an answer will never throw an UniqueValidationException
	public Answer save(Answer answer) throws UniqueValidationException
	{
		try
		{
			answer = answerRepository.save(answer);
		}
		catch (DataIntegrityViolationException e)
		{
			throw new UniqueValidationException(e);
		}
		return answer;
	}

	/**
	 * Saves the given answers.
	 *
	 * @param answers the non-null answers
	 * @return the saved answers
	 * @throws UniqueValidationException can be thrown if an answer already exist in the database
	 */
	@CheckedTransactional
	public Collection<Answer> save(Collection<Answer> answers) throws UniqueValidationException
	{
		try
		{
			answers = answerRepository.save(answers);
		}
		catch (DataIntegrityViolationException e)
		{
			throw new UniqueValidationException(e);
		}
		return answers;
	}


	private Answer getOrCreateAnswerForQuestion(List<Answer> answers, Question question, User user)
	{
		return answers.stream()
				.filter(a -> a.getQuestion().equals(question))
				.findFirst()
				.orElseGet(() -> AnswerFactory.get().createNewAnswer(question, user));
	}

	private List<Answer> extractResultForQuestion(User user, NaturalKey naturalKey, boolean createMissing)
	{
		final Question question = questionRepository.findByNaturalKey(naturalKey);
		Objects.requireNonNull(question, "question with natural key " + naturalKey + " doesn't exist");
		
		final Answer answer = user.getId() != null ? answerRepository.findByUserAndQuestion(user, question) : null;
		
		final List<Answer> answers = answer == null ? Collections.emptyList() : Lists.newArrayList(answer);
		return createMissing ? createMissingAnswers(answers, Collections.singletonList(question), user) : answers;
	}

	private List<Answer> extractResultForQuestionGroup(User user, NaturalKey naturalKey, boolean createMissing)
	{
		final QuestionGroup questionGroup = questionGroupRepository.findByNaturalKey(naturalKey);
		Objects.requireNonNull(questionGroup, "question group with natural key " + naturalKey + " doesn't exist");

		final List<Question> questions = questionRepository.findActiveQuestionsForQuestionGroups(Collections.singleton(questionGroup));
		final List<Answer> answers = user.getId() != null ? answerRepository.findAnswersForUser(user, questions) : null;

		return createMissing ? createMissingAnswers(answers, questions, user) : answers;
	}

	private List<Answer> extractResultForQuestionnaire(User user, NaturalKey naturalKey, boolean createMissing)
	{
		final Questionnaire questionnaire = questionnaireRepository.findByNaturalKey(naturalKey);
		Objects.requireNonNull(questionnaire, "questionnaire with natural key " + naturalKey + " doesn't exist");

		final List<QuestionGroup> questionGroups = questionGroupRepository.findActiveByQuestionnaire(questionnaire);
		final List<Question> questions = questionRepository.findActiveQuestionsForQuestionGroups(questionGroups);
		final List<Answer> answers = user.getId() != null ? answerRepository.findAnswersForUser(user, questions) : null;

		return createMissing ? createMissingAnswers(answers, questions, user) : answers;
	}
	
	private List<Answer> createMissingAnswers(List<Answer> answers, List<Question> questions, User user)
	{
		Objects.requireNonNull(answers);
		Objects.requireNonNull(questions);
		Objects.requireNonNull(user);
		
		final List<Answer> allAnswers = new ArrayList<>();
		for(Question question : questions)
		{
			final Answer answer = answers.stream()
					.filter(a -> a.getUser().equals(user))
					.filter(a -> a.getQuestion().equals(question))
					.findFirst()
					.orElse(AnswerFactory.get().createNewAnswer(question, user));
			
			allAnswers.add(answer);
		}
		
		return allAnswers;
	}
	
	public List<Answer> getAnswers(User user)
	{
		return answerRepository.findByUser(user).stream().filter(Answer::isAnswered).collect(Collectors.toList());
	}

	/*public void deleteAnswerByUser(User user){
		answerRepository.deleteByUser(user);
	}*/
}
