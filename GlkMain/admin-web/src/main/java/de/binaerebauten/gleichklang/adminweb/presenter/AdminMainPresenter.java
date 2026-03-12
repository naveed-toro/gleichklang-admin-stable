package de.binaerebauten.gleichklang.adminweb.presenter;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.adminweb.presenter.handler.UserControlHandler;
import de.binaerebauten.gleichklang.adminweb.view.AdminMainView;
import de.binaerebauten.gleichklang.adminweb.view.AdminMainView.MainViewListener;
import de.binaerebauten.gleichklang.adminweb.view.popup.AdminReminderPopup;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.presenter.ManualNavigatePresenter;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.security.AuthenticationService.RedirectGoal;
import de.binaerebauten.gleichklang.core.service.AdminService;
import de.binaerebauten.gleichklang.core.service.ReminderService;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.view.component.MenuItem;
import de.binaerebauten.gleichklang.core.view.popup.ChangePasswordPopup;
import org.springframework.context.ApplicationContext;


import java.util.Collection;

public class AdminMainPresenter extends ManualNavigatePresenter implements MainViewListener, ViewChangeListener
{
	private final DefaultNavigator navigator;
	private final AuthenticationService authenticationService;
	private final AdminMainView adminMainView;
    private final MailQueueService mailQueueService;
	private final ReminderService reminderService;
	final Admin currentAdmin;
	UserControlHandler userControlHandler;
	
	public AdminMainPresenter(ApplicationContext ctx, AdminMainView adminMainView, DefaultNavigator navigator)
	{
		super(adminMainView);
		
		this.navigator = navigator;
		this.authenticationService = ctx.getBean(AuthenticationService.class);
		reminderService = ctx.getBean(ReminderService.class);
		currentAdmin = ctx.getBean(AdminService.class).getCurrentUser();
		mailQueueService = ctx.getBean(MailQueueService.class);
		userControlHandler = new UserControlHandler(ctx, this);

		this.adminMainView = adminMainView;
		adminMainView.setListener(this);
		initNavigator();
		
		final AdminService adminService = ctx.getBean(AdminService.class);
		final Admin currentAdmin = adminService.getCurrentUser();
		
		if (currentAdmin.isResetPassword())
		{
			final ChangePasswordPopup changePasswordPopup = new ChangePasswordPopup(adminService::changePassword);
			changePasswordPopup.show();
		}

		createWarning();
	}

	private void initNavigator()
	{
		Collection<MenuItem<String>> items = this.navigator.getParentItems();
		if (items.isEmpty())
		{
			items = this.navigator.getMenuItems();
			items.forEach(adminMainView::addMenuItem);
		}
		else
		{
			items.forEach(adminMainView::addParentMenuItem);
		}

		this.navigator.addViewChangeListener(this);
	}

	
	@Override
	public void changeView(MenuItem<String> menuItem)
	{
		if (menuItem != null && !menuItem.isDisabled())
		{
			navigator.navigateTo(menuItem.getContent());
			UI.getCurrent().setScrollTop(0);
			UI.getCurrent().markAsDirty();
		}
	}
	
	@Override
	public void enter(ParametersHolder parameterMap)
	{
	
	}
	
	@Override
	public void logout()
	{
		authenticationService.logout(RedirectGoal.NONE);
	}

	@Override
	public int countPendingMails()
	{
		return mailQueueService.countPendingMails();
	}


	@Override
	public void openReminder(){ showReminderList();
	}

	private void showReminderList()
	{

		AdminReminderPopup reminderPopup = new AdminReminderPopup(reminderService, AdminReminderPopup.Operation.VIEW, null,  currentAdmin, userControlHandler);

		tryOpenPopup(reminderPopup);

	}

	@Override
	public boolean beforeViewChange(ViewChangeEvent event) {
		return true;
	}

	@Override
	public void afterViewChange(ViewChangeEvent event) {
		final String viewName = event.getViewName();
		final MenuItem menuItem = navigator.getMenuByKey(viewName);

		if (menuItem != null) {
			adminMainView.setSelectedMenuItem(menuItem);
		}
	}

	public void createWarning(){

		if(countPendingMails()>1000) {
			Notification.show("Pending Mail Count Exceeds to "+countPendingMails(), Notification.Type.WARNING_MESSAGE);
		}
	}
}
