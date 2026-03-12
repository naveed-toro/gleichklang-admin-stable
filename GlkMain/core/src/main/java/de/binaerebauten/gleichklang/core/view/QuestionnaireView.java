package de.binaerebauten.gleichklang.core.view;

import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.view.component.Savable;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupComponent;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupContainer;

import java.util.List;

public interface QuestionnaireView extends NavigateView<QuestionnaireView.QuestionnaireViewListener>, Savable
{
	interface QuestionnaireViewListener	extends NavigateView.NavigateViewListener
	{
		/**
		 * Creates all subviews.
		 */
		void createSubViews();
		
		void save(List<Answer> answers);
	}

	interface QuestionnaireActivationListener extends DefaultView.DefaultViewListener
	{
		void activateQuestionnaire(Questionnaire questionnaire, boolean enabled);
	}

	Questionnaire getQuestionnaire();

	void addQuestionGroupComponent(QuestionGroupComponent questionGroupComponent);

	void setQuestionGroupActivated(QuestionGroupComponent questionGroupComponent, boolean activated);

	void setSelectedDropdownValue(QuestionGroupComponent questionGroupComponent);

	void removeAllQuestionGroups();

	void setMargin(boolean margin);
	
	QuestionGroupContainer getQuestionGroupContainer();
}
