package de.binaerebauten.gleichklang.adminweb.presenter;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.vaadin.ui.Notification;

import de.binaerebauten.gleichklang.adminweb.presenter.handler.AdminMessageHandler;
import de.binaerebauten.gleichklang.adminweb.presenter.handler.UserControlHandler;
import de.binaerebauten.gleichklang.adminweb.view.AdminMessageView;
import de.binaerebauten.gleichklang.adminweb.view.AdminMessageView.AdminMessageTab;
import de.binaerebauten.gleichklang.adminweb.view.AdminMessageView.AdminMessageViewListener;
import de.binaerebauten.gleichklang.adminweb.view.component.AdminWorkItemTable.Directory;
import de.binaerebauten.gleichklang.adminweb.view.popup.AdminEmailPopup;
import de.binaerebauten.gleichklang.adminweb.view.popup.AdminReminderPopup;
import de.binaerebauten.gleichklang.core.model.mail.AdminEmail;
import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.model.reminder.AdminReminder;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.AdminService;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.MessageService.MessageDirectory;
import de.binaerebauten.gleichklang.core.service.ReminderService;
import de.binaerebauten.gleichklang.core.service.mail.MailReceiveService;
import de.binaerebauten.gleichklang.core.view.component.AdminEmailListComponent;

import java.util.Objects;

/**
 * Created by rgoerner on 26.04.16.
 */
public class AdminMessagePresenter extends NavigatePresenter implements AdminMessageViewListener
{
	private static final Logger LOG = LoggerFactory.getLogger(AdminMessagePresenter.class);
	private final MessageService messageService;
	private final ReminderService reminderService;
	private final AdminMessageView view;
	private final AdminMessageHandler adminMessageHandler;
	private final MailReceiveService adminEmailService;
	final Admin currentAdmin;
	private final UserControlHandler userControlHandler1;
	
	public AdminMessagePresenter(ApplicationContext ctx, AdminMessageView view)
	{
		super(view);
		
		this.view = view;
		
		final UserControlHandler userControlHandler = new UserControlHandler(ctx, this);
		adminMessageHandler = new AdminMessageHandler(ctx, userControlHandler, this);
		
		messageService = ctx.getBean(MessageService.class);
		reminderService = ctx.getBean(ReminderService.class);
		currentAdmin = ctx.getBean(AdminService.class).getCurrentUser();
		adminEmailService = ctx.getBean(MailReceiveService.class);
		userControlHandler1=userControlHandler;
		view.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		refreshView();
		this.view.reset();
	}
	
	@Override
	public void leave()
	{
		this.view.setOutgoingWorkItemsHandler(null);
		this.view.setIncomingWorkItemsHandler(null);
		this.view.setDraftWorkItemsHandler(null);
		
		super.leave();
	}
	
	private void refreshView()
	{
		onTabSelected(view.getSelectedMessageTab());
	}
	
	@Override
	public void onTabSelected(AdminMessageTab selectedTab)
	{
		switch (selectedTab)
		{
			case DRAFT:
				view.setDraftWorkItemsHandler(messageService.createAdminWorkItemMessagesHandler(MessageDirectory.DRAFT));
				break;
			case INCOMING:
			case INCOMING_LOVE_SCAMMER:
			case INCOMING_ABUSE:
			case INCOMING_PENDING_PAYMENT:
			case INCOMING_NEW_ZIP:
				view.setIncomingWorkItemsHandler(messageService.createAdminWorkItemMessagesHandler(MessageDirectory.INCOMING));
				break;
			case OUTGOING:
				view.setOutgoingWorkItemsHandler(messageService.createAdminWorkItemMessagesHandler(MessageDirectory.OUTGOING));
				break;
			case REMINDERS:
				view.setAdminRemindersHandler(reminderService.createAllAdminRemindersHandler(currentAdmin));
			case ADMIN_EMAILS:
				view.setAdminEmailsHandler(adminEmailService.createMailDisplayHandler());
		}
	}
	
	@Override
	public void openAdminWorkItem(AdminWorkItem adminWorkItem, Directory directory)
	{
		adminMessageHandler.openAdminWorkItem(adminWorkItem, directory, e -> refreshView());
	}

	@Override
	public void addNewAdminReminder() {
		final AdminReminderPopup popup = new AdminReminderPopup(reminderService, AdminReminderPopup.Operation.ADD, null, currentAdmin,null);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

	@Override
	public void editAdminReminder(AdminReminder reminder) {
		final AdminReminderPopup popup = new AdminReminderPopup(reminderService, AdminReminderPopup.Operation.EDIT, reminder,  currentAdmin,null);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);

	}

	@Override
	public void deleteAdminReminder(AdminReminder reminder) {
		final AdminReminderPopup popup = new AdminReminderPopup(reminderService, AdminReminderPopup.Operation.DELETE, reminder, currentAdmin,null);
		popup.addCloseListener(e -> refreshView());
		tryOpenPopup(popup);
	}

    @Override
    public void openUser(AdminReminder reminder) {
            if (Objects.isNull(reminder.getUser())) {
                MessageBox.show("This reminder is not for user");
                return;
            }
            userControlHandler1.openUser(reminder.getUser().getId());
        }



    @Override
	public void openAdminEmail(AdminEmailListComponent.AdminEmailOperationData data, AdminEmailListComponent.Operation operation) {


		try {

			switch (operation)
			{
				case REPLY:
				case READ:
				{
					AdminEmail email = data.getEmail();
					final AdminEmailPopup popup = new AdminEmailPopup(adminEmailService, messageService, operation, email, currentAdmin);
					popup.addCloseListener(e -> closeAdminEmail(email));
					tryOpenPopup(popup);
				}
				break;
				case DELETE:
				{
					AdminEmail email = data.getEmail();
					adminEmailService.markDeleted(email);
					Notification.show("Email Marked Deleted", Notification.Type.TRAY_NOTIFICATION);
					refreshView();
				}break;

				case SEARCH:
				{
					view.setAdminEmailsHandler(adminEmailService.createFilteredMailHandler(data.getSearchType(), data.getParamValue(), data.getDate()));
				}
				break;
				case CLEAR:
				case RELOAD:
				{
					view.setAdminEmailsHandler(adminEmailService.createMailDisplayHandler());
				}
				break;

			}
		}
		catch (Exception ex)
		{
			LOG.error("openAdminEmail", ex);
		}
	}


	private void closeAdminEmail(AdminEmail adminEmail)
	{
		refreshView();
	}
}
