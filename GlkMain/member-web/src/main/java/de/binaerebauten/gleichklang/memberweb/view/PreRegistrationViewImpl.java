package de.binaerebauten.gleichklang.memberweb.view;


import com.mysql.cj.util.StringUtils;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.questionnaire.Answer;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.service.LocatableHandler;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.component.question.QuestionComponent;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.initializer.MemberUI;
import de.binaerebauten.gleichklang.memberweb.view.PreregistrationView.PreregistrationViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.GenericViewHeader;

import java.util.EnumSet;

public class PreRegistrationViewImpl extends AbstractNavigateView<PreregistrationViewListener> implements PreregistrationView
{
	private final String landingRootUrl;

	private final ComponentGroup<User> userComponentGroup = new ComponentGroup<>(User.class);
	private final ComponentGroup<UserPaymentSettings> userPaymentSettingComponentGroup = new ComponentGroup<>(UserPaymentSettings.class);

	private final ChangeEmailField changeEmailField;
	private final TextField actionCodeField;
	private final ComponentReplacer<QuestionComponent> sexQuestionComponent = new ComponentReplacer<>();

	private final SaveHelper saveHelper;

	private boolean sendConfirmationMail = true;

	ReCaptcha captcha;

	public PreRegistrationViewImpl(String landingRoot, boolean languageSelectionActivated)
	{
		this.landingRootUrl = landingRoot;

		captcha = new ReCaptcha(
				"6Ld59dUUAAAAAGHsKplqoiUiHlzLn7XbklFh-B9D",
				new ReCaptchaOptions() {{//your options
					theme = "light";
					sitekey = "6Ld59dUUAAAAAOHqBuLuYWo1b3tbn_KI_H55oyzO";
				}}
		,UI.getCurrent().getLocale().getLanguage());

		saveHelper = createSaveHelper();

		final ComboBox languageComboBox = createLanguageComboBox();
		changeEmailField = createChangeEmailField();
		actionCodeField = createActionCodeTextField();

		final VerticalLayout layout = new VerticalLayout();
		layout.addStyleName(CssStyle.PREREGISTRATION_VIEW.getStyleName());
		layout.setSizeFull();

		// language selector
		if(languageSelectionActivated)
		{
			layout.addComponent(new Label());
			layout.addComponent(languageComboBox);
		}

		// logo
		final Component logo = ComponentFactory.getInstance().getLogo();
		layout.addComponent(logo);
		layout.setComponentAlignment(logo, Alignment.MIDDLE_CENTER);

		// content and advertisement
		final HorizontalLayout registrationViewWrapper = new HorizontalLayout();
		registrationViewWrapper.setWidth(100, Unit.PERCENTAGE);
		layout.addComponent(registrationViewWrapper);

		final VerticalLayout registrationContent = new VerticalLayout();
		registrationContent.addStyleName(CssStyle.PREREIGSTRATION_CONTENT.getStyleName());
		registrationViewWrapper.addComponent(registrationContent);

		final CustomLayout content = new CustomLayout("registration");
		content.setSizeFull();
		content.addStyleName(CssStyle.PREREGISTRATION_ADVERTISEMENT.getStyleName());
		registrationViewWrapper.addComponent(content);
		registrationViewWrapper.setComponentAlignment(content, Alignment.TOP_LEFT);

		registrationViewWrapper.setExpandRatio(registrationContent, 0.7f);
		registrationViewWrapper.setExpandRatio(content, 0.3f);

		final Component registrationView = createRegistrationView();
		registrationView.setSizeFull();
		registrationContent.addComponent(saveHelper.getValidationComponent());
		registrationContent.addComponent(registrationView);

		setStyleName(CssStyle.MEMBER_PREREG_VIEW.getStyleName());

		setCompositionRoot(layout);
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
	public void startRegistration(User user, UserPaymentSettings userPaymentSettings, Answer sexAnswer, LocatableHandler locatableHandler)
	{
		final QuestionComponent questionComponent = new QuestionComponent(sexAnswer, locatableHandler);

		userComponentGroup.setItemDataSource(user);
		userPaymentSettingComponentGroup.setItemDataSource(userPaymentSettings);
		sexQuestionComponent.setComponent(questionComponent);

		saveHelper.removeAllFields();
		saveHelper.addFields(userComponentGroup, userPaymentSettingComponentGroup);
		saveHelper.addFields(questionComponent.getFields());

	}

	private SaveHelper createSaveHelper()
	{
		final SaveHelper saveHelper = new SaveHelper(this::preregisterUser);
		saveHelper.getSaveButton().setCaption(I18N.REGISTRATIONVIEW_REGISTER_BUTTON_CAPTION.msg());
		saveHelper.getSaveButton().setStyleName(CssStyle.PREREGISTRATION_BUTTON.getStyleName());
		saveHelper.getSaveButton().addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
		saveHelper.getSaveButton().setIcon(new ThemeResource("img/double-chevron.svg"));

		return saveHelper;
	}

	private Component createRegistrationView()
	{
		final VerticalLayout registrationView = new VerticalLayout();
		registrationView.setSpacing(true);
		registrationView.setMargin(true);

		registrationView.setStyleName(CssStyle.PREREGISTRATION_PANEL.getStyleName());

		final ThemeResource headerIcon = new ThemeResource("img/registration-header-icon.svg");
		final GenericViewHeader header = new GenericViewHeader(I18N.REGISTRATIONVIEW_LABEL.msg(), I18N.REGISTRATIONVIEW_DESCRIPTION.msg(), headerIcon);
		registrationView.addComponent(header);

		registrationView.addComponent(createMembershipPanel());
		registrationView.addComponent(createPersonalPanel());
		registrationView.addComponent(createRecommendationCategoryPanel());
		registrationView.addComponent(createActionCodePanel());
		registrationView.addComponent(createAcceptRulesPanel());


		if (MemberUI.isEnableDebugFeatures())
		{
			registrationView.addComponent(createSendEmailField());
		}

		registrationView.addComponent(saveHelper.getSaveButton());
		registrationView.addComponent(new Label("<br/><br/><br/><br/>",ContentMode.HTML));
		return registrationView;
	}

	private Component createMembershipPanel()
	{
		final FormPanel membershipGroupView = new FormPanel(I18N.REGISTRATIONVIEW_MEMBERSHIP_GROUP_LABEL.msg());
		membershipGroupView.setStyleName(CssStyle.GK_PANEL.getStyleName());

		if (MemberUI.isEnableDebugFeatures())
		{
			final Button quickRegisterBtn = new Button(I18N.REGISTRATIONVIEW_QUICKREGISTER_BUTTON.msg());
			quickRegisterBtn.setEnabled(false);
			quickRegisterBtn.setDescription(I18N.REGISTRATIONVIEW_QUICKREGISTER_DESC.msg());
			quickRegisterBtn.addStyleName("danger");
			quickRegisterBtn.addClickListener(event -> quickRegister(changeEmailField.getValue()));
			membershipGroupView.addComponent(quickRegisterBtn, 0);
			changeEmailField.addValueChangeListener(event -> quickRegisterBtn.setEnabled(!StringUtils.isNullOrEmpty(changeEmailField.getValue())));
		}

		membershipGroupView.addComponent(changeEmailField);
		membershipGroupView.addFormElement(createAliasTextField());
		membershipGroupView.addComponent(createChangePasswordField());

		return membershipGroupView;
	}

	private FormPanel createPersonalPanel()
	{
		final FormPanel personalGroup = new FormPanel(I18N.REGISTRATIONVIEW_PERSON_GROUP_LABEL.msg());
		personalGroup.setStyleName(CssStyle.GK_PANEL.getStyleName());

		final BirthDateField birthDateField = new BirthDateField(true);
		userComponentGroup.bind(birthDateField, User_.birthDate);

		personalGroup.addFormElement(birthDateField);
		personalGroup.addFormElement(sexQuestionComponent);

		return personalGroup;
	}

	private Component createRecommendationCategoryPanel()
	{
		final FormPanel recommendationGroup = new FormPanel(I18N.REGISTRATIONVIEW_RECOMMENDATION_CATEGORY_LABEL.msg() + " *");
		recommendationGroup.setStyleName(CssStyle.GK_PANEL.getStyleName());
		recommendationGroup.addStyleName(CssStyle.PREREGISTRATION_RECOMMENDATION.getStyleName());

		final BeanItemContainer<RecommendationCategory> dataSource = new BeanItemContainer<>(RecommendationCategory.class, EnumSet.allOf(RecommendationCategory.class));

		final OptionGroup optionGroup = userComponentGroup.buildAndBind(true, OptionGroup.class, User_.categories);
		optionGroup.setCaption(I18N.REGISTRATIONVIEW_CAPTION_CATEGORIES.msg());
		optionGroup.setContainerDataSource(dataSource);
		optionGroup.setNullSelectionAllowed(true);

		// extra custom component to suppress caption, caption is necessary for validationComponent
		recommendationGroup.addComponent(new CustomComponent(optionGroup));

		return recommendationGroup;
	}

	private Component createActionCodePanel()
	{
		final FormPanel actionCodePanel = new FormPanel(I18N.REGISTRATIONVIEW_ACTIONCODE_LABEL.msg());
		actionCodePanel.setDescription(I18N.REGISTRATIONVIEW_ACTIONCODE_DESCRIPTION.msg());
		actionCodePanel.addStyleName(CssStyle.GK_PANEL.getStyleName());

		actionCodePanel.addFormElement(actionCodeField);

		return actionCodePanel;
	}

	private Component createAcceptRulesPanel()
	{
		final FormPanel acceptedRulesPanel = new FormPanel(I18N.REGISTRATIONVIEW_RULES_LABEL.msg());
		acceptedRulesPanel.setDescription(I18N.REGISTRATIONVIEW_RULES_DESCRIPTION.msg(landingRootUrl + "/agb/", landingRootUrl + "/datenschutz/", landingRootUrl + "/kennenlernen/miteinander/"), -1);
		acceptedRulesPanel.setStyleName(CssStyle.GK_PANEL.getStyleName());
		acceptedRulesPanel.addStyleName(CssStyle.PREREGISTRATION_CHECKBOX_LEFTALIGN.getStyleName());

		acceptedRulesPanel.addFormElement(userComponentGroup.buildAndBind(true, I18N.REGISTRATIONVIEW_RULES_PRIVACY.msg(), User_.userSettings, UserSettings_.privacyPolicyAccepted));
		acceptedRulesPanel.addFormElement(userComponentGroup.buildAndBind(true, I18N.REGISTRATIONVIEW_RULES_GENERALTERMS.msg(), User_.userSettings, UserSettings_.generalTermsAccepted));
		acceptedRulesPanel.addFormElement(userComponentGroup.buildAndBind(true, I18N.REGISTRATIONVIEW_RULES_CANCELATION.msg(), User_.userSettings, UserSettings_.cancellationPolicyAccepted));
		acceptedRulesPanel.addFormElement(userComponentGroup.buildAndBind(true, I18N.REGISTRATIONVIEW_RULES_COMMUNITYRULES.msg(), User_.userSettings, UserSettings_.communityRulesAccepted));
		acceptedRulesPanel.addFormElement(userComponentGroup.buildAndBind(I18N.PREREGISTRATIONVIEW_CAPTION_ENABLEMARKETINGNOTIFICATIONS.msg(), User_.userSettings, UserSettings_.enableMarketingNotifications));
        acceptedRulesPanel.addFormElement(captcha);

		return acceptedRulesPanel;
	}

	private Component createSendEmailField()
	{
		final FormPanel tempPanel = new FormPanel("Temp Panel");
		tempPanel.setStyleName(CssStyle.DANGER.getStyleName());

		final CheckBox sendEmailCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, "Send Confirmation Email? ");
		sendEmailCheckBox.setValue(sendConfirmationMail);
		sendEmailCheckBox.addValueChangeListener(event -> sendConfirmationMail = sendEmailCheckBox.getValue());

		tempPanel.addFormElement(sendEmailCheckBox);

		return tempPanel;
	}

