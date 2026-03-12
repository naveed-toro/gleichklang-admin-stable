package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.view.I18N;


/**
 * Created by rgoerner on 19.05.16.
 */
public class NewUserAdminMessagesComponent extends CustomComponent
{

    private final VerticalLayout wrapper;
    private Button messageButton;


    public NewUserAdminMessagesComponent()
    {
        wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setStyleName(CssStyle.ADMIN_MESSAGE_WRAPPER.getStyleName());
        setCompositionRoot(wrapper);
    }

    public void setNewAdminMessages()
    {
        wrapper.removeAllComponents();

        messageButton = new Button();
        messageButton.setCaption(I18N.USERADMIN_MESSAGES_NEW.msg());
        messageButton.addStyleName(ValoTheme.BUTTON_LINK);
        messageButton.setIcon(FontAwesome.CHEVRON_RIGHT);
        wrapper.addComponents(messageButton);

    }

    public Button getMessagesButton()
    {
        return messageButton;
    }

}

