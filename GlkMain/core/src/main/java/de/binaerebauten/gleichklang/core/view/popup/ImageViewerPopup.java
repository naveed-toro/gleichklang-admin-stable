package de.binaerebauten.gleichklang.core.view.popup;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.view.component.ImageViewer;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.List;
import java.util.Map;

/**
 * Created by rgoerner on 29.08.16.
 *
 * TODO: RG diskutieren, ob ImageViewer mit ImageViewerPopup zusammengelegt werden kann
 */
public class ImageViewerPopup extends Popup
{

    public ImageViewerPopup(Map<MediaUploadFile, List<MediaUploadFile>> medias)
    {
        super();

        this.setHeight("100%");
        this.setWidth("100%");
        this.setStyleName(CssStyle.MEDIA_CAROUSEL_POPUP.getStyleName());
        this.setContent(createMediaPreviewComponent(medias.entrySet().iterator().next().getValue(), medias.entrySet().iterator().next().getKey()));
    }

    /**
     * Create a component to display a media in bigger size. It include an ability to go to next or previous item.
     *
     * @param mediaUploadFiles all medias for previous and next ability
     * @param mediaUploadFile  the current to display media
     * @return
     */
    private Component createMediaPreviewComponent(List<MediaUploadFile> mediaUploadFiles, MediaUploadFile mediaUploadFile)
    {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        final ImageViewer imageViewer = new ImageViewer(mediaUploadFiles, mediaUploadFile);

        layout.addComponent(imageViewer);
        layout.setComponentAlignment(imageViewer, Alignment.MIDDLE_CENTER);

        return layout;
    }
}
