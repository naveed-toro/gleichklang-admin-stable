package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.I18NEntity.BaseName;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion.RepresentationType;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion.SelectionType;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.QuestionType;
import de.binaerebauten.gleichklang.core.model.questionnaire.Question.Requirement;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.TranslationComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class QuestionPopup extends Popup
{
	public interface SaveCallback
	{
		void save(Question question, Collection<I18NEntity> i18NEntities) throws ValidationException;
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(QuestionPopup.class);
	
	private final ComponentGroup<NumberQuestion> numberQuestionComponentGroup;
	private final ComponentGroup<ChoiceQuestion> choiceQuestionComponentGroup;
	private final ComponentGroup<TextQuestion> textQuestionComponentGroup;
	private final ComponentGroup<RegionQuestion> regionQuestionComponentGroup;
	
	private final SaveHelper saveHelper;
	private final SaveCallback saveCallback;
	
	private final Map<QuestionType, TranslationComponent> nameTranslationComponents = new HashMap<>();
	private final Map<QuestionType, TranslationComponent> descriptionTranslationComponents = new HashMap<>();
	
	private final Component numberQuestionComponent;
	private final Component choiceQuestionComponent;
	private final Component textQuestionComponent;
	private final Component regionQuestionComponent;
	private final ComboBox questionTypeComboBox;
	
	private final boolean editMode;
	private final List<Component> deactivateInEditModeComponents = new ArrayList<>();
	
	private boolean defaultEmptyAnswerEnabled;
	private Choice defaultChoice;

	/**
	 * For editing a question.
	 *
	 * @param question         the question for editing
	 * @param i18nNames        the corresponding name translations
	 * @param i18nDescriptions the corresponding description translations
	 * @param questionGroups   available questionGroups
	 * @param saveCallback     callback for saving the edited entity
	 */
	public QuestionPopup(Question question, Collection<I18NEntity> i18nNames,
			Collection<I18NEntity> i18nDescriptions, Collection<QuestionGroup> questionGroups,
			Collection<ChoiceGroup> choiceGroups, SaveCallback saveCallback)
	{
		this(Collections.singleton(question), i18nNames, i18nDescriptions, questionGroups, choiceGroups,
				question instanceof ChoiceQuestion ? ((ChoiceQuestion) question).getDefaultChoice() : null,
				saveCallback, true);
	}

	/**
	 * For creating new questions.
	 *
	 * @param questions      new entities for the questions
	 * @param questionGroups available questionGroups
	 * @param saveCallback   callback for saving the new entity
	 */
	public QuestionPopup(Collection<Question> questions, Collection<QuestionGroup> questionGroups,
			Collection<ChoiceGroup> choiceGroups, SaveCallback saveCallback)
	{
		this(questions, null, null, questionGroups, choiceGroups, null, saveCallback, false);
	}
	
	private QuestionPopup(Collection<Question> questions, Collection<I18NEntity> i18nNames,
			Collection<I18NEntity> i18nDescriptions, Collection<QuestionGroup> questionGroups,
			Collection<ChoiceGroup> choiceGroups, Choice defaultChoice,
			SaveCallback saveCallback, boolean editMode)
	{
		super(I18N.QUESTIONPOPUP_CAPTION_NAME.msg());

		this.defaultChoice = defaultChoice;
		this.saveCallback = saveCallback;
		this.editMode = editMode;

		numberQuestionComponentGroup = new ComponentGroup<>(NumberQuestion.class);
		choiceQuestionComponentGroup = new ComponentGroup<>(ChoiceQuestion.class);
		textQuestionComponentGroup = new ComponentGroup<>(TextQuestion.class);
		regionQuestionComponentGroup = new ComponentGroup<>(RegionQuestion.class);
		
		saveHelper = new SaveHelper(this::save);
		saveHelper.setShowUnsavedNotification(false);
		
		for (QuestionType questionType : QuestionType.values())
		{
			final TranslationComponent nameTranslationComponent =
					new TranslationComponent(i18nNames, BaseName.QUESTION_NAME);
			final TranslationComponent descriptionTranslationComponent =
					new TranslationComponent(I18N.QUESTIONPOPUP_CAPTION_DESCRIPTION.msg(),
							i18nDescriptions, BaseName.QUESTION_DESCRIPTION,
							RichTextArea.class, false);
			
			nameTranslationComponents.put(questionType, nameTranslationComponent);
			descriptionTranslationComponents.put(questionType, descriptionTranslationComponent);
			
			saveHelper.addFields(nameTranslationComponent.getFields());
			saveHelper.addFields(descriptionTranslationComponent.getFields());
		}
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		numberQuestionComponent = createNumberQuestionComponent(questionGroups);
		choiceQuestionComponent = createChoiceQuestionComponent(questionGroups, choiceGroups);
		textQuestionComponent = createTextQuestionComponent(questionGroups);
		regionQuestionComponent = createRegionQuestionComponent(questionGroups);
		questionTypeComboBox = createQuestionTypeComboBox();

		layout.addComponents(saveHelper.getValidationComponent(),
				questionTypeComboBox, numberQuestionComponent,
				choiceQuestionComponent, textQuestionComponent,
				regionQuestionComponent, createControlButtons());
		setContent(layout);
		
		questions.forEach(this::load);
		
		saveHelper.addFields(questionTypeComboBox);
		saveHelper.addFields(numberQuestionComponentGroup);
		saveHelper.addFields(choiceQuestionComponentGroup);
		saveHelper.addFields(textQuestionComponentGroup);
		saveHelper.addFields(regionQuestionComponentGroup);
	}
	
	private ComboBox createQuestionTypeComboBox()
	{
		final ComboBox comboBox = ComponentFactory.getInstance().createField(ComboBox.class);
		comboBox.setRequired(true);
		comboBox.addValueChangeListener(event ->
		{
			final QuestionType questionType = (QuestionType) comboBox.getValue();
			
			numberQuestionComponent.setVisible(QuestionType.NUMBER_QUESTION.equals(questionType));
			choiceQuestionComponent.setVisible(QuestionType.CHOICE_QUESTION.equals(questionType));
			textQuestionComponent.setVisible(QuestionType.TEXT_QUESTION.equals(questionType));
			regionQuestionComponent.setVisible(QuestionType.REGION_QUESTION.equals(questionType));
			
			center();
		});
		return comboBox;
	}

	private CheckBox createDefaultEmptyAnswerCheckBox()
	{
		String label = I18N.QUESTIONPOPUP_CAPTION_DEFAULTEMPTYANSWER.msg();
		CheckBox checkBox = (CheckBox) ComponentFactory.getInstance().createFieldByType(boolean.class, label);
		checkBox.addValueChangeListener(e -> defaultEmptyAnswerEnabled = (boolean) e.getProperty().getValue());
		return checkBox;
	}

	private void load(Question question)
	{
		QuestionType questionType = null;
		
		if (question instanceof NumberQuestion)
		{
			questionType = QuestionType.NUMBER_QUESTION;
			numberQuestionComponentGroup.setItemDataSource((NumberQuestion) question);
		}
		else if (question instanceof ChoiceQuestion)
		{
			questionType = QuestionType.CHOICE_QUESTION;
			choiceQuestionComponentGroup.setItemDataSource((ChoiceQuestion) question);
		}
		else if (question instanceof TextQuestion)
		{
			questionType = QuestionType.TEXT_QUESTION;
			textQuestionComponentGroup.setItemDataSource((TextQuestion) question);
		}
		else if (question instanceof RegionQuestion)
		{
			questionType = QuestionType.REGION_QUESTION;
			regionQuestionComponentGroup.setItemDataSource((RegionQuestion) question);
		}
		
		questionTypeComboBox.addItem(questionType);
		if (questionTypeComboBox.size() == 1)
		{
			questionTypeComboBox.setValue(questionType);
			questionTypeComboBox.setVisible(false);
		}
		else
		{
			questionTypeComboBox.setValue(null);
			questionTypeComboBox.setVisible(true);
		}
		
		deactivateInEditModeComponents.forEach(c -> c.setEnabled(!editMode));
	}
	
	private Component createRegionQuestionComponent(Collection<QuestionGroup> questionGroups)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		layout.addComponent(createQuestionComponent(QuestionType.REGION_QUESTION, questionGroups, e -> {}));
		layout.addComponent(regionQuestionComponentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_WITHRELOCATION.msg(), RegionQuestion_.withRelocation));
		
		return layout;
	}
	
	private Component createTextQuestionComponent(Collection<QuestionGroup> questionGroups)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		final Field<?> numberOfLinesField = textQuestionComponentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_NUMBEROFLINES.msg(), TextQuestion_.numberOfLines);
		final ComboBox representationTypeComboBox = textQuestionComponentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_REPRESENTATIONTYPE.msg(), ComboBox.class, TextQuestion_.representationType);

		representationTypeComboBox.addValueChangeListener(e -> numberOfLinesField.setEnabled(!TextQuestion.RepresentationType.RICH_TEXT.equals(representationTypeComboBox.getValue())));

		layout.addComponent(createQuestionComponent(QuestionType.TEXT_QUESTION, questionGroups, e -> {}));
		layout.addComponent(textQuestionComponentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_MAXLENGTH.msg(), TextQuestion_.maxLength));
		layout.addComponent(numberOfLinesField);
		layout.addComponent(representationTypeComboBox);
		
		return layout;
	}
	
	private Component createChoiceQuestionComponent(Collection<QuestionGroup> questionGroups, Collection<ChoiceGroup> choiceGroups)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		final ComboBox choiceGroupComboBox = choiceQuestionComponentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_CHOICEVALUEGROUP.msg(), ComboBox.class, ChoiceQuestion_.choiceGroup);
		choiceGroupComboBox.setContainerDataSource(new BeanItemContainer<>(ChoiceGroup.class, choiceGroups));
		choiceGroupComboBox.setItemCaptionPropertyId(ChoiceGroup.NAME);
		
		final ComboBox selectionComboBox = choiceQuestionComponentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_SELECTIONTYPE.msg(), ComboBox.class, ChoiceQuestion_.selectionType);
		final ComboBox representationTypeComboBox = choiceQuestionComponentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_REPRESENTATIONTYPE.msg(), ComboBox.class, ChoiceQuestion_.representationType);
		final CheckBox defaultEmptyAnswerCheckBox = createDefaultEmptyAnswerCheckBox();
		final ComboBox defaultEmptyAnswerComboBox = createDefaultEmptyAnswerComboBox(defaultEmptyAnswerCheckBox);
		
		choiceGroupComboBox.addValueChangeListener(e -> {
			defaultEmptyAnswerComboBox.clear();
			((ChoiceGroup) e.getProperty().getValue()).getChoices().forEach(choice -> {
				defaultEmptyAnswerComboBox.addItem(choice);
				defaultEmptyAnswerComboBox.setItemCaption(choice, choice.getName());
			});
			
			// Should be placed here, because it is called after creating is finished
			if (Objects.nonNull(defaultChoice))
			{
				showDefaultEmptyAnswer(defaultEmptyAnswerCheckBox, defaultEmptyAnswerComboBox);
			}
		});
		
		selectionComboBox.addValueChangeListener(e -> defaultEmptyAnswerCheckBox.setEnabled(SelectionType.SINGLE.equals(e.getProperty().getValue())));
		
		representationTypeComboBox.addValidator((Validator) value ->
		{
			final RepresentationType representationType = (RepresentationType) value;
			final SelectionType selectionType = (SelectionType) selectionComboBox.getValue();
			
			if (SelectionType.MULTIPLE.equals(selectionType) && RepresentationType.RADIO.equals(representationType))
				throw new InvalidValueException(I18N.QUESTIONPOPUP_ERROR_REPRESENTATIONTYPE.msg());
		});

		layout.addComponent(createQuestionComponent(QuestionType.CHOICE_QUESTION, questionGroups, e ->
		{
			final Requirement requirement = (Requirement) e.getProperty().getValue();
			final SelectionType selectionType = (SelectionType) selectionComboBox.getValue();
			boolean defaultEmptyAnswerAvailable = SelectionType.SINGLE.equals(selectionType) &&
					(requirement == Requirement.OPTIONAL || requirement == Requirement.IMPORTANT);
			defaultEmptyAnswerCheckBox.setEnabled(defaultEmptyAnswerAvailable);
		}));
		layout.addComponent(choiceGroupComboBox);
		layout.addComponent(selectionComboBox);
		layout.addComponent(representationTypeComboBox);

		layout.addComponent(defaultEmptyAnswerCheckBox);
		layout.addComponent(defaultEmptyAnswerComboBox);

		deactivateInEditModeComponents.add(choiceGroupComboBox);

		return layout;
	}
	
	private ComboBox createDefaultEmptyAnswerComboBox(CheckBox defaultEmptyAnswerCheckBox)
	{
		final ComboBox defaultEmptyAnswerComboBox = ComponentFactory.getInstance().createField(ComboBox.class);
		defaultEmptyAnswerComboBox.setDescription(I18N.QUESTIONPOPUP_CAPTION_DEFAULTEMPTYANSWER_DESCRIPTION.msg());
		defaultEmptyAnswerComboBox.setEnabled(false);
		defaultEmptyAnswerCheckBox.addValueChangeListener(e -> defaultEmptyAnswerComboBox.setEnabled((Boolean) e.getProperty().getValue()));
		choiceQuestionComponentGroup.addAdditionalFields(defaultEmptyAnswerComboBox);
		
		defaultEmptyAnswerComboBox.addValueChangeListener(e -> defaultChoice = (Choice) e.getProperty().getValue());

		return defaultEmptyAnswerComboBox;
	}
	
	private void showDefaultEmptyAnswer(final CheckBox defaultEmptyAnswerCheckBox, final ComboBox defaultEmptyAnswerComboBox)
	{
		defaultEmptyAnswerCheckBox.setEnabled(true);
		defaultEmptyAnswerCheckBox.setValue(true);
		defaultEmptyAnswerComboBox.setValue(defaultChoice);
	}
	
	private Component createNumberQuestionComponent(Collection<QuestionGroup> questionGroups)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		final TextField minComponent = numberQuestionComponentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_MINVAL.msg(), TextField.class, NumberQuestion_.minVal);
		final TextField maxComponent = numberQuestionComponentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_MAXVAL.msg(), TextField.class, NumberQuestion_.maxVal);

		final Validator validator = value ->
		{
			final Integer maxValue = (Integer) maxComponent.getConvertedValue();
			final Integer minValue = (Integer) minComponent.getConvertedValue();
			
			if (minValue != null && maxValue != null)
			{
				if (minValue > maxValue)
				{
					throw new InvalidValueException("max darf nicht kleiner als min sein");
				}
			}
		};
		maxComponent.addValidator(validator);
		
		layout.addComponent(createQuestionComponent(QuestionType.NUMBER_QUESTION, questionGroups, e -> {}));
		layout.addComponent(minComponent);
		layout.addComponent(maxComponent);

		return layout;
	}

	private Component createQuestionComponent(QuestionType questionType,
			Collection<QuestionGroup> questionGroups,
			Property.ValueChangeListener mandatoryComboBoxChangeListener)
	{
		ComponentGroup<? extends Question> componentGroup = null;
		
		switch (questionType)
		{
			case CHOICE_QUESTION:
				componentGroup = choiceQuestionComponentGroup;
				break;
			case NUMBER_QUESTION:
				componentGroup = numberQuestionComponentGroup;
				break;
			case TEXT_QUESTION:
				componentGroup = textQuestionComponentGroup;
				break;
			case REGION_QUESTION:
				componentGroup = regionQuestionComponentGroup;
				break;
		}
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		final ComboBox questionGroupComboBox = componentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_QUESTIONGROUP.msg(), ComboBox.class, Question_.questionGroup);
		questionGroupComboBox.setContainerDataSource(new BeanItemContainer<>(QuestionGroup.class, questionGroups));
		questionGroupComboBox.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ITEM);
		questionGroupComboBox.setItemCaptionPropertyId(DefaultI18N.NAME);
		
		layout.addComponent(questionGroupComboBox);
		layout.addComponent(componentGroup.buildAndBind(true, I18N.QUESTIONPOPUP_CAPTION_KEY.msg(), Question_.i18nKey));
		layout.addComponent(nameTranslationComponents.get(questionType));
		layout.addComponent(descriptionTranslationComponents.get(questionType));
		
		ComboBox mandatoryComboBox = (ComboBox) componentGroup.buildAndBind(I18N.QUESTIONPOPUP_CAPTION_MANDATORY.msg(), Question_.requirement);
		mandatoryComboBox.addValueChangeListener(mandatoryComboBoxChangeListener);
		
		layout.addComponent(mandatoryComboBox);
		layout.addComponent(componentGroup.buildAndBind(I18N.QUESTIONPOPUP_CAPTION_ONLYADMINVISIBLE.msg(), Question_.onlyAdminVisible));
		layout.addComponent(componentGroup.buildAndBind(I18N.QUESTIONPOPUP_CAPTION_ADJUSTABLERELATIONSHIPVISIBILITY.msg(), Question_.adjustableRelationshipVisibility));
		
		return layout;
	}
	
	private Component createControlButtons()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.addComponents(saveHelper.getSaveButton());
		
		return layout;
	}
	
	private void save() throws ValidationException
	{
		Question question = null;
		final QuestionType questionType = (QuestionType) questionTypeComboBox.getValue();
		switch (questionType)
		{
			case NUMBER_QUESTION:
				question = numberQuestionComponentGroup.getItemDataSource().getBean();
				break;
			case CHOICE_QUESTION:
				question = choiceQuestionComponentGroup.getItemDataSource().getBean();
				break;
			case TEXT_QUESTION:
				question = textQuestionComponentGroup.getItemDataSource().getBean();
				break;
			case REGION_QUESTION:
				question = regionQuestionComponentGroup.getItemDataSource().getBean();
				break;
		}
		
		final TranslationComponent nameTranslationComponent = nameTranslationComponents.get(questionType);
		final TranslationComponent descriptionTranslationComponent = descriptionTranslationComponents.get(questionType);
		
		nameTranslationComponent.setI18nKey(question.getI18nKey());
		descriptionTranslationComponent.setI18nKey(question.getI18nKey());
		
		final HashSet<I18NEntity> i18NEntities = new HashSet<>();
		i18NEntities.addAll(nameTranslationComponent.getI18NEntities());
		i18NEntities.addAll(descriptionTranslationComponent.getI18NEntities());

		// Add a default empty answer iff. the corresponding check box is checked
		if (question instanceof ChoiceQuestion && Objects.nonNull(defaultChoice))
		{
			ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;
			if (defaultEmptyAnswerEnabled)
			{
				choiceQuestion.setDefaultChoice(defaultChoice);
			}
			else
			{
				// Remove using orphan removal property
				choiceQuestion.setDefaultChoice(null);
			}
		}

		saveCallback.save(question, i18NEntities);
		close();
	}
}
