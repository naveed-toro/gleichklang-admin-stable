package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO Muss noch vom MatchingService verwendet werden, damit auch die gleichen Berechnungen für Filter & Matching verwendet wird
 *
 */
public class MatchingUtils
{
	public static final double ALLOWED_MISSING_ANSWERS_RATIO = 0.5d;

	public static Collection<Question> getRequiredCount(Collection<? extends Question> questions, RecommendationCategory category)
	{
		final Set<Question> resultQuestions = new HashSet<>();
		for (Question question : questions)
		{
			final RecommendationCategory questionCategory = question.getQuestionGroup().getQuestionnaire().getRecommendationCategory();
			final boolean isRequiredCategory = questionCategory == null || questionCategory.equals(category);
			
			if (isRequiredCategory && question.isRequired())
			{
				resultQuestions.add(question);
			}
		}

		return resultQuestions;
	}
}
