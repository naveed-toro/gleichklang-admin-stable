package de.binaerebauten.gleichklang.core.view.component.validator;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.launcher.H2SchemaGenerator;
import de.binaerebauten.gleichklang.core.utils.FunctionalUtils.Recursive;
import de.binaerebauten.gleichklang.core.view.I18N;
import de.binaerebauten.gleichklang.core.view.commit_strategy.DefaultValidationStrategy;
import de.binaerebauten.gleichklang.core.view.commit_strategy.ValidationStrategy;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.binaerebauten.gleichklang.core.view.I18N.REGISTRATIONVIEW_VALIDATION_EMPTY_QUESTIONS;
import static de.binaerebauten.gleichklang.core.view.I18N.REGISTRATIONVIEW_VALIDATION_INVALID_QUESTIONS;

/**
 * This component is used for validation of fields it can be placeabel
 */
public class ValidationComponent extends CustomComponent
{
	private static final Logger log = LoggerFactory.getLogger(ValidationComponent.class);

	public interface ValidChangedListener
	{
		void onValidChanged(boolean isValid);
	}
	
	private enum NotificationType
	{
		FAILURE("failure"),
		WARN("warn");
		
		private final String name;
		
		NotificationType(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
	}
	
	private final Set<ValidChangedListener> validChangedListeners = new HashSet<>();
	private final VerticalLayout errorLayout;
	private final VerticalLayout unsavedLayout;
	private final Collection<Field<?>> fields = new LinkedHashSet<>();
	private ValidationStrategy validationStrategy = new DefaultValidationStrategy();
	private boolean isValid = true;
	private final ValueChangeListener valueChangeListener = event -> onValueChanged((Field<?>) event.getProperty());

	private int minAgevalue=0;
	private int maxAgevalue=0;
	private int minHeightvalue=0;
	private int maxheightvalue=0;


	public ValidationComponent()
	{
		final VerticalLayout rootLayout = new VerticalLayout();
		
		validationStrategy = new DefaultValidationStrategy();
		
		errorLayout = new VerticalLayout();
		unsavedLayout = new VerticalLayout();
		
		errorLayout.setStyleName(NotificationType.FAILURE.getName());
		unsavedLayout.setStyleName(NotificationType.WARN.getName());
		unsavedLayout.addComponent(new Label(I18N.NOTIFICATION_NOT_SAVED.msg()));
		
		rootLayout.addComponents(errorLayout, unsavedLayout);
		setCompositionRoot(rootLayout);
		
		resetValidationPanel();
		resetUnsavedPanel();
	}
	
	private void updateEmptyStyle()
	{
		if (unsavedLayout.isVisible() || errorLayout.isVisible())
		{
			removeStyleName(CssStyle.FORM_PART_EMPTY.getStyleName());
		}
		else
		{
			addStyleName(CssStyle.FORM_PART_EMPTY.getStyleName());
		}
	}
	
	public void addFields(ComponentGroup<?>... componentGroups)
	{
		for (ComponentGroup<?> componentGroup : componentGroups)
		{
			componentGroup.getFields().forEach(this::addField);
		}
		updateIsValid();
	}
	
	public void addFields(Collection<? extends Field<?>> fields)
	{
		fields.forEach(this::addField);
		updateIsValid();
	}
	
	public void addFields(Field<?>... fields)
	{
		Arrays.stream(fields).forEach(this::addField);
		updateIsValid();
	}
	
	private void addField(Field<?> field)
	{
		if (field == null) return;
		
		this.fields.add(field);
		
		field.setRequiredError(de.binaerebauten.gleichklang.core.view.I18N.USERDATAVIEW_VALIDATION_REQUIRED.msg());
		
		if (field instanceof AbstractField)
		{
			((AbstractField<?>) field).setValidationVisible(false);
		}
		
		field.addValueChangeListener(valueChangeListener);
	}
	
	public void removeFields(Collection<? extends Field<?>> fields)
	{
		fields.forEach(this::removeField);
		updateIsValid();
	}
	
	public void removeFields(Field<?>... fields)
	{
		Arrays.stream(fields).forEach(this::removeField);
		updateIsValid();
	}
	
	private void removeField(Field<?> field)
	{
		if (field == null) return;
		
		this.fields.remove(field);
		
		field.removeValueChangeListener(valueChangeListener);
	}
	
	public void removeAllFields()
	{
		final List<Field<?>> removeFields = new ArrayList<>(fields);
		removeFields(removeFields);
	}
	
	/**
	 * Should be called from the commit method in the implementation class.
	 */
	public ValidationResult validate()
	{
		final ValidationResult validationResult = validateAllFields();
		
		showValidationResult(validationResult);
		
		return validationResult;
	}
	
