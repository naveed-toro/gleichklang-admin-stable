package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.service.file.FileService;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.MediaUploadComponent;
import de.binaerebauten.gleichklang.memberweb.view.MediaView.MediaViewListener;

import java.util.List;
import java.util.Map;

public interface MediaView extends NavigateView<MediaViewListener>
{
	interface MediaViewListener extends NavigateView.NavigateViewListener
	{

		void newMediaGallery();

		void editMediaGallery(MediaGallery item);

		void deleteMediaGallery(MediaGallery item);

		void loadMediaList(MediaGallery item);

		void deleteMedia(MediaUploadFile item);

		void loadMediaGalleries();

		void showMediaPopup(Map<MediaUploadFile, List<MediaUploadFile>> medias);

		void editMedia(MediaUploadFile mediaUploadFile);



	}
	void updateEditedMedia(MediaUploadFile mediaUploadFile);

	void setMediaGalleryList(Map<MediaGallery, MediaUploadFile> mediaGalleryList);

	void setMediaList(MediaGallery mediaGallery, List<MediaUploadFile> medias);

	void setMediaUploadComponent(MediaUploadComponent uploadComponent);

	void setAvatars(List<Avatar> avatars);

}
