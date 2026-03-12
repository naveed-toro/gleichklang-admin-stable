package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.popup.MenuPopup;

@SuppressWarnings("serial")
public class MemberMainViewImpl extends AbstractNavigateView<MemberMainView.MemberMainViewListener> implements MemberMainView
{
	private final StylableMenuBar menuBar;
	private final MenuTree menuTree;
	private final VerticalLayout content;
	private final Label userLabel;
	private final ComboBox languageComboBox;
	
	public MemberMainViewImpl(boolean languageSelectionActivated, Device device)
	{
		userLabel = createUserLabel();
		languageComboBox = createLanguageComboBox();
		languageComboBox.setVisible(languageSelectionActivated);
		menuBar = createMenuBar();
		menuTree = createMenuTree();
		content = new VerticalLayout();
		content.addStyleName(CssStyle.MAIN_VIEW.getStyleName());
		
		initView(device);
	}
	
	private Label createUserLabel()
	{
		final Label label = new Label();
		label.setContentMode(ContentMode.HTML);
		label.setSizeUndefined();
		
		return label;
	}
	
	/* Creating View Components */
	
	private Component createAdminMessageLayout()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setHeight(100, Unit.PERCENTAGE);

		final Link impressum = new Link("Impressum", new ExternalResource("https://www.gleichklang.de/impressum/"));
		impressum.setTargetName("_blank");
		impressum.addStyleName(CssStyle.IMPRESSUM_LABEL.getStyleName());

		final Button messageToAdminButton = new Button();
		messageToAdminButton.setIcon(FontAwesome.ENVELOPE_O);
		messageToAdminButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
		messageToAdminButton.addStyleName(CssStyle.MESSAGE_TO_ADMIN_BUTTON.getStyleName());
		messageToAdminButton.addClickListener(e -> UI.getCurrent().getNavigator().navigateTo("MESSAGE"));
		messageToAdminButton.setDescription(I18N.REGISTRATIONVIEW_MESSAGE_TO_ADMIN_BUTTON.msg());

		layout.addComponent(impressum);
		layout.addComponent(messageToAdminButton);
		layout.setComponentAlignment(messageToAdminButton, Alignment.MIDDLE_CENTER);
		layout.addStyleName(CssStyle.REGISTRATION_MESSAGE_TO_ADMIN_LAYOUT.getStyleName());
		
