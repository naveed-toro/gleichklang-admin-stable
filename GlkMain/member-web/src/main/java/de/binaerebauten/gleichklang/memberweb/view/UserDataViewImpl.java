package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.commit_strategy.DefaultValidationStrategy;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem;
import de.binaerebauten.gleichklang.memberweb.view.UserDataView.UserDataViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.GenericViewHeader;
import de.binaerebauten.gleichklang.memberweb.view.component.PersonalDataComponent;
import de.binaerebauten.gleichklang.memberweb.view.component.PersonalDataComponent.PersonalDataHandler;
import com.vaadin.annotations.JavaScript;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("serial")
public class UserDataViewImpl extends AbstractNavigateView<UserDataViewListener> implements UserDataView
{
	private final ComponentGroup<UserSettings> notificationsFieldGroup;

	SubscriptionService subscriptionService;

	UserService userService;
	
	private final OptionGroup deleteUserOptionGroup;
	private final OptionGroup mailsAfterCancelOptionGroup;
	private final Map<CancelReason, CheckBox> cancelReasonCheckBoxes;
	private final ComponentReplacer<PersonalDataComponent> personalDataComponent;
	private final NavigationComponent<UserDataTab> navigationComponent;
	
	private final ChangeEmailField changeEmailField;
	private final ChangePasswordField changePasswordField;

	private final SaveHelper notificationSaveHelper;
	private final SaveHelper passwordSaveHelper;
	private final SaveHelper emailSaveHelper;
	
	private User user;
	
	private boolean deactivatedChangeListener = false;
	
	public UserDataViewImpl()
	{
		notificationSaveHelper = new SaveHelper(this::saveNotifications);
		passwordSaveHelper = new SaveHelper(this::savePassword);
		emailSaveHelper = new SaveHelper(this::saveEmail);
		
		personalDataComponent = new ComponentReplacer<>();
		notificationsFieldGroup = new ComponentGroup<>(UserSettings.class);
		
		changeEmailField = new ChangeEmailField();
		changePasswordField = new ChangePasswordField(true, I18N.PASSWORDPANEL_CAPTION_CHANGEPASSWORD.msg(), I18N.PASSWORDPANEL_DESCRIPTION_CHANGEPASSWORD.msg(SignableUser.MIN_PASSWORD_LENGTH));
		
		cancelReasonCheckBoxes = createCancelReasonCheckBoxes();
		deleteUserOptionGroup = createDeleteUserOptionGroup();
		mailsAfterCancelOptionGroup = createMailsAfterCancelOptionGroup();
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setStyleName(CssStyle.USER_DATA_VIEW_WRAPPER.getStyleName());
		
		navigationComponent = createNavigationComponent();

		layout.addComponents(createHeader(), navigationComponent);

		setCompositionRoot(layout);
	}

	private Component createHeader()
	{
		final GenericViewHeader header = new GenericViewHeader();
		header.addStyleName(CssStyle.GENERIC_HEADER_BLUE.getStyleName());
		header.setCaption(MemberMenuItem.USER_DATA.toString());
		header.setDescription(I18N.USERDATA_PERSONALINFO_DESCRIPTION.msg());
		header.setIcon(new ThemeResource("img/icon_questionnaire_aboutme-outline.svg"));
		
		return header;
	}

	private Map<CancelReason, CheckBox> createCancelReasonCheckBoxes()
	{
		final Map<CancelReason, CheckBox> cancelReasonCheckBoxes = new EnumMap<>(CancelReason.class);
		
		for (final CancelReason cancelReason : CancelReason.values())
		{
			cancelReasonCheckBoxes.put(cancelReason, ComponentFactory.getInstance().createField(CheckBox.class, cancelReason.toString()));
		}
		
		return cancelReasonCheckBoxes;
	}
	
	private NavigationComponent<UserDataTab> createNavigationComponent()
	{
		final NavigationComponent<UserDataTab> navigationComponent = new NavigationComponent<>(UserDataTab.class);
		
		navigationComponent.addNavigation(UserDataTab.USERDATA, personalDataComponent);
		navigationComponent.addNavigation(UserDataTab.PASSWORD, createPasswordTab());
		navigationComponent.addNavigation(UserDataTab.EMAIL, createChangeEmailTab());
		navigationComponent.addNavigation(UserDataTab.MESSAGES, createNotificationTab());
		navigationComponent.addNavigation(UserDataTab.CANCELLATION, createSubscriptionCancelTab());
		
		return navigationComponent;
	}

	@Override
	public void setListener(UserDataViewListener listener)
	{
		super.setListener(listener);
		navigationComponent.setSubNavigationListener(listener);
	}
	
