package de.binaerebauten.gleichklang.memberweb.view.popup;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.service.file.AvatarUploadFile;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;
import de.binaerebauten.gleichklang.core.view.component.MediaGalleryComponent;
import de.binaerebauten.gleichklang.core.view.component.UploadComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.view.I18N;

import java.util.*;

/* TODO Refactoring notwendig!!!!! */
public class AvatarPopup extends GenericPopup
{
	public interface AvatarCallback
	{
		void saveAvatars(Collection<AvatarUploadFile> avatars);
		
		List<MediaUploadFile> getMedias(MediaGallery mediaGallery);
		
		Map<MediaGallery, MediaUploadFile> getMediaGalleries();
	}

	public interface  SaveAvatarFromGalleryCallback
	{
		void saveAvatarFromMediaGallery(MediaUploadFile mediaUploadFile, RecommendationCategory recommendationCategory);
	}

	public interface SaveAvatarsAsMediaCallback
	{
		void save(AvatarUploadFile avatar);
	}

	private final ComboBox categoryComboBox;
	private final ComponentContainer avatarUpload;
	private final Map<RecommendationCategory, AvatarUploadFile> uploadFiles;

	private final MediaGalleryComponent mediaGalleryComponent;

	private final AvatarCallback avatarCallback;
	private final SaveAvatarFromGalleryCallback saveAvatarFromGalleryCallback;
	private final SaveAvatarsAsMediaCallback saveAvatarsAsMediaCallback;

	private final Button backToSelectMedia;
	private final Label existingAvatarInfoText;

	private final Map<RecommendationCategory, MediaUploadFile> avatarsFromMediasToSave;

	public AvatarPopup(Map<RecommendationCategory, AvatarUploadFile> uploadFiles, AvatarCallback avatarCallback,
					   SaveAvatarFromGalleryCallback saveAvatarFromGalleryCallback, Collection<RecommendationCategory> categories, SaveAvatarsAsMediaCallback saveAvatarsAsMediaCallback)
	{
        setCaption(I18N.AVATAR_POPUP_CAPTION.msg());
		addStyleName(CssStyle.AVATAR_POPUP.getStyleName());

        this.avatarCallback = avatarCallback;
        this.uploadFiles = uploadFiles;
		this.saveAvatarFromGalleryCallback = saveAvatarFromGalleryCallback;
		this.saveAvatarsAsMediaCallback = saveAvatarsAsMediaCallback;
		this.avatarsFromMediasToSave = new HashMap<>();
		
		this.mediaGalleryComponent = new MediaGalleryComponent();
		this.mediaGalleryComponent.setOnMediaGalleryClickListener(this::gotoMediaList);
		this.mediaGalleryComponent.setOnMediaSelectListener(this::setMediaAsAvatar);
		this.mediaGalleryComponent.setBackToMediaGalleriesListener(this::backToMediaGalleries);
		this.mediaGalleryComponent.setMediaGalleryList(avatarCallback.getMediaGalleries());
		this.mediaGalleryComponent.setAutoPreview(false);
		this.mediaGalleryComponent.setVisible(false);

		this.existingAvatarInfoText = new Label();
		existingAvatarInfoText.setValue(I18N.AVATAR_INFOTEXT.msg());
		existingAvatarInfoText.setVisible(true);

        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setWidth(100, Unit.PERCENTAGE);

		layout.addComponent(existingAvatarInfoText);

        final VerticalLayout componentLayout = new VerticalLayout();
        componentLayout.setSizeFull();
        componentLayout.setSpacing(true);

        avatarUpload = new CssLayout();
		avatarUpload.setStyleName(CssStyle.AVATAR_UPLOAD.getStyleName());

		final HorizontalLayout comboboxWrapper = new HorizontalLayout();
		comboboxWrapper.setSizeFull();
		comboboxWrapper.addStyleName(CssStyle.RELATIONSHIP_CATEGORY_FILTER_CONTROL.getStyleName());

        categoryComboBox = createCategoryComboBox(categories);

		comboboxWrapper.addComponent(categoryComboBox);

		componentLayout.addComponent(comboboxWrapper);

		componentLayout.addComponent(avatarUpload);
		componentLayout.addComponent(mediaGalleryComponent);

        layout.addComponent(componentLayout);

		backToSelectMedia = new Button(I18N.AVATAR_BACK_BUTTON.msg());
		backToSelectMedia.setVisible(false);

        final Component controlButtons = createControlButtons();

		addFooterComponent(controlButtons);

		setPopupContent(layout);
	}

