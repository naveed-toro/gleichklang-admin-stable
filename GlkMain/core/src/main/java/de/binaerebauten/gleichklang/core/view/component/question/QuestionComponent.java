package de.binaerebauten.gleichklang.core.view.component.question;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.matching.Activator;
import de.binaerebauten.gleichklang.core.model.questionnaire.*;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer.AnswerType;
import de.binaerebauten.gleichklang.core.model.questionnaire.ChoiceQuestion.SelectionType;
import de.binaerebauten.gleichklang.core.security.EntityContentSanitizer;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;
import de.binaerebauten.gleichklang.core.utils.DefaultI18N;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.QuickRegistrable;
import de.binaerebauten.gleichklang.core.view.converter.SetConverter;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static de.binaerebauten.gleichklang.core.model.questionnaire.Answer.DEFAULT_RELATIONSHIP_VISIBLE;

/**
 * Creates UI components for {@link Question} and {@link Answer} component
 * dependent on question types.
 */
public class QuestionComponent extends CustomComponent implements QuickRegistrable
{
	public interface ActivationListener
	{
		void activationChanged(Collection<Activator> changedActivators);
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(QuestionComponent.class);
	
	private final ComponentGroup<RegionAnswer> regionAnswerComponentGroup = new ComponentGroup<>(RegionAnswer.class);
	private final ComponentGroup<TextAnswer> textAnswerComponentGroup = new ComponentGroup<>(TextAnswer.class);
	private final ComponentGroup<NumberAnswer> numberAnswerComponentGroup = new ComponentGroup<>(NumberAnswer.class);
	private final ComponentGroup<ChoiceAnswer> choiceAnswerComponentGroup = new ComponentGroup<>(ChoiceAnswer.class);
	
	private final QuestionnaireActivation activation;
	private final Component answerComponent;
	private final CheckBox profileVisibleComponent;
	private final Question question;
	private final Answer answer;
	private final AnswerType answerType;
	private ActivationListener activationListener = null;
	
	public QuestionComponent(Answer answer, LocatableHandler locatableHandler)
	{
		this(answer, locatableHandler, null);
	}
	
	public QuestionComponent(Answer answer, LocatableHandler locatableHandler, QuestionnaireActivation activation)
	{
		Objects.requireNonNull(locatableHandler);
		Objects.requireNonNull(answer);
		
		this.answer = answer;
		this.answerType = AnswerType.valueOf(answer);
		this.question = answer.getQuestion();
		this.activation = activation;
		
		this.answerComponent = createAnswerComponent(locatableHandler);
		this.profileVisibleComponent = createProfileVisibleComponent();
		
		setCompositionRoot(createLayout());
		load(answer);
	}
	
	private CheckBox createProfileVisibleComponent()
	{
		return getComponentGroup().buildAndBind(false, de.binaerebauten.gleichklang.core.view.I18N.PROFILE_VISIBLE.msg(), CheckBox.class, Answer_.relationshipVisible);
	}
	
	private Component createLayout()
	{
		final FormPanel layout = new FormPanel();
		
		layout.addFormElement(answerComponent);
		
		if (question.isAdjustableRelationshipVisibility())
		{
			layout.addFormElement(profileVisibleComponent);
		}
		
		return layout;
	}
	
	private void load(Answer answer)
	{
		switch (answerType)
		{
			case TEXT:
				textAnswerComponentGroup.setItemDataSource((TextAnswer) answer);
				break;
			case CHOICE:
				choiceAnswerComponentGroup.setItemDataSource((ChoiceAnswer) answer);
				break;
			case NUMBER:
				numberAnswerComponentGroup.setItemDataSource((NumberAnswer) answer);
				break;
			case REGION:
				regionAnswerComponentGroup.setItemDataSource((RegionAnswer) answer);
				((RegionAnswerComponent) answerComponent).initComponent();
				break;
		}
		
		if (activation != null)
			setActivated(activation.isEnabledQuestion(answer.getQuestion()));
	}
	
