package de.binaerebauten.gleichklang.memberweb.view.component;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

import de.binaerebauten.gleichklang.core.view.component.OnDemandLink;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;



import java.util.HashMap;
import java.util.Map;

/**
 * Project: gleichklang
 * Created by Domi on 15.04.2016.
 */
public class FileLinkComponent extends Panel {

    private VerticalLayout content = new VerticalLayout();
    private Label descriptionLabel = new Label();


    private Map<String, OnDemandLink.OnDemandStreamSource> streamSourceList = new HashMap<>();

    public FileLinkComponent(Map<String, OnDemandLink.OnDemandStreamSource> streamSourceList) {
        this.streamSourceList = streamSourceList;

        content.setSizeUndefined();
        content.setStyleName(CssStyle.FILE_DOWNLOAD_WRAPPER.getStyleName());

        setContent(content);

        initDescription();
        initLinks();
    }

    private void initDescription() {
        content.addComponent(descriptionLabel);
        descriptionLabel.setSizeUndefined();
        setDescription(I18N.FILE_LINK_DESCRIPTION.msg());
    }

    public void setDescription(String description) {
        if (description == null) {
            descriptionLabel.setVisible(false);
            return;
        }
        descriptionLabel.setCaption(description);
        descriptionLabel.setVisible(true);
    }

    private void initLinks() {
        VerticalLayout linkLayout = new VerticalLayout();
        linkLayout.setSizeFull();

        for (Map.Entry<String, OnDemandLink.OnDemandStreamSource> item : streamSourceList.entrySet()) {
            OnDemandLink link = new OnDemandLink(item.getKey(), item.getValue());
            link.setIcon(FontAwesome.DOWNLOAD);
            link.setTargetName("_blank");
            linkLayout.addComponent(link);
        }

        content.addComponent(linkLayout);
    }
}
