package de.binaerebauten.gleichklang.adminweb.view.util;

import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Choice;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion;
import de.binaerebauten.gleichklang.core.model.questionnaire.NumberQuestion;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class RecommendationCategoryVisitor implements QuestionsMappingVisitor<EnumSet<RecommendationCategory>>
{
	@Override
	public EnumSet<RecommendationCategory> visit(AffinityMapping affinityMapping)
	{
		final Set<ChoiceQuestion> questions = affinityMapping.getQuestions();

		if (questions == null || questions.isEmpty())
			return null;

		if (questions.stream().map(question -> question.getQuestionGroup().getQuestionnaire().getRecommendationCategory()).distinct().count() != 1)
			return null;

		final RecommendationCategory category = questions.iterator().next().getQuestionGroup().getQuestionnaire().getRecommendationCategory();

		return category != null ? EnumSet.of(category) : EnumSet.allOf(RecommendationCategory.class);
	}

	@Override
	public EnumSet<RecommendationCategory> visit(ChoiceQuestionsMapping choiceQuestionsMapping)
	{
		final ChoiceQuestion sourceQuestion = choiceQuestionsMapping.getSourceQuestion();
		final ChoiceQuestion targetQuestion = choiceQuestionsMapping.getTargetQuestion();
		final MatchingMatrix matrix = choiceQuestionsMapping.getMatrix();

		if (sourceQuestion == null || targetQuestion == null || matrix == null)
			return null;

		if (!matrix.getSourceChoiceGroup().equals(sourceQuestion.getChoiceGroup()) || !matrix.getTargetChoiceGroup().equals(targetQuestion.getChoiceGroup()))
			return null;

		final RecommendationCategory sourceCategory = sourceQuestion.getQuestionGroup().getQuestionnaire().getRecommendationCategory();
		final RecommendationCategory targetCategory = targetQuestion.getQuestionGroup().getQuestionnaire().getRecommendationCategory();

		if (sourceCategory == null && targetCategory == null) return null;

		if (sourceCategory != null)
		{
			return sourceCategory.equals(targetCategory) || targetCategory == null ? EnumSet.of(sourceCategory) : null;
		}

		return EnumSet.of(targetCategory);
	}

	@Override
	public EnumSet<RecommendationCategory> visit(NumberQuestionsMapping numberQuestionsMapping)
	{
		final NumberQuestion factQuestion = numberQuestionsMapping.getFactQuestion();
		final NumberQuestion minQuestion = numberQuestionsMapping.getMinQuestion();
		final NumberQuestion maxQuestion = numberQuestionsMapping.getMaxQuestion();

		if (factQuestion == null || minQuestion == null || maxQuestion == null)
			return null;

		final RecommendationCategory factCategory = factQuestion.getQuestionGroup().getQuestionnaire().getRecommendationCategory();
		final RecommendationCategory minCategory = minQuestion.getQuestionGroup().getQuestionnaire().getRecommendationCategory();
		final RecommendationCategory maxCategory = maxQuestion.getQuestionGroup().getQuestionnaire().getRecommendationCategory();

		if (!Objects.equals(minCategory, maxCategory)) return null;

		if (factCategory == null && minCategory == null) return null;

		if (factCategory != null)
		{
			return factCategory.equals(minCategory) || minCategory == null ? EnumSet.of(factCategory) : null;
		}

		return EnumSet.of(minCategory);
	}

	@Override
	public EnumSet<RecommendationCategory> visit(AgeQuestionMapping ageQuestionMapping)
	{
		final NumberQuestion minQuestion = ageQuestionMapping.getMinAgeQuestion();
		final NumberQuestion maxQuestion = ageQuestionMapping.getMaxAgeQuestion();

		if (minQuestion == null || maxQuestion == null)
			return null;

		final RecommendationCategory minCategory = minQuestion.getQuestionGroup().getQuestionnaire().getRecommendationCategory();
		final RecommendationCategory maxCategory = maxQuestion.getQuestionGroup().getQuestionnaire().getRecommendationCategory();

		if (minCategory == null || !minCategory.equals(maxCategory))
			return null;

		return EnumSet.of(minCategory);
	}

	@Override
	public EnumSet<RecommendationCategory> visit(AvatarQuestionMapping avatarQuestionMapping)
	{
		final ChoiceQuestion avatarQuestion = avatarQuestionMapping.getAvatarQuestion();
		final Choice trueChoice = avatarQuestionMapping.getTrueChoice();

		if (avatarQuestion == null || trueChoice == null)
			return null;

		if (!avatarQuestion.getChoiceGroup().getChoices().contains(trueChoice))
			return null;

		final RecommendationCategory sourceCategory = avatarQuestion.getQuestionGroup().getQuestionnaire().getRecommendationCategory();

		return sourceCategory != null ? EnumSet.of(sourceCategory) : null;
	}
}
