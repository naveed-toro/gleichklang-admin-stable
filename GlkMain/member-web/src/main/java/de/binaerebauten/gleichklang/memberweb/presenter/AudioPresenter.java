package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;


import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.file.AudioService;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;

import de.binaerebauten.gleichklang.core.view.component.AudioUploadComponent;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import de.binaerebauten.gleichklang.memberweb.view.AudioView;

import de.binaerebauten.gleichklang.memberweb.view.popup.AudioGalleryPopup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;


public class AudioPresenter extends NavigatePresenter implements AudioView.AudioViewListener , AudioGalleryPopup.AudioGalleryPopupCallback
{
	private static final Logger LOG = LoggerFactory.getLogger(AudioPresenter.class);

	private final User currentUser;
	private final AudioView view;
	private final  LazyBeanItemContainer.LazyBeanFilteredItemsHandler<UserAudio> handler;



	@Autowired
	@Lazy
	private  AudioService audioService;

	private final ClientInformation.Device device;

	public AudioPresenter(ApplicationContext ctx, AudioView view, ClientInformation.Device device)
	{
		super(view);

		System.out.print("In audio view page");
		this.view = view;
		this.device = device;
		currentUser = ctx.getBean(UserRepository.class).findOne(ctx.getBean(AuthenticationService.class).getAuthenticatedUserId());
		audioService = ctx.getBean(AudioService.class);
		handler = audioService.createAudioHandler(currentUser);
		view.setListener(this);
		view.setUserAudioTableHandler(handler);
	}

	@Override
	public void addNewAudioPopup()
	{

		int fileCount = audioService.getCountByUser(currentUser);

		if(fileCount >= 2)
		{
			MessageBox.show(I18N.CANNOT_ADD_AUDIO.msg(), MessageBox.MessageBoxButtons.OK, dialogResult ->
			{
				return;
			});
		}

		else if(audioService.isSingleAudioForBothCategories(currentUser.getId())){
			MessageBox.show(I18N.CANNOT_ADD_AUDIO_BOTH_CATEGORIES_UPLOADED.msg(), MessageBox.MessageBoxButtons.OK, dialogResult ->
			{
				return;
			});
		}

		else if (currentUser.getCategories().size()==1 && fileCount ==1){

			MessageBox.show(I18N.CANNOT_ADD_AUDIOS.msg(), MessageBox.MessageBoxButtons.OK, dialogResult ->
			{
				return;
			});
		}

		else {
			final AudioGalleryPopup popup = new AudioGalleryPopup(device, currentUser, audioService, this);
			popup.addStyleName(CssStyle.GENERIC_POPUP.getStyleName());
			popup.addClickListener((event)->refreshView());
			popup.addCloseListener(e -> refreshView());
			tryOpenPopup(popup);
		}
	}

	@Override
	public void editNewAudioPopup(UserAudio userAudio)
	{
		final AudioGalleryPopup popup = new AudioGalleryPopup(audioService, currentUser, userAudio,device, this);
		popup.setHeight("500px");
		popup.setWidth("500px");
		popup.addStyleName(CssStyle.GENERIC_POPUP.getStyleName());
		tryOpenPopup(popup);
	}


	@Override
	public void deleteAudio(UserAudio item)
	{
		MessageBox.show(I18N.CONFIRM_DELETE_AUDIO.msg(), MessageBox.MessageBoxButtons.YES_NO, dialogResult ->
		{
			if (MessageBox.DialogResult.YES.equals(dialogResult)) {
				audioService.deleteAudio(item);
				refreshView();
			}
		});
	}

	@Override
	public void playAudioPopup(UserAudio audio)
	{
         // TODO : add impl
	}

    @Override
    public void refreshView() {


		view.setUserAudioTableHandler(handler);
    }

    @Override
	public void editAudio(UserAudio userAudio) {

		editNewAudioPopup(userAudio);

	}


	@Override
	public void enter(String parameters) {

	}


	@Override
	public void performAfterUpload(UserAudio audio, Operation operation) {

		view.setUserAudioTableHandler(handler);
		switch (operation) {
			case EDIT:
				Notification.show("Edited Successfully", Type.TRAY_NOTIFICATION);
				break;
			case UPLOAD:
				Notification.show("Saved Successfully", Type.TRAY_NOTIFICATION);
				break;

		}

	}
}
