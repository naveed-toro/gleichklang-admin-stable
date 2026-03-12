package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class CustomLoginForm extends LoginForm
{
	private TextField usernameField;
	private PasswordField passwordField;
	private Button loginButton;
	
	private String usernameString = null;
	private String passwordString = null;
	
	@Override
	protected TextField createUsernameField()
	{
		usernameField = super.createUsernameField();
		usernameField.setNullRepresentation("");
		usernameField.addStyleName(CssStyle.LOGIN_INPUT_FIELDS.getStyleName());
		if(usernameString != null) usernameField.setValue(usernameString);
		return usernameField;
	}
	
	@Override
	protected PasswordField createPasswordField()
	{
		passwordField = super.createPasswordField();
		passwordField.setNullRepresentation("");
		passwordField.addStyleName(CssStyle.LOGIN_INPUT_FIELDS.getStyleName());
		if(passwordString != null) passwordField.setValue(passwordString);
		return passwordField;
	}
	
	@Override
	protected Button createLoginButton()
	{
		loginButton = super.createLoginButton();
		loginButton.addStyleName(CssStyle.LOGIN_BUTTON.getStyleName());
		return loginButton;
	}
	
	public void setLogin(String login)
	{
		this.usernameString = login;
		if (usernameField != null)
			usernameField.setValue(login);
	}
	
	public void setPassword(String password)
	{
		this.passwordString = password;
		if (passwordField != null)
			passwordField.setValue(password);
	}
	
	@Override
	public void setLoginButtonCaption(String caption)
	{
		super.setLoginButtonCaption(caption);
		if(loginButton != null)
			loginButton.setCaption(caption);
	}
	
	@Override
	public void setUsernameCaption(String caption)
	{
		super.setUsernameCaption(caption);
		if(usernameField != null)
			usernameField.setCaption(caption);
	}
	
	@Override
	public void setPasswordCaption(String caption)
	{
		super.setPasswordCaption(caption);
		if(passwordField != null)
			passwordField.setCaption(caption);
	}
}