	private ValidationResult validateAllFields()
	{
		final ValidationResult validationResult = new ValidationResult(validationStrategy);
		
		/*
		  Only visible components should be validated.
		  <p>
		  It may happen (e.g. due to combobox selection) that some components
		  are hidden from the UI, but they were added to the validation component
		  already. These components should not be validated.
		 */
		
		final Stream<Field<?>> visibleFieldsStream = getVisibleFieldStream();
		visibleFieldsStream.forEach(field -> validateField(field, validationResult));
		
		return validationResult;
	}
	
	private Stream<Field<?>> getVisibleFieldStream()
	{
		final Recursive<Function<Component, Boolean>> isVisible = new Recursive<>();
		isVisible.func = c -> Objects.isNull(c) || c.isVisible() && isVisible.func.apply(c.getParent());
		
		return fields.stream().filter(isVisible.func::apply);
	}
	
	/**
	 * validates field with immediately feedback.
	 *
	 * @param field
	 */
	private void onValueChanged(Field<?> field)
	{
		resetValidationPanel();
		resetFieldValidation(field);
		
		final ValidationResult validationResult = new ValidationResult(validationStrategy);
		validateField(field, validationResult);
		
		showFieldValidation(validationResult);
		showUnsavedPanel();
		updateIsValid();
	}
	
