package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.file.AudioService;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.view.AudioUploader;
import java.util.ArrayList;
import java.util.List;
import de.binaerebauten.gleichklang.memberweb.view.I18N;
import de.binaerebauten.gleichklang.memberweb.view.component.GenericViewHeader;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.StreamResource;

@JavaScript({  "vaadin://js/WebAudioRecorder.min.js", "vaadin://js/app.js","vaadin://js/WebAudioRecorderMp3.min.js","vaadin://js/WebAudioRecorderOgg.min.js","vaadin://js/WebAudioRecorderWav.min.js","vaadin://js/jquery.min.js" })
public class AudioGalleryPopup extends GenericPopup
{

	public interface AudioGalleryPopupCallback
	{

		enum Operation
		{
			UPLOAD,
			EDIT
		}
		void performAfterUpload(UserAudio audio, Operation operation);

	}

	public AudioGalleryPopupCallback callback;
	public   String filePath;
	public User user;
	public AudioService service;
	public final VerticalLayout mainLayout = new VerticalLayout();
	public final HorizontalLayout horizontalLayout = new HorizontalLayout();
	public final HorizontalLayout buttonSection = new HorizontalLayout();
	public final HorizontalLayout messageLayout = new HorizontalLayout();
	public CheckBox friendship = new CheckBox(I18N.CATEGORY_FRIENDSHIP.msg());
	public CheckBox partnership = new CheckBox(I18N.CATEGORY_PARTNERSHIP.msg());
	public Upload upload = new Upload();


	public boolean audioForFriendshipAdded = false,audioForPartnershipAdded = false;
	public List<UserAudio> fileList;

