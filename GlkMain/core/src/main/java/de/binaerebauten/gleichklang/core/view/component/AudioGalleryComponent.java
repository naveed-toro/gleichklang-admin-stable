package de.binaerebauten.gleichklang.core.view.component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Audio;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

import de.binaerebauten.gleichklang.core.model.audio.UserAudio;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

/**
 * The media gallery component show the media galleries and
 * all medias in it. Also it is possible to see a preview of a media.
 * The component can be used for display only or also for
 * editing / removing media or media galleries.
 */
public class AudioGalleryComponent extends CustomComponent
{
	private static final Logger LOG = LoggerFactory.getLogger(AudioGalleryComponent.class);
	//@Value("${security.salt}") String filePath;



	public interface AddAudioListener
	{
		void addAudio();
	}

	public interface RemoveAudioListener
	{
		void removeAudio(UserAudio media);
	}

	public interface EditAudioListener
	{
		void editAudio(UserAudio media);
	}

	public interface PlayAudioListener
	{
		void playAudio(UserAudio media);
	}



	private AddAudioListener addAudioListener = null;
	private RemoveAudioListener removeAudioListener = null;
	private EditAudioListener editAudioListener = null;
	private PlayAudioListener playAudioListener = null;
	final CustomLazyBeanPagingComponent<UserAudio> pagingComponent;


	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy   HH:mm");
	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	public AudioGalleryComponent(Device device)
	{
		VerticalLayout baseLayout = new VerticalLayout();
		pagingComponent = createPagingComponent(device);
		baseLayout.addComponent(pagingComponent);
		baseLayout.setComponentAlignment(pagingComponent, Alignment.MIDDLE_CENTER);

		setCompositionRoot(baseLayout);

	}

	private CustomLazyBeanPagingComponent<UserAudio> createPagingComponent(Device device)
	{
		final CustomLazyBeanPagingComponent<UserAudio>
				pagingComponent = new CustomLazyBeanPagingComponent<>();

		pagingComponent.getAddNewButton().addClickListener((event ->  addAudioListener.addAudio()));

		pagingComponent.initView(ClientInformation.Device.getDefault());
		pagingComponent.setSizeFull();
		pagingComponent.setStyleName(CssStyle.RELATIONSHIP_TABLE.getStyleName());
		pagingComponent.addGeneratedColumn(itemId -> createAudioRow(device, itemId));
		return pagingComponent;
	}

	private Component createAudioRow(Device device, UserAudio audio)
	{
		final CssLayout wrapper = new CssLayout();
		wrapper.setWidth(100, Unit.PERCENTAGE);

		wrapper.setStyleName(CssStyle.RELATIONSHIP_MATCH_WRAPPER.getStyleName());
		// Play Icon
		final Component playicon = createAudioSection(device , audio);

		// User Info
		final Component audioInfo = createAudioInfoComponent(device, audio);

		wrapper.addComponents(playicon , audioInfo);
		if (device == ClientInformation.Device.MOBILE)
		{
			final VerticalLayout layout = new VerticalLayout();
			layout.addComponents(playicon , audioInfo);
			return layout;
		}

		return wrapper;
	}