	private ComponentGroup<? extends Answer> getComponentGroup()
	{
		switch (answerType)
		{
			case TEXT:
				return textAnswerComponentGroup;
			case CHOICE:
				return choiceAnswerComponentGroup;
			case NUMBER:
				return numberAnswerComponentGroup;
			case REGION:
				return regionAnswerComponentGroup;
		}
		
		return textAnswerComponentGroup;
	}
	
	public Answer getAnswer()
	{
		return getComponentGroup().getItemDataSource().getBean();
	}
	
	@Override
	public void quickRegister(String value)
	{
		switch (answerType)
		{
			case TEXT:
				((AbstractTextField) answerComponent).setValue(value);
				break;
			case CHOICE:
				final AbstractSelect abstractSelect = (AbstractSelect) answerComponent;
				abstractSelect.select(abstractSelect.getVisibleItemIds().iterator().next());
				break;
			case NUMBER:
				((AbstractTextField) answerComponent).setValue("42");
				break;
			case REGION:
				break;
		}
	}
	
	public Collection<Field<?>> getFields()
	{
		return getComponentGroup().getFields();
	}
	
	public void setActivated(boolean enabledQuestion)
	{
		setVisible(enabledQuestion);
		if (!enabledQuestion) resetValue();
	}
	
	public void resetValue()
	{
		if (answerComponent instanceof Field)
		{
			((Field<?>) answerComponent).setValue(null);
			profileVisibleComponent.setValue(DEFAULT_RELATIONSHIP_VISIBLE);
		}
	}
	
	private AbstractField<String> createNumberField(NumberQuestion question)
	{
		final TextField resultField = (TextField) numberAnswerComponentGroup.buildAndBind(question.isRequired(), question.getName(), NumberAnswer_.numberValue);
		
		final int minValue = question.getMinVal();
		final int maxValue = question.getMaxVal();
		
		resultField.setConverter(new StringToIntegerConverter());
		resultField.setConversionError(de.binaerebauten.gleichklang.core.view.component.I18N.ANSWERFIELD_CONVERSION_ERROR.msg(minValue, maxValue));
		
		if (minValue > 0 || maxValue > 0)
		{
			final Validator integerRangeValidator = new IntegerRangeValidator(
					de.binaerebauten.gleichklang.core.view.component.I18N.QUESTIONNAIRE_NUMBER_RANGE_FAIL.msg(minValue, maxValue), minValue, maxValue);
			resultField.addValidator(integerRangeValidator);
		}
		
		return resultField;
	}
	
	private AbstractField<String> createTextField(TextQuestion question)
	{
		final Class<? extends AbstractField<String>> representationType;
		
		if (TextQuestion.RepresentationType.RICH_TEXT.equals(question.getRepresentationType()))
		{
			representationType = RichTextArea.class;
		}
		else
		{
			representationType = question.getNumberOfLines() > 1 ? TextArea.class : TextField.class;
		}
		
		final AbstractField<String> textField = textAnswerComponentGroup.buildAndBind(question.isRequired(), question.getName(), representationType, TextAnswer_.textValue);
		textField.addValidator(new StringLengthValidator(I18N.QUESTIONCOMPONENT_VALIDATION_TEXTLENGTH.msg(), 0, question.getMaxLength(), !question.isRequired())
		{
			@Override
			protected boolean isValidValue(String value)
			{
				return super.isValidValue(EntityContentSanitizer.SANITIZER.sanitize(value));
			}
		});
		
		if (textField instanceof AbstractTextField)
		{
			((AbstractTextField) textField).setMaxLength(question.getMaxLength());
		}
		
		if (textField instanceof TextArea)
		{
			((TextArea) textField).setRows(question.getNumberOfLines());
		}
		
		return textField;
	}
	
