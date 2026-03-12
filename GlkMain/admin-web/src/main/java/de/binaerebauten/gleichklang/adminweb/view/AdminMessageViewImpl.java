package de.binaerebauten.gleichklang.adminweb.view;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.ImmutableMap;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.adminweb.view.component.AdminWorkItemTable;
import de.binaerebauten.gleichklang.adminweb.view.component.AdminWorkItemTable.Directory;
import de.binaerebauten.gleichklang.core.model.mail.AdminEmail;
import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.reminder.AdminReminder;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.AdminEmailListComponent;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanTable;
import de.binaerebauten.gleichklang.core.view.component.TableControl;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rgoerner on 26.04.16.
 */
@SuppressWarnings("serial")
public class AdminMessageViewImpl extends AbstractNavigateView<AdminMessageView.AdminMessageViewListener> implements AdminMessageView
{
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private final Map<AdminMessageTab, MessageType> messageTypeMapping = ImmutableMap.of(
			AdminMessageTab.INCOMING, MessageType.DEFAULT,
			AdminMessageTab.INCOMING_LOVE_SCAMMER, MessageType.LOVE_SCAMMER,
			AdminMessageTab.INCOMING_ABUSE, MessageType.ABUSE,
			AdminMessageTab.INCOMING_PENDING_PAYMENT, MessageType.PENDING_PAYMENT,
			AdminMessageTab.INCOMING_NEW_ZIP, MessageType.NEW_ZIP);
	
	private final Map<MessageType, AdminWorkItemTable> incomingTables = new HashMap<>();
	private final AdminWorkItemTable outgoingTable;
	private final AdminWorkItemTable draftTable;
	private  final  TableControl<AdminReminder> tableControl ;
	private AdminEmailListComponent adminExternalMailComponent;
	//private final TableControl<AdminEmail> adminEmailTableControl;

	private final TabSheet tabSheet;
	private final BiMap<AdminMessageTab, TabSheet.Tab> tabs = EnumHashBiMap.create(AdminMessageTab.class);
	
	public AdminMessageViewImpl()
	{
		messageTypeMapping.values().forEach(messageType ->
		{
			final AdminWorkItemTable incomingTable = createTable(Directory.INCOMING);
			incomingTable.setMessageTypeFilter(messageType);
			incomingTables.put(messageType, incomingTable);
		});

		adminExternalMailComponent = new AdminEmailListComponent();
		draftTable = createTable(Directory.DRAFT);
		outgoingTable = createTable(Directory.OUTGOING);
		tableControl = new TableControl<>(createAdminRemindersTable());
		//adminEmailTableControl = new TableControl<>(createAdminEmailTable());
		
		tabSheet = createTabControl();
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		layout.setSizeFull();
		layout.addComponent(tabSheet);
		setSizeFull();
		setCompositionRoot(layout);
	}
	
	private AdminWorkItemTable createTable(Directory directory)
	{
		final AdminWorkItemTable workItemTable = new AdminWorkItemTable(directory);
		workItemTable.addItemClickListener(item -> getListener().openAdminWorkItem(item, directory));
		workItemTable.setWidth(100, Unit.PERCENTAGE);
		
		return workItemTable;
	}

