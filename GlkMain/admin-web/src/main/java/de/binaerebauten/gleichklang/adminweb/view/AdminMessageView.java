package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.view.AdminMessageView.AdminMessageViewListener;
import de.binaerebauten.gleichklang.adminweb.view.component.AdminWorkItemTable.Directory;
import de.binaerebauten.gleichklang.core.model.mail.AdminEmail;
import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.model.reminder.AdminReminder;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.AdminEmailListComponent;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;

/**
 * Created by rgoerner on 26.04.16.
 */
public interface AdminMessageView extends NavigateView<AdminMessageViewListener>
{
	enum AdminMessageTab implements DefaultEnumI18N
	{
		INCOMING,
		INCOMING_LOVE_SCAMMER,
		INCOMING_ABUSE,
		INCOMING_PENDING_PAYMENT,
		INCOMING_NEW_ZIP,
		REMINDERS,
		ADMIN_EMAILS,
		OUTGOING,
		DRAFT;
		
		@Override
		public String toString()
		{
			return msg();
		}
	}
	
	interface AdminMessageViewListener extends NavigateView.NavigateViewListener
    {
        void onTabSelected(AdminMessageViewImpl.AdminMessageTab selectedTab);
	
        void openAdminWorkItem(AdminWorkItem adminWorkItem, Directory directory);

		void  addNewAdminReminder();

		void editAdminReminder(AdminReminder reminder);
		void deleteAdminReminder(AdminReminder reminder);
		void openUser(AdminReminder reminder);

		void openAdminEmail(AdminEmailListComponent.AdminEmailOperationData data, AdminEmailListComponent.Operation operation);
	}
	
	void changeTab(AdminMessageViewImpl.AdminMessageTab tab);

    AdminMessageViewImpl.AdminMessageTab getSelectedMessageTab();

    void setIncomingWorkItemsHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminWorkItem> handler);

    void setOutgoingWorkItemsHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminWorkItem> handler);

    void setDraftWorkItemsHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminWorkItem> handler);

	void setAdminRemindersHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminReminder> handler);

	void setAdminEmailsHandler( LazyBeanItemContainer.LazyBeanFilteredItemsHandler<AdminEmail> handler);

    void reset();


}