package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.AudioUploadComponent;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;

import java.util.List;
import java.util.Map;

public interface AudioView extends NavigateView<AudioView.AudioViewListener>
{
	interface AudioViewListener extends NavigateView.NavigateViewListener
	{

		void addNewAudioPopup();

		void editNewAudioPopup(UserAudio userAudio);

		void deleteAudio(UserAudio item);

		void editAudio(UserAudio userAudio);

		void playAudioPopup(UserAudio audio);

        void refreshView();



	}
	void setUserAudioTableHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<UserAudio> handler);




}
