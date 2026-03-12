package de.binaerebauten.gleichklang.adminweb.view;

import com.vaadin.ui.ComponentContainer;
import de.binaerebauten.gleichklang.adminweb.view.AdminMainView.MainViewListener;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.MenuItem;

public interface AdminMainView extends NavigateView<MainViewListener>
{

	interface MainViewListener extends NavigateView.NavigateViewListener
	{
		void changeView(MenuItem<String> menuItem);
		
		void logout();

		void openReminder();

		 int countPendingMails();
	}
	
	void addParentMenuItem(MenuItem<String> menuItem);

	void addMenuItem(MenuItem<String> menuItem);

	ComponentContainer getViewPort();

	void setSelectedMenuItem(MenuItem<String> menuItem);
	
	void setCurrentAdmin(Admin currentAdmin);


}