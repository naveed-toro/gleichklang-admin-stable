package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.adminweb.view.AdminMainView.MainViewListener;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.I18N;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

@SuppressWarnings("serial")
public class AdminMainViewImpl extends AbstractNavigateView<MainViewListener> implements AdminMainView
{
	private final StylableMenuBar menuBar;
	private final VerticalLayout viewPort; //bad wording. this is not the viewPort! it's the content-area.
	private final LabelField adminLabel;

	public AdminMainViewImpl()
	{
		adminLabel = createAdminLabel();
		menuBar = createMenuBar();
		viewPort = new VerticalLayout();
		viewPort.setWidth(100, Unit.PERCENTAGE);
		viewPort.addStyleName(CssStyle.MAIN_VIEW.getStyleName());

		//this is the viewPort
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();

        final Component header = createHeader();
        header.setHeightUndefined();
        final Component body = createBody();

		layout.addComponent(header);
		layout.addComponent(body);
        layout.setExpandRatio(header, 0.0f);
        layout.setExpandRatio(body, 1.0f);

//        setSizeFull();

		setCompositionRoot(layout);
	}
	
	private LabelField createAdminLabel()
	{
		final LabelField labelField = ComponentFactory.getInstance().createField(LabelField.class);
		labelField.setContentMode(ContentMode.HTML);
		labelField.addStyleName(CssStyle.CURRENT_USER.getStyleName());
		
		return labelField;
	}
	
	private StylableMenuBar createMenuBar() {
		final StylableMenuBar menuBar = new StylableMenuBar("mainMenu");
		menuBar.setImmediate(true);
		menuBar.setResponsive(true);
		menuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
        menuBar.addStyleName(CssStyle.MENU_BAR.getStyleName());
        menuBar.setWidth(100, Unit.PERCENTAGE);
		return menuBar;
	}


	private Component createBody()
	{
		final Panel panel = new Panel();
		panel.setContent(viewPort);
		panel.setSizeFull();
        panel.setStyleName(CssStyle.ADMIN_MAIN_VIEW_VIEWPORT_WRAPPER.getStyleName());
		return panel;
	}

	
	private Component createHeader()
	{
		final VerticalLayout headerWrapper = new VerticalLayout();
		headerWrapper.setWidth(100, Unit.PERCENTAGE);
		headerWrapper.setStyleName(CssStyle.HEADER_WRAPPER.getStyleName());
		headerWrapper.addComponent(ComponentFactory.getInstance().getBuildNumberComponent());

        final GridLayout headerLayout = new GridLayout(4, 1);
        headerLayout.setWidth(100, Unit.PERCENTAGE);
        headerLayout.setColumnExpandRatio(0, 0.0f);
        headerLayout.setColumnExpandRatio(1, 1.0f);
        headerLayout.setColumnExpandRatio(2, 0.0f);
		headerLayout.setColumnExpandRatio(3, 0.0f);

		Button logoutBtn = new Button();
		logoutBtn.setStyleName(CssStyle.LOGOUT.getStyleName());
		logoutBtn.setIcon(FontAwesome.POWER_OFF);
		logoutBtn.addClickListener(event -> fireEvent(MainViewListener::logout));
		headerLayout.addComponent(logoutBtn, 3, 0);
		headerLayout.setComponentAlignment(logoutBtn, Alignment.MIDDLE_CENTER);


		Button reminders = new Button();
		reminders.setStyleName(CssStyle.LOGOUT.getStyleName());
		reminders.setIcon(FontAwesome.BELL);
		reminders.addClickListener(event -> fireEvent(MainViewListener::openReminder));
		headerLayout.addComponent(reminders, 2, 0);
		headerLayout.setComponentAlignment(reminders, Alignment.MIDDLE_LEFT);

		HorizontalLayout userLayout = new HorizontalLayout();
		userLayout.setSizeFull();

		VerticalLayout logoLayout = new VerticalLayout();
		logoLayout.setSizeFull();

		final Component logo = ComponentFactory.getInstance().getLogo();
		logo.setHeight(60, Unit.PIXELS);
		logo.setWidth(300, Unit.PIXELS);
		headerLayout.addComponent(logo, 0, 0);
        headerLayout.setComponentAlignment(logo, Alignment.MIDDLE_LEFT);

		headerLayout.addComponent(adminLabel, 1, 0);
        headerLayout.setComponentAlignment(adminLabel, Alignment.MIDDLE_RIGHT);

		headerWrapper.addComponents(headerLayout);
		headerWrapper.addComponent(menuBar);
		headerWrapper.setComponentAlignment(menuBar, Alignment.MIDDLE_CENTER);

		return headerWrapper;
	}

	
	@Override
	public void addParentMenuItem(MenuItem<String> menuItem)
	{
        if (menuItem.getChildList().size() == 0) {
            MenuBar.MenuItem menubarItem = menuBar.addItem(menuItem.toString(), new CustomMenuBarCommand(menuItem));
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
            };
        }
	}

	@Override
	public void addMenuItem(MenuItem<String> menuItem){

	}

	@Override
	public ComponentContainer getViewPort()
	{
		return viewPort;
	}

	@Override
	public void setSelectedMenuItem(MenuItem<String> menuItem)
	{
		menuBar.highlightTopLevelForMenuItem(menuItem);
	}
	
	@Override
	public void setCurrentAdmin(Admin currentAdmin)
	{
		adminLabel.setValue(null);
		if(currentAdmin != null) adminLabel.setValue(I18N.MAINVIEW_LOGGED_IN_AS.msg(currentAdmin.getAlias()));
	}
}