	@Override
	public void selectSubNavigation(UserDataTab navigationEnum)
	{
		navigationComponent.setSelectedNavigation(navigationEnum);
	}
	
	private Component createPasswordTab()
	{
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.addComponent(passwordSaveHelper.getValidationComponent());
		mainLayout.addComponent(changePasswordField);
		
		final FooterCommandBar commandBar = new FooterCommandBar(passwordSaveHelper.getSaveButton());
		mainLayout.addComponent(commandBar);
		
		passwordSaveHelper.addFields(changePasswordField);
		
		return mainLayout;
	}
	
	private Component createNotificationTab()
	{
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setStyleName(CssStyle.MULTI_COMPONENT_PANEL.getStyleName());
		mainLayout.setSpacing(true);
		
		final Label captionLabel = new Label(I18N.NOTIFICATIONSPANEL_CAPTION_SURPRESSNOTIFICATIONS.msg());
		captionLabel.setStyleName(CssStyle.MULTI_COMPONENT_CAPTION.getStyleName());
		
		final Label descriptionLabel = new Label(I18N.NOTIFICATIONSPANEL_DESCRIPTION_SURPRESSNOTIFICATIONS.msg());
		descriptionLabel.setStyleName(CssStyle.MULTI_COMPONENT_DESCRIPTION.getStyleName());
		descriptionLabel.setContentMode(ContentMode.HTML);
		
		mainLayout.addComponents(notificationSaveHelper.getValidationComponent(), captionLabel, descriptionLabel);
		
		final FormPanel notificationPanel = new FormPanel(I18N.NOTIFICATIONSPANEL_CAPTION_SURPRESSNOTIFICATIONS.msg());
		
		final List<CheckBox> disableNotificationsCheckBoxes = new ArrayList<>();
		final CheckBox blockAllCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.USER_CAPTION_MAILBLOCKED.msg());
		
		disableNotificationsCheckBoxes.add(notificationsFieldGroup.buildAndBind(I18N.USER_CAPTION_DISABLERECOMMENDATIONNOTIFICATIONS.msg(), CheckBox.class, UserSettings_.disableRecommendationNotifications));
		disableNotificationsCheckBoxes.add(notificationsFieldGroup.buildAndBind(I18N.USER_CAPTION_DISABLECIPERNOTIFICATIONS.msg(), CheckBox.class, UserSettings_.disableCipherMessageNotifications));
		disableNotificationsCheckBoxes.add(notificationsFieldGroup.buildAndBind(I18N.USER_CAPTION_DISABLEPOSITIVERANKINGNOTIFICATIONS.msg(), CheckBox.class, UserSettings_.disablePositiveRankingNotifications));
		disableNotificationsCheckBoxes.add(notificationsFieldGroup.buildAndBind(I18N.USER_CAPTION_DISABLEFOOTPRINTNOTIFICATIONS.msg(), CheckBox.class, UserSettings_.disableFootprintNotifications));
		disableNotificationsCheckBoxes.add(notificationsFieldGroup.buildAndBind(I18N.USER_CAPTION_DISABLENEWSNOTIFICATIONS.msg(), CheckBox.class, UserSettings_.disableNewsNotifications));
		
		final ValueChangeListener blockAllChangeListener = createBlockAllChangeListener(blockAllCheckBox, disableNotificationsCheckBoxes);
		final ValueChangeListener disableNotificationChangeListener = createDisableNotificationChangeListener(blockAllCheckBox, disableNotificationsCheckBoxes);
		
		blockAllCheckBox.addValueChangeListener(blockAllChangeListener);
		disableNotificationsCheckBoxes.forEach(c -> c.addValueChangeListener(disableNotificationChangeListener));
		
		disableNotificationsCheckBoxes.forEach(notificationPanel::addFormElement);
		notificationPanel.addFormElement(blockAllCheckBox);
		notificationPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
		mainLayout.addComponent(notificationPanel);
		
		final FooterCommandBar commandRow = new FooterCommandBar(notificationSaveHelper.getSaveButton());
		mainLayout.addComponent(commandRow);
		
		notificationSaveHelper.addFields(notificationsFieldGroup);
		
