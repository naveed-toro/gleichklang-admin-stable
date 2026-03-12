package de.binaerebauten.gleichklang.adminweb.view.component;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.message.*;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.user.Admin_;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.utils.LocaleAware;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.converter.LocalDateConverter;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.filter.AbstractFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleAttributeFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleAttributeFilter.NestedAttributeFilter1;
import de.binaerebauten.gleichklang.core.view.filter.SimpleAttributeFilter.NestedAttributeFilter2;
import de.binaerebauten.gleichklang.core.view.filter.SimpleDateFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleDateFilter.NestedDateFilter1;
import de.binaerebauten.gleichklang.core.view.filter.SimpleStringFilter;
import de.binaerebauten.gleichklang.core.view.filter.SimpleStringFilter.NestedStringFilter1;
import de.binaerebauten.gleichklang.core.view.filter.SimpleStringFilter.NestedStringFilter3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

@SuppressWarnings("serial")
public class AdminWorkItemTable extends CustomComponent implements LocaleAware
{
	public interface ItemClickListener extends LazyBeanItemComponent.ItemClickListener<AdminWorkItem>
	{
	}
	
	public enum Directory
	{
		INCOMING,
		OUTGOING,
		Reminder, DRAFT
	}
	
	private final DateTimeFormatter dateTimeFormatter;
	private final LazyBeanPagingComponent<AdminWorkItem> adminWorkItemTable;
	private final Directory directory;
	private final SimpleAttributeFilter<AdminWorkItem, MessageType> messageTypeFilter;
	@Autowired
	@Lazy
	private UserService userService;
	
	public AdminWorkItemTable(Directory directory)
	{
		this.directory = directory;
		
		dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(LocaleAware.super.getLocale());
		
		adminWorkItemTable = createTable();
		initTable(directory);

		userService = AppUI.getApplicationContext().getBean(UserService.class);
		
		messageTypeFilter = createMessageTypeFilter();
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setSizeFull();
		layout.setMargin(true);
		
		layout.addComponents(createFilterComponent(), adminWorkItemTable, createTableFooter());
		setCompositionRoot(layout);
	}
	
	private SimpleAttributeFilter<AdminWorkItem, MessageType> createMessageTypeFilter()
	{
		final SimpleAttributeFilter<AdminWorkItem, MessageType> messageTypeFilter;
		messageTypeFilter = new NestedAttributeFilter1<>(AdminWorkItem_.message, Message_.messageType);
		messageTypeFilter.setItemComponent(adminWorkItemTable);
		
		return messageTypeFilter;
	}
	
	public void setMessageTypeFilter(MessageType messageType)
	{
		messageTypeFilter.setValue(messageType);
	}
	
