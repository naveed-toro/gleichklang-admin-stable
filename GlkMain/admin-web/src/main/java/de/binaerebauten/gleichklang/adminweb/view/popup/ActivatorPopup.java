package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.matching.*;
import de.binaerebauten.gleichklang.core.model.matching.Activator.ActivatorType;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.utils.PropertyPathBuilder;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class ActivatorPopup extends Popup
{
	public interface SaveCallback
	{
		void saveMapping(Activator activator) throws ValidationException;
	}
	
	private final SaveCallback saveCallback;
	private final ComponentGroup<QuestionActivator> questionActivatorComponentGroup;
	private final ComponentGroup<QuestionnaireActivator> questionnaireActivatorComponentGroup;
	private final ComponentGroup<QuestionGroupActivator> questionGroupActivatorComponentGroup;
	
	private final ComboBox activatorTypeComboBox;
	
	public ActivatorPopup(Collection<Activator> activators, Collection<Questionnaire> questionnaires, Collection<QuestionGroup> questionGroups, Collection<Question> questions, Collection<ChoiceQuestion> activatingQuestions, SaveCallback saveCallback)
	{
		Objects.requireNonNull(saveCallback);
		this.saveCallback = saveCallback;
		
		setCaption(I18N.ACTIVATORPOPUP_CAPTION_TITLE.msg());
		
		questionActivatorComponentGroup = new ComponentGroup<>(QuestionActivator.class);
		questionnaireActivatorComponentGroup = new ComponentGroup<>(QuestionnaireActivator.class);
		questionGroupActivatorComponentGroup = new ComponentGroup<>(QuestionGroupActivator.class);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		activatorTypeComboBox = ComponentFactory.getInstance().createField(ComboBox.class);
		activatorTypeComboBox.setRequired(true);
		
		final Component questionComponent = createQuestionComponent(questions, activatingQuestions);
		final Component questionGroupComponent = createQuestionGroupComponent(questionGroups, activatingQuestions);
		final Component questionnaireComponent = createQuestionnaireComponent(questionnaires, activatingQuestions);
		
		activatorTypeComboBox.addValueChangeListener(event ->
		{
			ActivatorType activatorType = (ActivatorType) activatorTypeComboBox.getValue();
			
			questionComponent.setVisible(ActivatorType.QUESTION.equals(activatorType));
			questionGroupComponent.setVisible(ActivatorType.QUESTION_GROUP.equals(activatorType));
			questionnaireComponent.setVisible(ActivatorType.QUESTIONNAIRE.equals(activatorType));
			
			center();
		});
		
		final SaveHelper saveHelper = new SaveHelper(this::save);
		saveHelper.setShowUnsavedNotification(false);
		
		layout.addComponents(saveHelper.getValidationComponent(), activatorTypeComboBox, questionComponent, questionGroupComponent, questionnaireComponent, saveHelper.getSaveButton());
		setContent(layout);
		
		activators.forEach(this::load);
		
		saveHelper.addFields(activatorTypeComboBox);
		saveHelper.addFields(questionActivatorComponentGroup);
		saveHelper.addFields(questionnaireActivatorComponentGroup);
		saveHelper.addFields(questionGroupActivatorComponentGroup);
	}
	
	public ActivatorPopup(Activator activator, Collection<Questionnaire> questionnaires, Collection<QuestionGroup> questionGroups, Collection<Question> questions, Collection<ChoiceQuestion> activatingQuestions, SaveCallback saveCallback)
	{
		this(Collections.singleton(activator), questionnaires, questionGroups, questions, activatingQuestions, saveCallback);
	}
	
	private void load(Activator activator)
	{
		ActivatorType activatorType = null;
		
		if (activator instanceof QuestionActivator)
		{
			activatorType = ActivatorType.QUESTION;
			questionActivatorComponentGroup.setItemDataSource((QuestionActivator) activator);
		}
		else if (activator instanceof QuestionnaireActivator)
		{
			activatorType = ActivatorType.QUESTIONNAIRE;
			questionnaireActivatorComponentGroup.setItemDataSource((QuestionnaireActivator) activator);
		}
		else if (activator instanceof QuestionGroupActivator)
		{
			activatorType = ActivatorType.QUESTION_GROUP;
			questionGroupActivatorComponentGroup.setItemDataSource((QuestionGroupActivator) activator);
		}
		
		activatorTypeComboBox.addItem(activatorType);
		if (activatorTypeComboBox.size() == 1)
		{
			activatorTypeComboBox.setValue(activatorType);
			activatorTypeComboBox.setVisible(false);
		}
		else
		{
			activatorTypeComboBox.setValue(null);
			activatorTypeComboBox.setVisible(true);
		}
	}
	
	private Component createActivatorComponent(Collection<ChoiceQuestion> activatingQuestions, ComponentGroup<? extends Activator> componentGroup)
	{
		final BeanItemContainer<ChoiceQuestion> choiceQuestionBeanItemContainer = new BeanItemContainer<>(ChoiceQuestion.class, activatingQuestions);
		final BeanItemContainer<Choice> choiceBeanItemContainer = new BeanItemContainer<>(Choice.class);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		final ComboBox activatorQuestionComboBox = componentGroup.buildAndBind(true, I18N.ACTIVATORPOPUP_CAPTION_ACTIVATINGQUESTION.msg(), ComboBox.class, Activator_.activatingQuestion);
		activatorQuestionComboBox.setContainerDataSource(choiceQuestionBeanItemContainer);
		activatorQuestionComboBox.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ITEM);
		activatorQuestionComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		
		final OptionGroup activatorQuestionValuesOptionGroup = componentGroup.buildAndBind(true, I18N.ACTIVATORPOPUP_CAPTION_ACTIVATINGCHOICEVALUES.msg(), OptionGroup.class, Activator_.activatingChoices);
		activatorQuestionValuesOptionGroup.setContainerDataSource(choiceBeanItemContainer);
		activatorQuestionValuesOptionGroup.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
		activatorQuestionValuesOptionGroup.setItemCaptionPropertyId(DefaultI18N.NAME);
		activatorQuestionComboBox.addValueChangeListener(event ->
		{
			choiceBeanItemContainer.removeAllItems();
			ChoiceQuestion selectedQuestion = (ChoiceQuestion) activatorQuestionComboBox.getValue();
			if (selectedQuestion != null)
				choiceBeanItemContainer.addAll(selectedQuestion.getChoiceGroup().getChoices());
		});
		
		layout.addComponent(componentGroup.buildAndBind(true, I18N.ACTIVATORPOPUP_CAPTION_NATURALKEY.msg(), Activator_.naturalKey));
		layout.addComponents(activatorQuestionComboBox, activatorQuestionValuesOptionGroup);
		
		return layout;
	}
	
	private Component createQuestionComponent(Collection<Question> questions, Collection<ChoiceQuestion> activatingQuestions)
	{
		final BeanItemContainer<Question> questionBeanItemContainer = new BeanItemContainer<>(Question.class, questions);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setVisible(false);
		layout.setSpacing(true);
		
		final ComboBox questionComboBox = questionActivatorComponentGroup.buildAndBind(true, I18N.ACTIVATORPOPUP_CAPTION_ENABLESQUESTION.msg(), ComboBox.class, QuestionActivator_.enablesQuestion);
		questionComboBox.setContainerDataSource(questionBeanItemContainer);
		questionComboBox.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ITEM);
		questionComboBox.setItemCaptionPropertyId(PropertyPathBuilder.getFieldName(Question_.i18nKey));
		
		layout.addComponents(createActivatorComponent(activatingQuestions, questionActivatorComponentGroup), questionComboBox);
		
		return layout;
	}
	
	private Component createQuestionGroupComponent(Collection<QuestionGroup> questionGroups, Collection<ChoiceQuestion> activatingQuestions)
	{
		final BeanItemContainer<QuestionGroup> questionGroupBeanItemContainer = new BeanItemContainer<>(QuestionGroup.class, questionGroups);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setVisible(false);
		layout.setSpacing(true);
		
		final ComboBox questionGroupComboBox = questionGroupActivatorComponentGroup.buildAndBind(true, I18N.ACTIVATORPOPUP_CAPTION_ENABLESQUESTIONGROUP.msg(), ComboBox.class, QuestionGroupActivator_.enablesQuestionGroup);
		questionGroupComboBox.setContainerDataSource(questionGroupBeanItemContainer);
		questionGroupComboBox.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ITEM);
		questionGroupComboBox.setItemCaptionPropertyId(DefaultI18N.NAME);
		
		layout.addComponents(createActivatorComponent(activatingQuestions, questionGroupActivatorComponentGroup), questionGroupComboBox);
		
		return layout;
	}
	
	private Component createQuestionnaireComponent(Collection<Questionnaire> questionnaires, Collection<ChoiceQuestion> activatingQuestions)
	{
		final BeanItemContainer<Questionnaire> questionnaireBeanItemContainer = new BeanItemContainer<>(Questionnaire.class, questionnaires);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setVisible(false);
		layout.setSpacing(true);
		
		final ComboBox questionnaireComboBox = questionnaireActivatorComponentGroup.buildAndBind(true, I18N.ACTIVATORPOPUP_CAPTION_ENABLESQUESTIONNAIRE.msg(), ComboBox.class, QuestionnaireActivator_.enablesQuestionnaire);
		questionnaireComboBox.setContainerDataSource(questionnaireBeanItemContainer);
		questionnaireComboBox.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ITEM);
		questionnaireComboBox.setItemCaptionPropertyId(DefaultI18N.NAME);
		
		layout.addComponents(createActivatorComponent(activatingQuestions, questionnaireActivatorComponentGroup), questionnaireComboBox);
		
		return layout;
	}
	
	private void save() throws ValidationException
	{
		switch ((ActivatorType) activatorTypeComboBox.getValue())
		{
			case QUESTION:
				saveCallback.saveMapping(questionActivatorComponentGroup.getItemDataSource().getBean());
				break;
			case QUESTIONNAIRE:
				saveCallback.saveMapping(questionnaireActivatorComponentGroup.getItemDataSource().getBean());
				break;
			case QUESTION_GROUP:
				saveCallback.saveMapping(questionGroupActivatorComponentGroup.getItemDataSource().getBean());
				break;
		}
		close();
	}
}
