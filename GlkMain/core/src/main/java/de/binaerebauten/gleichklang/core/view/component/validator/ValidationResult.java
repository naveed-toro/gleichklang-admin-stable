package de.binaerebauten.gleichklang.core.view.component.validator;

import com.google.common.base.Strings;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;
import de.binaerebauten.gleichklang.core.view.commit_strategy.DefaultValidationStrategy;
import de.binaerebauten.gleichklang.core.view.commit_strategy.ValidationStrategy;
import de.binaerebauten.gleichklang.core.view.component.CustomNotification;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidationResult
{
	private final Map<Field<?>, Validator.InvalidValueException> invalidFields = new LinkedHashMap<>();
	private final Map<Field<?>, Validator.EmptyValueException> emptyFields = new LinkedHashMap<>();
	
	private final List<String> otherErrors = new ArrayList<>();
	private ValidationStrategy commitStrategy = new DefaultValidationStrategy();
	
	public ValidationResult(@NotNull ValidationStrategy commitStrategy)
	{
		this.commitStrategy = commitStrategy;
	}
	
	public void addInvalidField(Field<?> invalidField, Validator.InvalidValueException ex)
	{
		this.invalidFields.put(invalidField, ex);
	}
	
	public void addInvalidFields(Map<Field<?>, Validator.InvalidValueException> invalidFields)
	{
		invalidFields.forEach((field, e) ->
		{
			if (e instanceof EmptyValueException)
			{
				this.emptyFields.put(field, (EmptyValueException) e);
			}
			else
			{
				this.invalidFields.put(field, e);
			}
		});
	}
	
	public void addEmptyValueField(Field<?> invalidField, EmptyValueException ex)
	{
		this.emptyFields.put(invalidField, ex);
	}
	
	public Map<Field<?>, EmptyValueException> getEmptyFields()
	{
		return emptyFields;
	}
	
	public Map<Field<?>, Validator.InvalidValueException> getInvalidFields()
	{
		return invalidFields;
	}
	
	public boolean isSuccess()
	{
		return (invalidFields.isEmpty() && otherErrors.isEmpty()) && (commitStrategy.ignoreEmptyValueExceptions() || emptyFields.isEmpty());
	}
	
	public List<String> getOtherErrors()
	{
		return otherErrors;
	}
	
	public void addOtherError(String error)
	{
		if (!Strings.isNullOrEmpty(error))
		{
			this.otherErrors.add(error);
		}
	}
	
	public void addValidationResult(ValidationResult otherValidationResult)
	{
		this.invalidFields.putAll(otherValidationResult.getInvalidFields());
		this.otherErrors.addAll(otherValidationResult.getOtherErrors());
		this.emptyFields.putAll(otherValidationResult.getEmptyFields());
	}
	
	/**
	 * Shows notification after validation. Following notifications are
	 * possible:
	 * <p>
	 * DATA_NOT_SAVED DATA_SAVED NO_CHANGES
	 */
	public void showValidationNotification()
	{
		if (isSuccess())
		{
			CustomNotification.show(commitStrategy.getSuccessMessage(), Notification.Type.TRAY_NOTIFICATION);
		}
		else
		{
			CustomNotification.show(commitStrategy.getFailMessage(), CustomNotification.Type.WARNING_TRAY_NOTIFICATION);
		}
	}
	
	public ValidationStrategy getCommitStrategy()
	{
		return commitStrategy;
	}
	
	public List<String> getEmptyFieldNames()
	{
		return emptyFields.keySet().stream().map(Component::getCaption).collect(Collectors.toList());
	}
}
