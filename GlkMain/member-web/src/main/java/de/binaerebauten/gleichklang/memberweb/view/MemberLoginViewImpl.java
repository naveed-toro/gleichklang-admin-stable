package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.view.I18N;
import de.binaerebauten.gleichklang.core.view.LoginViewImpl;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.MemberLoginView.MemberLoginViewListener;

public class MemberLoginViewImpl extends LoginViewImpl<MemberLoginViewListener> implements MemberLoginView
{
	private final Button registerButton;
	private static final String AUTHENTICATED_USER_TYPE_ATTRIBUTE = "AUTHENTICATED_USER_TYPE";
	
	public MemberLoginViewImpl(boolean languageSelectionActivated)
	{
		super("User");
		
		final ComboBox languageComboBox = createLanguageComboBox();
		registerButton = createRegisterButton();
		
		if(languageSelectionActivated)
		{
			addComponent(languageComboBox, true);
			addComponent(new Label(), true);
		}
		addButton(registerButton);
		
		setStyleName(CssStyle.MEMBER_LOGIN_VIEW.getStyleName());
	}
	
	private ComboBox createLanguageComboBox()
	{
		final ComboBox comboBox = ComponentFactory.getInstance().createField(Language.class, ComboBox.class);
		comboBox.setValue(Language.valueOf(UI.getCurrent().getLocale()));
		comboBox.addValueChangeListener(event -> getListener().changeLanguage((Language) comboBox.getValue()));
		comboBox.setSizeUndefined();
		comboBox.addStyleName(CssStyle.COMBOBOX_LANGUAGE_SELECTOR.getStyleName());
		return comboBox;
	}
	
	@Override
	public void setRegistrationVisible(boolean visible)
	{
		registerButton.setVisible(visible);
	}
	
	private Button createRegisterButton()
	{
		final Button button = new Button(I18N.LOGINVIEW_ACTION_PREREGISTER.msg());
		button.addClickListener(event -> fireEvent(MemberLoginViewListener::startRegistration));
		
		return button;
	}
	
	@Override
	public SignableUser.UserType getType()
	{
		// adding the user type in session.
		VaadinSession vaadinSession = VaadinSession.getCurrent();
		vaadinSession.setAttribute(AUTHENTICATED_USER_TYPE_ATTRIBUTE, SignableUser.UserType.MEMBER);
		return SignableUser.UserType.MEMBER;
	}
	
}