	private AbstractSelect createChoiceField(ChoiceQuestion question)
	{
		final BeanItemContainer<Choice> choicesContainer = new BeanItemContainer<>(Choice.class, question.getChoiceGroup().getChoices());
		final boolean multipleSelection = SelectionType.MULTIPLE.equals(question.getSelectionType());
		final AbstractSelect choicesField;
		final Class<? extends AbstractSelect> representationType;
		
		switch (question.getRepresentationType())
		{
			case AFFINITY_INVERTED:
			case AFFINITY:
				representationType = OptionGroup.class;
				break;
			case RADIO:
				representationType = OptionGroup.class;
				break;
			default:
				representationType = multipleSelection ? OptionGroup.class : ComboBox.class;
		}
		
		choicesField = choiceAnswerComponentGroup.buildAndBind(question.isRequired(), question.getName(), representationType, ChoiceAnswer_.choices);
		choicesField.setStyleName("horizontal");
		
		if (choicesField instanceof ComboBox)
		{
			final ComboBox comboBox = (ComboBox) choicesField;
			comboBox.setTextInputAllowed(false);
			comboBox.setInputPrompt(de.binaerebauten.gleichklang.core.view.component.I18N.QUESTIONNAIRE_INPUT_PROMPT.msg());
		}
		
		choicesField.setMultiSelect(multipleSelection);
		if (!multipleSelection) choicesField.setConverter(new SetConverter<>());
		choicesField.setContainerDataSource(choicesContainer);
		choicesField.setNullSelectionAllowed(!question.isRequired());
		choicesField.setItemCaptionMode(AbstractSelect.ItemCaptionMode.ITEM);
		choicesField.setItemCaptionPropertyId(DefaultI18N.NAME);
		choicesField.addValueChangeListener(this::onValueChanged);
		
		return choicesField;
	}
	
	private void onValueChanged(ValueChangeEvent event)
	{
		LOG.info("Change question event {} ", answer.getQuestion().getName());
		
		if (this.activation != null && answer instanceof ChoiceAnswer)
		{
			final Object value = event.getProperty().getValue();
			final Set<Choice> choices = value instanceof Set ? (Set<Choice>) value : Collections.singleton((Choice) value);
			
			final ChoiceAnswer choiceAnswer = (ChoiceAnswer) answer;
			final Set<Activator> changedActivators = this.activation.updateActivatingAnswer(choiceAnswer, choices);
			
			if (activationListener != null)
			{
				activationListener.activationChanged(changedActivators);
			}
		}
		
		if (!answer.isAnswered())
		{
			answerComponent.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
			answerComponent.removeStyleName(CssStyle.ANSWERED.getStyleName());
		}
		else
		{
			answerComponent.addStyleName(CssStyle.ANSWERED.getStyleName());
			answerComponent.removeStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
		}
	}
	
	private Component createAnswerComponent(LocatableHandler locatableHandler)
	{
		final AbstractComponent answerComponent;
		
		switch (answerType)
		{
			case TEXT:
				answerComponent = createTextField((TextQuestion) question);
				break;
			case CHOICE:
				answerComponent = createChoiceField((ChoiceQuestion) question);
				break;
			case NUMBER:
				answerComponent = createNumberField((NumberQuestion) question);
				break;
			case REGION:
				answerComponent = new RegionAnswerComponent((RegionQuestion) question, regionAnswerComponentGroup, locatableHandler);
				this.addStyleName(CssStyle.REGION_QUESTION_LAYOUT.getStyleName());
				break;
			default:
				return null;
		}
		
		answerComponent.setCaption(question.getName());
		answerComponent.addStyleName(CssStyle.QUESTION.getStyleName());
		
		final boolean hasDescription = question.hasKey(question.getI18nKey(), I18NEntity.BaseName.QUESTION_DESCRIPTION);
		if (hasDescription)
		{
			if (answerType != AnswerType.REGION) // prevent permanently showing of tooltip in case of region question
				answerComponent.setDescription(question.getDescription());
		}
		
		if (!answer.isAnswered() && answerType != AnswerType.REGION)
			answerComponent.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
		else if (answer.isAnswered() && answerType != AnswerType.REGION)
			answerComponent.addStyleName(CssStyle.ANSWERED.getStyleName());
		
		return answerComponent;
	}
	
	public void setActivationListener(ActivationListener activationListener)
	{
		this.activationListener = activationListener;
	}
}
