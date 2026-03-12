package de.binaerebauten.gleichklang.core.model.matching;

public interface QuestionsMappingVisitor<T>
{
	T visit(AffinityMapping affinityMapping);

	T visit(ChoiceQuestionsMapping choiceQuestionsMapping);

	T visit(NumberQuestionsMapping numberQuestionsMapping);

	T visit(AgeQuestionMapping ageQuestionMapping);

	T visit(AvatarQuestionMapping avatarQuestionMapping);
}
