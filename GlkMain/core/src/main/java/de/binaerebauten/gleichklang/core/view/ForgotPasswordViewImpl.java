package de.binaerebauten.gleichklang.core.view;

import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.springframework.beans.factory.annotation.Autowired;

public class ForgotPasswordViewImpl extends AbstractNavigateView<ForgotPasswordView.ForgotPasswordViewListener> implements ForgotPasswordView
{

	public ForgotPasswordViewImpl()
	{

		final VerticalLayout rootLayout = new VerticalLayout();

		final Component logo = ComponentFactory.getInstance().getLogo();
		rootLayout.setSpacing(true);

		final Component control = createControl();

		rootLayout.addComponent(logo);
		rootLayout.addComponent(control);

		rootLayout.setComponentAlignment(logo, Alignment.MIDDLE_CENTER);

		control.setWidth(100, Unit.PERCENTAGE);
		rootLayout.setComponentAlignment(control, Alignment.MIDDLE_CENTER);

		setCompositionRoot(rootLayout);
	}

	private Component createControl()
	{
		final Panel panel = new Panel();
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		
		final TextField loginTextField = ComponentFactory.getInstance().createField(TextField.class, I18N.LOGINVIEW_CAPTION_EMAIL.msg());
		loginTextField.addValueChangeListener(e -> trimEmailRepeated(loginTextField));

		loginTextField.addValidator(new EmailValidator(I18N.LOGINVIEW_VALIDATION_INVALIDEMAIL.msg()));
		
		layout.addComponent(loginTextField);

		final CssLayout buttonPanel = new CssLayout();
		buttonPanel.addStyleName(CssStyle.LOGIN_FORGOT_BUTTON_PANEL.getStyleName());

		final Button forgotPasswordBtn = new Button(I18N.LOGINVIEW_BUTTON_RESET_PASSWORD.msg());
		forgotPasswordBtn.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		forgotPasswordBtn.addClickListener(event -> {
			fireEvent(eventAction -> {
				eventAction.forgotPassword(loginTextField.getValue().trim());
			});
		});
		
		final Button backToLoginBtn = new Button(I18N.LOGINVIEW_BUTTON_BACK_TO_LOGIN.msg());
		backToLoginBtn.addClickListener(e -> fireEvent(action -> action.backToLogin(null)));
		buttonPanel.addComponent(backToLoginBtn);
		buttonPanel.addComponent(forgotPasswordBtn);

		layout.addComponent(buttonPanel);
		layout.setComponentAlignment(buttonPanel, Alignment.MIDDLE_CENTER);

		panel.setContent(layout);
		panel.addStyleName(CssStyle.LOGIN_FORGOT_WRAPPER.getStyleName());

		return panel;
	}

	private void trimEmailRepeated(TextField textField)
	{


		if(textField.getValue() !=null)
			textField.setValue(textField.getValue().trim());

	}
}
