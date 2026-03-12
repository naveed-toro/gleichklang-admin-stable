package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.JsIncludePostfix;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailDomainMapping;
import de.binaerebauten.gleichklang.core.model.systemconfig.EmailTemplateMapping;
import de.binaerebauten.gleichklang.core.repository.JsIncludePostfixRepository;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.TableControl;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import javax.mail.URLName;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SystemConfigurationViewImpl extends AbstractNavigateView<SystemConfigurationView.SystemConfigurationViewListener> implements SystemConfigurationView
{

	private final TableControl<EmailDomainMapping> emailDomainMappings;

	private final TableControl<EmailTemplateMapping> emailTemplateMapping;

	private final HorizontalLayout horizontalLayout;

	private JsIncludePostfixRepository jsIncludePostfixRepository;
	private final Environment environment;

	private  final TextField textField;
	private final Button button;
	Label label1;
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Value("${custom_js_file}")
	private String jsFilePath;

	public SystemConfigurationViewImpl()
	{
		jsIncludePostfixRepository = AppUI.getApplicationContext().getBean(JsIncludePostfixRepository.class);
		environment = AppUI.getApplicationContext().getBean(Environment.class);

		emailDomainMappings = createEmailDomainMappingsAdministration();

		emailTemplateMapping = createEmailTemplateConfigurationAdministration();

		horizontalLayout = new HorizontalLayout();

		textField = new TextField("JS-Postfix");
		if(jsIncludePostfixRepository.findLastRecord()!=null && jsIncludePostfixRepository.findLastRecord().getKeyName()!=null) {
			textField.setValue(jsIncludePostfixRepository.findLastRecord().getKeyName());
		}
		Label label  = new Label("                  ");
		button  = new Button("Save");
		button.setStyleName(CssStyle.BUTTON_POSTFIX.getStyleName());

		final TabSheet tabSheet = createTabSheet();
		final VerticalLayout layout = new VerticalLayout();
		layout.addComponent(tabSheet);

		layout.setSizeFull();
		layout.setMargin(true);
		layout.setSpacing(true);
		setCompositionRoot(layout);

		label1 = new Label();
		if(jsIncludePostfixRepository.findLastRecord()!=null && jsIncludePostfixRepository.findLastRecord().getKeyName()!=null) {
			String[] jsPath = environment.getProperty("custom_js_file").split("\\.js");
			String js = jsPath[0] + jsIncludePostfixRepository.findLastRecord().getKeyName() + ".js" + jsPath[1];
			label1.setValue(js);
		}
		label1.setStyleName(CssStyle.LABEL_STYLE.getStyleName());
		horizontalLayout.addComponent(textField);
		horizontalLayout.addComponent(label);
		horizontalLayout.addComponent(button);
		horizontalLayout.addComponent(label1);
		horizontalLayout.setMargin(true);
		horizontalLayout.setHeight("600px");
		saveJsString(textField,button,label1);
	}

	private void saveJsString(TextField textField,Button button,Label textArea){
		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				JsIncludePostfix jsIncludePostfix = new JsIncludePostfix();
				if(textField.getValue().trim().length()>12){
					Notification.show("String too long");
					return;
				}
				jsIncludePostfix.setKeyName(textField.getValue().trim());
				jsIncludePostfixRepository.updatePostfix(textField.getValue().trim());
				String[] jsPath = environment.getProperty("custom_js_file").split("\\.js");
				String js=null;
				if(jsIncludePostfixRepository.findLastRecord()!=null && jsIncludePostfixRepository.findLastRecord().getKeyName()!=null){
					js = jsPath[0] + jsIncludePostfixRepository.findLastRecord().getKeyName() + ".js" + jsPath[1];
				}
				else {
					js=jsPath[0]+textField.getValue()+".js"+jsPath[1];
				}
				textArea.setValue(js);
				Notification.show("Successfully saved");
			}
		});
	}

	private TabSheet createTabSheet()
	{
		final TabSheet tabSheet = new TabSheet();

		tabSheet.addTab(emailDomainMappings, I18N.SYSTEMCONFIG_TAB_EMAIL_DOMAIN_MAPPINGS.msg());
		tabSheet.addTab(emailTemplateMapping, I18N.EMAIL_TEMPLATE_CONFIGURATION.msg());
		tabSheet.addTab(horizontalLayout, "JS INCLUDE POSTFIX");
		tabSheet.setWidth(97, Unit.PERCENTAGE);
		return tabSheet;
	}

	private void activateTab(TabSheet tabSheet, TabSheet.Tab tab)
	{
		tab.setVisible(true);
		tabSheet.setSelectedTab(tab);
	}

	private TableControl<EmailDomainMapping> createEmailDomainMappingsAdministration()
	{
		final TableControl<EmailDomainMapping> tableControl = new TableControl<>(createEmailDomainsMappingTable());
		tableControl.setMargin(true);

		tableControl.setNewCallback(() -> fireEvent(SystemConfigurationView.SystemConfigurationViewListener::addNewEmailDomainMapping));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editEmailDomainMapping(item)));
		tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteEmailDomainMapping(item)));
		//use undelete event call back to set active / inactive status
		tableControl.addButton(I18N.EMAIL_MAPPING_ACTIVATE_DEACTIVATE_BUTTON.msg(), item -> fireEvent(eventAction -> eventAction.activeInactiveEmailDomainMapping(item)));
		return tableControl;
	}

	private TableControl<EmailTemplateMapping> createEmailTemplateConfigurationAdministration()
	{
		final TableControl<EmailTemplateMapping> tableControl = new TableControl<>(createEmailTemplateConfigurationTable());
		tableControl.setMargin(true);

		//tableControl.setNewCallback(() -> fireEvent(SystemConfigurationView.SystemConfigurationViewListener::addNewEmailDomainMapping));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editEmailTemplate(item)));
		//tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteEmailTemplate(item)));
		//use undelete event call back to set active / inactive status
		tableControl.addButton(I18N.EMAIL_MAPPING_ACTIVATE_DEACTIVATE_BUTTON.msg(), item -> fireEvent(eventAction -> eventAction.activeInactiveEmailTemplateMapping(item)));
		return tableControl;
	}

	private LazyBeanTable<EmailDomainMapping> createEmailDomainsMappingTable()
	{
		final LazyBeanTable<EmailDomainMapping> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setSizeFull();
		table.addGeneratedColumn(I18N.SYSTEMCONFIG_TAB_EMAIL_DOMAIN_MAPPINGS_NAME.msg(), (source, itemId, columnId) -> itemId.getMappingName() );
		table.addGeneratedColumn(I18N.SYSTEMCONFIG_TAB_EMAIL_DOMAIN_MAPPINGS_VALUE.msg(), (source, itemId, columnId) -> itemId.getMappingValue());
		table.addGeneratedColumn(I18N.SYSTEMCONFIG_TAB_EMAIL_DOMAIN_MAPPINGS_ACTIVE.msg(), (source, itemId, columnId) -> formatActive(itemId.isActive()));
		//table.addGeneratedColumn(I18N.SYSTEMCONFIG_TAB_EMAIL_DOMAIN_MAPPINGS_DELETED.msg(), (source, itemId, columnId) -> itemId.isDeleted());
		table.addGeneratedColumn(I18N.SYSTEMCONFIG_TAB_EMAIL_DOMAIN_MAPPINGS_CREATE_DATE.msg(), (source, itemId, columnId) -> formatDate(itemId.getCreateDate()));
		table.addGeneratedColumn(I18N.SYSTEMCONFIG_TAB_EMAIL_DOMAIN_MAPPINGS_UPDATE_DATE.msg(), (source, itemId, columnId) -> formatDate(itemId.getChangeDate()));
		return table;
	}

	private LazyBeanTable<EmailTemplateMapping> createEmailTemplateConfigurationTable()
	{
		final LazyBeanTable<EmailTemplateMapping> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setSizeFull();
		table.addGeneratedColumn(I18N.TEMPLATE_NAME.msg(), (source, itemId, columnId) -> itemId.getTemplateName() );
		table.addGeneratedColumn(I18N.TEMPLATE_DESCRIPTION.msg(), (source, itemId, columnId) -> itemId.getTemplateDescription());
		table.addGeneratedColumn(I18N.LANGUAGE.msg(), (source, itemId, columnId) -> itemId.getTemplateLanguage());
		table.addGeneratedColumn(I18N.SYSTEMCONFIG_TAB_EMAIL_DOMAIN_MAPPINGS_ACTIVE.msg(), (source, itemId, columnId) -> formatActive(itemId.isActive()));
		return table;
	}

	String formatActive(boolean active)
	{
		if(active)return "Active";
		else
			return "Inactive";
	}

	String formatDate(LocalDateTime dateI)
	{
		try
		{
			return dateI.format(formatter);
		}
		catch (Exception e)
		{
			// formatting exception return as is
		}
		return dateI.toString();
	}

	@Override
	public void setEmailDomainMappingsHandler(LazyBeanFilteredItemsHandler<EmailDomainMapping> handler) {
		emailDomainMappings.getTable().setHandler(handler);
	}

	@Override
	public void setEmailTemplateMappingsHandler(LazyBeanFilteredItemsHandler<EmailTemplateMapping> handler) {
		emailTemplateMapping.getTable().setHandler(handler);
	}

}
