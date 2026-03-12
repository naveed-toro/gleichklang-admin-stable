package de.binaerebauten.gleichklang.core.view.popup;

import com.google.common.collect.Lists;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;
import de.binaerebauten.gleichklang.core.view.QuestionnaireView;
import de.binaerebauten.gleichklang.core.view.QuestionnaireViewImpl;
import de.binaerebauten.gleichklang.core.view.component.FooterCommandBar;
import de.binaerebauten.gleichklang.core.view.component.I18N;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupComponent;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupVerticalLayout;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.*;
import java.util.stream.Collectors;

public class AnswerPopup extends GenericPopup
{
	public interface SaveListener
	{
		void save(List<Answer> answers);
	}
	
	private final List<QuestionGroupComponent> questionGroupComponents = new ArrayList<>();
	
	private final VerticalLayout layout;
	private final LocatableHandler locatableHandler;
	private final QuestionnaireActivation activation;
	private final SaveHelper saveHelper;
	
	public AnswerPopup(Answer answer, String caption, LocatableHandler locatableHandler, QuestionnaireActivation activation, SaveListener saveListener)
	{
		this(Lists.newArrayList(answer), caption, locatableHandler, activation, saveListener);
	}
	
	public AnswerPopup(List<Answer> answers, String caption, LocatableHandler locatableHandler, QuestionnaireActivation activation, SaveListener saveListener)
	{
		setCaption(caption);
		
		Objects.requireNonNull(answers);
		Objects.requireNonNull(locatableHandler);
		Objects.requireNonNull(activation);
		Objects.requireNonNull(saveListener);
		
		this.locatableHandler = locatableHandler;
		this.activation = activation;
		
		saveHelper = new SaveHelper(() ->
		{
			saveListener.save(questionGroupComponents.stream().flatMap(v -> v.getAnswers().stream()).collect(Collectors.toList()));
			this.close();
		});
		
		layout = new VerticalLayout();
		layout.setWidth(100, Unit.PERCENTAGE);
		layout.setSpacing(true);
		
		layout.addComponent(saveHelper.getValidationComponent());
		initAnswerViews(answers);
		layout.addComponent(createControls());
		
		setPopupContent(layout);
	}
	
	private void initAnswerViews(List<Answer> answers)
	{
		final Map<QuestionGroup, List<Answer>> result = answers.stream().collect(Collectors.groupingBy(answer -> answer.getQuestion().getQuestionGroup()));
		final Map<Questionnaire, QuestionnaireView> questionnaireViewMap = new HashMap<>();

		for (QuestionGroup questionGroup : result.keySet().stream().sorted(QuestionGroup.COMPARATOR).collect(Collectors.toList()))
		{
			final Questionnaire questionnaire = questionGroup.getQuestionnaire();
			final QuestionGroupComponent questionGroupComponent = new QuestionGroupComponent(questionGroup, result.get(questionGroup), locatableHandler, activation);
			
			QuestionnaireView questionnaireView = questionnaireViewMap.get(questionnaire);
			if (questionnaireView == null)
			{
				questionnaireView = new QuestionnaireViewImpl(questionnaire, new QuestionGroupVerticalLayout());
				layout.addComponent(questionnaireView);
				questionnaireViewMap.put(questionnaire, questionnaireView);
			}
			
			saveHelper.addFields(questionGroupComponent.getFields());
			questionGroupComponents.add(questionGroupComponent);
			questionnaireView.addQuestionGroupComponent(questionGroupComponent);
		}
	}
	
	private Component createControls()
	{
		final FooterCommandBar commandBar = new FooterCommandBar();
		commandBar.addStyleName(CssStyle.POPUP.getStyleName());
		
		final Button backButton = new Button(I18N.FORMPANEL_ACTION_BACK.msg(), event -> close());
		backButton.setIcon(FontAwesome.CHEVRON_LEFT);
		commandBar.addButton(backButton, FooterCommandBar.Position.LEFT);
		commandBar.addButton(saveHelper.getSaveButton(), FooterCommandBar.Position.RIGHT);
		
		return commandBar;
	}
}
