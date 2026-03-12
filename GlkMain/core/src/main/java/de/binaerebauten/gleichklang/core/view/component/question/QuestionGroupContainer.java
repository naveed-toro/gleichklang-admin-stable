package de.binaerebauten.gleichklang.core.view.component.question;

import com.vaadin.ui.Component;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.view.component.QuickRegistrable;
import de.binaerebauten.gleichklang.core.view.component.Savable;

import java.util.List;

public interface QuestionGroupContainer extends Component, QuickRegistrable, Savable
{
	interface SaveListener
	{
		void save(List<Answer> answers);
	}
	
	void addQuestionGroupComponent(QuestionGroupComponent questionGroupComponent, String caption);

	void setQuestionGroupActivated(QuestionGroupComponent questionGroupComponent, boolean activated);

	void setTab(QuestionGroupComponent questionGroupComponent);
	
	void removeAllComponents();
	
	void setSaveListener(SaveListener saveListener);

	void setTabsVisible(boolean visible);
}