	private LazyBeanTable<AdminReminder> createAdminRemindersTable()
	{
		final LazyBeanTable<AdminReminder> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setSizeFull();
		table.addGeneratedColumn("Title", (source, itemId, columnId) -> itemId.getReminderTitle() );
		table.addGeneratedColumn("Due Date", (source, itemId, columnId) -> formatDate(itemId.getDueDate()));
		table.addGeneratedColumn("Status", (source, itemId, columnId) -> itemId.getReminderStatus());
		table.addGeneratedColumn("Added By", (source, itemId, columnId) -> itemId.getAdmin().getAlias());
		table.addGeneratedColumn("User", (source, itemId, columnId) -> itemId.getUser()!=null?itemId.getUser().getAlias():"");
		table.setCellStyleGenerator((source, itemId, columnId) ->
		{

			if (itemId.getReminderStatus() != AdminReminder.ReminderStatus.DONE &&  LocalDateTime.now().isAfter(itemId.getDueDate()))
				return CssStyle.DANGER;
			return null;
		});

		//table.addGeneratedColumn(I18N.SYSTEMCONFIG_TAB_EMAIL_DOMAIN_MAPPINGS_DELETED.msg(), (source, itemId, columnId) -> itemId.isDeleted());


		return table;
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
	
	private TabSheet createTabControl()
	{
		final TabSheet tabSheet = new TabSheet();
		
		messageTypeMapping.forEach((adminMessageTab, messageType) -> tabs.put(adminMessageTab, tabSheet.addTab(createIncomingTab(messageType), adminMessageTab.toString())));
		tabs.put(AdminMessageTab.OUTGOING, tabSheet.addTab(createOutgoingTab(), AdminMessageTab.OUTGOING.toString()));
		tabs.put(AdminMessageTab.DRAFT, tabSheet.addTab(createDraftTab(), AdminMessageTab.DRAFT.toString()));
		tabs.put(AdminMessageTab.ADMIN_EMAILS, tabSheet.addTab(createAdminEmailsTab(), "EXTERNAL"));
		tabs.put(AdminMessageTab.REMINDERS, tabSheet.addTab(createRemindersTab(), AdminMessageTab.REMINDERS.toString()));

		tabSheet.setWidth(97, Unit.PERCENTAGE);
		
		tabSheet.addSelectedTabChangeListener(event -> getListener().onTabSelected(getSelectedMessageTab()));
		
		return tabSheet;
	}
	
	private Component createIncomingTab(MessageType messageType)
	{
		return incomingTables.get(messageType);
	}
	
	private Component createOutgoingTab()
	{
		return outgoingTable;
	}
	
	private Component createDraftTab()
	{
		return draftTable;
	}

	private Component createRemindersTab()
	{

		tableControl.setMargin(true);

		tableControl.setNewCallback(() -> fireEvent(AdminMessageView.AdminMessageViewListener::addNewAdminReminder));
		tableControl.setEditCallback(item -> fireEvent(eventAction -> eventAction.editAdminReminder(item)));
		tableControl.setDeleteCallback(item -> fireEvent(eventAction -> eventAction.deleteAdminReminder(item)));
		tableControl.addButton("Show" ,item -> fireEvent(eventAction -> eventAction.openUser(item)));
		return tableControl;
	}

	private Component createAdminEmailsTab()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(adminExternalMailComponent);
		adminExternalMailComponent.setStyleName(CssStyle.MEDIA_GALLERY_WRAPPER.getStyleName());
		adminExternalMailComponent.setAdminEmailListener((data, operation) -> fireEvent(eventAction -> eventAction.openAdminEmail(data,operation)));
		layout.setComponentAlignment(adminExternalMailComponent, Alignment.TOP_CENTER);
		return layout;
	}
	
	@Override
	public void changeTab(AdminMessageTab tab)
	{
		tabSheet.setSelectedTab(tabs.get(tab));
	}
	
	@Override
	public AdminMessageTab getSelectedMessageTab()
	{
		return tabs.inverse().get(tabSheet.getTab(tabSheet.getSelectedTab()));
	}
	
	@Override
	public void setIncomingWorkItemsHandler(LazyBeanFilteredItemsHandler<AdminWorkItem> handler)
	{
		if(handler == null)
		{
			incomingTables.values().forEach(t -> t.setAdminWorkItemsHandler(null));
			return;
		}
		
		final MessageType messageType = messageTypeMapping.get(getSelectedMessageTab());
		if (incomingTables.containsKey(messageType))
		{
			incomingTables.get(messageType).setAdminWorkItemsHandler(handler);
		}
	}
	
	@Override
	public void setOutgoingWorkItemsHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminWorkItem> handler)
	{
		outgoingTable.setAdminWorkItemsHandler(handler);
	}
	
	@Override
	public void setDraftWorkItemsHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminWorkItem> handler)
	{
		draftTable.setAdminWorkItemsHandler(handler);
	}

	@Override
	public void setAdminRemindersHandler(LazyBeanFilteredItemsHandler<AdminReminder> handler) {

		tableControl.getTable().setHandler(handler);

	}

	@Override
	public void setAdminEmailsHandler(LazyBeanFilteredItemsHandler<AdminEmail> handler) {

		
		adminExternalMailComponent.setAdminEmailTableHandler(handler);

	}

	@Override
	public void reset()
	{
		tabSheet.setSelectedTab(0);
	}

}
