package de.binaerebauten.gleichklang.core.view;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.themes.Reindeer;
import de.binaerebauten.gleichklang.core.model.user.SignableUser.UserType;
import de.binaerebauten.gleichklang.core.view.LoginView.LoginViewListener;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.ComponentReplacer;
import de.binaerebauten.gleichklang.core.view.component.CustomLoginForm;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

@SuppressWarnings("serial")
public abstract class LoginViewImpl<T extends LoginViewListener> extends AbstractNavigateView<T> implements LoginView<T>
{
	private final CustomLoginForm loginForm;
	private final HorizontalLayout linkLayout;
	private final ComponentReplacer<Label> messageLabel;
	private final VerticalLayout rootLayout;
	
	public LoginViewImpl(String userType)
	{
		rootLayout = new VerticalLayout();
		rootLayout.setSizeFull();
		
		loginForm = createLoginForm(userType);
		linkLayout = createLinkLayout();
		messageLabel = createMessageLabelReplacer();
		
		final Component header = ComponentFactory.getInstance().getLogo();
		final Component additionalText = createAdditionalText();
		
		rootLayout.addComponent(header);
		rootLayout.addComponent(messageLabel);
		rootLayout.addComponent(loginForm);
		rootLayout.addComponent(linkLayout);
		if (getType() == UserType.MEMBER)
			rootLayout.addComponent(additionalText);
		
		rootLayout.setComponentAlignment(header, Alignment.MIDDLE_CENTER);
		rootLayout.setComponentAlignment(messageLabel, Alignment.MIDDLE_CENTER);
		rootLayout.setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);
		rootLayout.setComponentAlignment(linkLayout, Alignment.MIDDLE_CENTER);
		if (getType() == UserType.MEMBER)
			rootLayout.setComponentAlignment(additionalText, Alignment.MIDDLE_CENTER);
		
		setCompositionRoot(rootLayout);
	}
	
	private HorizontalLayout createLinkLayout()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		layout.addStyleName(CssStyle.LOGIN_LINK_LAYOUT.getStyleName());
		
		layout.addComponent(createForgotPasswordButton());
		
		return layout;
	}
	
	private ComponentReplacer<Label> createMessageLabelReplacer()
	{
		final ComponentReplacer<Label> componentReplacer = new ComponentReplacer<>();
		componentReplacer.addStyleName(CssStyle.MEMBER_LOGIN_VIEW_MESSAGE.getStyleName());
		
		return componentReplacer;
	}
	
	@Override
	public void setLoginCaption(String caption)
	{
		loginForm.setLoginButtonCaption(caption);
	}
	
	protected void addButton(Button button)
	{
		button.setStyleName(Reindeer.BUTTON_LINK);
		linkLayout.addComponent(button);
	}
	
	protected void addComponent(Component component, boolean asFirst)
	{
		if (component == null) return;
		
		if (asFirst)
		{
			rootLayout.addComponentAsFirst(component);
		}
		else
		{
			rootLayout.addComponent(component);
		}
	}
	
	private Label createMessageLabel(String value)
	{
		final Label label = new Label(value, ContentMode.HTML);
		label.setStyleName(CssStyle.SUCCESS.getStyleName());
		label.setWidth(50, Unit.PERCENTAGE);
		
		return label;
	}
	
	private void handleLogin(LoginEvent event)
	{
		fireEvent(action -> action.login(event.getLoginParameter("username"), event.getLoginParameter("password"), getType()));
	}
	
	private Component createAdditionalText()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth(50, Unit.PERCENTAGE);
		
		final Label additionalText = new Label(I18N.LOGINVIEW_CAPTION_ADDITIONALTEXT.msg());
		additionalText.setContentMode(ContentMode.HTML);
		layout.addComponent(additionalText);
		layout.addComponent(new Label());
		layout.setComponentAlignment(additionalText, Alignment.MIDDLE_CENTER);
		
		return layout;
	}
	
	private CustomLoginForm createLoginForm(String userType)
	{
		final CustomLoginForm loginForm = new CustomLoginForm();
		
		loginForm.addStyleName(CssStyle.LOGIN_WRAPPER.getStyleName());
		loginForm.setLoginButtonCaption(I18N.LOGINVIEW_ACTION_LOGIN.msg());
		loginForm.addLoginListener(this::handleLogin);
		if(userType.equalsIgnoreCase("Admin")){
			loginForm.setUsernameCaption(I18N.LOGINVIEW_CAPTION_EMAIL.msg());
		}
		else {
			loginForm.setUsernameCaption(I18N.LOGINVIEW_CAPTION_EMAIL_ALIAS.msg());
		}
		loginForm.setPasswordCaption(I18N.LOGINVIEW_CAPTION_PASSWORD.msg());
		
		return loginForm;
	}
	
	@Override
	public void setPassword(String password)
	{
		loginForm.setPassword(password);
	}
	
	@Override
	public void setLogin(String login)
	{
		loginForm.setLogin(login);
	}
	
	private Button createForgotPasswordButton()
	{
		final Button button = new Button(I18N.LOGINVIEW_LINK_FORGOT_PASSWORD.msg());
		button.setStyleName(Reindeer.BUTTON_LINK);
		button.addClickListener(event -> fireEvent(LoginViewListener::forgotPassword));
		return button;
	}
	
	@Override
	public void setMessage(String message)
	{
		messageLabel.setComponent(createMessageLabel(message));
	}
}


