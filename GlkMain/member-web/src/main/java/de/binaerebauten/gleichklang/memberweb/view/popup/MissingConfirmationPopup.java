package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ChangeEmailField;
import de.binaerebauten.gleichklang.core.view.component.FormPanel;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem;
import de.binaerebauten.gleichklang.memberweb.view.UserDataView.UserDataTab;
import de.binaerebauten.gleichklang.memberweb.view.component.GenericViewHeader;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class MissingConfirmationPopup extends GenericPopup
{
	public interface MissingConfirmationListener
	{
		void sendChangeMail(User user, String newEmail) throws ValidationException;
	}

	private final ChangeEmailField changeEmailField;
	private final SaveHelper emailSaveHelper;
	private final MissingConfirmationListener listener;
	private final User user;
	private final UserRepository userRepository;
	private final DefaultNavigator navigator;

	public MissingConfirmationPopup(ApplicationContext ctx, MissingConfirmationListener listener, User user , DefaultNavigator navigator)
	{
		this.listener = Objects.requireNonNull(listener);
		this.user = Objects.requireNonNull(user);
		this.navigator = navigator;

		changeEmailField = new ChangeEmailField();
		changeEmailField.setValue(user.getEmail());

		emailSaveHelper = new SaveHelper(this::saveEmail);
		emailSaveHelper.getSaveButton().setCaption(I18N.MISSINGCONFIRMATIONPOPUP_ACTION_SAVE.msg());

		setPopupContent(createLayout());
//		setFooter(emailSaveHelper.getSaveButton());
		this.userRepository = ctx.getBean(UserRepository.class);
	}

	private Component createLayout()
	{
		emailSaveHelper.setSuccessMessage(I18N.MISSINGCONFIRMATIONPOPUP_VALIDATION_SAVED.msg());

		final HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(emailTab());
		layout.addComponent(new Label("\t\t\t\t\t\t\t\t\t\t\t"));
		layout.addComponent(emailSaveHelper.getSaveButton());

		final Label label = new Label();
		label.setCaption(I18N.MISSINGCONFIRMATIONPOPUP_CAPTION_DESCRIPTIONS.msg(user.getEmail()) + "\n\n" + I18N.MISSINGCONFIRMATIONPOPUP_CAPTION_DESCRIPT.msg());
		label.addStyleName(CssStyle.TEXT_COLOR.getStyleName());
		final FormPanel formPanel = new FormPanel(I18N.MISSINGCONFIRMATIONPOPUP_CAPTION_TITLE.msg());
		formPanel.setDescription(I18N.MISSINGCONFIRMATIONPOPUP_CAPTION_DESCRIPTION.msg());
		formPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
		formPanel.addComponent(label);
		formPanel.addComponent(layout);

		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.addComponent(formPanel);
		return mainLayout;
	}

	private void saveEmail() throws ValidationException
	{
		listener.sendChangeMail(user, user.getEmail());
		close();
	}

	public Button emailTab() {
		final Button button = new Button(I18N.MISSINGCONFIRMATIONPOPUP_ACTION_TAB.msg() , FontAwesome.EDIT);
		button.setHeight("55px");
		button.addClickListener(event -> {
			this.navigator.navigateTo(getViewKey(MemberMenuItem.USER_DATA, UserDataTab.USERDATA));
			this.navigator.navigateTo(getViewKey(MemberMenuItem.USER_DATA, UserDataTab.EMAIL));
			this.close();
		});

		return button;
	}

	public String getViewKey(NavigationEnum... navigationEnums)
	{
		return Arrays.stream(navigationEnums)
				.map(NavigationEnum::getPath)
				.collect(Collectors.joining("/"));
	}

}
