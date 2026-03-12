package de.binaerebauten.gleichklang.core.view.component.question;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import de.binaerebauten.gleichklang.core.model.matching.Activator;
import de.binaerebauten.gleichklang.core.model.matching.QuestionActivator;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionGroup;
import de.binaerebauten.gleichklang.core.model.questionnaire.QuestionnaireActivation;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.QuickRegistrable;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionComponent.ActivationListener;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.*;
import java.util.stream.Collectors;

public class QuestionGroupComponent extends CustomComponent implements QuickRegistrable
{
	private final Map<Question, QuestionComponent> questionComponents = new HashMap<>();
	private final String name;
	private final String i18Key;
	private final QuestionnaireActivation activation;
	private ActivationListener activationListener = null;
	
	public QuestionGroupComponent(QuestionGroup questionGroup, List<Answer> answers, LocatableHandler locatableHandler, QuestionnaireActivation activation)
	{
		Objects.requireNonNull(questionGroup);
		Objects.requireNonNull(answers);
		Objects.requireNonNull(locatableHandler);
		Objects.requireNonNull(activation);
		
		this.activation = activation;
		this.name = questionGroup.getName();
		this.i18Key = questionGroup.getI18nKey();
		setCompositionRoot(createLayout(questionGroup, answers, locatableHandler));
	}
	
	private Component createLayout(QuestionGroup questionGroup, List<Answer> answers, LocatableHandler locatableHandler)
	{
		final FormPanel questionGroupPanel = new FormPanel(name);
		questionGroupPanel.setDescription(questionGroup.getDescription());
		questionGroupPanel.setStyleName(CssStyle.GK_PANEL.getStyleName());
		questionGroupPanel.setIcon(new ThemeResource("img/question-mark.svg"));
		
		for (Answer answer : answers)
		{
			final QuestionComponent questionComponent = new QuestionComponent(answer, locatableHandler, activation);
			questionComponent.setActivationListener(this::onActivationChanged);
			this.questionComponents.put(answer.getQuestion(), questionComponent);

			Answer.AnswerType answerType = Answer.AnswerType.valueOf(answer);
			if (answerType == Answer.AnswerType.REGION)
			{
				questionGroupPanel.addStyleName(CssStyle.REGION_QUESTION_GROUP.getStyleName());
			}

			questionGroupPanel.addFormElement(questionComponent);
		}
		
		return questionGroupPanel;
	}
	
	private void onActivationChanged(Collection<Activator> changedActivators)
	{
		for (Activator activator : changedActivators)
		{
			if (activator instanceof QuestionActivator)
			{
				final Question question = ((QuestionActivator) activator).getEnablesQuestion();
				final boolean visible = activation.isEnabledQuestion(question);
				setQuestionActivated(question, visible);
			}
		}
		
		if(activationListener != null) activationListener.activationChanged(changedActivators);
	}
	
	public List<Field<?>> getFields()
	{
		return questionComponents.values().stream().map(QuestionComponent::getFields).flatMap(Collection::stream).collect(Collectors.toList());
	}
	
	public List<Answer> getAnswers()
	{
		return questionComponents.values().stream().map(QuestionComponent::getAnswer).collect(Collectors.toList());
	}
	
	@Override
	public void quickRegister(String value)
	{
		questionComponents.values().forEach(questionComponent -> questionComponent.quickRegister(value));
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setQuestionActivated(Question question, boolean activated)
	{
		final QuestionComponent questionComponent = questionComponents.get(question);
		
		if (questionComponent != null)
		{
			questionComponent.setActivated(activated);
		}
	}
	
	public void setActivationListener(ActivationListener activationListener)
	{
		this.activationListener = activationListener;
	}
}
