package de.binaerebauten.gleichklang.core.view.popup;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.*;

public class InfoPopup extends GenericPopup
{
	public interface EditAnswerListener
	{
		void editAnswer(Answer answer);
	}

	private EditAnswerListener editAnswerListener;
	private final Set<Button> editButtons = new HashSet<>();
	private final Map<Component, Integer> questionComponents;

	public InfoPopup(String caption, List<Answer> content)
	{
		this.setCaption(caption);
		this.addStyleName(CssStyle.INFO_POPUP.getStyleName());
		this.setIcon(new ThemeResource("img/icon_profile_white.svg"));
		
		questionComponents = new HashMap<>();
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth(100, Unit.PERCENTAGE);
		layout.setSpacing(true);
		
		final FormPanel panel = new FormPanel();
		panel.setContent(createContent(content));
		panel.setHeightUndefined();

		final Button backButton = new Button(I18N.INFOPOPUP_ACTION_BACK.msg(), event -> close());
		backButton.setVisible(false);

		layout.addComponent(panel);
		layout.addComponent(backButton);

		setPopupContent(layout);
		setVisible(true);
	}
	
	private Component createContent(List<Answer> content)
	{
		VerticalLayout layout = new VerticalLayout();
        layout.setWidth(100, Unit.PERCENTAGE);

		if (content != null)
			content.forEach(answer -> layout.addComponent(createAnswerComponent(answer)));

		return  layout;
	}
	
	private Component createAnswerComponent(Answer answer)
	{
		final Button editButton = new Button();
		editButton.addClickListener(event -> editAnswerListener.editAnswer(answer));
		editButton.setIcon(FontAwesome.PENCIL);
		editButton.setVisible(false);
		editButtons.add(editButton);
		editButton.addStyleName(CssStyle.EDIT_BUTTON.getStyleName());

		final VerticalLayout answerComponent = new VerticalLayout();
		answerComponent.setStyleName(CssStyle.ANSWER_COMPONENT.getStyleName());
		answerComponent.setSpacing(true);

		final Label questionnaire = new Label(answer.getQuestion().getName());
        questionnaire.addStyleName(CssStyle.QUESTION_CATEGORY.getStyleName());
		answerComponent.addComponent(questionnaire);

		final Label answ = new Label(answer.getValue());
        answ.setStyleName(CssStyle.QUESTION_ANSWER.getStyleName());
		answerComponent.addComponent(answ);

		answerComponent.addComponent(editButton);

		if ((answer.getValue() == null || answer.getValue().isEmpty() || answer.getValue().equals("")))
			questionComponents.putIfAbsent(answerComponent, 1);
		else
			questionComponents.putIfAbsent(answerComponent, 0);

		return answerComponent;
	}
	
	public void setEditAnswerListener(EditAnswerListener editAnswerListener)
	{
		this.editAnswerListener = editAnswerListener;
		editButtons.forEach(editButton -> editButton.setVisible(editAnswerListener != null));
	}
}