	private Component createAudioInfoComponent(Device device, UserAudio audio)
	{
		final CssLayout wrapper = new CssLayout();
		wrapper.addStyleName(CssStyle.RELATIONSHIP_USERINFO_WRAPPER.getStyleName());

		// alias

		final Label fileName;
		fileName = new Label("Aufnahme vom:");
		fileName.addStyleName(CssStyle.RELATIONSHIP_USER_LABEL.getStyleName());
		fileName.setContentMode(ContentMode.HTML);
		wrapper.addComponent(fileName);

		// creation date
		final Label created = new Label(audio.getCreateDate().format(FORMATTER));
		created.addStyleName(CssStyle.RELATIONSHIP_DATE_LABEL.getStyleName());
		wrapper.addComponent(created);

		// Category

		//Add spaces
		Layout verticalspace = new VerticalLayout();
		verticalspace.setHeight("10px");
		wrapper.addComponent(verticalspace);

		// Symbols
		final CssLayout symbolRow = new CssLayout();
		symbolRow.addStyleName(CssStyle.RELATIONSHIP_SYMBOLS.getStyleName());
		if(audio.getFriendship()) {
			final Image image = new Image();
			image.setSource(RecommendationCategory.FRIENDSHIP.getIcon());
			image.setHeight("35px");
			image.setDescription(RecommendationCategory.FRIENDSHIP.msg());
			symbolRow.addComponent(image);
		}

		if(audio.getPartnership()) {
			final Image image = new Image();
			image.setSource(RecommendationCategory.PARTNERSHIP.getIcon());
			image.setHeight("35px");
			image.setDescription(RecommendationCategory.PARTNERSHIP.msg());
			symbolRow.addComponent(image);
		}

		wrapper.addComponent(symbolRow);

		//Add spaces
		Layout verticalspace2 = new VerticalLayout();
		verticalspace2.setHeight("10px");
		wrapper.addComponent(verticalspace2);

		// Buttons

		final CssLayout buttonRow = new CssLayout();
		buttonRow.addStyleName(CssStyle.RELATIONSHIP_DATE_LABEL.getStyleName());



		final Button editButton = new Button();
		editButton.setIcon(FontAwesome.PENCIL);
		editButton.setPrimaryStyleName(CssStyle.BTN_EDIT.getStyleName());
		editButton.setWidth("30px");
		editButton.setVisible(editAudioListener != null);
		//editButton.setStyleName(CssStyle.EDIT_BUTTON.getStyleName());

		buttonRow.addComponent(editButton);

		editButton.addClickListener(item->
		{
			if(editAudioListener != null)
				editAudioListener.editAudio(audio);
		});

		Layout horizontalspace = new HorizontalLayout();
		horizontalspace.setWidth("10px");
		buttonRow.addComponent(horizontalspace);

		final Button deleteButton = new Button();

		deleteButton.setIcon(FontAwesome.TRASH_O);
		deleteButton.setPrimaryStyleName(CssStyle.BTN_REMOVE.getStyleName());
		deleteButton.setWidth("30px");
		deleteButton.setVisible(removeAudioListener != null);
		//deleteButton.setStyleName(CssStyle.EDIT_BUTTON.getStyleName());

		buttonRow.addComponent(deleteButton);

		deleteButton.addClickListener(item->
				{
					if(removeAudioListener != null)
						removeAudioListener.removeAudio(audio);
				}
		);

		wrapper.addComponent(buttonRow);

		return wrapper;
	}

	private Component createAudioSection(Device device ,UserAudio audio)
	{
		final CssLayout wrapper = new CssLayout();
		wrapper.addStyleName(CssStyle.RELATIONSHIP_AVATAR_WRAPPER.getStyleName());
		final Image image = new Image();
		image.addClickListener((clickEvent) -> {
			playAudioListener.playAudio(audio);
		});
		wrapper.addComponent(image);
		String path = audio.getPath();
		if (device == Device.DESKTOP) {
			Audio audiofile = new Audio();
			audiofile.setVisible(true);
			audiofile.setSource(new FileResource(new File(audio.getPath())));
			wrapper.addComponent(audiofile);
		}
		else {
			try {
				Label audioLabel = new Label();
				byte[] audioBytes = Files.readAllBytes(new File(path).toPath());
				audioLabel.setContentMode(ContentMode.HTML);
				audioLabel.setValue("<audio controls src=\"data:audio/mp3;base64," + new String(Base64.getEncoder().encode(audioBytes)) + "\" type=\"audio/mpeg\"></audio>");
				wrapper.addComponent(audioLabel);
			} catch (IOException e) {
				LOG.error("createAudioSection", e);
			}
		}
		return wrapper;
	}


	private Component createAddAudioComponent()
	{
		Layout layout = new VerticalLayout();

		final Button addGallery = new Button();
		//addGallery.setIcon(FontAwesome.PLUS_CIRCLE);
		addGallery.addClickListener(event ->  addAudioListener.addAudio());
		addGallery.setHeight("50px");

		addGallery.setWidth("300px");

		layout.addComponent(addGallery);
		((VerticalLayout) layout).setComponentAlignment(addGallery, Alignment.MIDDLE_CENTER);
		return addGallery;
	}

	public void setAddAudioListener(AddAudioListener addAudioListener)
	{
		this.addAudioListener = addAudioListener;
	}

	public void setRemoveAudioListener(RemoveAudioListener removeAudioListener)
	{
		this.removeAudioListener = removeAudioListener;
	}
	public void setEditAudioListener(EditAudioListener editAudioListener)
	{
		this.editAudioListener = editAudioListener;
	}

	public void setPlayAudioListener(PlayAudioListener playAudioListener)
	{
		this.playAudioListener = playAudioListener;
	}

	public AddAudioListener getAddAudioListener() {
		return addAudioListener;
	}


	public void setUserAudioTableHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<UserAudio> handler)
	{
		pagingComponent.setHandler(handler);
	}

}
