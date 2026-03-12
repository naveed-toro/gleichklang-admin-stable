package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import de.binaerebauten.gleichklang.core.view.component.OnDemandFileDownloader;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Domi on 04.10.2016.
 */
public class FileViewerComponent extends Panel {

    private final VerticalLayout content = new VerticalLayout();
    private final Label descriptionLabel = new Label();

//    private final List<OnDemandFileDownloader> fileDownloader = new ArrayList<>();
    private Map<String, StreamResource> streamResourceMap = new HashMap<>();

    public FileViewerComponent(Map<String, StreamResource> streamResourceMap) {
        this.streamResourceMap = streamResourceMap;

        content.setSizeUndefined();
        content.setStyleName(CssStyle.FILE_DOWNLOAD_WRAPPER.getStyleName());

        setContent(content);

        initDescription();
        initLinks();
    }

    private void initDescription() {
        content.addComponent(descriptionLabel);
        descriptionLabel.setSizeUndefined();

    }

    private void initLinks() {
        final VerticalLayout linkLayout = new VerticalLayout();
        linkLayout.setSizeFull();

        for (Map.Entry<String, StreamResource> item : streamResourceMap.entrySet()) {
            Button downloadButton = new Button(item.getKey());
            downloadButton.setIcon(FontAwesome.DOWNLOAD);
//            downloadButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
            downloadButton.addStyleName(CssStyle.TEXT_BUTTON.getStyleName());
            downloadButton.addStyleName(CssStyle.FILE_DOWNLOAD_BUTTON.getStyleName());

            BrowserWindowOpener browserWindowOpener = new BrowserWindowOpener(item.getValue());
            browserWindowOpener.extend(downloadButton);

            linkLayout.addComponent(downloadButton);
        }

        content.addComponent(linkLayout);
    }


    public void setDescription(String description) {
        if (description == null) {
            descriptionLabel.setVisible(false);
            return;
        }
        descriptionLabel.setCaption(description);
        descriptionLabel.setVisible(true);
    }
}
