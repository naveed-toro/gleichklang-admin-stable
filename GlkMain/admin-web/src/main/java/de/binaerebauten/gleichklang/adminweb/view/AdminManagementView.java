package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;

public interface AdminManagementView extends NavigateView<AdminManagementView.AdminManagementListener>
{
	void setAdminHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Admin> handler);

	interface AdminManagementListener extends NavigateView.NavigateViewListener
	{

		void newAdmin();

		void editAdmin(Admin admin);

		void deleteAdmin(Admin admin);

		void removeExcedingMails();

		int countPendingMails();

	}
}
