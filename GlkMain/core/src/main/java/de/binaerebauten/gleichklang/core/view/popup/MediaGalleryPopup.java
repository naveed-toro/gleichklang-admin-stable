package de.binaerebauten.gleichklang.core.view.popup;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.view.component.MediaGalleryComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MediaGalleryPopup extends GenericPopup
{
	public interface MediaGalleryPopupListener
	{
		void showMedias(MediaGalleryPopup sender, MediaGallery mediaGallery);
		
		void showMediaPopup(Map<MediaUploadFile, List<MediaUploadFile>> medias);
	}
	
	private final MediaGalleryComponent mediaGalleryComponent;
	private final MediaGalleryPopupListener listener;
	private final Map<MediaGallery, MediaUploadFile> mediaGalleryWithPreview;
	private final Button backToProfileButton;
	
	public MediaGalleryPopup(MediaGalleryPopupListener listener, String caption, Map<MediaGallery, MediaUploadFile> mediaGalleryWithPreview)
	{
		this.listener = Objects.requireNonNull(listener);
		this.mediaGalleryWithPreview = mediaGalleryWithPreview;
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.MEDIA_GALLERY.getStyleName());
		layout.setSpacing(true);
		layout.setMargin(true);
		
		mediaGalleryComponent = createMediaGalleryComponent();
		mediaGalleryComponent.setMainCaption(I18N.MEDIAGALLERYPOPUP_CAPTION_GALLERY.msg());
		layout.addComponent(mediaGalleryComponent);
		
		center();
		setCaption(caption);
		setIcon(new ThemeResource("img/media.svg"));
		setPopupContent(layout);
		
		backToProfileButton = new Button(de.binaerebauten.gleichklang.core.view.popup.I18N.MEDIAGALLERYPOPUP_ACTION_BACK.msg());
		backToProfileButton.setIcon(FontAwesome.CHEVRON_LEFT);
		backToProfileButton.addClickListener(event -> close());
		addFooterComponent(backToProfileButton);
		
		showMediaGalleries();
	}
	
	private MediaGalleryComponent createMediaGalleryComponent()
	{
		final MediaGalleryComponent mediaGalleryComponent = new MediaGalleryComponent();
		
		mediaGalleryComponent.setOnMediaGalleryClickListener(mediaGallery -> listener.showMedias(this, mediaGallery));
		mediaGalleryComponent.setOnMediaClickListener(listener::showMediaPopup);
		mediaGalleryComponent.setBackToMediaGalleriesListener(this::showMediaGalleries);
		
		return mediaGalleryComponent;
	}
	
	private void showMediaGalleries()
	{
		mediaGalleryComponent.setMediaGalleryList(mediaGalleryWithPreview);
		backToProfileButton.setVisible(true);
	}
	
	public void showMedias(MediaGallery mediaGallery, List<MediaUploadFile> medias)
	{
		mediaGalleryComponent.setMediaList(mediaGallery, medias);
		backToProfileButton.setVisible(false);
	}
}
