package de.binaerebauten.gleichklang.core.view.popup;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ChangePasswordField;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

public class ChangePasswordPopup extends GenericPopup
{
	public interface ChangePasswordListener
	{
		void changePassword(String newPassword) throws ValidationException;
	}
	
	private final ChangePasswordField changePasswordField;
	private final ChangePasswordListener changePasswordListener;

	public ChangePasswordPopup(ChangePasswordListener changePasswordListener)
	{
		this.changePasswordListener = changePasswordListener;
		
		final String caption = I18N.CHANGEPASSWORDPOPUP_CAPTION_PASSWORD.msg();
		final String description = I18N.CHANGEPASSWORDPOPUP_CAPTION_PASSWORDDESCRIPTION.msg(SignableUser.MIN_PASSWORD_LENGTH);
		
		this.setCaption(caption);
		this.setIcon(new ThemeResource("img/icon_password.svg"));
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		
		changePasswordField = new ChangePasswordField(false, caption, description);
		
		final SaveHelper saveHelper = new SaveHelper(this::changePassword);
		saveHelper.setSuccessMessage(I18N.CHANGEPASSWORDPOPUP_VALIDATION_SUCCESSFUL.msg());
		saveHelper.setFailMessage(I18N.CHANGEPASSWORDPOPUP_VALIDATION_FAILED.msg());
		saveHelper.addFields(changePasswordField);
		
		
		layout.addComponent(saveHelper.getValidationComponent());
		layout.addComponent(changePasswordField);
		layout.addComponent(saveHelper.getSaveButton());
		
        addStyleName(CssStyle.POPUP_TYPE_GREEN.getStyleName());
		setPopupContent(layout);
	}
	
	private void changePassword() throws ValidationException
	{
		changePasswordListener.changePassword(changePasswordField.getNewPassword());
		close();
	}
}