	private Component createControlButtons()
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		final Button saveButton = new Button(I18N.BUTTON_SAVE.msg());
		saveButton.setIcon(FontAwesome.SAVE);
		saveButton.addClickListener(event ->
		{
			if (!avatarsFromMediasToSave.isEmpty())
			{
				Iterator it = avatarsFromMediasToSave.entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry pair = (Map.Entry)it.next();
					saveAvatarFromGalleryCallback.saveAvatarFromMediaGallery((MediaUploadFile) pair.getValue(), (RecommendationCategory) pair.getKey());
				}
			}
			else
				saveAvatars();

			this.close();
		});

		final Button cancelButton = new Button(I18N.BUTTON_CANCEL.msg(), event -> close());
		cancelButton.setIcon(FontAwesome.TIMES);

		backToSelectMedia.addClickListener((event) ->
		{
			mediaGalleryComponent.setVisible(false);
			avatarUpload.setVisible(true);
			backToSelectMedia.setVisible(false);
		});


		layout.addComponents(backToSelectMedia, saveButton, cancelButton);
        layout.setComponentAlignment(backToSelectMedia, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(saveButton, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(cancelButton, Alignment.MIDDLE_CENTER);

		return layout;
	}

	private void saveAvatars()
	{
		avatarCallback.saveAvatars(uploadFiles.values());
		close();
	}

	private void setMediaAsAvatar(MediaUploadFile mediaAsAvatar)
	{
		avatarsFromMediasToSave.putIfAbsent((RecommendationCategory) categoryComboBox.getValue(), mediaAsAvatar);
		avatarUpload.removeAllComponents();
		avatarUpload.addComponents(createAvatarFromMediaUploadFile(mediaAsAvatar));
		avatarUpload.setVisible(true);
		mediaGalleryComponent.setVisible(false);
	}

	private Component createAvatarFromMediaUploadFile(MediaUploadFile mediaUploadFile)
	{
		final VerticalLayout layout = new VerticalLayout();
        layout.addStyleName(CssStyle.NEW_MEDIA_AVATAR.getStyleName());

		final Image image = new Image();

		if (mediaUploadFile.getPath() != null)
			image.setSource(getImageResource(mediaUploadFile));

		final Button removeButton = new Button();
		removeButton.setIcon(FontAwesome.TRASH_O);
		removeButton.setStyleName(CssStyle.BTN_REMOVE_AVATAR.getStyleName());
		removeButton.addClickListener((event) ->
		{
			avatarsFromMediasToSave.remove(categoryComboBox.getValue());
			avatarUpload.removeAllComponents();
			final Component newAvatarUpload = createAvatarUpload((RecommendationCategory) categoryComboBox.getValue());
			avatarUpload.addComponents(newAvatarUpload);
			backToMediaGalleries();
			backToSelectMedia.setVisible(false);
			existingAvatarInfoText.setVisible(false);
		});

		layout.addComponents(image, removeButton);
		layout.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(removeButton, Alignment.BOTTOM_CENTER);

		return layout;
	}

	private Component createAvatarUpload(RecommendationCategory category)
	{
		if (category == null) return null;

		final AvatarUploadFile avatarUploadFile = uploadFiles.get(category);

		final HorizontalLayout wrapper = new HorizontalLayout();

		final Button selectFromMediaGalleries = new Button();
		selectFromMediaGalleries.setStyleName(CssStyle.SELECT_FROM_GALLERY_BTN.getStyleName());
		selectFromMediaGalleries.setCaption(I18N.AVATAR_SELECT_FROM_GALLERY.msg());
		selectFromMediaGalleries.setIcon(FontAwesome.CAMERA);
		selectFromMediaGalleries.addClickListener((event) ->
		{
			avatarUpload.setVisible(false);
			mediaGalleryComponent.setVisible(true);
			backToSelectMedia.setVisible(true);
		});

		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.UPLOAD_WRAPPER.getStyleName());

		final VerticalLayout layout2 = new VerticalLayout();
		layout.setStyleName(CssStyle.SELECT_FROM_GALLERY_WRAPPER.getStyleName());


		final Image image = new Image();

		final Label text = new Label(I18N.AVATAR_NEW.msg());
		
		final UploadComponent<AvatarUploadFile> uploadComponent = new UploadComponent<>(I18N.AVATAR_POPUP_CAPTION.msg(), "", avatarUploadFile);
		uploadComponent.setStyleName(CssStyle.UPLOAD_COMPONENT.getStyleName());

		final Button removeButton = new Button();
		removeButton.setIcon(FontAwesome.TRASH_O);
		removeButton.setStyleName(CssStyle.BTN_REMOVE_AVATAR.getStyleName());

		uploadComponent.setUploadFileChangedListener((changedUploadFile, uploadResult) ->
		{
			switch (uploadResult)
			{
				case VIRUS:
					Notification.show(I18N.POPUP_VIRUS.msg(), Type.ERROR_MESSAGE);
					break;
				case FAILED:
					Notification.show(I18N.POPUP_FAILED.msg(), Type.ERROR_MESSAGE);
					break;
				case SUCCESS:
					//save only direct upload files in avatar gallery
					if (saveAvatarsAsMediaCallback != null)
						saveAvatarsAsMediaCallback.save((AvatarUploadFile) changedUploadFile);

					existingAvatarInfoText.setVisible(false);
					break;
			}

			if (avatarUploadFile.getPath() != null)
				image.setSource(getImageResource(avatarUploadFile));

			removeButton.setVisible(avatarUploadFile.getPath() != null);
			uploadComponent.setVisible(avatarUploadFile.getPath() == null);
			wrapper.addStyleName(avatarUploadFile.getPath() == null ? CssStyle.NEW_AVATAR.getStyleName() : CssStyle.EXISTING_AVATAR.getStyleName());
			wrapper.removeStyleName(avatarUploadFile.getPath() != null ? CssStyle.NEW_AVATAR.getStyleName() : CssStyle.EXISTING_AVATAR.getStyleName());
			selectFromMediaGalleries.setVisible(avatarUploadFile.getPath() == null);
			text.setVisible(avatarUploadFile.getPath() == null);
		});
		
		removeButton.addClickListener(event ->
		{
			avatarUploadFile.setDeleted();
			image.setSource(null);
			removeButton.setVisible(false);
			wrapper.addStyleName(avatarUploadFile.getPath() == null ? CssStyle.NEW_AVATAR.getStyleName() : CssStyle.EXISTING_AVATAR.getStyleName());
			wrapper.removeStyleName(avatarUploadFile.getPath() != null ? CssStyle.NEW_AVATAR.getStyleName() : CssStyle.EXISTING_AVATAR.getStyleName());
			selectFromMediaGalleries.setVisible(true);
			uploadComponent.setVisible(true);
			text.setVisible(true);
			existingAvatarInfoText.setVisible(false);
		});

		if (avatarUploadFile.getPath() != null)
			image.setSource(getImageResource(avatarUploadFile));

		existingAvatarInfoText.setVisible(avatarUploadFile.getPath() != null);
		removeButton.setVisible(avatarUploadFile.getPath() != null);
		uploadComponent.setVisible(avatarUploadFile.getPath() == null);
		selectFromMediaGalleries.setVisible(avatarUploadFile.getPath() == null);
		wrapper.addStyleName(avatarUploadFile.getPath() == null ? CssStyle.NEW_AVATAR.getStyleName() : CssStyle.EXISTING_AVATAR.getStyleName());
		text.setVisible(avatarUploadFile.getPath() == null);
		
		layout.addComponents(image, uploadComponent, text, removeButton);
		layout.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(removeButton, Alignment.BOTTOM_CENTER);

		layout2.addComponent(selectFromMediaGalleries);
		layout2.setComponentAlignment(selectFromMediaGalleries, Alignment.MIDDLE_CENTER);

		wrapper.addComponents(uploadComponent.createDragAndDrop(layout), layout2);

		return wrapper;
	}

	private Resource getImageResource(AvatarUploadFile avatarUploadFile)
	{
		return new FileResource(avatarUploadFile.getPath().toFile());
	}

	private ComboBox createCategoryComboBox(Collection<RecommendationCategory> categories)
	{
		Objects.requireNonNull(categories);

		final ComboBox comboBox = ComponentFactory.getInstance().createField(ComboBox.class);
		comboBox.setContainerDataSource(new BeanItemContainer<>(RecommendationCategory.class, uploadFiles.keySet()));
		comboBox.setCaption(I18N.AVATAR_COMBOBOX_CATEGORY.msg());
		comboBox.setTextInputAllowed(false);
		comboBox.setItemStyleGenerator((ComboBox.ItemStyleGenerator) (source, itemId) -> CssStyle.TABSHEET_DROPDOWN_POPUP_ITEMS.name());

		for (RecommendationCategory category : categories)
		{
			comboBox.addItem(category);
			comboBox.setItemCaption(category, category.getName());
			comboBox.setItemIcon(category, category.getIcon());
		}

		comboBox.addValueChangeListener(event -> refreshAvatarUpload(comboBox));
		comboBox.setValue(uploadFiles.keySet().iterator().next());
		comboBox.setEnabled(categories.size() > 1);


		return comboBox;
	}

	public void selectCategory(RecommendationCategory recommendationCategory)
	{
		categoryComboBox.setValue(recommendationCategory);
		refreshAvatarUpload(categoryComboBox);
	}

	private void refreshAvatarUpload(ComboBox comboBox)
	{
		avatarUpload.removeAllComponents();

		final Component newAvatarUpload = createAvatarUpload((RecommendationCategory) comboBox.getValue());

		avatarUpload.addComponents(newAvatarUpload);
	}

	private Resource getImageResource(MediaUploadFile mediaUploadFile)
	{
		if (mediaUploadFile == null || mediaUploadFile.getPath() == null)
			return null;

		return new FileResource(mediaUploadFile.getPath().toFile());
	}

	private void gotoMediaList(MediaGallery mediaGallery)
	{
		final List<MediaUploadFile> medias = avatarCallback.getMedias(mediaGallery);
		
		mediaGalleryComponent.setMediaList(mediaGallery, medias);
		backToSelectMedia.setVisible(false);
	}

	private void backToMediaGalleries()
	{
		mediaGalleryComponent.setMediaGalleryList(avatarCallback.getMediaGalleries());
		backToSelectMedia.setVisible(true);
	}
}