	private Component createFilterComponent()
	{
		final SimpleStringFilter<AdminWorkItem> emailFilter;
		final SimpleStringFilter<AdminWorkItem> aliasFilter;
		final SimpleStringFilter<AdminWorkItem> lastNameFilter;
		final SimpleStringFilter<AdminWorkItem> firstNameFilter;
		final SimpleStringFilter<AdminWorkItem> subjectFilter;
		final SimpleStringFilter<AdminWorkItem> bodyFilter;
		final SimpleStringFilter<AdminWorkItem> adminFilter;
		final SimpleDateFilter<AdminWorkItem> dateFilter;
		final AbstractFilter<AdminWorkItem, AdminWorkItem.AdminWorkItemStatus, AdminWorkItem.AdminWorkItemStatus> adminWorkItemStatusFilter;
		final AbstractFilter<AdminWorkItem, Boolean, Boolean> unreadFilter;
		
		subjectFilter = new NestedStringFilter1<>(AdminWorkItem_.message, Message_.subject, true);
		bodyFilter = new NestedStringFilter1<>(AdminWorkItem_.message, Message_.body, true);
		adminFilter = new NestedStringFilter1<>(AdminWorkItem_.admin, Admin_.alias, true);
		
		switch (directory)
		{
			case INCOMING:
				emailFilter = new NestedStringFilter3<>(AdminWorkItem_.message, Message_.senderEnvelope, Envelope_.user, User_.email);
				aliasFilter = new NestedStringFilter3<>(AdminWorkItem_.message, Message_.senderEnvelope, Envelope_.user, User_.alias);
				firstNameFilter = new NestedStringFilter3<>(AdminWorkItem_.message, Message_.senderEnvelope, Envelope_.user, User_.firstName);
				lastNameFilter = new NestedStringFilter3<>(AdminWorkItem_.message, Message_.senderEnvelope, Envelope_.user, User_.lastName);
				dateFilter = new NestedDateFilter1<>(AdminWorkItem_.message, Message_.sendDate);
				adminWorkItemStatusFilter = new SimpleAttributeFilter<>(AdminWorkItem_.workItemStatus);
				unreadFilter = new NestedAttributeFilter2<>(AdminWorkItem_.message, Message_.receiverEnvelope, ReceiverEnvelope_.read);
				break;
			default:
				emailFilter = new NestedStringFilter3<>(AdminWorkItem_.message, Message_.receiverEnvelope, Envelope_.user, User_.email);
				aliasFilter = new NestedStringFilter3<>(AdminWorkItem_.message, Message_.receiverEnvelope, Envelope_.user, User_.alias);
				firstNameFilter = new NestedStringFilter3<>(AdminWorkItem_.message, Message_.senderEnvelope, Envelope_.user, User_.firstName);
				lastNameFilter = new NestedStringFilter3<>(AdminWorkItem_.message, Message_.receiverEnvelope, Envelope_.user, User_.lastName);
				dateFilter = null;
				adminWorkItemStatusFilter = null;
				unreadFilter = null;
		}
		
		emailFilter.setItemComponent(adminWorkItemTable);
		aliasFilter.setItemComponent(adminWorkItemTable);
		firstNameFilter.setItemComponent(adminWorkItemTable);
		lastNameFilter.setItemComponent(adminWorkItemTable);
		subjectFilter.setItemComponent(adminWorkItemTable);
		adminFilter.setItemComponent(adminWorkItemTable);
		bodyFilter.setItemComponent(adminWorkItemTable);
		if (dateFilter != null)
		{
			dateFilter.setItemComponent(adminWorkItemTable);
		}
		if (adminWorkItemStatusFilter != null)
		{
			adminWorkItemStatusFilter.setItemComponent(adminWorkItemTable);
		}
		if (unreadFilter != null)
		{
			unreadFilter.setItemComponent(adminWorkItemTable);
		}
		
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setSizeFull();



		// Changes for GR3-56, allow sibling emails to be searched when filtering
		final TextField emailTextField = ComponentFactory.getInstance().createField(TextField.class, I18N.ADMINWORKITEMTABLE_FILTER_EMAIL.msg());
		emailTextField.addTextChangeListener(event ->
		{

			String eventText= event.getText();

			try {
				if (eventText != null && eventText.indexOf("@") != -1 && eventText.lastIndexOf(".") > eventText.indexOf("@")) {
					List<String> lst = userService.findSiblingIds(emailTextField.getValue());
					String vals = "";
					if (lst != null && lst.size() != 0) {
						vals = String.join(",", lst);
					} else {
						vals = emailTextField.getValue();
					}

					emailFilter.setValue(vals);
				} else {
					emailFilter.setValue(event.getText());
				}
			}catch (Exception e)
			{
				emailFilter.setValue(event.getText());
			}
		});


		
		final TextField aliasTextField = ComponentFactory.getInstance().createField(TextField.class, I18N.ADMINWORKITEMTABLE_FILTER_ALIAS.msg());
		aliasTextField.addTextChangeListener(event ->
		{
			aliasFilter.setValue(event.getText());
		});

		final TextField firstNameTextField = ComponentFactory.getInstance().createField(TextField.class, I18N.ADMINWORKITEMTABLE_FILTER_FIRSTNAME.msg());
		firstNameTextField.addTextChangeListener(event ->
		{
			firstNameFilter.setValue(event.getText());
		});

		final TextField lastNameTextField = ComponentFactory.getInstance().createField(TextField.class, I18N.ADMINWORKITEMTABLE_FILTER_LASTNAME.msg());
		lastNameTextField.addTextChangeListener(event ->
		{
			lastNameFilter.setValue(event.getText());
		});
		
		final TextField subjectTextField = ComponentFactory.getInstance().createField(TextField.class, I18N.ADMINWORKITEMTABLE_FILTER_SUBJECT.msg());
		subjectTextField.addTextChangeListener(event ->
		{
			subjectFilter.setValue(StringUtils.deSanitizeSpecialCharacters(event.getText()));
		});

		final TextField adminTextField = ComponentFactory.getInstance().createField(TextField.class, I18N.ADMINWORKITEMTABLE_FILTER_ADMIN.msg());
		adminTextField.addTextChangeListener(event ->
		{
			adminFilter.setValue(StringUtils.deSanitizeSpecialCharacters(event.getText()));
		});
		final TextField bodyTextField = ComponentFactory.getInstance().createField(TextField.class, I18N.ADMINWORKITEMTABLE_FILTER_BODY.msg());
		bodyTextField.addTextChangeListener(event ->
		{
			bodyFilter.setValue(StringUtils.deSanitizeSpecialCharacters(event.getText()));
		});
		
		layout.addComponents(emailTextField, aliasTextField, firstNameTextField, lastNameTextField, subjectTextField,adminTextField, bodyTextField);
		
		if (dateFilter != null)
		{
			final DateField dateField = ComponentFactory.getInstance().createField(DateField.class, I18N.ADMINWORKITEMTABLE_FILTER_DATE.msg());
			dateField.setResolution(Resolution.DAY);
			dateField.setConverter(new LocalDateConverter());
			dateField.addValueChangeListener(event ->
			{
				dateFilter.setValue((LocalDate) dateField.getConvertedValue());
			});
			
			layout.addComponent(dateField);
		}
		
		if (adminWorkItemStatusFilter != null)
		{
			final ComboBox adminWorkItemStatusComboBox = ComponentFactory.getInstance().createField(AdminWorkItem.AdminWorkItemStatus.class, ComboBox.class);
			adminWorkItemStatusComboBox.setCaption(I18N.ADMINWORKITEMTABLE_FILTER_STATUS.msg());
			adminWorkItemStatusComboBox.setNullSelectionAllowed(true);
			adminWorkItemStatusComboBox.addValueChangeListener(event ->
			{
				adminWorkItemStatusFilter.setValue((AdminWorkItem.AdminWorkItemStatus) adminWorkItemStatusComboBox.getValue());
			});
			
			layout.addComponent(adminWorkItemStatusComboBox);
		}
		
		if (unreadFilter != null)
		{
			final CheckBox unreadCheckBox = ComponentFactory.getInstance().createField(CheckBox.class, I18N.ADMINWORKITEMTABLE_FILTER_UNREAD.msg());
			unreadCheckBox.addValueChangeListener(event ->
			{
				unreadFilter.setValue(unreadCheckBox.getValue() ? false : null);
			});
			
			layout.addComponent(unreadCheckBox);
			layout.setComponentAlignment(unreadCheckBox, Alignment.BOTTOM_CENTER);
		}
		
		layout.setStyleName(CssStyle.TABLE_FILTERBAR.getStyleName());
		
		return layout;
	}
	
