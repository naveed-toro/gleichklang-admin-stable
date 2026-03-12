package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.ui.ComponentContainer;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.MenuItem;

public interface MemberMainView extends NavigateView<MemberMainView.MemberMainViewListener>
{

    interface MemberMainViewListener extends NavigateView.NavigateViewListener
    {
        void changeView(MenuItem<String> menuItem);

        void logout();
	
		void changeLanguage(Language language);
	}

    void addParentMenuItem(MenuItem<String> menuItem);

    void addMenuItem(MenuItem<String> menuItem);

    ComponentContainer getViewPort();

    void setSelectedMenuItem(MenuItem<String> menuItem);
	
	void setCurrentUser(User currentUser);
}
