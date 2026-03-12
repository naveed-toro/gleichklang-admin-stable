package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Strings;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.view.PasswordStrengthValidator;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.Objects;

public class ChangePasswordField extends CustomField<String> implements QuickRegistrable
{
	private class LengthValidator extends StringLengthValidator
	{
		public LengthValidator()
		{
			super(I18N.CHANGEPASSWORD_VALIDATION_PASSWORDLENGTH.msg(SignableUser.MIN_PASSWORD_LENGTH), SignableUser.MIN_PASSWORD_LENGTH, null, false);
		}
	}
	
	private class EqualsValidator extends AbstractStringValidator
	{
		public EqualsValidator()
		{
			super(I18N.CHANGEPASSWORD_VALIDATION_PASSWORDSNOTEQUAL.msg());
		}
		
		@Override
		protected boolean isValidValue(String value)
		{
			return Objects.equals(newPasswordField.getValue(), repeatPwTextField.getValue());
		}
	}
	
	private final HorizontalLayout passwordCheckComponent;
	private final PasswordField newPasswordField;
	private final PasswordField repeatPwTextField;
	private final PasswordField oldPasswordField;
	
	private final FormPanel layout;
	
	public ChangePasswordField()
	{
		this(false, "", "");
	}
	
	public ChangePasswordField(boolean showOldPasswordField, String caption, String description)
	{
		super.setValidationVisible(false);
		
		layout = new FormPanel(caption);
		layout.setDescription(description);
		layout.setMargin(false);
		layout.addStyleName(CssStyle.GK_PANEL.getStyleName());
		
		if (showOldPasswordField)
		{
			oldPasswordField = createPasswordField(I18N.CHANGEPASSWORD_CAPTION_OLDPASSWORD.msg());
			layout.addFormElement(oldPasswordField);
		}
		else
		{
			oldPasswordField = null;
		}
		
		passwordCheckComponent = createPasswordCheckComponent();
		newPasswordField = createPasswordField(I18N.CHANGEPASSWORD_CAPTION_NEWPASSWORD.msg());
		repeatPwTextField = createPasswordField(I18N.CHANGEPASSWORD_CAPTION_REPEATNEWPASSWORD.msg());
		
		repeatPwTextField.addValidator(new EqualsValidator());
		newPasswordField.addValidator(new LengthValidator());
		
		newPasswordField.addValueChangeListener(event -> updatePasswordStrength(newPasswordField.getValue()));
		
		layout.addFormElement(passwordCheckComponent);
		layout.addFormElement(newPasswordField);
		layout.addFormElement(repeatPwTextField);
	}
	
	private HorizontalLayout createPasswordCheckComponent()
	{
		final HorizontalLayout passwordCheckComponent = new HorizontalLayout();
		passwordCheckComponent.setStyleName(CssStyle.PASSWORD_CHECK.getStyleName());
		
		final ProgressBar progressBar = new ProgressBar();
		progressBar.setStyleName(CssStyle.PASSWORD_CHECK_EMPTY.getStyleName());
		
		final Label label = new Label(I18N.CHANGEPASSWORD_PASSWORD_STRENGTH.msg());
		label.setStyleName(CssStyle.PASSWORD_CHECK_LABEL.getStyleName());
		
		passwordCheckComponent.addComponent(progressBar);
		passwordCheckComponent.addComponent(label);
		
		return passwordCheckComponent;
	}
	
	private PasswordField createPasswordField(String caption)
	{
		final PasswordField passwordField = ComponentFactory.getInstance().createField(PasswordField.class, caption);
		passwordField.setRequired(true);
		passwordField.setRequiredError(I18N.FIELD_REQUIRED.msg());
		
		passwordField.addValueChangeListener(event -> fireValueChange(true));
		
		return passwordField;
	}
	