	public AudioGalleryPopup(Device device, User user, AudioService audioService, AudioGalleryPopupCallback callback)
	{
		this.callback = callback;
		setWidth("500px");
		setHeight("300px");
		setCaption(I18N.ADD_NEW_AUDIO.msg());
		setIcon(FontAwesome.FILE_AUDIO_O);
		final VerticalLayout layout = new VerticalLayout();
		createResponsiveLayout(device);
		this.setStyleName(CssStyle.MEDIA_GALLERY_POPUP.getStyleName());
		this.user = user;
		this.service = audioService;
		this.filePath = audioService.getAudioFilePath();
		this.audioForFriendshipAdded = service.isAudioPresentForFriendship(user);
		this.audioForPartnershipAdded = service.isAudioPresentForPartnership(user);
		this.fileList = service.getFileList(user);

		final TabSheet tabSheet = new TabSheet();
		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent selectedTabChangeEvent) {
				TabSheet tabsheet = selectedTabChangeEvent.getTabSheet();
				Layout tab = (Layout) tabsheet.getSelectedTab();
				String caption = tabsheet.getTab(tab).getCaption();
				tab.removeAllComponents();
				// Fill the tab content

				if (caption.equals(I18N.Record_Audios.msg())) {
					tab.addComponent(createAudioRecord());
				}
				else if (caption.equals(I18N.Record_Audio.msg())){
					tab.addComponent(createAudioRecord());
				}
				else {
					tab.addComponent(createAudioGalleryComponent(audioService , user));
				}
			}
		});
		if (device == Device.MOBILE) {
			tabSheet.addTab(new VerticalLayout(), I18N.Upload_Audios.msg());
			tabSheet.addTab(new VerticalLayout(), I18N.Record_Audios.msg());
		}
		else if (device == Device.DESKTOP){
			tabSheet.addTab(new VerticalLayout(), I18N.Upload_Audio.msg());
			tabSheet.addTab(new VerticalLayout(), I18N.Record_Audio.msg());
		}
		setPopupContent(tabSheet);
	}

	public AudioGalleryPopup(AudioService audioService, User user, UserAudio userAudio, Device device, AudioGalleryPopupCallback callback)
	{
		setCaption(I18N.EDIT_AUDIO_GALLARY.msg());
		this.callback = callback;
		setIcon(FontAwesome.FILE_AUDIO_O);
		final VerticalLayout layout = new VerticalLayout();
		this.service = audioService;
		this.user = user;
		this.audioForFriendshipAdded = service.isAudioPresentForFriendship(user);
		this.audioForPartnershipAdded = service.isAudioPresentForPartnership(user);

		final Component mediaForm = createEditGalleryComponent(userAudio);
		layout.addComponent(mediaForm);
		layout.setComponentAlignment(mediaForm, Alignment.MIDDLE_CENTER);
		createResponsiveLayout(device);
		this.setStyleName(CssStyle.MEDIA_GALLERY_POPUP.getStyleName());
		setPopupContent(layout);
	}

	private Component createEditGalleryComponent(UserAudio userAudio)
	{
		final VerticalLayout mainLayout = new VerticalLayout();
		final HorizontalLayout horizontalLayout = new HorizontalLayout();
		HorizontalLayout horizontalLayoutButton = new HorizontalLayout();


		final HorizontalLayout categorySection = new HorizontalLayout();
		if(user.getCategories().contains(RecommendationCategory.FRIENDSHIP)){
			categorySection.addComponent(friendship);
			//if(service.isAudioPresentForFriendship(user)){
			if(userAudio.getFriendship()){
				friendship.setValue(true);
			}
		}

		if(user.getCategories().contains(RecommendationCategory.PARTNERSHIP)){
			categorySection.addComponent(partnership);
			//if(service.isAudioPresentForPartnership(user)){
			if(userAudio.getPartnership()){
				partnership.setValue(true);
			}
		}

		mainLayout.addComponent(categorySection);
        Button savebutton  = new Button(I18N.SAVE.msg());
        Button cancelButton = new Button(I18N.CANCEL.msg());

        savebutton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                if(!friendship.getValue() && !partnership.getValue()){
                    MessageBox.show(I18N.SELECT_CATEGORY.msg());
                    return;
                }

                if((audioForFriendshipAdded && friendship.getValue()) && (audioForPartnershipAdded && partnership.getValue()))
				{
					MessageBox.show(I18N.AUDIO_ALREADY_ADDED_FOR_CATEGORIES.msg());
					return;
				}
				if(service.getUserAudios(userAudio.getAuthor().getId()).size()==2){
                	MessageBox.show(I18N.AUDIO_ALREADY_ADDED_FOR_CATEGORIES.msg());
					return;
				}

				userAudio.setPartnership(partnership.getValue());
                userAudio.setFriendship(friendship.getValue());
				service.updateAudio(userAudio);
				callback.performAfterUpload(userAudio,  AudioGalleryPopupCallback.Operation.EDIT);
				close();

            }

        });
		cancelButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				close();
			}
		});
		horizontalLayoutButton.addComponent(savebutton);
		horizontalLayout.setSpacing(true);
		HorizontalLayout spacer = new HorizontalLayout();
		spacer.setWidth("10px");
		horizontalLayoutButton.addComponent(spacer);
		horizontalLayoutButton.addComponent(cancelButton);
		mainLayout.setSpacing(true);
		mainLayout.addComponent(horizontalLayoutButton);
		return mainLayout;
	}

	private Component createAudioGalleryComponent(AudioService audioService, User user)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(createUploadHeader());
		this.user = user;
		HorizontalLayout uploadLayout = new HorizontalLayout();
		HorizontalLayout spacerLayout = new HorizontalLayout();
		VerticalLayout tabLayout = new VerticalLayout();
		VerticalLayout space = new VerticalLayout();
		space.setHeight("30px");
		spacerLayout.setWidth(50, Unit.PIXELS);
		AudioUploader receiver = new AudioUploader( this );
		upload = new Upload();
		upload.setButtonCaption(I18N.START_UPLOAD.msg());
		//label.setStyleName();
		upload.setReceiver(receiver);
		upload.addSucceededListener(receiver);
		upload.setImmediate(true);
		upload.setHeight("50px");
		upload.setEnabled(true);
		HorizontalLayout categorySection = createCheckbox();
		uploadLayout.addComponent(upload);
		uploadLayout.addComponent(spacerLayout);
		uploadLayout.addComponent(buttonSection);
		uploadLayout.setComponentAlignment(buttonSection, Alignment.TOP_CENTER);
		tabLayout.addComponent(layout);
		tabLayout.addComponent(categorySection);
		tabLayout.addComponent(messageLayout);
		tabLayout.addComponent(space);
		tabLayout.addComponent(uploadLayout);
		return tabLayout;
	}

	private Component createUploadHeader()
	{
		final GenericViewHeader header = new GenericViewHeader();
		header.addStyleName(CssStyle.GENERIC_HEADER_BLUE.getStyleName());
		header.setDescription(I18N.AUDIOGALLERY_HEADER_UPLOAD_DESCRIPTION.msg(service.getConverter()));
		return header;
	}

	private Component createRecordHeader()
	{
		final GenericViewHeader header = new GenericViewHeader();
		header.addStyleName(CssStyle.GENERIC_HEADER_BLUE.getStyleName());
		header.setDescription(I18N.AUDIOGALLERY_HEADER_RECORD_DESCRIPTION.msg());
		return header;
	}

	private HorizontalLayout createCheckbox(){
		HorizontalLayout categorySection = new HorizontalLayout();
		List<CheckBox> checkboxCategory = new ArrayList<>();
		if (service.isSelectionAllowedForFriendship(user)) {
			categorySection.addComponent(friendship);
			checkboxCategory.add(friendship);
		}
		categorySection.setMargin(Boolean.parseBoolean("10px"));
		if (service.isSelectionAllowedForPartnership(user)) {
			categorySection.addComponent(partnership);
			checkboxCategory.add(partnership);
		}

		if (checkboxCategory.size() == 1) {
			checkboxCategory.get(0).setValue(true);
		}
		return categorySection;
	}

	private Component createAudioRecord(){
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(createRecordHeader());
		final VerticalLayout subLayout = new VerticalLayout();
		final VerticalLayout tabLayout = new VerticalLayout();
		HorizontalLayout categorySection = createCheckbox();
		Long userId = user.getId();
		String id = "audio_" + Math.random();
		subLayout.setId(id);
		subLayout.setHeight("250px");
		StringBuilder script = new StringBuilder();
		// @formatter:off
		script
				.append("try {")
				.append(" if(document.getElementById('" + id + "')) { document.getElementById('" + id + "').innerHTML = '")
				.append("<div id=\"controls\">")
				.append("    <button id=\"recordButton\">"+I18N.Record.msg()+"</button>")
				.append("    <button id=\"stopButton\" disabled>"+I18N.Stop.msg()+"</button>")
				.append("<input type=\"hidden\" value="+userId+" id=record>")
				.append("<input type=\"hidden\" value="+service.getServerPath()+" id=server>")
				.append("</div>")
//				.append("<div id=\"formats\"></div>")
				.append("<div id=\"image\"></div>")
				.append("<ol id=\"recordingsList\"></ol>';")
				.append("var script = document.createElement('script');")
				.append("script.type = 'text/javascript';")
				.append("script.setAttribute('src', 'VAADIN/js/app.js');")
				.append("document.body.appendChild(script);")
				.append("script.setAttribute('src', 'VAADIN/js/WebAudioRecorder.min.js');")
				.append("document.body.appendChild(script);")
				.append("script.setAttribute('src', 'VAADIN/js/WebAudioRecorderOgg.min.js');")
				.append("document.body.appendChild(script);")
				.append("script.setAttribute('src', 'VAADIN/js/WebAudioRecorderMp3.min.js');")
				.append("document.body.appendChild(script);")
				.append("script.setAttribute('src', 'VAADIN/js/WebAudioRecorderWav.min.js');")
				.append("document.body.appendChild(script);}")
				.append("} catch (e) {")
				.append("	alert(e);")
				.append("}")
				.append("");
		com.vaadin.ui.JavaScript.getCurrent().execute(script.toString());
		tabLayout.addComponent(layout);
		tabLayout.addComponent(categorySection);
		tabLayout.addComponent(new VerticalLayout(new Label(" ")));
		tabLayout.addComponent(subLayout);
		return tabLayout;
	}

	@Override
	public void onDeviceChanged(Device device)
	{
		createResponsiveLayout(device);
	}

	private void createResponsiveLayout(Device device)
	{
		if (device == Device.DESKTOP)
			createDesktopView();
		else if (device == Device.TABLET)
			createTabletView();
		else if (device == Device.MOBILE)
			createMobileView();

	}

	private void createMobileView()
	{
		this.setWidth("95%");
		this.setHeight("80%");
		this.center();
	}

	private void createTabletView()
	{
		this.setWidth("70%");
		this.setHeight("80%");
		this.center();
	}

	private void createDesktopView()
	{
		this.setWidth("50%");
		this.setHeight("80%");
		this.center();
	}

}
