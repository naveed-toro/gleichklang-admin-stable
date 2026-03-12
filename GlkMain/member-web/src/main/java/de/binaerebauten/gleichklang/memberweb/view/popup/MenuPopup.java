package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.ui.Component;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

/**
 * Created by Domi on 28/07/16.
 */
public class MenuPopup extends Popup {

    private Component menuContent;

    public MenuPopup() {
        setSizeFull();
        setClosable(true);
        setModal(false);
        setResizable(false);
        setCaption(null);
        setDraggable(false);

        addStyleName(CssStyle.MENU_POPUP.getStyleName());
    }

    public MenuPopup(Component content) {
        this();

        menuContent = content;
        setContent(menuContent);
    }

    public static void display(Component content) {
        MenuPopup menuPopup = new MenuPopup(content);
        menuPopup.show();
    }
}
