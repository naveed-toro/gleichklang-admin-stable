package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;

/**
 * Created by Domi on 13.02.2017.
 */
public class CustomMenuBarCommand implements MenuBar.Command {

    private final MenuItem<String> menuItem;

    public CustomMenuBarCommand(MenuItem<String> menuItem) {
        this.menuItem = menuItem;
    }

    public MenuItem<String> getMenuItem() {
        return menuItem;
    }

    @Override
    public void menuSelected(MenuBar.MenuItem selectedItem) {
        UI.getCurrent().getNavigator().navigateTo(menuItem.getContent());
    }
}
