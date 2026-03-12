package de.binaerebauten.gleichklang.adminweb.view.popup.systemconfig;


import com.google.common.collect.ImmutableMap;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.I18NEntity;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailTemplateMapping;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.repository.systemconfig.EmailTemplateRepository;
import de.binaerebauten.gleichklang.core.service.DynamicContentTemplateService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.MailTemplateInstance;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.CustomNotification;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.component.validator.SaveHelper;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import de.binaerebauten.gleichklang.adminweb.view.popup.EmailTemplateConstants;
import de.binaerebauten.gleichklang.adminweb.view.popup.I18N;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EditMailTemplatePopup extends Popup
{

	private static final Map<String,UserMailTemplate> map;


	static {
		map = new HashMap<>();
		for (UserMailTemplate v : UserMailTemplate.values()) {

			map.put(v.getTemplateName(), v);
		}
	}

	public static UserMailTemplate findByKey(String name) {
		return map.get(name);
	}


	protected final EmailTemplateMapping mapping;
	protected final  EmailTemplateRepository repository;
	final String popupHeight="500px",popupWidth="900px";
	final VerticalLayout parentLayout;
	final VerticalLayout customErrorSpace = new VerticalLayout();
	final HorizontalLayout buttonsLayout = new HorizontalLayout();
	protected final SaveHelper saveHelper;
	protected final TextArea valueTextField = new TextArea();
	protected final TextField emailTextField = new TextField();
	protected TextArea descriptionTextField;
	protected final ComponentGroup componentGroup;
	protected final Button refreshTextButton, sendTestMailButton;
	final ComboBox footerDropdown = new ComboBox("Include Footer");
	private final DynamicContentTemplateService service;
	private final MailSendService mailSendService;
	private final Admin currentAdmin;

	public interface SaveCallback
	{
		void save(EmailTemplateMapping mapping) throws ValidationException;
	}

	public EditMailTemplatePopup(EmailTemplateMapping mapping, EmailTemplateRepository repository, DynamicContentTemplateService service, MailSendService mailSendService, Admin currentAdmin)
	{
		super(EmailTemplateConstants.edit + mapping.getTemplateName());
		this.mapping = mapping;
		this.repository = repository;
		this.service = service;
		this.mailSendService = mailSendService;
		this.currentAdmin = currentAdmin;
		parentLayout = new VerticalLayout();

		this.refreshTextButton = new Button(EmailTemplateConstants.reload);
		this.sendTestMailButton = new Button((I18N.SENT_TEXT_MAIL_BUTTON.msg()));
		refreshTextButton.addClickListener((clickEvent)->{refreshText();});
		sendTestMailButton.addClickListener((clickEvent)->{sendTestMail();});
		this.saveHelper = new SaveHelper(this::save);
		this.saveHelper.setShowUnsavedNotification(false);
		componentGroup = new ComponentGroup(EmailTemplateMapping.class);

		this.setWidth(popupWidth);
		this.setHeight(popupHeight);
		parentLayout.setMargin(true);
		parentLayout.setSpacing(true);
		parentLayout.addComponent(customErrorSpace);
		parentLayout.addComponent(createMappingValuesSection());
		parentLayout.addComponent(createControlButtons());
		setContent(parentLayout);
	}

	private Component createMappingValuesSection()
	{

		valueTextField.setWidth(EmailTemplateConstants.eightHundredPX);
		valueTextField.setHeight(EmailTemplateConstants.fourHundredPX);
		valueTextField.setValue(mapping.getTemplateText());
		if(mapping.getTemplateDescription().equals("header") || mapping.getTemplateDescription().equals("footer")){
			descriptionTextField= new TextArea(I18N.HEADER_FOOTER_NOT_EDITED.msg());
			descriptionTextField.setWidth(EmailTemplateConstants.fourHundredPXx);
			descriptionTextField.setHeight(EmailTemplateConstants.seventyPXx);
			descriptionTextField.setValue(mapping.getTemplateDescription());
			descriptionTextField.setReadOnly(true);
		}
		else {
			descriptionTextField= new TextArea("Description");
			descriptionTextField.setWidth(EmailTemplateConstants.fourHundredPXx);
			descriptionTextField.setHeight(EmailTemplateConstants.seventyPXx);
			descriptionTextField.setValue(mapping.getTemplateDescription());
		}
		parentLayout.addComponent(descriptionTextField);
		emailTextField.setValue(currentAdmin.getEmail());
		//parentLayout.addComponent( new Label(mapping.getTemplateDescription()));
		parentLayout.addComponent(valueTextField);

		//Label emptyLabel3 = new Label("");
		//emptyLabel3.setHeight("1em");
		//parentLayout.addComponent(emptyLabel3);

		parentLayout.addComponent(createFooterDropdown());


		//Label emptyLabel4 = new Label("");
		//emptyLabel4.setHeight("1em");
		//parentLayout.addComponent(emptyLabel4);
		return valueTextField;
	}

	private Component createControlButtons()
	{
		Label emptyLabel3 = new Label("");
		emptyLabel3.setHeight(EmailTemplateConstants.lem);

		parentLayout.addComponent(emptyLabel3);
		parentLayout.setMargin(true);
		buttonsLayout.addComponent(refreshTextButton);
		buttonsLayout.addComponent(sendTestMailButton);
		buttonsLayout.addComponent(saveHelper.getSaveButton());
		buttonsLayout.addComponent(new Label(I18N.TEMPLATE_TEST_MAIL_LAYOUT_NAME.msg()));
		buttonsLayout.addComponent(emailTextField);
		return buttonsLayout;
	}

	protected void save() throws ValidationException
	{
		customErrorSpace.setVisible(false);

		if(templateDataValid()) {

			//formEnabled(false); // disable form elements
			// TODO : Add exception handling for Object to Long conversion of footer value
			repository.updateEmailTemplate(valueTextField.getValue(),descriptionTextField.getValue(), mapping.getId());
		}else
		{throw  new ValidationException(I18N.EMAIL_TEMPLATE_MAPPING_NOT_SAVED_MESSAGE.msg()); }
	}

	private HashMap dataForTemplateTest()
	{
		HashMap hm = new HashMap();
		Map<String, Object> variables = ImmutableMap.of(EmailTemplateConstants.user, currentAdmin);
		hm.putAll(variables);
		hm.put(EmailTemplateConstants.templateText,EmailTemplateConstants.bTrue);
		hm.put(EmailTemplateConstants.templateTestText,valueTextField.getValue());
		hm.put(EmailTemplateConstants.templateTestFooter,footerDropdown.getValue());
		return hm;
	}

	private boolean templateDataValid()
	{
		int openBraceCount =0, closeBraceCount=0;
		String templateText = valueTextField.getValue();
		openBraceCount = StringUtils.countMatches(templateText,"{");
		closeBraceCount = StringUtils.countMatches(templateText,"}");
		if(templateText == null || templateText.isEmpty())
		{

			CustomNotification.show(I18N.GENERIC_ERROR_MESSAGE.msg(), Notification.Type.TRAY_NOTIFICATION);
			showCustomError( customErrorSpace, I18N.CUSTOME_ERROR_MESSAGE.msg()); return false;}
		if(openBraceCount != closeBraceCount)
		{
			CustomNotification.show(I18N.GENERIC_ERROR_MESSAGE.msg(), Notification.Type.TRAY_NOTIFICATION);
			showCustomError( customErrorSpace, I18N.MISMATCHED_ERROR_MESSAGE.msg()); return false; }
		return true;
	}


	void showCustomError(Layout layout , String message)
	{

		layout.removeAllComponents();
		Label emptyLabel3 = new Label("");
		emptyLabel3.setHeight(EmailTemplateConstants.lem);
		layout.addComponent(emptyLabel3);
		layout.addComponent(new Label(message));
		layout.setStyleName(EmailTemplateConstants.failure);
		layout.addStyleName(CssStyle.FORM_PART_EMPTY.getStyleName());
		layout.setVisible(true);

	}


	void refreshText()
	{

		valueTextField.setValue(mapping.getTemplateText());

	}

	private boolean sendTestMail() {

		if(templateDataValid()) {
			// TODO add remove hard coding for Language and messages
			System.out.print(EmailTemplateConstants.template + mapping.getTemplateName());
			UserMailTemplate template = map.get(mapping.getTemplateName());
			if (template != null) {
				MailTemplateInstance instance = MailTemplateInstance.createTemplateForTest(service, dataForTemplateTest(), template, getLanguageInstance(mapping.getTemplateLanguage()));
				if(isValidEmailAddress(emailTextField.getValue())) {
					currentAdmin.setEmail(emailTextField.getValue()); // TODO remove this email id post testing
					mailSendService.sendEmail(currentAdmin, instance);
					final String msg = String.format(I18N.EMAIL_SENT.msg() + currentAdmin.getEmail());
					CustomNotification.show(msg, Notification.Type.TRAY_NOTIFICATION);
				}
				else{
					CustomNotification.show(I18N.INVALID_EMAIL_ERROR_MESSAGE.msg(), Notification.Type.TRAY_NOTIFICATION);
				}
			} else {
				CustomNotification.show(I18N.TEMPLATE_NOT_CONFIGURED_ERROR_MESSAGE.msg() + mapping.getTemplateName(), Notification.Type.TRAY_NOTIFICATION);
			}
			return true;
		}else {
			return false;
		}
	}

	private HorizontalLayout createFooterDropdown()
	{

		final HorizontalLayout footerDropdownLayout = new HorizontalLayout();
		if(!mapping.getTemplateDescription().contains(EmailTemplateConstants.sFooter) && !mapping.getTemplateDescription().contains(EmailTemplateConstants.sHeader))
		{

			List<EmailTemplateMapping> footerLst = repository.findByTemplateDescriptionAndTemplateLanguage(EmailTemplateConstants.footer,mapping.getTemplateLanguage());

			footerDropdownLayout.setSizeFull();
			footerDropdownLayout.setStyleName(CssStyle.TABSHEET_DROPDOWN_LIGHTGREEN.getStyleName());
			//TODO: Make footer list dynamic
			if(footerLst != null && !footerLst.isEmpty()) {


				footerDropdown.setItemStyleGenerator((ComboBox.ItemStyleGenerator) (source, itemId) ->
						CssStyle.TABSHEET_DROPDOWN_POPUP_ITEMS.name());
				for (EmailTemplateMapping template : footerLst) {
					footerDropdown.addItem(template.getId());
					footerDropdown.setItemCaption(template.getId(), template.getTemplateName());
					footerDropdown.setDescription(template.getTemplateDescription());

				}
				footerDropdown.select(mapping.getTemplateFooter());
				footerDropdown.setValue(mapping.getTemplateFooter());
				//footerDropdown.setImmediate(true);
				footerDropdown.setTextInputAllowed(false);
				footerDropdown.setNullSelectionAllowed(false);
				footerDropdownLayout.addComponent(footerDropdown);
				parentLayout.addComponent(footerDropdown);
			}

		}
		else {
			footerDropdownLayout.addComponent(new Label(EmailTemplateConstants.headerFooter));
		}



		return footerDropdownLayout;



	}

	public boolean isValidEmailAddress(String email) {
		String ePattern = EmailTemplateConstants.regex;
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}

	private I18NEntity.Language getLanguageInstance(String lang){

		if(lang != null)
			switch (lang.toUpperCase())
			{
				case EmailTemplateConstants.en:
					return I18NEntity.Language.EN;
				case EmailTemplateConstants.de:
					return I18NEntity.Language.DE;
				default:
					return  I18NEntity.Language.DE;
			}

		return  I18NEntity.Language.DE;
	}

}