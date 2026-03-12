package de.binaerebauten.gleichklang.core.presenter.media;

import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.presenter.PopupOpener;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.view.popup.ImageViewerPopup;
import de.binaerebauten.gleichklang.core.service.file.MediaService;
import de.binaerebauten.gleichklang.core.view.popup.MediaGalleryPopup;
import de.binaerebauten.gleichklang.core.view.popup.MediaGalleryPopup.MediaGalleryPopupListener;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

public class MediaGalleryHandler implements MediaGalleryPopupListener
{
	private final PopupOpener popupOpener;
	
	private final MediaService mediaService;
	
	public MediaGalleryHandler(ApplicationContext ctx, PopupOpener popupOpener)
	{
		this.popupOpener = popupOpener;
		
		mediaService = ctx.getBean(MediaService.class);
	}
	
	@Override
	public void showMediaPopup(Map<MediaUploadFile, List<MediaUploadFile>> medias)
	{
		final ImageViewerPopup popup = new ImageViewerPopup(medias);
		popup.setDraggable(false);
		popupOpener.tryOpenPopup(popup);
	}
	
	@Override
	public void showMedias(MediaGalleryPopup sender, MediaGallery mediaGallery)
	{
		final List<MediaUploadFile> medias = mediaService.getMedias(mediaGallery);
		sender.showMedias(mediaGallery, medias);
	}
}
