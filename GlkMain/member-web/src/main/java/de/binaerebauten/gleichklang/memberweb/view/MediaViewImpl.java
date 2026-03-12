package de.binaerebauten.gleichklang.memberweb.view;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.MediaGalleryComponent;
import de.binaerebauten.gleichklang.core.view.component.MediaUploadComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem;
import de.binaerebauten.gleichklang.memberweb.view.MediaView.MediaViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.GenericViewHeader;

import java.util.List;
import java.util.Map;

public class MediaViewImpl extends AbstractNavigateView<MediaViewListener> implements MediaView
{
	private final MediaGalleryComponent mediaGalleryComponent;


	public MediaViewImpl()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(createHeader());

		mediaGalleryComponent = createMediaGalleryComponent();
		mediaGalleryComponent.setStyleName(CssStyle.MEDIA_GALLERY_WRAPPER.getStyleName());
		layout.addComponent(mediaGalleryComponent);
		layout.setComponentAlignment(mediaGalleryComponent, Alignment.TOP_CENTER);

		setCompositionRoot(layout);
	}

	private MediaGalleryComponent createMediaGalleryComponent()
	{
		final MediaGalleryComponent mediaGalleryComponent = new MediaGalleryComponent();

		mediaGalleryComponent.setAddMediaGalleryListener(() -> fireEvent(MediaViewListener::newMediaGallery));
		mediaGalleryComponent.setOnMediaGalleryClickListener(item -> fireEvent(eventAction -> eventAction.loadMediaList(item)));
		mediaGalleryComponent.setRemoveMediaGalleryListener(item -> fireEvent(eventAction -> eventAction.deleteMediaGallery(item)));
		mediaGalleryComponent.setEditMediaGalleryListener(item -> fireEvent(eventAction -> eventAction.editMediaGallery(item)));
		mediaGalleryComponent.setRemoveMediaListener(item -> fireEvent(eventAction -> eventAction.deleteMedia(item)));
		mediaGalleryComponent.setEditMediaListener(item -> fireEvent(eventAction -> eventAction.editMedia(item)));
		mediaGalleryComponent.setBackToMediaGalleriesListener(() -> fireEvent(eventAction -> eventAction.loadMediaGalleries()));
		mediaGalleryComponent.setOnMediaClickListener(item -> fireEvent(eventAction -> eventAction.showMediaPopup(item)));

		return mediaGalleryComponent;
	}

	private Component createHeader()
	{
		final GenericViewHeader header = new GenericViewHeader();
		header.addStyleName(CssStyle.GENERIC_HEADER_BLUE.getStyleName());
		header.setCaption(MemberMenuItem.MEDIA.toString());
		header.setDescription(I18N.MEDIAGALLERY_HEADER_DESCRIPTION.msg());
		header.setIcon(new ThemeResource("img/media-outline.svg"));

		return header;
	}

	@Override
	public void updateEditedMedia(MediaUploadFile mediaUploadFile)
	{
		mediaGalleryComponent.updateEditedMediaUploadFile(mediaUploadFile);
	}

	@Override
	public void setMediaGalleryList(Map<MediaGallery, MediaUploadFile> mediaGalleryList)
	{
		mediaGalleryComponent.setMediaGalleryList(mediaGalleryList);
	}

	@Override
	public void setMediaList(MediaGallery mediaGallery, List<MediaUploadFile> medias)
	{
		mediaGalleryComponent.setMediaList(mediaGallery, medias);
	}

	@Override
	public void setMediaUploadComponent(MediaUploadComponent uploadComponent)
	{
		this.mediaGalleryComponent.setMediaUploadComponent(uploadComponent);
	}

	@Override
	public void setAvatars(List<Avatar> avatars)
	{
		mediaGalleryComponent.setAvatars(avatars);
	}

}
