package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminRole;
import de.binaerebauten.gleichklang.core.model.user.Admin_;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * This popup allows an admin to create a new admin and to edit an existing admin.
 */
public class AdminPopup extends Popup
{
	/**
	 * Called when an admin presses the save button.
	 */
	public interface SaveCallback
	{
		/**
		 * Saves the given admin.
		 *
		 * @param admin the admin
		 * @throws ValidationException
		 */
		void save(Admin admin) throws ValidationException;
	}

	/**
	 * Called when an admin presses the reset and send new password button.
	 */
	public interface ResetPasswordCallback
	{
		/**
		 * Resets the password of the given admin and saves it.
		 *
		 * @param admin the admin
		 * @throws ValidationException
		 */
		void resetPasswordAndSave(Admin admin) throws ValidationException, MailException;
	}

	private static final Logger LOG = LoggerFactory.getLogger(AdminPopup.class);

	private final ComponentGroup<Admin> adminComponentGroup;
	private final Admin admin;

	private final AdminPopup.SaveCallback saveCallback;
	private final AdminPopup.ResetPasswordCallback resetPasswordCallback;
	private Button resetPasswordButton;
	private TwinColSelect adminRoleSelection;

	public AdminPopup(Admin admin, AdminPopup.SaveCallback saveCallback, AdminPopup.ResetPasswordCallback resetPasswordCallback)
	{
		this.admin = admin;
		this.saveCallback = saveCallback;
		this.resetPasswordCallback = resetPasswordCallback;

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		this.adminComponentGroup = new ComponentGroup<>(Admin.class);
		this.adminComponentGroup.setItemDataSource(this.admin);

		layout.addComponent(this.adminComponentGroup.buildAndBind(true, I18N.ADMINPOPUP_COMPONENT_ALIAS.msg(), Admin_.alias));
		layout.addComponent(this.adminComponentGroup.buildAndBind(true, I18N.ADMINPOPUP_COMPONENT_EMAIL.msg(), Admin_.email));
		layout.addComponent(this.adminComponentGroup.buildAndBind(I18N.ADMINPOPUP_COMPONENT_FIRSTNAME.msg(), Admin_.firstName));
		layout.addComponent(this.adminComponentGroup.buildAndBind(I18N.ADMINPOPUP_COMPONENT_LASTNAME.msg(), Admin_.lastName));
		this.admin.setChangeDate(LocalDateTime.now());
		layout.addComponent(createAdminRoleSelectionComponent(this.admin));

		final HorizontalLayout controlButtons = createControlButtons();
		layout.addComponent(controlButtons);
		layout.setComponentAlignment(controlButtons, Alignment.BOTTOM_RIGHT);
		setContent(layout);
	}

	private Component createAdminRoleSelectionComponent(Admin admin)
	{
		adminRoleSelection = adminComponentGroup.buildAndBind(I18N.ADMINPOPUP_ADMINROLES.msg(), TwinColSelect.class,
				Admin_.roles);
		adminRoleSelection.setContainerDataSource(
				new BeanItemContainer<>(AdminRole.class, Arrays.asList(AdminRole.values())));
		adminRoleSelection.setLeftColumnCaption(I18N.ADMINPOPUP_EXISTING_ADMINROLES.msg());
		adminRoleSelection.setRightColumnCaption(I18N.ADMINPOPUP_ASSIGNED_ADMINROLES.msg());
		adminRoleSelection.setValue(admin.getRoles());

		return adminRoleSelection;
	}

	private HorizontalLayout createControlButtons()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		final Button saveButton = new Button(I18N.ADMINPOPUP_ACTION_SAVE.msg());
		saveButton.addClickListener(event -> {
			onSave();
		});

		resetPasswordButton = new Button(I18N.ADMINPOPUP_ACTION_RESET_PASSWORD.msg());
		resetPasswordButton.setStyleName(CssStyle.DANGER.getStyleName());
		resetPasswordButton.addClickListener(event -> onPasswordReset());
		layout.addComponents(resetPasswordButton, saveButton);

		return layout;
	}

	private void onSave()
	{
		try
		{
			this.adminComponentGroup.commit();

			saveCallback.save(this.admin);

			close();
		}
		catch (ValidationException e)
		{
			LOG.error(e.getMessage());
			Notification.show(I18N.ADMINPOPUP_CONSTRAINT_ERROR.msg(), e.getMessage(),
					Notification.Type.ERROR_MESSAGE);
		}
		catch (MailException e)
		{
			LOG.error(e.getMessage());
			Notification.show(I18N.ADMINPOPUP_MAIL_XCPTN.msg(), e.getMessage(),
					Notification.Type.WARNING_MESSAGE);
		}
		catch (FieldGroup.CommitException | Validator.InvalidValueException e)
		{
			LOG.error(e.getMessage());
			Notification.show(I18N.ADMINPOPUP_VALUE_XCPTN.msg(), e.getMessage(),
					Notification.Type.WARNING_MESSAGE);
		}
	}

	private void onPasswordReset()
	{
		try
		{
			this.adminComponentGroup.commit();

			resetPasswordCallback.resetPasswordAndSave(this.admin);

			close();
		}
		catch (ValidationException e)
		{
			LOG.error(e.getMessage());
			Notification.show(I18N.ADMINPOPUP_CONSTRAINT_ERROR.msg(), e.getMessage(),
					Notification.Type.ERROR_MESSAGE);
		}
		catch (MailException e)
		{
			LOG.error(e.getMessage());
			Notification.show(I18N.ADMINPOPUP_MAIL_XCPTN.msg(), e.getMessage(),
					Notification.Type.WARNING_MESSAGE);
		}
		catch (FieldGroup.CommitException | Validator.InvalidValueException e)
		{
			LOG.error(e.getMessage());
			Notification.show(I18N.ADMINPOPUP_VALUE_XCPTN.msg(), e.getMessage(),
					Notification.Type.WARNING_MESSAGE);
		}
	}
	
}