		return mainLayout;
	}

	private ValueChangeListener createBlockAllChangeListener(CheckBox blockAllCheckBox, List<CheckBox> disableNotificationsCheckBoxes)
	{
		return e ->
		{
			if (deactivatedChangeListener) return;
			deactivatedChangeListener = true;
			disableNotificationsCheckBoxes.forEach(c -> c.setValue(blockAllCheckBox.getValue()));
			deactivatedChangeListener = false;
		};
	}
	
	private ValueChangeListener createDisableNotificationChangeListener(CheckBox blockAllCheckBox, List<CheckBox> disableNotificationsCheckBoxes)
	{
		return e ->
		{
			if (deactivatedChangeListener) return;
			deactivatedChangeListener = true;
			blockAllCheckBox.setValue(disableNotificationsCheckBoxes.stream().allMatch(AbstractField::getValue));
			deactivatedChangeListener = false;
		};
	}
	
	private Component createSubscriptionCancelTab()
	{
		final SaveHelper saveHelper = new SaveHelper(this::cancelUser);
		saveHelper.getSaveButton().setCaption(I18N.CANCELATIONPANEL_ACTION_CANCELSUBSCRIPTION.msg());
		saveHelper.getSaveButton().setIcon(FontAwesome.TRASH_O);
		saveHelper.setShowValidationNotification(false);

		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setStyleName(CssStyle.MULTI_COMPONENT_PANEL.getStyleName());
		
		final Label captionLabel = new Label(I18N.CANCELATIONPANEL_CAPTION_CANCELATION.msg());
		captionLabel.setStyleName(CssStyle.MULTI_COMPONENT_CAPTION.getStyleName());

		final Label descriptionLabel;
		subscriptionService = AppUI.getApplicationContext().getBean(SubscriptionService.class);
		userService = AppUI.getApplicationContext().getBean(UserService.class);

		if(subscriptionService.findCurrentSubscription(userService.getCurrentUser()).isPresent() &&
				subscriptionService.findCurrentSubscription(userService.getCurrentUser()).get().isAutomaticRenewal()) {
			descriptionLabel= new Label(I18N.CANCELATIONPANEL_DESCRIPTION_CANCELATION.msg()+
					I18N.CANCELATIONPANEL_DESCRIPTION_CANCELATION1.msg()+
					I18N.CANCELATIONPANEL_DESCRIPTION_CANCELATION2.msg());
		}
		else{
			descriptionLabel= new Label(I18N.CANCELATIONPANEL_DESCRIPTION_CANCELATION.msg()+
					I18N.CANCELATIONPANEL_DESCRIPTION_CANCELATION3.msg()+
					I18N.CANCELATIONPANEL_DESCRIPTION_CANCELATION2.msg());
		}
		
		descriptionLabel.setStyleName(CssStyle.MULTI_COMPONENT_DESCRIPTION.getStyleName());
		descriptionLabel.setContentMode(ContentMode.HTML);
		
		mainLayout.addComponents(captionLabel, descriptionLabel);
		
		final FormPanel mainPanel = new FormPanel();
		
		final FormPanel deletePanel = new FormPanel(I18N.CANCELATIONPANEL_CAPTION_DELETETITLE.msg());
		deletePanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
		deletePanel.addComponent(deleteUserOptionGroup);
		
		final FormPanel saveMailAddressPanel = new FormPanel(I18N.CANCELATIONPANEL_CAPTION_SAVEMAILTITLE.msg());
		saveMailAddressPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
		saveMailAddressPanel.setVisible(false);
		saveMailAddressPanel.addComponent(mailsAfterCancelOptionGroup);
		
		final FormPanel cancelReasonPanel = new FormPanel(I18N.USER_CAPTION_CANCELREASON.msg());
		cancelReasonPanel.setVisible(false);
		cancelReasonCheckBoxes.values().forEach(cancelReasonPanel::addFormElement);
		cancelReasonPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
		
		deleteUserOptionGroup.addValueChangeListener(listener ->
		{
			final boolean deleteAccount = (boolean) deleteUserOptionGroup.getConvertedValue();
			
			saveMailAddressPanel.setVisible(true);
			cancelReasonPanel.setVisible(true);
			mailsAfterCancelOptionGroup.setValue(null);
			
			final String trueCaption;
			final String falseCaption;
			
			if (deleteAccount)
			{
				trueCaption = I18N.CANCELATIONPANEL_CAPTION_SAVEMAILYESWITHDELETED.msg();
				falseCaption = I18N.CANCELATIONPANEL_CAPTION_SAVEMAILNOWITHDELETED.msg();
			}
			else
			{
				trueCaption = I18N.CANCELATIONPANEL_CAPTION_SAVEMAILYES.msg();
				falseCaption = I18N.CANCELATIONPANEL_CAPTION_SAVEMAILNO.msg();
			}
			
			mailsAfterCancelOptionGroup.setItemCaption(true, trueCaption);
			mailsAfterCancelOptionGroup.setItemCaption(false, falseCaption);
		});
		
		final FooterCommandBar commandRow = new FooterCommandBar(saveHelper.getSaveButton());
		
		mainPanel.addComponent(deletePanel);
		mainPanel.addComponent(saveMailAddressPanel);
		mainPanel.addComponent(cancelReasonPanel);
		mainPanel.addComponent(commandRow);
		
		mainLayout.addComponent(mainPanel);
		
		saveHelper.addFields(deleteUserOptionGroup, mailsAfterCancelOptionGroup);
		cancelReasonCheckBoxes.values().forEach(saveHelper::addFields);
		
		return mainLayout;
	}
	
	private OptionGroup createMailsAfterCancelOptionGroup()
	{
		final OptionGroup optionGroup = ComponentFactory.getInstance().createField(Boolean.class, OptionGroup.class);
		optionGroup.setRequired(true);
		return optionGroup;
	}
	
	private OptionGroup createDeleteUserOptionGroup()
	{
		final OptionGroup optionGroup = ComponentFactory.getInstance().createField(OptionGroup.class);
		final String trueCaption = I18N.CANCELATIONPANEL_CAPTION_DELETEYES.msg();
		final String falseCaption = I18N.CANCELATIONPANEL_CAPTION_DELETENO.msg();
		
		optionGroup.addItems(Boolean.FALSE, Boolean.TRUE);
		optionGroup.setItemCaption(true, trueCaption);
		optionGroup.setItemCaption(false, falseCaption);
		optionGroup.setRequired(true);
		return optionGroup;
	}
	
	private Component createChangeEmailTab()
	{
		emailSaveHelper.setSuccessMessage(I18N.USERDATAVIEW_VALIDATION_NEWEMAILSAVED.msg());
		
		final FormPanel formPanel = new FormPanel(I18N.MAILPANEL_CAPTION_CHANGEEMAIL.msg());
		formPanel.setDescription(de.binaerebauten.gleichklang.core.view.component.I18N.USERDATAVIEW_DESCRIPTION_EMAIL.msg());
		formPanel.addComponent(changeEmailField);
		formPanel.addStyleName(CssStyle.GK_PANEL.getStyleName());
		
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		final FooterCommandBar commandBar = new FooterCommandBar(emailSaveHelper.getSaveButton());
		
		mainLayout.addComponent(emailSaveHelper.getValidationComponent());
		mainLayout.addComponent(formPanel);
		mainLayout.addComponent(commandBar);
		
		emailSaveHelper.addFields(changeEmailField);
		
		return mainLayout;
	}
	
	private void saveNotifications()
	{
		getListener().saveUser(user);
	}
	
	private void cancelUser()
	{
		user.getCancelReasons().clear();
		
		cancelReasonCheckBoxes.forEach((cancelReason, checkBox) ->
		{
			if (checkBox.getValue())
			{
				user.getCancelReasons().add(cancelReason);
			}
		});
		
		final boolean deleteUser = (boolean) deleteUserOptionGroup.getConvertedValue();
		final boolean mailsAfterCancel = (boolean) mailsAfterCancelOptionGroup.getConvertedValue();
		
		getListener().cancelSubscription(user, deleteUser, mailsAfterCancel);
	}
	
	@Override
	public void setUser(User user)
	{
		this.user = user;
		
		cancelReasonCheckBoxes.values().forEach(checkBox -> checkBox.setValue(false));
		changeEmailField.reset();
		
		UserSettings userSettings = null;
		
		if (user != null)
		{
			userSettings = user.getUserSettings();
			
			user.getCancelReasons().forEach(cancelReason -> cancelReasonCheckBoxes.get(cancelReason).setValue(true));
			changeEmailField.setValue(user.getEmail());
		}
		
		notificationsFieldGroup.setItemDataSource(userSettings);
		notificationSaveHelper.resetValidationResult();
		emailSaveHelper.resetValidationResult();
	}
	
	@Override
	public void initPersonalDataTab(PersonalDataHandler personalDataHandler)
	{
		final PersonalDataComponent personalDataComponent = new PersonalDataComponent(new DefaultValidationStrategy(), personalDataHandler);
		personalDataComponent.setVisibleSaveButton(true);
		this.personalDataComponent.setComponent(personalDataComponent);
	}
	
	private void savePassword() throws ValidationException
	{
		getListener().savePassword(changePasswordField.getOldPassword(), changePasswordField.getNewPassword());
	}
	
	private void saveEmail() throws ValidationException
	{
		getListener().sendChangeMail(user, changeEmailField.getValue());
	}
	
	@Override
	public void setCancelSubscriptionVisible(boolean visible)
	{
		navigationComponent.setNavigationVisible(UserDataTab.CANCELLATION, visible);
	}
	
	@Override
	public UserDataTab getSelectedUserDataTab()
	{
		return navigationComponent.getSelectedNavigation();
	}
	
	@Override
	public void initPasswordTab()
	{
		changePasswordField.reset();
		passwordSaveHelper.resetValidationResult();
	}
}
