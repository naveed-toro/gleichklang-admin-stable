package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.MenuBar;


/**
 * Project: gleichklang-parent
 * Created by Domi on 07.07.2016.
 */
public class StylableMenuBar extends MenuBar {

    public final static String HIGHLIGHT_CLASS = "highlighted";

    private String popupStyleName;

    public StylableMenuBar(String popupStyleName) {
        this.popupStyleName = popupStyleName;
    }

    @Override
    public String getPrimaryStyleName() {
        if (popupStyleName != null) {
            return popupStyleName + " " + super.getPrimaryStyleName();
        }
        return super.getPrimaryStyleName();
    }

    public String getPopupStyleName() {
        return popupStyleName;
    }

    public void setPopupStyleName(String popupStyleName) {
        this.popupStyleName = popupStyleName;
    }

    /**
     * Highlights the top level menu item for a given menu entry.
     *
     * @param menuItem name of menu item
     */
    public void highlightTopLevelForMenuItem(de.binaerebauten.gleichklang.core.view.component.MenuItem menuItem) {
        for (MenuItem item : getItems()) {
            if (containsMenuItem(menuItem, item)) {
                item.setStyleName(HIGHLIGHT_CLASS);
            } else {
                item.setStyleName("");
            }

        }
    }

    private boolean containsMenuItem(de.binaerebauten.gleichklang.core.view.component.MenuItem item, MenuItem menuItem) {
        CustomMenuBarCommand command = (CustomMenuBarCommand)menuItem.getCommand();

        if (command != null && command.getMenuItem() == item) {
            return true;
        }

        if (menuItem.hasChildren()) {
            for (MenuItem childItem : menuItem.getChildren()) {
                if (containsMenuItem(item, childItem)) {
                    return true;
                }
            }
        }

        return false;
    }
}
