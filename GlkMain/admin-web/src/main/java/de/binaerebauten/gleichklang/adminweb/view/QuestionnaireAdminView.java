package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.QuestionnaireAdminView.QuestionnaireAdminViewListener;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.TableControl.MoveHandler;

public interface QuestionnaireAdminView extends NavigateView<QuestionnaireAdminViewListener>
{
	interface QuestionnaireAdminViewListener extends NavigateView.NavigateViewListener
	{
		void newQuestionnaire();

		void editQuestionnaire(Questionnaire questionnaire);

		void deleteQuestionnaire(Questionnaire questionnaire);

		void undeleteQuestionnaire(Questionnaire questionnaire);

		void newQuestionGroup(Questionnaire questionnaire);

		void editQuestionGroup(QuestionGroup questionGroup);

		void deleteQuestionGroup(QuestionGroup questionGroup);

		void undeleteQuestionGroup(QuestionGroup questionGroup);

		void newQuestion(QuestionGroup questionGroup);

		void editQuestion(Question question);

		void deleteQuestion(Question question);

		void undeleteQuestion(Question question);

		void newChoiceGroup();

		void editChoiceGroup(ChoiceGroup choiceGroup);

		void deleteChoiceGroup(ChoiceGroup choiceGroup);

		void undeleteChoiceGroup(ChoiceGroup choiceGroup);
	}

	void setQuestionGroupHandler(LazyBeanFilteredItemsHandler<QuestionGroup> handler);

	void setQuestionnaireHandler(LazyBeanFilteredItemsHandler<Questionnaire> handler);

	void setQuestionHandler(LazyBeanFilteredItemsHandler<Question> handler);

	void setChoiceGroupHandler(LazyBeanFilteredItemsHandler<ChoiceGroup> handler);

	void setMoveQuestionnaireHandler(MoveHandler<Questionnaire> moveItemsHandler);

	void setMoveQuestionGroupHandler(MoveHandler<QuestionGroup> moveItemsHandler);

	void setMoveQuestionHandler(MoveHandler<Question> moveItemsHandler);
}