	private ChangeEmailField createChangeEmailField()
	{
		final ChangeEmailField changeEmailField = new ChangeEmailField();
		userComponentGroup.bind(changeEmailField, User_.email);

		return changeEmailField;
	}

	private ChangePasswordField createChangePasswordField()
	{
		final ChangePasswordField changePasswordField = new ChangePasswordField();
		userComponentGroup.bind(changePasswordField, User_.password);

		return changePasswordField;
	}

	private TextField createAliasTextField()
	{
		final TextField aliasTextField = this.userComponentGroup.buildAndBind(true, I18N.USERDATAPANEL_CAPTION_ALIAS.msg(), TextField.class, User_.alias);
		aliasTextField.addValidator(value -> getListener().validateAlias(aliasTextField.getValue()));

		return aliasTextField;
	}

	private TextField createActionCodeTextField()
	{
		return userPaymentSettingComponentGroup.buildAndBind(false, I18N.REGISTRATIONVIEW_ACTIONCODE_LABEL.msg(), TextField.class, UserPaymentSettings_.actionCode);
	}

	private void quickRegister(String value)
	{
		for (Field<?> field : saveHelper.getValidationComponent().getFields())
		{
			if (!actionCodeField.equals(field))
			{
				if (field instanceof QuickRegistrable)
				{
					((QuickRegistrable) field).quickRegister(value);
				}
				else if (field instanceof AbstractTextField)
				{
					((AbstractTextField) field).setValue(value);
				}
				else if (field instanceof CheckBox)
				{
					((CheckBox) field).setValue(true);
				}
				else if (field instanceof OptionGroup)
				{
					final OptionGroup optionGroup = (OptionGroup) field;
					optionGroup.setValue(optionGroup.getContainerDataSource().getItemIds());
				}
				else if (field instanceof AbstractSelect)
				{
					final AbstractSelect abstractSelect = (AbstractSelect) field;
					abstractSelect.select(abstractSelect.getVisibleItemIds().iterator().next());
				}
			}
		}
	}

	private void preregisterUser() throws ValidationException
	{
		if (!captcha.validate()) {
			MessageBox.show(I18N.PRE_REGISTER_RECAPTCHA_ERROR_MESSAGE.msg());
			captcha.reload();
		} else {
			getListener().preregister
					(
							userComponentGroup.getItemDataSource().getBean(),
							sendConfirmationMail,
							userPaymentSettingComponentGroup.getItemDataSource().getBean(),
							sexQuestionComponent.getComponent().getAnswer()
					);
		}

	}
}