	private void updatePasswordStrength(String password)
	{
		final ProgressBar progressBar = (ProgressBar) passwordCheckComponent.getComponent(0);
		final Label progressBarLabel = (Label) passwordCheckComponent.getComponent(1);
		
		final int strength = PasswordStrengthValidator.validatePasswordStrength(password);
		
		switch (strength)
		{
			case 0:
				progressBar.setValue(0f);
				progressBar.setStyleName(CssStyle.PASSWORD_CHECK_EMPTY.getStyleName());
				progressBarLabel.setValue(I18N.CHANGEPASSWORD_PASSWORD_STRENGTH.msg());
				break;
			case 1:
				progressBar.setValue(0.25f);
				progressBar.setStyleName(CssStyle.PASSWORD_CHECK_VERY_WEAK.getStyleName());
				progressBarLabel.setValue(I18N.CHANGEPASSWORD_PASSWORD_VERY_WEAK.msg());
				break;
			case 2:
				progressBar.setValue(0.5f);
				progressBar.setStyleName(CssStyle.PASSWORD_CHECK_WEAK.getStyleName());
				progressBarLabel.setValue(I18N.CHANGEPASSWORD_PASSWORD_WEAK.msg());
				break;
			case 3:
				progressBar.setValue(0.75f);
				progressBar.setStyleName(CssStyle.PASSWORD_CHECK_STRONG.getStyleName());
				progressBarLabel.setValue(I18N.CHANGEPASSWORD_PASSWORD_STRONG.msg());
				break;
			default:
				progressBar.setValue(1.0f);
				progressBar.setStyleName(CssStyle.PASSWORD_CHECK_VERY_STRONG.getStyleName());
				progressBarLabel.setValue(I18N.CHANGEPASSWORD_PASSWORD_VERY_STRONG.msg());
				break;
		}
	}
	
	public void reset()
	{
		super.setInternalValue(null);
		
		if (oldPasswordField != null) oldPasswordField.setValue(null);
		repeatPwTextField.setValue(null);
		newPasswordField.setValue(null);
	}
	
	public String getOldPassword()
	{
		return oldPasswordField == null ? null : oldPasswordField.getValue();
	}
	
	public String getNewPassword()
	{
		return newPasswordField.getValue();
	}
	
	@Override
	public void validate() throws InvalidValueException
	{
		if (oldPasswordField != null) oldPasswordField.validate();
		newPasswordField.validate();
		repeatPwTextField.validate();
	}
	
	@Override
	public void setComponentError(ErrorMessage componentError)
	{
		super.setComponentError(componentError);
	}
	
	@Override
	public void setRequired(boolean required)
	{
		if (oldPasswordField != null) oldPasswordField.setRequired(required);
		newPasswordField.setRequired(required);
		repeatPwTextField.setRequired(required);
	}
	
	@Override
	public void setRequiredError(String requiredMessage)
	{
		if (oldPasswordField != null) oldPasswordField.setRequiredError(requiredMessage);
		newPasswordField.setRequiredError(requiredMessage);
		repeatPwTextField.setRequiredError(requiredMessage);
	}
	
	@Override
	public void setValidationVisible(boolean validateAutomatically)
	{
		if (oldPasswordField != null) oldPasswordField.setValidationVisible(validateAutomatically);
		newPasswordField.setValidationVisible(validateAutomatically);
		repeatPwTextField.setValidationVisible(validateAutomatically);
	}
	
	@Override
	public void quickRegister(String password)
	{
		newPasswordField.setValue(password);
		repeatPwTextField.setValue(password);
	}
	
	@Override
	public String getCaption()
	{
		if (!Strings.isNullOrEmpty(super.getCaption()))
			return super.getCaption();
		if (!Strings.isNullOrEmpty(layout.getCaption()))
			return layout.getCaption();
		return I18N.CHANGEPASSWORD_CAPTION_DEFAULTFIELDNAME.msg();
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
	protected String getInternalValue()
	{
		return newPasswordField.getValue();
	}
	
	@Override
	protected void setInternalValue(String newValue)
	{
		if (newValue != null)
			throw new UnsupportedOperationException("programmatically setting password other than null is not supported");
		
		reset();
	}
}