	/**
	 * Validates field and saves the result to validation result
	 *
	 * @param field
	 * @param validationResult
	 */
	private void validateField(Field<?> field, ValidationResult validationResult)
	{
		try
		{
			field.validate();

			//Validation for age and height so that max age and height always greater than min age and height.
			if(field.getCaption()!=null &&
					(field.getCaption().equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MIN_AGE.msg())
							|| field.getCaption().equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MAX_AGE.msg())
							|| field.getCaption().equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MIN_HEIGHT.msg())
							|| field.getCaption().equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MAX__HEIGHT.msg())
					        || field.getCaption().equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MIN_AGE_FRIENDSHIP.msg())
					        || field.getCaption().equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MAX_AGE_FRIENDSHIP.msg())))
			{
				validateAgeAndHeight(field,validationResult);
			}
		}
		catch (Validator.EmptyValueException ex)
		{
			validationResult.addEmptyValueField(field, ex);
		}
		catch (Validator.InvalidValueException ex)
		{
			validationResult.addInvalidField(field, ex);
		}
	}

	/**
	 * This method is used for validating minimum age and height should be greater than maximum age and height.
    * @param field
    * @param validationResult
    */
	public void validateAgeAndHeight(Field<?> field, ValidationResult validationResult )
	{

		if(field.getCaption()!=null && field.getValue()!=null)
		{
			String caption=field.getCaption();

			if(caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MIN_AGE.msg()) || caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MIN_AGE_FRIENDSHIP.msg()))
			{
				minAgevalue=Integer.parseInt(field.getValue().toString());
			}

			if(caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MAX_AGE.msg()) || caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MAX_AGE_FRIENDSHIP.msg()))
			{
				maxAgevalue=Integer.parseInt(field.getValue().toString());
			}
			if((minAgevalue!=0 && maxAgevalue !=0 && minAgevalue>maxAgevalue)
					&& (caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MIN_AGE.msg())
					|| caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MAX_AGE.msg())
			        || caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MAX_AGE_FRIENDSHIP.msg())
			        || caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MIN_AGE_FRIENDSHIP.msg())))
			{
				validationResult.addInvalidField(field, new Validator.InvalidValueException(I18N.REGISTRATIONVIEW_VALIDATION_MIN_MAX_AGE_VALIDATION_ERROR.msg()));
			}


			if(caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MIN_HEIGHT.msg()))
			{
				minHeightvalue=Integer.parseInt(field.getValue().toString());
			}
			if(caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MAX__HEIGHT.msg()))
			{
				maxheightvalue=Integer.parseInt(field.getValue().toString());
			}
			if((minHeightvalue!=0 && maxheightvalue !=0 && minHeightvalue>maxheightvalue)
					&& (caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MIN_HEIGHT.msg())
					|| caption.equalsIgnoreCase(I18N.REGISTRATIONVIEW_VALIDATION_MAX__HEIGHT.msg())))
			{
				validationResult.addInvalidField(field, new Validator.InvalidValueException(I18N.REGISTRATIONVIEW_VALIDATION_MIN_MAX_HEIGHT_VALIDATION_ERROR.msg()));
			}
		}

	}


	public Collection<Field<?>> getFields()
	{
		return Collections.unmodifiableCollection(fields);
	}
	
	public Collection<Field<?>> getVisibleFields()
	{
		return getVisibleFieldStream().collect(Collectors.toList());
	}
	
	public void resetValidationPanel()
	{
		errorLayout.setVisible(false);
		errorLayout.removeAllComponents();
		
		updateEmptyStyle();
	}
	
	public void resetUnsavedPanel()
	{
		unsavedLayout.setVisible(false);
		
		updateEmptyStyle();
	}
	
	public void resetFieldValidations()
	{
		for (Field<?> field : fields)
		{
			resetFieldValidation(field);
		}
	}
	
	private void resetFieldValidation(Field<?> field)
	{
		if (field instanceof AbstractField)
		{
			((AbstractField<?>) field).setComponentError(null);
		}
	}
	
	/**
	 * shows validation result if validationResult has invalidValues,
	 * invalidValues are shown otherwise positive notification is returned
	 *
	 * @param validationResult filled validation result
	 */
	public void showValidationResult(ValidationResult validationResult)
	{
		resetUnsavedPanel();
		resetValidationPanel();
		resetFieldValidations();
		
		showFieldValidation(validationResult);
		showValidationPanel(validationResult);
	}
	
	private void showFieldValidation(ValidationResult validationResult)
	{
		showFieldValidation(validationResult.getEmptyFields());
		showFieldValidation(validationResult.getInvalidFields());
	}
	
	private void showFieldValidation(Map<Field<?>, ? extends Validator.InvalidValueException> fields)
	{
		fields.keySet().stream()
				.filter(f -> f instanceof AbstractField)
				.map(f -> (AbstractField<?>) f)
				.forEach(f -> f.setComponentError(new UserError(fields.get(f).getLocalizedMessage())));
	}
	
	private void showValidationPanel(ValidationResult validationResult)
	{
		showValidationPanel(validationResult.getInvalidFields(), REGISTRATIONVIEW_VALIDATION_INVALID_QUESTIONS.msg(), true);
		showValidationPanel(validationResult.getEmptyFields(), REGISTRATIONVIEW_VALIDATION_EMPTY_QUESTIONS.msg(), false);
		
		for (String validationError : validationResult.getOtherErrors())
		{
			errorLayout.addComponent(new Label(validationError, ContentMode.HTML));
		}
		
		errorLayout.setVisible(!validationResult.isSuccess());
		
		//needed to enable correct focusing of error message
		if (!validationResult.isSuccess())
		{
			final Button placeholder = new Button();
			placeholder.setStyleName(CssStyle.FOCUS_BTN.getStyleName());
			errorLayout.addComponent(placeholder, 0);
			placeholder.focus();
		}
		
		updateEmptyStyle();
	}
	
	private void showValidationPanel(Map<Field<?>, ? extends Validator.InvalidValueException> fields, String label, boolean printExceptionMessage)
	{
		if (fields.isEmpty()) return;
		
		final Label invalidQuestionsLabel = ComponentFactory.getInstance().createBoldLabel(label);
		errorLayout.addComponent(invalidQuestionsLabel);
		
		for (Field<?> invalidField : fields.keySet())
		{
			final String exceptionMessage = printExceptionMessage ? " (" + fields.get(invalidField).getLocalizedMessage() + ")" : "";
			String fieldName = Objects.nonNull(invalidField.getCaption()) ? invalidField.getCaption() : invalidField.getDescription();
			errorLayout.addComponent(new Label(fieldName + exceptionMessage));
		}
	}
	
	private void showUnsavedPanel()
	{
		unsavedLayout.setVisible(getValidationStrategy().isShowUnsavedNotification());
		
		updateEmptyStyle();
	}
	
	public ValidationStrategy getValidationStrategy()
	{
		return validationStrategy;
	}
	
	public void setValidationStrategy(ValidationStrategy validationStrategy)
	{
		this.validationStrategy = validationStrategy;
	}
	
	private void updateIsValid()
	{
		final boolean isValid = validateAllFields().isSuccess();
		
		if (this.isValid != isValid)
		{
			validChangedListeners.forEach(l -> l.onValidChanged(isValid));
			this.isValid = isValid;
		}
	}
	
	public void addValidChangedListener(ValidChangedListener validChangedListener)
	{
		if (validChangedListener == null) return;
		validChangedListeners.add(validChangedListener);
	}
	
	public void removeValidChangedListener(ValidChangedListener validChangedListener)
	{
		if (validChangedListener == null) return;
		validChangedListeners.remove(validChangedListener);
	}
}
