package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.server.FileResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;

import java.util.Objects;

/**
 * Created by rgoerner on 16.12.16.
 */
public class MediaUploadComponent extends CustomComponent
{
    public interface MediaUploadCallback
    {
        void save(MediaGallery gallery, MediaUploadFile mediaUploadFile) throws ValidationException;
    }

    private final MediaUploadCallback callBack;

    private final MediaUploadFile file;

    private final MediaGallery gallery;


    public MediaUploadComponent(MediaGallery gallery, MediaUploadFile mediaUploadFile, MediaUploadCallback callback)
    {
        Objects.requireNonNull(mediaUploadFile);
        Objects.requireNonNull(callback);
        Objects.requireNonNull(gallery);

        this.gallery = gallery;
        this.callBack = callback;
        this.file = mediaUploadFile;

        this.setWidth(225, Unit.PIXELS);

        setCompositionRoot(createUploadMediaComponent(file));
    }

    private Component createUploadMediaComponent(MediaUploadFile mediaUploadFile)
    {
        final VerticalLayout layout = new VerticalLayout();
        layout.setWidth(100, Unit.PERCENTAGE);
        layout.setHeightUndefined();
        layout.setStyleName("media-upload");

        final UploadComponent<MediaUploadFile> uploadComponent;
        uploadComponent = new UploadComponent<>("", "", mediaUploadFile);
        layout.addComponent(uploadComponent);
        layout.addStyleName("image-upload");
        layout.setComponentAlignment(uploadComponent, Alignment.MIDDLE_CENTER);

        layout.addComponent(new Label(I18N.MEDIAGALLERY_NEWPICTURE.msg()));

        uploadComponent.setUploadFileChangedListener((changedUploadFile, uploadResult) ->
        {
            switch (uploadResult)
            {
                case VIRUS:
                    Notification.show("Virus", Notification.Type.ERROR_MESSAGE);
                    break;
                case FAILED:
                    Notification.show("Fehler", Notification.Type.ERROR_MESSAGE);
                    break;
                case SUCCESS:
                    try
                    {
                        callBack.save(gallery, mediaUploadFile);

                        Image image = new Image();
                        FileResource file = new FileResource(mediaUploadFile.getPath().toFile());
                        image.setSource(file);
                        image.setHeight("80%");
    
                        final Label name = new Label(mediaUploadFile.getClearName(20));
                    }
                    catch (ValidationException e)
                    {
                        Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
                    }
                    break;
            }
        });

        return uploadComponent.createDragAndDrop(layout);
    }
}
