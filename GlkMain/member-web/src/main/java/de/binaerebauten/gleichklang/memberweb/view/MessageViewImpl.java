package de.binaerebauten.gleichklang.memberweb.view;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.ComboBox.ItemStyleGenerator;
import com.vaadin.ui.TabSheet.Tab;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.ComboBoxUtils;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable.CategoryFilter;
import de.binaerebauten.gleichklang.core.view.component.message.MessageTable.Directory;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.MessageView.MessageViewListener;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Objects;

@SuppressWarnings("serial")
public class MessageViewImpl extends AbstractNavigateView<MessageViewListener> implements MessageView
{
	private final MessageTable draftTable;
	private final MessageTable incomingTable;
	private final MessageTable outgoingTable;
	private final MessageTable deletedTable;
	private final TabSheet tabSheet;
	private final VerticalLayout layout;
	private final BiMap<MessageTab, Tab> tabs = EnumHashBiMap.create(MessageTab.class);
	private final HorizontalLayout dropdown;
	private final Button focusPlaceholder;
	private final Button goToTopButton;

	private Component categoryFilterControlComponent = new Label();
	
	public MessageViewImpl(Device device)
	{
		layout = new VerticalLayout();
		layout.setSizeFull();

		
		incomingTable = createIncomingTable(device);
		outgoingTable = createOutgoingTable(device);
		draftTable = createDraftTable(device);
		deletedTable = createDeletedTable(device);
		tabSheet = createTabControl();

		focusPlaceholder = new Button();
		focusPlaceholder.addStyleName(CssStyle.FOCUS_BTN.getStyleName());

		goToTopButton = new Button();
		goToTopButton.addStyleName(CssStyle.GO_TO_TOP.getStyleName());
		goToTopButton.addStyleName(CssStyle.BLUE.getStyleName());
		goToTopButton.setIcon(FontAwesome.CHEVRON_UP);
		goToTopButton.setCaption(I18N.GOTOTOP.msg());
		goToTopButton.addClickListener(event -> {
		    // needs to call both otherwise its not reliable for
            // mobile and desktop mode
		    focusPlaceholder.focus();
		    UI.getCurrent().setScrollTop(0);
        });
		
		dropdown = createTabDropdown();

		layout.addComponent(focusPlaceholder, 0);
		layout.addComponent(goToTopButton, 1);

		focusPlaceholder.focus();
		initView(device);
		setCompositionRoot(layout);
	}
	
	private MessageTable createDraftTable(Device device)
	{
		final MessageTable messageTable = new MessageTable(Directory.DRAFT, true, device,null);
		
		messageTable.setNewButtonClickListener(() -> fireEvent(MessageViewListener::writeMessage));
		messageTable.setOpenButtonClickListener(message -> fireEvent(eventAction -> eventAction.openMessage(message)));
		
		messageTable.addTableButton(FontAwesome.TRASH_O, I18N.MESSAGEVIEW_ACTION_DELETE.msg(), messages -> fireEvent(eventAction -> eventAction.hideMessages(messages)));
		messageTable.addTableButton(FontAwesome.TRASH_O, I18N.MESSAGEVIEW_ACTION_FINALDELETE.msg(), messages -> fireEvent(eventAction -> eventAction.deleteMessages(messages)));


		return messageTable;
	}
	
	private MessageTable createIncomingTable(Device device)
	{
		final MessageTable messageTable = new MessageTable(Directory.INCOMING, true, device,null);
		
		messageTable.setNewButtonClickListener(() -> fireEvent(MessageViewListener::writeMessage));
		messageTable.setOpenButtonClickListener(message -> fireEvent(eventAction -> eventAction.openMessage(message)));
		
		messageTable.addTableButton(FontAwesome.TRASH_O, I18N.MESSAGEVIEW_ACTION_DELETE.msg(), messages -> fireEvent(eventAction -> eventAction.hideMessages(messages)));

		return messageTable;
	}
	
	private Component createCategoryFilterControl(Collection<RecommendationCategory> visibleCategories,
			String preselectedCategory, CategoryFilter categoryFilter)
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.addStyleName(CssStyle.MESSAGE_CATEGORY_FILTER_CONTROL.getStyleName());
		layout.setVisible(visibleCategories.size() > 1);
		
		final ComboBox recommendationCategoryDropdown = ComponentFactory.getInstance().createField(ComboBox.class);
		recommendationCategoryDropdown.setTextInputAllowed(false);
		recommendationCategoryDropdown.addItem(I18N.MESSAGEVIEW_ALLCATEGORIES.msg());
		recommendationCategoryDropdown.setItemStyleGenerator((ItemStyleGenerator) (source, itemId) -> CssStyle.TABSHEET_DROPDOWN_POPUP_ITEMS.name());
		recommendationCategoryDropdown.select(I18N.MESSAGEVIEW_ALLCATEGORIES.msg());
		for (RecommendationCategory category : visibleCategories)
		{
			recommendationCategoryDropdown.addItem(category);
			recommendationCategoryDropdown.setItemCaption(category, "   " + category.getName());
			recommendationCategoryDropdown.setItemIcon(category, category.getIcon());
		}
		
