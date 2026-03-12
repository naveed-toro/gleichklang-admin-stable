package de.binaerebauten.gleichklang.memberweb.view;


import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.view.AbstractNavigateView;
import de.binaerebauten.gleichklang.core.view.component.*;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory.MemberMenuItem;
import de.binaerebauten.gleichklang.memberweb.view.component.GenericViewHeader;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;

public class AudioViewImpl extends AbstractNavigateView<AudioView.AudioViewListener> implements AudioView
{
	private final AudioGalleryComponent audioGalleryComponent;

	public AudioViewImpl(Device device)
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(createHeader());

		audioGalleryComponent = createAudioGalleryComponent(device);
		audioGalleryComponent.setStyleName(CssStyle.MEDIA_GALLERY_WRAPPER.getStyleName());
		layout.addComponent(audioGalleryComponent);

		layout.setComponentAlignment(audioGalleryComponent, Alignment.TOP_CENTER);

		setCompositionRoot(layout);
	}

	private AudioGalleryComponent createAudioGalleryComponent(Device device)
	{
		final AudioGalleryComponent audioGalleryComponent = new AudioGalleryComponent(device);
		audioGalleryComponent.setAddAudioListener(() -> fireEvent(AudioViewListener::addNewAudioPopup));
		audioGalleryComponent.setRemoveAudioListener(item -> fireEvent(eventAction -> eventAction.deleteAudio(item)));
		audioGalleryComponent.setEditAudioListener(item -> fireEvent(eventAction -> eventAction.editAudio(item)));
		audioGalleryComponent.setPlayAudioListener(item -> fireEvent(eventAction -> eventAction.playAudioPopup(item)));
		return audioGalleryComponent;
	}



	private Component createHeader()
	{
		final GenericViewHeader header = new GenericViewHeader();
		header.addStyleName(CssStyle.GENERIC_HEADER_BLUE.getStyleName());
		header.setCaption("AUDIO");
		header.setDescription(I18N.AUDIOGALLERY_HEADER_DESCRIPTION.msg());
		//header.setIcon(new ThemeResource("img/audio-icon.png"));
		header.setIcon(new ThemeResource("img/volume-audio-svgrepo-com.svg"));

		//header.setIcon(FontAwesome.VOLUME_UP);

		return header;

	}

	private Component createAudioRootComponent()
	{
		VerticalLayout mainLayout = new VerticalLayout();
		TableControl<UserAudio> audioTableControl ;
		final LazyBeanTable<UserAudio> table = new LazyBeanTable<>();
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setSizeFull();
		table.addGeneratedColumn("Title", (source, itemId, columnId) -> itemId.getName() );
		table.addGeneratedColumn("Added Date", (source, itemId, columnId) -> itemId.getCreateDate() );
		//table.addGeneratedColumn("For Partnership", (source, itemId, columnId) -> itemId.getVisibleCategory() );
		table.addGeneratedColumn("For FriendShip", (source, itemId, columnId) -> itemId.getAuthor() );
		audioTableControl = new TableControl<>(table);


		audioTableControl.setNewCallback(() -> fireEvent(eventAction -> {eventAction.addNewAudioPopup(); table.refresh();}));
		audioTableControl.setEditCallback(item -> fireEvent(eventAction -> {eventAction.editAudio(item); table.refresh(); }) );
		audioTableControl.setDeleteCallback(item -> fireEvent(eventAction -> {eventAction.deleteAudio(item); table.refresh(); }) );
		audioTableControl.setSizeFull();
		audioTableControl.setButtonCaptions("Upload New Audio","Edit Audio","Delete Audio");
		mainLayout.addComponent(audioTableControl);
		mainLayout.setComponentAlignment(audioTableControl, Alignment.MIDDLE_CENTER);

		return mainLayout;
	}

//	@Override
//	public void onDeviceChanged(Device device)
//	{
//		super.onDeviceChanged(device);
//
//		audioGalleryComponent.onDeviceChanged(device);
//	}

	@Override
	public void setUserAudioTableHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<UserAudio> handler) {
		audioGalleryComponent.setUserAudioTableHandler(handler);
	}
}
