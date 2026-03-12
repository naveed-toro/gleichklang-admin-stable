package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupContainer;
import de.binaerebauten.gleichklang.core.view.QuestionnaireViewImpl;

public class RegistrationQuestionnaireViewImpl extends QuestionnaireViewImpl implements RegistrationQuestionnaireView
{
	public RegistrationQuestionnaireViewImpl(Questionnaire questionnaire, QuestionGroupContainer questionGroupContainer)
	{
		super(questionnaire, questionGroupContainer);
		//		resetValidationErrors();
	}
	
	@Override
	public void quickFill()
	{
		getQuestionGroupContainer().quickRegister("test value");
	}
}