		recommendationCategoryDropdown.addValueChangeListener(event ->
		{
			final RecommendationCategory selectedCategory =
					ComboBoxUtils.getValue(recommendationCategoryDropdown, RecommendationCategory.class);
			if (Objects.isNull(selectedCategory))
			{
				categoryFilter.reset();
			}
			else
			{
				categoryFilter.setCategoryEnabled(selectedCategory);
			}

			incomingTable.refresh();
			outgoingTable.refresh();
			draftTable.refresh();
			deletedTable.refresh();
		});

		if (!StringUtils.isBlank(preselectedCategory))
		{
			RecommendationCategory category = RecommendationCategory.valueOf(preselectedCategory);
			recommendationCategoryDropdown.select(category);
		}
		
		layout.addComponent(recommendationCategoryDropdown);
		
		return layout;
	}
	
	private MessageTable createDeletedTable(Device device)
	{
		final MessageTable messageTable = new MessageTable(Directory.DELETED, true, device,null);
		
		messageTable.setOpenButtonClickListener(message -> getListener().openMessage(message));
		messageTable.setNewButtonClickListener(() -> fireEvent(MessageViewListener::writeMessage));
		
		messageTable.addTableButton(FontAwesome.ENVELOPE_O, I18N.MESSAGEVIEW_ACTION_UNDELETE.msg(), messages -> fireEvent(eventAction -> eventAction.restoreMessages(messages)));
		messageTable.addTableButton(FontAwesome.TRASH_O, I18N.MESSAGEVIEW_ACTION_FINALDELETE.msg(), messages -> fireEvent(eventAction -> eventAction.deleteMessages(messages)));
		
		return messageTable;
	}
	
	private MessageTable createOutgoingTable(Device device)
	{
		final MessageTable messageTable = new MessageTable(Directory.OUTGOING, true, device,null);
		
		messageTable.setOpenButtonClickListener(message -> fireEvent(eventAction -> eventAction.openMessage(message)));
		messageTable.setNewButtonClickListener(() -> fireEvent(MessageViewListener::writeMessage));
		
		messageTable.addTableButton(FontAwesome.TRASH_O, I18N.MESSAGEVIEW_ACTION_DELETE.msg(), messages -> fireEvent(eventAction -> eventAction.hideMessages(messages)));
		
		return messageTable;
	}
	
	private TabSheet createTabControl()
	{
		final TabSheet tabSheet = new TabSheet();
		tabSheet.setStyleName(CssStyle.MESSAGE_VIEW_PANEL.getStyleName());
		
		tabs.put(MessageTab.INCOMING, tabSheet.addTab(createIncomingTab(), I18N.MESSAGEVIEW_TAB_INCOMING.msg()));
		tabs.put(MessageTab.OUTGOING, tabSheet.addTab(createOutgoingTab(), I18N.MESSAGEVIEW_TAB_OUTGOING.msg()));
		tabs.put(MessageTab.DRAFT, tabSheet.addTab(createDraftTab(), I18N.MESSAGEVIEW_TAB_DRAFT.msg()));
		tabs.put(MessageTab.DELETED, tabSheet.addTab(createDeletedTab(), I18N.MESSAGEVIEW_TAB_DELETED.msg()));
		
		tabSheet.addSelectedTabChangeListener(event -> fireEvent(eventAction -> eventAction.onTabSelected(getSelectedMessageTab())));
		
		return tabSheet;
	}
	
	private HorizontalLayout createTabDropdown()
	{
		final HorizontalLayout dropDownWrapper = new HorizontalLayout();
		dropDownWrapper.setSizeFull();
		dropDownWrapper.setStyleName(CssStyle.TABSHEET_DROPDOWN_LIGHTGREEN.getStyleName());
		
		final ComboBox dropdown = new ComboBox();
		dropdown.setItemStyleGenerator((ItemStyleGenerator) (source, itemId) ->
				CssStyle.TABSHEET_DROPDOWN_POPUP_ITEMS.name());
		
		dropdown.addItem(I18N.MESSAGEVIEW_TAB_INCOMING.msg());
		dropdown.addItem(I18N.MESSAGEVIEW_TAB_OUTGOING.msg());
		dropdown.addItem(I18N.MESSAGEVIEW_TAB_DRAFT.msg());
		dropdown.addItem(I18N.MESSAGEVIEW_TAB_DELETED.msg());
		
		dropdown.setValue(I18N.MESSAGEVIEW_TAB_INCOMING.msg());
		
		dropdown.setImmediate(true);
		dropdown.setTextInputAllowed(false);
		dropdown.setNullSelectionAllowed(false);
		//tabs are invisible. listenerevents in dropdown will be forwarded to tabcontrol
		dropdown.addValueChangeListener(event -> setSelectedTab(dropdown.getValue().toString()));
		
		dropDownWrapper.addComponent(dropdown);
		
		return dropDownWrapper;
	}
	
	private Component createViewHeader()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeUndefined();
		final Label label = new Label(I18N.MESSAGEVIEW_HEADER.msg());
		layout.addComponent(label);
		layout.setStyleName(CssStyle.VIEW_HEADER.getStyleName());
		return layout;
	}
	
	private Component createIncomingTab()
	{
		return incomingTable;
	}
	
	private Component createOutgoingTab()
	{
		return outgoingTable;
	}
	
	private Component createDraftTab()
	{
		return draftTable;
	}
	
	private Component createDeletedTab()
	{
		return deletedTable;
	}
	
	private void setSelectedTab(String value)
	{
		if (value.equals(I18N.MESSAGEVIEW_TAB_OUTGOING.msg()))
		{
			tabSheet.setSelectedTab(1);
		}
		else if (value.equals(I18N.MESSAGEVIEW_TAB_DRAFT.msg()))
		{
			tabSheet.setSelectedTab(2);
		}
		else if (value.equals(I18N.MESSAGEVIEW_TAB_DELETED.msg()))
		{
			tabSheet.setSelectedTab(3);
		}
		else
		{
			tabSheet.setSelectedTab(0);
		}
	}
	
	@Override
	public void onDeviceChanged(Device device)
	{
		layout.removeAllComponents();
		
		initView(device);
		
		incomingTable.onDeviceChanged(device);
		outgoingTable.onDeviceChanged(device);
		draftTable.onDeviceChanged(device);
		deletedTable.onDeviceChanged(device);

		layout.addComponent(focusPlaceholder, 0);
		layout.addComponent(goToTopButton, 1);
		focusPlaceholder.focus();

	}
	
	@Override
	public void initMobileView()
	{
		layout.addComponent(createViewHeader());
		layout.addComponent(categoryFilterControlComponent);
		layout.addComponent(dropdown);
		tabSheet.setTabsVisible(false);
		layout.addComponent(tabSheet);
	}
	
	@Override
	public void initDesktopView()
	{
		layout.addComponent(createViewHeader());
		layout.addComponent(categoryFilterControlComponent);
		layout.addComponent(dropdown);
		tabSheet.setTabsVisible(false);
		layout.addComponent(tabSheet);
	}
	
	@Override
	public void changeTab(MessageTab tab)
	{
		tabSheet.setSelectedTab(tabs.get(tab));
	}
	
	@Override
	public MessageTab getSelectedMessageTab()
	{
		incomingTable.clearValue();
		outgoingTable.clearValue();
		draftTable.clearValue();
		deletedTable.clearValue();
		return tabs.inverse().get(tabSheet.getTab(tabSheet.getSelectedTab()));
	}
	
	@Override
	public void goToIncomingMessages()
	{
		changeTab(MessageTab.INCOMING);
	}
	
	@Override
	public void setIncomingMessagesHandler(LazyBeanFilteredItemsHandler<Message> handler)
	{
		incomingTable.setMessagesHandler(handler);
	}
	
	@Override
	public void setOutgoingMessagesHandler(LazyBeanFilteredItemsHandler<Message> handler)
	{
		outgoingTable.setMessagesHandler(handler);
	}
	
	@Override
	public void setDraftMessagesHandler(LazyBeanFilteredItemsHandler<Message> handler)
	{
		draftTable.setMessagesHandler(handler);
	}
	
	@Override
	public void setDeletedMessagesHandler(LazyBeanFilteredItemsHandler<Message> handler)
	{
		deletedTable.setMessagesHandler(handler);
	}
	
	@Override
	public void setActiveRecommendationCategories(Collection<RecommendationCategory> categories, String category)
	{
		Objects.requireNonNull(categories);
		
		final CategoryFilter categoryFilter = new CategoryFilter(true, categories);
		incomingTable.setCategoryFilter(categoryFilter);
		outgoingTable.setCategoryFilter(categoryFilter);
		draftTable.setCategoryFilter(categoryFilter);
		deletedTable.setCategoryFilter(categoryFilter);

		final Component newFilterControl = createCategoryFilterControl(categories, category, categoryFilter);
		layout.replaceComponent(this.categoryFilterControlComponent, newFilterControl);
		this.categoryFilterControlComponent = newFilterControl;
		if(categories.size()==1) {
			if(categories.contains(RecommendationCategory.FRIENDSHIP)){
				categoryFilter.setCategoryEnabled(RecommendationCategory.FRIENDSHIP);
			}
			else{
				categoryFilter.setCategoryEnabled(RecommendationCategory.PARTNERSHIP);
			}
			incomingTable.refresh();
		}
	}
	
	@Override
	public void reset()
	{
		tabSheet.setSelectedTab(0);
	}
}
