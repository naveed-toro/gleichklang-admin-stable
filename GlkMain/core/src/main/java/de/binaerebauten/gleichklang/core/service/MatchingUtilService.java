package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.matching.MatrixValue.Strictness;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.NumberQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AnswerRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchRepository;
import de.binaerebauten.gleichklang.core.repository.matching.MatchingRepository;
import de.binaerebauten.gleichklang.core.utils.MatchingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Matching service help functions (main functionality is in the admin)
 */
@Service
public class MatchingUtilService
{
	private final MatchingRepository matchingRepository;
	private final MatchRepository matchRepository;
	private final AnswerRepository answerRepository;
	
	@Autowired
	public MatchingUtilService(MatchingRepository matchingRepository, MatchRepository matchRepository, AnswerRepository answerRepository)
	{
		this.matchingRepository = Objects.requireNonNull(matchingRepository);
		this.matchRepository = Objects.requireNonNull(matchRepository);
		this.answerRepository = Objects.requireNonNull(answerRepository);
	}
	
	private Collection<ChoiceQuestion> getChoiceQuestions(RecommendationCategory category)
	{
		final Set<ChoiceQuestion> choiceQuestions = new HashSet<>();
		
		final Set<ChoiceQuestionsMapping> choiceQuestionsMappings = matchingRepository.getChoiceQuestionMappings(category);
		final Set<AffinityMapping> affinityMappings = matchingRepository.getAffinityMappings(category);
		final Set<AvatarQuestionMapping> avatarQuestionsMappings = matchingRepository.getAvatarQuestionMappings(category);
		
		choiceQuestionsMappings.stream().map(ChoiceQuestionsMapping::getSourceQuestion).forEach(choiceQuestions::add);
		choiceQuestionsMappings.stream().map(ChoiceQuestionsMapping::getTargetQuestion).forEach(choiceQuestions::add);
		
		affinityMappings.stream().map(AffinityMapping::getQuestions).forEach(choiceQuestions::addAll);
		
		avatarQuestionsMappings.stream().map(AvatarQuestionMapping::getAvatarQuestion).forEach(choiceQuestions::add);
		
		return choiceQuestions;
	}
	
	private Collection<NumberQuestion> getNumberQuestions(RecommendationCategory category)
	{
		final Set<NumberQuestion> numberQuestions = new HashSet<>();
		
		final Set<NumberQuestionsMapping> numberQuestionMappings = matchingRepository.getNumberQuestionMappings(category);
		final Set<AgeQuestionMapping> ageQuestionsMappings = matchingRepository.getAgeQuestionMappings(category);
		
		numberQuestionMappings.stream().map(NumberQuestionsMapping::getFactQuestion).forEach(numberQuestions::add);
		numberQuestionMappings.stream().map(NumberQuestionsMapping::getMinQuestion).forEach(numberQuestions::add);
		numberQuestionMappings.stream().map(NumberQuestionsMapping::getMaxQuestion).forEach(numberQuestions::add);
		
		ageQuestionsMappings.stream().map(AgeQuestionMapping::getMaxAgeQuestion).forEach(numberQuestions::add);
		ageQuestionsMappings.stream().map(AgeQuestionMapping::getMinAgeQuestion).forEach(numberQuestions::add);
		
		return numberQuestions;
	}
	
	public Collection<Question> getRequiredQuestions(RecommendationCategory recommendationCategory)
	{
		final List<Question> questions = new ArrayList<>();
		questions.addAll(getNumberQuestions(recommendationCategory));
		questions.addAll(getChoiceQuestions(recommendationCategory));
		
		return questions;
	}
	
	public double getMissingAnswerRatio(User user, RecommendationCategory category)
	{
		final Collection<Question> questions = getRequiredQuestions(category);
		final Collection<Question> requiredQuestions = MatchingUtils.getRequiredCount(questions, category);
		
		if (requiredQuestions.isEmpty()) return 0d;
		
		return 1d - ((double) answerRepository.countCompleteAnswers(user.getId(), requiredQuestions) / (double) requiredQuestions.size());
	}
	
	public Map<Strictness, Long> getMatchCount(User user, RecommendationCategory category)
	{
		final Map<Strictness, Long> result = new HashMap<>();
		for (Strictness strictness : Strictness.values())
		{
			if (!Strictness.lastStrictness.equals(strictness))
			{
				result.put(strictness, matchRepository.countByStrictnessAndCategory(user.getId(), strictness, category));
			}
		}
		return result;
	}

	public Map<Strictness, Long> getAllocatableMatchCount(User user, RecommendationCategory category)
	{
		final List<Match> matches = matchRepository.findMatchesForUser(user.getId(), Strictness.lastStrictness, category);
		return matches.stream().collect(Collectors.groupingBy(Match::getStrictness, Collectors.counting()));
	}
}
