package de.binaerebauten.gleichklang.memberweb.view;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable.Directory;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable.FilterConfiguration;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable.FilterConfiguration.Filter;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem;
import de.binaerebauten.gleichklang.memberweb.view.component.GenericViewHeader;

public class UserAdminMessageViewImpl extends AbstractNavigateView<UserAdminMessageView.AdminMessageViewListener> implements UserAdminMessageView
{
	private final VerticalLayout root;
	private final MessageTable incomingTable;
	private final MessageTable outgoingTable;
	
	private final BiMap<AdminMessageTab, TabSheet.Tab> tabs = EnumHashBiMap.create(AdminMessageTab.class);
	private final TabSheet tabSheet;

	private final Button focusPlaceholder;
	
	public UserAdminMessageViewImpl(Device device)
	{
		incomingTable = createIncomingTable(device);
		outgoingTable = createOutgoingTable(device);
		tabSheet = createTabControl();
		//tabSheet.setWidth("98%");
		
		root = new VerticalLayout();
		setCompositionRoot(root);

		focusPlaceholder = new Button();
		focusPlaceholder.addStyleName(CssStyle.FOCUS_BTN.getStyleName());

		root.addComponent(focusPlaceholder);
		focusPlaceholder.focus();
		
		initView(device);
	}
	
	private Component createHeader()
	{
		final GenericViewHeader header = new GenericViewHeader();
		header.addStyleName(CssStyle.GENERIC_HEADER_BLUE.getStyleName());
		header.setCaption(MemberMenuItem.MESSAGE_TO_GLEICHKLANG.toString());
		header.setDescription(I18N.USERDATA_MESSAGETOGLEICHKLANG.msg());
		header.setIcon(new ThemeResource("img/mail-gleichklang.svg"));

		return header;
	}
	
	private MessageTable createIncomingTable(Device device)
	{
		final FilterConfiguration filterConfiguration = new FilterConfiguration(true);
		filterConfiguration.removeActivatedFilter(Filter.ALIAS, Filter.CANCEL_MESSAGE);
		
		final MessageTable messageTable = new MessageTable(Directory.INCOMING, filterConfiguration, device,null);
		messageTable.setSelectable(false);
		messageTable.addStyleName(CssStyle.USER_ADMIN_MESSAGE_TABLE.getStyleName());

		messageTable.setNewButtonClickListener(() -> fireEvent(AdminMessageViewListener::writeMessage));
		messageTable.setOpenButtonClickListener(message -> fireEvent(eventAction -> eventAction.openMessage(message)));

		//messageTable.addTableButton(FontAwesome.TRASH_O, I18N.MESSAGEVIEW_ACTION_DELETE.msg(), messages -> fireEvent(eventAction -> eventAction.hideMessages(messages)));

		return messageTable;
	}
	
	private MessageTable createOutgoingTable(Device device)
	{
		final FilterConfiguration filterConfiguration = new FilterConfiguration(true);
		filterConfiguration.removeActivatedFilter(Filter.ALIAS, Filter.CANCEL_MESSAGE);
		
		final MessageTable messageTable = new MessageTable(Directory.OUTGOING, filterConfiguration, device,null);
		messageTable.setSelectable(false);
		messageTable.addStyleName(CssStyle.USER_ADMIN_MESSAGE_TABLE.getStyleName());
		
		messageTable.setOpenButtonClickListener(message -> fireEvent(eventAction -> eventAction.openMessage(message)));
		
		//messageTable.addTableButton(FontAwesome.TRASH_O, I18N.MESSAGEVIEW_ACTION_DELETE.msg(), messages -> fireEvent(eventAction -> eventAction.hideMessages(messages)));
		
		return messageTable;
	}
	
	private TabSheet createTabControl()
	{
		final TabSheet tabSheet = new TabSheet();
		tabSheet.setStyleName(CssStyle.MESSAGE_VIEW_PANEL.getStyleName());
		tabs.put(AdminMessageTab.INCOMING, tabSheet.addTab(createIncomingTab(), I18N.MESSAGEVIEW_TAB_INCOMING.msg()));
		tabs.put(AdminMessageTab.OUTGOING, tabSheet.addTab(createOutgoingTab(), I18N.MESSAGEVIEW_TAB_OUTGOING.msg()));
		
		tabSheet.addSelectedTabChangeListener(event -> fireEvent(eventAction -> eventAction.onTabSelected(getSelectedMessageTab())));
		
		return tabSheet;
	}

	private HorizontalLayout createTabDropdown()
	{
		final HorizontalLayout dropDownWrapper = new HorizontalLayout();
		dropDownWrapper.setSizeFull();
		dropDownWrapper.setStyleName(CssStyle.TABSHEET_DROPDOWN_GREEN.getStyleName());

		final ComboBox dropdown = new ComboBox();
		dropdown.setItemStyleGenerator(new ComboBox.ItemStyleGenerator() {

			@Override
			public String getStyle(ComboBox source, Object itemId) {
				return CssStyle.TABSHEET_DROPDOWN_POPUP_ITEMS.name();
			}
		});

		dropdown.addItem(I18N.MESSAGEVIEW_TAB_INCOMING.msg());
		dropdown.addItem(I18N.MESSAGEVIEW_TAB_OUTGOING.msg());

		dropdown.setValue(I18N.MESSAGEVIEW_TAB_INCOMING.msg());

		dropdown.setImmediate(true);
		dropdown.setTextInputAllowed(false);
		dropdown.setNullSelectionAllowed(false);
		//tabs are invisible. listenerevents in dropdown will be forwarded to tabcontrol
		dropdown.addValueChangeListener(event -> fireEvent(eventAction -> eventAction.onTabSelected(getSelectedInbox(dropdown.getValue().toString()))));

		dropDownWrapper.addComponent(dropdown);

		return dropDownWrapper;
	}

	private UserAdminMessageView.AdminMessageTab getSelectedInbox(String value)
	{
		UserAdminMessageView.AdminMessageTab tab = UserAdminMessageView.AdminMessageTab.INCOMING;
		tabSheet.setSelectedTab(0);

		if (value.equals(I18N.MESSAGEVIEW_TAB_OUTGOING.msg()))
		{
			tab = UserAdminMessageView.AdminMessageTab.OUTGOING;
			tabSheet.setSelectedTab(1);
		}

		return tab;
	}
	
	private Component createIncomingTab()
	{
		return incomingTable;
	}
	
	private Component createOutgoingTab()
	{
		return outgoingTable;
	}
	
	@Override
	public void onDeviceChanged(Device device)
	{
		initView(device);
		
		incomingTable.onDeviceChanged(device);
		outgoingTable.onDeviceChanged(device);

	}
	
	@Override
	public void initDesktopView()
	{
		root.removeAllComponents();
		root.addComponent(focusPlaceholder);
		root.addComponents(createHeader());
		root.addComponent(createTabDropdown());
		tabSheet.setTabsVisible(false);
		root.addComponent(tabSheet);

		focusPlaceholder.focus();
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
	public void setIncomingMessagesHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Message> handler)
	{
		incomingTable.setMessagesHandler(handler);
	}
	
	@Override
	public void setOutgoingMessagesHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Message> handler)
	{
		outgoingTable.setMessagesHandler(handler);
	}
	
	@Override
	public void reset()
	{
		tabSheet.setSelectedTab(0);
	}
}
