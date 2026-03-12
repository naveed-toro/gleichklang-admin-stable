package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Strings;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.Objects;

/**
 * This component is used for changing an email address of the {@link
 * de.binaerebauten.gleichklang.core.model.user.SignableUser} This component
 * does all necessary UI validation such as
 * email format validation
 * empty validation
 * email exists validation
 * email match validation
 */
public class ChangeEmailField extends CustomField<String> implements QuickRegistrable
{
	public interface UniqueEmailListener
	{
		boolean isMailUnqiue(String mail);
	}
	
	private class UniqueEmailValidator extends AbstractStringValidator
	{
		private UniqueEmailListener listener = null;
		
		public UniqueEmailValidator()
		{
			super(I18N.USER_VALIDATION_EMAILALREADYUSED.msg());
		}
		
		@Override
		protected boolean isValidValue(String value)
		{
			return listener == null || listener.isMailUnqiue(value);
		}
	}
	
	private class EqualsValidator extends AbstractStringValidator
	{
		public EqualsValidator()
		{
			super(I18N.USERDATAVIEW_VALIDATION_EMAILSNOTEQUAL.msg());
		}
		
		@Override
		protected boolean isValidValue(String value)
		{
			return Objects.equals(newMailTextField.getValue(), repeatMailTextField.getValue());
		}
	}
	
	private final Label actualMail;
	public final TextField newMailTextField;
	public final TextField repeatMailTextField;
	
	private final FormPanel layout;
	private final UniqueEmailValidator uniqueEmailValidator = new UniqueEmailValidator();
	
	public ChangeEmailField()
	{
		super.setValidationVisible(false);
		
		layout = new FormPanel();
		layout.addStyleName(CssStyle.GK_PANEL.getStyleName());
		
		actualMail = new Label();
		newMailTextField = createEmailTextField(I18N.EMAIL_CAPTION_NEWEMAIL.msg());
		repeatMailTextField = createEmailTextField(I18N.EMAIL_CAPTION_REPEATNEWEMAIL.msg());
		
		newMailTextField.addValidator(new EmailValidator(I18N.EMAIL_VALIDATION_INVALIDEMAIL.msg()));
		newMailTextField.addValidator(uniqueEmailValidator);
		repeatMailTextField.addValidator(new EqualsValidator());
		
		final Component labelComponent = layout.addFormElement(actualMail);
		labelComponent.setVisible(false);
		
		layout.addFormElement(newMailTextField);
		layout.addFormElement(repeatMailTextField);
		
		actualMail.addValueChangeListener(event ->
		{
			labelComponent.setVisible(!Strings.isNullOrEmpty(actualMail.getValue()));
		});
	}
	
	public void setUniqueEmailListener(UniqueEmailListener uniqueEmailListener)
	{
		uniqueEmailValidator.listener = uniqueEmailListener;
	}
	
	private TextField createEmailTextField(String label)
	{
		final TextField textField = ComponentFactory.getInstance().createField(TextField.class, label);
		textField.setRequired(true);
		textField.setRequiredError(I18N.FIELD_REQUIRED.msg());

		textField.addValueChangeListener(e -> trimMailTextField());
		textField.addValueChangeListener(e -> trimEmailRepeated());
		
		return textField;
	}

	private void trimEmailRepeated()
	{


		if(repeatMailTextField.getValue() !=null)
			repeatMailTextField.setValue(repeatMailTextField.getValue().trim());

		fireValueChange(true);
	}

	private void trimMailTextField(){

		if(newMailTextField.getValue() !=null)
			newMailTextField.setValue(newMailTextField.getValue().trim());

		fireValueChange(true);

	}
	
	@Override
	public void validate() throws InvalidValueException
	{
		newMailTextField.validate();
		repeatMailTextField.validate();
	}
	
	@Override
	public void setComponentError(ErrorMessage componentError)
	{
		super.setComponentError(componentError);
	}
	
	@Override
	public void setRequired(boolean required)
	{
		newMailTextField.setRequired(required);
		repeatMailTextField.setRequired(required);
	}
	
	@Override
	public void setRequiredError(String requiredMessage)
	{
		newMailTextField.setRequiredError(requiredMessage);
		repeatMailTextField.setRequiredError(requiredMessage);
	}
	
	@Override
	public void setValidationVisible(boolean validateAutomatically)
	{
		newMailTextField.setValidationVisible(validateAutomatically);
		repeatMailTextField.setValidationVisible(validateAutomatically);
	}
	
	@Override
	public String getCaption()
	{
		if(!Strings.isNullOrEmpty(super.getCaption())) return super.getCaption();
		if(!Strings.isNullOrEmpty(layout.getCaption())) return layout.getCaption();
		return I18N.EMAIL_CAPTION_DEFAULTFIELDNAME.msg();
	}
	
	@Override
	protected String getInternalValue()
	{
		return newMailTextField.getValue();
	}
	
	@Override
	protected void setInternalValue(String newValue)
	{
		super.setInternalValue(newValue);
		
		reset();
		actualMail.setValue(newValue);
	}
	
	public void reset()
	{
		newMailTextField.setValue(null);
		repeatMailTextField.setValue(null);
	}
	
	@Override
	public void quickRegister(String email)
	{
		newMailTextField.setValue(email);
		repeatMailTextField.setValue(email);
	}
	
	@Override
	protected Component initContent()
	{
		return layout;
	}
	
	@Override
	public Class<? extends String> getType()
	{
		return String.class;
	}
	
	@Override
	public void setCaption(String panelCaption)
	{
		layout.setCaption(panelCaption);
	}
}