		return layout;
	}
	
	private HorizontalLayout createCurrentUserHeader()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeUndefined();
		layout.setStyleName(CssStyle.CURRENT_USER.getStyleName());
		layout.setHeight(100, Unit.PERCENTAGE);
		
		layout.addComponent(userLabel);
		layout.setComponentAlignment(userLabel, Alignment.MIDDLE_CENTER);
		
		return layout;
	}
	
	private MenuTree createMenuTree()
	{
		final MenuTree menuTree = new MenuTree();
		menuTree.setImmediate(true);
		menuTree.setAutoExpandSelection(true);
		
		menuTree.addValueChangeListener(event -> {
			fireEvent(eventAction ->
			{
				MenuItem menuItem = (MenuItem) menuTree.getValue();
				
				if (!menuTree.hasChildren(menuItem)) {
					eventAction.changeView(menuItem);
					if (menuTree.getParent() instanceof Window) {
						((Window)menuTree.getParent()).close();
					}
				}
			});
		});
		
		return menuTree;
	}
	
	private StylableMenuBar createMenuBar()
	{
		final StylableMenuBar menuBar = new StylableMenuBar("mainMenu");
		menuBar.setImmediate(true);
		menuBar.setAutoOpen(true);
		menuBar.setResponsive(true);
		menuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
		menuBar.addStyleName(CssStyle.MENU_BAR.getStyleName());
		
		return menuBar;
	}
	
	private Component createMenuButton()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setHeight(100, Unit.PERCENTAGE);
		
		final Button menuButton = new Button();
		menuButton.setIcon(FontAwesome.BARS);
		menuButton.setStyleName(CssStyle.BUTTON_MAIN_MENU.getStyleName());
		menuButton.addClickListener(e -> fireEvent(eventAction -> {
			MenuPopup.display(menuTree);
		}));
		
		layout.addComponent(menuButton);
		layout.setComponentAlignment(menuButton, Alignment.MIDDLE_CENTER);
		
		return layout;
	}
	
	private Component createMenuView(Device device)
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setStyleName(CssStyle.MENU_WRAPPER.getStyleName());
		layout.setHeight(100, Unit.PERCENTAGE);
		layout.setSizeFull();
		
		Component menuComponent;
		
		switch (device)
		{
			case MOBILE:
				menuComponent = createMenuButton();
				break;
			
			default:
				menuComponent = menuBar;
				menuBar.setSizeFull();
				break;
		}
		
		layout.addComponent(menuComponent);
		
		return layout;
	}
	
	private Component createLogoView(Device device)
	{
		HorizontalLayout logoLayout = new HorizontalLayout();
		logoLayout.setHeight(100, Unit.PERCENTAGE);
		
		Component logo;
		switch (device)
		{
			case TABLET:
			case MOBILE:
				logo = ComponentFactory.getInstance().getSmallLogo();
				break;
			
			default:
				logo = ComponentFactory.getInstance().getLogo();
		}
		logoLayout.addComponent(logo);
		logoLayout.setComponentAlignment(logo, Alignment.MIDDLE_CENTER);
		
		return logoLayout;
	}
	
	private Component createLogoutView()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setHeight(65, Unit.PIXELS);
		layout.setWidth(65, Unit.PIXELS);
		
		final Button logoutBtn = new Button();
		logoutBtn.setHeight(65, Unit.PIXELS);
		logoutBtn.setWidth(65, Unit.PIXELS);
		logoutBtn.setStyleName(CssStyle.LOGOUT.getStyleName());
		logoutBtn.setIcon(FontAwesome.POWER_OFF);
		logoutBtn.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		logoutBtn.setCaption(I18N.MEMBERWEBWIEV_BTNLOGOUT.msg());
		
		logoutBtn.addClickListener(event -> fireEvent(MemberMainViewListener::logout));
		
		layout.addComponent(logoutBtn);
		layout.setComponentAlignment(logoutBtn, Alignment.MIDDLE_CENTER);
		
		return layout;
	}
	
	private Component createDesktopHeader()
	{
		final VerticalLayout headerWrapper = new VerticalLayout();
		headerWrapper.setSizeFull();
		headerWrapper.setStyleName(CssStyle.HEADER_WRAPPER.getStyleName());
		
		final HorizontalLayout topHeaderWrapper = new HorizontalLayout();
		topHeaderWrapper.setStyleName(CssStyle.TOP_HEADER_WRAPPER.getStyleName());
		final Component buildInfo = ComponentFactory.getInstance().getBuildNumberComponent();
		buildInfo.setStyleName(CssStyle.BUILD_INFO.getStyleName());
		topHeaderWrapper.addComponent(buildInfo);
		
		final Component currentUser = createCurrentUserHeader();
		final Component languageSelection = createLanguageSelection();
		final Component logoutLayout = createLogoutView();
		
		topHeaderWrapper.addComponents(currentUser, languageSelection, logoutLayout);
		
		headerWrapper.addComponent(topHeaderWrapper);
		headerWrapper.setComponentAlignment(topHeaderWrapper, Alignment.MIDDLE_RIGHT);
		
		// Logobar
		
		final HorizontalLayout logoBar = new HorizontalLayout();
		logoBar.setSizeFull();
		
		final Component logoLayout = createLogoView(Device.DESKTOP);
		logoLayout.setSizeFull();
		
		logoBar.addComponent(logoLayout);
		logoBar.setComponentAlignment(logoLayout, Alignment.MIDDLE_CENTER);
		
		headerWrapper.addComponent(logoBar);
		headerWrapper.setComponentAlignment(logoBar, Alignment.MIDDLE_CENTER);
		
		final HorizontalLayout navigationBar = new HorizontalLayout();
		navigationBar.setSizeFull();
		navigationBar.addStyleName(CssStyle.HEADER_NAVIGATION.getStyleName());
		
		final Component menu = createMenuView(Device.DESKTOP);
		navigationBar.addComponent(menu);
		navigationBar.setComponentAlignment(menu, Alignment.MIDDLE_LEFT);
		
		headerWrapper.addComponent(navigationBar);
		headerWrapper.addComponent(createAdminMessageLayout());
		headerWrapper.setComponentAlignment(navigationBar, Alignment.MIDDLE_LEFT);
		
		return headerWrapper;
	}
	
	private Component createLanguageSelection()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeUndefined();
		layout.setStyleName(CssStyle.CURRENT_USER.getStyleName());
		layout.setHeight(100, Unit.PERCENTAGE);
		
		layout.addComponent(languageComboBox);
		layout.setComponentAlignment(languageComboBox, Alignment.MIDDLE_CENTER);
		
		return layout;
	}
	
	private ComboBox createLanguageComboBox()
	{
		final ComboBox comboBox = ComponentFactory.getInstance().createField(Language.class, ComboBox.class);
		comboBox.addValueChangeListener(event -> getListener().changeLanguage((Language) comboBox.getValue()));
		comboBox.addStyleName(CssStyle.COMBOBOX_LANGUAGE_SELECTOR.getStyleName());
		return comboBox;
	}
	
	private Layout createMobileHeader()
	{
		final HorizontalLayout header = new HorizontalLayout();
		header.setSizeFull();
		header.addStyleName(CssStyle.HEADER_WRAPPER_MOBILE.getStyleName());
		
		final HorizontalLayout leftComponents = new HorizontalLayout();
		
		leftComponents.setHeight(100, Unit.PERCENTAGE);
		leftComponents.setWidthUndefined();
		header.addComponent(leftComponents);
		header.setComponentAlignment(leftComponents, Alignment.MIDDLE_LEFT);
		
		final Component menuComponent = createMenuView(Device.MOBILE);
		menuComponent.setHeight(50, Unit.PIXELS);
		leftComponents.addComponent(menuComponent);
		
		final Component logoComponent = createLogoView(Device.MOBILE);
		logoComponent.setHeight(50, Unit.PIXELS);
		logoComponent.setWidth(50, Unit.PIXELS);
		leftComponents.addComponent(logoComponent);
		
		final HorizontalLayout rightComponents = new HorizontalLayout();
		rightComponents.setHeight(100, Unit.PERCENTAGE);
		rightComponents.setWidthUndefined();
		header.addComponent(rightComponents);
		header.setComponentAlignment(rightComponents, Alignment.MIDDLE_RIGHT);
		
		//        final Component currentUserLayout = createCurrentUserHeader();
		//        rightComponents.addComponent(currentUserLayout);
		
		rightComponents.addComponent(createAdminMessageLayout());
		final Component logoutButton = createLogoutView();
		rightComponents.addComponent(logoutButton);
		
		return header;
	}
	
	@Override
	public void addParentMenuItem(MenuItem<String> menuItem)
	{
		menuTree.addMenuItem(menuItem);
		
		if (menuItem.getChildList().size() == 0) {
			MenuBar.MenuItem menubarItem = menuBar.addItem(menuItem.toString(), new CustomMenuBarCommand(menuItem));
			menubarItem.setStyleName("highlighted");
			menuItem.addVisibleChangedListener((i) -> menubarItem.setEnabled(menuItem.isVisible()));
			menubarItem.setEnabled(!menuItem.isDisabled());
			menubarItem.setVisible(menuItem.isVisible());
		} else {
			MenuBar.MenuItem menubarItem = menuBar.addItem(menuItem.toString(), null);
			menuItem.addVisibleChangedListener((i) -> menubarItem.setEnabled(menuItem.isVisible()));
			menubarItem.setEnabled(!menuItem.isDisabled());
			menubarItem.setVisible(menuItem.isVisible());
			for(MenuItem<String> item : menuItem)
			{
				MenuBar.MenuItem newItem = menubarItem.addItem(item.toString(), new CustomMenuBarCommand(item));
				newItem.setStyleName("mainmenu");
				newItem.setEnabled(!item.isDisabled());
				newItem.setVisible(item.isVisible());
				item.addVisibleChangedListener((i) -> newItem.setVisible(item.isVisible()));
			}
		}
	}
	
	
	@Override
	public void addMenuItem(MenuItem<String> menuItem){
		menuTree.addMenuItem(menuItem);
	}
	
	@Override
	public ComponentContainer getViewPort()
	{
		return content;
	}
	
	@Override
	public void setSelectedMenuItem(MenuItem<String> menuItem)
	{
		menuBar.highlightTopLevelForMenuItem(menuItem);
		menuTree.select(menuItem);
	}
	
	@Override
	public void setCurrentUser(User currentUser)
	{
		userLabel.setValue(null);
		languageComboBox.setValue(null);
		
		if (currentUser != null)
		{
			userLabel.setValue(currentUser.getAlias());
			languageComboBox.setValue(currentUser.getUserSettings().getLanguage());
		}
	}
	
	@Override
	public void initDesktopView()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		final Component header = createDesktopHeader();
		layout.addComponent(header);
		layout.addComponent(content);
		layout.setComponentAlignment(content, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(header, Alignment.MIDDLE_CENTER);
		
		setHeightUndefined();
		
		setCompositionRoot(layout);
	}
	
	@Override
	public void initMobileView()
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		
		final Layout header = createMobileHeader();
		header.setSizeFull();
		header.setHeight(75, Unit.PIXELS);
		
		final Panel contentPanel = new Panel();
		contentPanel.setSizeFull();
		contentPanel.setContent(content);
		
		layout.addComponents(header, contentPanel);
		layout.setComponentAlignment(header, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(contentPanel, Alignment.MIDDLE_CENTER);
		layout.setExpandRatio(header, 0.0f);
		layout.setExpandRatio(contentPanel, 1.0f);
		
		setHeight(100, Unit.PERCENTAGE);
		
		setCompositionRoot(layout);
	}
}
