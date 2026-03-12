package de.binaerebauten.gleichklang.core.view;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.questionnaire.Questionnaire;
import de.binaerebauten.gleichklang.core.view.component.QuestionnaireHeader;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupComponent;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionGroupContainer;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class QuestionnaireViewImpl extends AbstractNavigateView<QuestionnaireView.QuestionnaireViewListener> implements QuestionnaireView
{
	private final Questionnaire questionnaire;
	private final VerticalLayout rootLayout;
	private final QuestionGroupContainer questionGroupContainer;
	private final ComboBox dropdown;
	
	public QuestionnaireViewImpl(Questionnaire questionnaire, QuestionGroupContainer questionGroupContainer)
	{
		this.questionnaire = questionnaire;
		
		this.questionGroupContainer = questionGroupContainer;
		this.questionGroupContainer.setSaveListener(answers -> getListener().save(answers));
		
		rootLayout = new VerticalLayout();
		rootLayout.setStyleName(CssStyle.QUESTIONNAIRE_VIEW.getStyleName());
		
		VerticalLayout questionnairePanel = new VerticalLayout();
		questionnairePanel.setSpacing(true);
		
		questionnairePanel.setStyleName(CssStyle.QUESTIONNAIRE.getStyleName());

		final QuestionnaireHeader questionnaireHeader = new QuestionnaireHeader(questionnaire);
		questionnairePanel.addComponent(questionnaireHeader);

		final HorizontalLayout dropDownWrapper = new HorizontalLayout();
		dropDownWrapper.setSizeFull();
		dropDownWrapper.setStyleName(CssStyle.TABSHEET_DROPDOWN_GREEN.getStyleName());
		dropDownWrapper.addStyleName(CssStyle.QUESTIONNAIRE_DROPDOWN.getStyleName());

		dropdown = new ComboBox();
		dropdown.setTextInputAllowed(false);
		dropdown.setNullSelectionAllowed(false);

		dropDownWrapper.addComponent(dropdown);

		rootLayout.addComponent(questionnairePanel);
		rootLayout.addComponent(dropDownWrapper);
		rootLayout.addComponent(questionGroupContainer);

		rootLayout.setWidth(100, Unit.PERCENTAGE);
		setCompositionRoot(rootLayout);
		questionnaireHeader.focusHeader();
	}
	
	@Override
	public Questionnaire getQuestionnaire()
	{
		return questionnaire;
	}
	
	@Override
	public void addQuestionGroupComponent(QuestionGroupComponent questionGroupComponent)
	{
		this.dropdown.addItem(questionGroupComponent);
		this.dropdown.setItemCaption(questionGroupComponent, questionGroupComponent.getName());
		if (dropdown.getItemIds().size() == 1)
		{
			dropdown.setValue(questionGroupComponent);
			questionGroupContainer.setTab(questionGroupComponent);
			dropdown.addValueChangeListener(event -> {
				questionGroupContainer.setTab((QuestionGroupComponent) dropdown.getValue());
			});
		}

		dropdown.setVisible(dropdown.getItemIds().size() > 1);

		this.questionGroupContainer.setTabsVisible(dropdown.getItemIds().size() > 1);
		this.questionGroupContainer.addQuestionGroupComponent(questionGroupComponent, questionGroupComponent.getName());
	}
	
	@Override
	public void setQuestionGroupActivated(QuestionGroupComponent questionGroupComponent, boolean activated)
	{
		this.questionGroupContainer.setQuestionGroupActivated(questionGroupComponent, activated);
	}
	
	@Override
	public void removeAllQuestionGroups()
	{
		this.questionGroupContainer.removeAllComponents();
	}
	
	@Override
	public void setMargin(boolean margin)
	{
		rootLayout.setMargin(margin);
	}
	
	@Override
	public QuestionGroupContainer getQuestionGroupContainer()
	{
		return questionGroupContainer;
	}
	
	@Override
	public void setSelectedDropdownValue(QuestionGroupComponent questionGroupComponent)
	{
		dropdown.select(questionGroupComponent);
	}
	
	@Override
	public void saveComplete(SaveResultListener saveResultListener)
	{
		questionGroupContainer.saveComplete(saveResultListener);
	}
}
