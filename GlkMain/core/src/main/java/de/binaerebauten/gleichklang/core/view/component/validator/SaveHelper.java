package de.binaerebauten.gleichklang.core.view.component.validator;

import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.I18N;
import de.binaerebauten.gleichklang.core.view.commit_strategy.DefaultValidationStrategy;
import de.binaerebauten.gleichklang.core.view.component.Committable;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.DialogResult;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxButtons;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxStyle;
import de.binaerebauten.gleichklang.core.view.component.Savable;
import de.binaerebauten.gleichklang.core.view.component.validator.ValidationComponent.ValidChangedListener;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class SaveHelper implements Savable
{
	public interface SaveListener
	{
		void save() throws ValidationException;
	}
	
	private final DefaultValidationStrategy defaultValidationStrategy;
	private final ValidationComponent validationComponent;
	private final Committable committable;
	private final SaveListener saveListener;
	private final Button saveButton;
	private final boolean autoCommit;
	private final ValidChangedListener validChangedListener;
	
	private boolean commitOnlyVisibleFields = true;
	private boolean showValidationNotification = true;
	
	public SaveHelper(SaveListener saveListener)
	{
		this(null, saveListener, true);
	}
	
	public SaveHelper(Committable committable, SaveListener saveListener)
	{
		this(committable, saveListener, false);
	}
	
	public SaveHelper(Committable committable, SaveListener saveListener, boolean autoCommit)
	{
		Objects.requireNonNull(saveListener);
		
		this.defaultValidationStrategy = new DefaultValidationStrategy();
		this.validationComponent = new ValidationComponent();
		this.committable = committable;
		this.saveListener = saveListener;
		this.autoCommit = autoCommit;
		
		this.saveButton = createSaveButton();
		this.validChangedListener = saveButton::setEnabled;
		
		this.validationComponent.setValidationStrategy(defaultValidationStrategy);
	}
	
	private Button createSaveButton()
	{
		final Button saveButton = new Button(I18N.SAVEHELPER_ACTION_SAVE.msg(), FontAwesome.SAVE);
		saveButton.addClickListener(event -> saveComplete());
		return saveButton;
	}
	
	public Button getSaveButton()
	{
		return saveButton;
	}
	
	public ValidationComponent getValidationComponent()
	{
		return validationComponent;
	}
	
	private void commit() throws CommitException
	{
		if (autoCommit)
		{
			final Collection<Field<?>> fields = commitOnlyVisibleFields ?
					validationComponent.getVisibleFields() :
					validationComponent.getFields();
			
			for (Field<?> field : fields)
			{
				try
				{
					field.commit();
				}
				catch (EmptyValueException ex)
				{
					// necessary because admin can save even there are empty
					if (!validationComponent.getValidationStrategy().ignoreEmptyValueExceptions())
					{
						throw ex;
					}
				}
			}
		}
		if (committable != null) committable.commit();
	}
	
	@Override
	public void saveComplete(SaveResultListener saveResultListener)
	{
		final ValidationResult validationResult = validationComponent.validate();
		if (validationResult.isSuccess())
		{
			final Map<Field<?>, EmptyValueException> emptyFields = validationResult.getEmptyFields();
			if (!emptyFields.isEmpty())
			{
				final String emptyFieldsString = StringUtils.join(validationResult.getEmptyFieldNames(), ", ");
				final String msg = String.format("Sie haben folgende notwendige Felder noch nicht ausgefühlt: \n%s. \nWollen Sie trotzdem Speichern?", emptyFieldsString);
				
				MessageBox.show(msg, MessageBoxButtons.YES_NO, MessageBoxStyle.ATTENTION, dialogResult ->
				{
					if (dialogResult.equals(DialogResult.YES))
					{
						commitAndSave(validationResult, saveResultListener);
					}
				});
			}
			else
			{
				commitAndSave(validationResult, saveResultListener);
			}
		}
		else
		{
			if (showValidationNotification)
				validationResult.showValidationNotification();
			
			if (saveResultListener != null)
				saveResultListener.saveResult(validationResult);
		}
	}
	
	private void commitAndSave(ValidationResult validationResult, SaveResultListener saveResultListener)
	{
		try
		{
			commit();
			saveListener.save();
		}
		catch (CommitException | InvalidValueException | ValidationException e)
		{
			validationResult.addOtherError(e.getLocalizedMessage());
		}
		
		validationComponent.showValidationResult(validationResult);
		
		if (showValidationNotification)
			validationResult.showValidationNotification();
		
		if (saveResultListener != null)
			saveResultListener.saveResult(validationResult);
	}
	
	public void addFields(ComponentGroup<?>... componentGroups)
	{
		validationComponent.addFields(componentGroups);
	}
	
	public void addFields(Collection<? extends Field<?>> fields)
	{
		validationComponent.addFields(fields);
	}
	
	public void addFields(Field<?>... fields)
	{
		validationComponent.addFields(fields);
	}
	
	public void removeFields(Collection<? extends Field<?>> fields)
	{
		validationComponent.removeFields(fields);
	}
	
	public void removeFields(Field<?>... fields)
	{
		validationComponent.removeFields(fields);
	}
	
	public void removeAllFields()
	{
		validationComponent.removeAllFields();
	}
	
	public void setShowValidationNotification(boolean showValidationNotification)
	{
		this.showValidationNotification = showValidationNotification;
	}
	
	public void setAutoEnabled(boolean autoEnabled)
	{
		if (autoEnabled)
			validationComponent.addValidChangedListener(validChangedListener);
		else
			validationComponent.removeValidChangedListener(validChangedListener);
	}
	
	public void setCommitOnlyVisibleFields(boolean commitOnlyVisibleFields)
	{
		this.commitOnlyVisibleFields = commitOnlyVisibleFields;
	}
	
	public void setShowUnsavedNotification(boolean showUnsavedNotification)
	{
		defaultValidationStrategy.setShowUnsavedNotification(showUnsavedNotification);
	}
	
	public void resetValidationResult()
	{
		validationComponent.resetUnsavedPanel();
		validationComponent.resetValidationPanel();
		validationComponent.resetFieldValidations();
	}
	
	public void setSuccessMessage(String successMessage)
	{
		defaultValidationStrategy.setSuccessMessage(successMessage);
	}
	
	public void setFailMessage(String failMessage)
	{
		defaultValidationStrategy.setFailMessage(failMessage);
	}
	
	public void setIgnoreEmptyValueExceptions(boolean ignoreEmptyValueExceptions)
	{
		defaultValidationStrategy.setIgnoreEmptyValueExceptions(ignoreEmptyValueExceptions);
	}
}