	private void initTable(Directory directory)
	{
		switch (directory)
		{
			case INCOMING:

				adminWorkItemTable.setCellStyleGenerator(this::onIncomingCellStyleGenerating);
				adminWorkItemTable.addGeneratedColumn(I18N.ADMINWORKITEMTABLE_HEADER_SENDER.msg(), itemid->itemid.getMessage().getSenderEnvelope().getUser().getAlias());
				adminWorkItemTable.addGeneratedColumn(I18N.ADMINWORKITEMTABLE_HEADER_SUBJECT.msg(), (source, itemId, columnId) -> createSubjectColumn(itemId));
				adminWorkItemTable.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_SENDDATE.msg(), (source, itemId, columnId) -> new Label(itemId.getMessage().getSendDate().format(dateTimeFormatter)));
				adminWorkItemTable.addGeneratedColumn(I18N.ADMINWORKITEMTABLE_HEADER_STATE.msg(),AdminWorkItem::getWorkItemStatus);
				adminWorkItemTable.addGeneratedColumn(I18N.ADMINWORKITEMTABLE_HEADER_RESPONSIBLE.msg(), itemid->itemid.getAdmin()!=null?itemid.getAdmin().getAlias():"");

				adminWorkItemTable.setSortPropertyId(false, AdminWorkItem_.message, Message_.sendDate);
				break;
			case OUTGOING:
				adminWorkItemTable.addGeneratedColumn(I18N.ADMINWORKITEMTABLE_HEADER_RECEIVER.msg(), itemid->itemid.getMessage().getReceiverEnvelope().getUser().getAlias());
				adminWorkItemTable.addGeneratedColumn(I18N.ADMINWORKITEMTABLE_HEADER_SUBJECT.msg(), (source, itemId, columnId) -> createSubjectColumn(itemId));
				adminWorkItemTable.addGeneratedColumn(I18N.MESSAGETABLE_HEADER_SENDDATE.msg(), (source, itemId, columnId) -> new Label(itemId.getMessage().getSendDate().format(dateTimeFormatter)));
				adminWorkItemTable.addGeneratedColumn(I18N.ADMINWORKITEMTABLE_HEADER_SENDER.msg(),  itemid->itemid.getAdmin().getAlias());

				adminWorkItemTable.setSortPropertyId(false, AdminWorkItem_.message, Message_.sendDate);
				break;
			case DRAFT:
				adminWorkItemTable.addGeneratedColumn(I18N.ADMINWORKITEMTABLE_HEADER_RECEIVER.msg(),  itemid->itemid.getMessage().getReceiverEnvelope().getUser().getAlias());
				adminWorkItemTable.addGeneratedColumn(I18N.ADMINWORKITEMTABLE_HEADER_SUBJECT.msg(), (source, itemId, columnId) -> createSubjectColumn(itemId));
				adminWorkItemTable.addGeneratedColumn(I18N.ADMINWORKITEMTABLE_HEADER_EDITOR.msg(), itemid->itemid.getAdmin().getAlias());

				adminWorkItemTable.setSortPropertyId(false, AdminWorkItem_.message, Message_.createDate);
				break;
		}
	}

	private LazyBeanPagingComponent<AdminWorkItem> createTable()
	{
		final LazyBeanPagingComponent<AdminWorkItem> table = new LazyBeanPagingComponent<>();

		table.setSelectable(false);
		table.setMultiSelect(false);
		table.setSizeFull();

		return table;
	}

	private String createSubjectColumn(AdminWorkItem workItem)
	{
		return StringUtils.deSanitizeSpecialCharacters(workItem.getMessage().getSubject());
	}

	private ComponentContainer createTableFooter()
	{
		final HorizontalLayout tableFooter = new HorizontalLayout();
		tableFooter.setSpacing(true);
		return tableFooter;
	}

	private CssStyle onIncomingCellStyleGenerating(LazyBeanItemComponent<AdminWorkItem> source, AdminWorkItem item, Object propertyId)
	{
		return item.getMessage().getReceiverEnvelope().isRead() ? CssStyle.DEFAULT : CssStyle.UNREAD;
	}

	public void setAdminWorkItemsHandler(LazyBeanFilteredItemsHandler<AdminWorkItem> handler)
	{
		adminWorkItemTable.setHandler(handler);
	}

	public void addItemClickListener(ItemClickListener itemClickListener)
	{
		adminWorkItemTable.addItemClickListener(itemClickListener);
	}

	public Directory getDirectory()
	{
		return directory;
	}
}
