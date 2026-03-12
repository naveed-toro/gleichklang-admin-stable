package de.binaerebauten.gleichklang.core.view.component;

import com.google.common.base.Strings;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The media gallery component show the media galleries and
 * all medias in it. Also it is possible to see a preview of a media.
 * The component can be used for display only or also for
 * editing / removing media or media galleries.
 */
public class MediaGalleryComponent extends CustomComponent
{
	public interface AddMediaGalleryListener
	{
		void addMediaGallery();
	}

	public interface EditMediaGalleryListener
	{
		void editMediaGallery(MediaGallery mediaGallery);
	}

	public interface RemoveMediaGalleryListener
	{
		void removeMediaGallery(MediaGallery mediaGallery);
	}

	public interface AddMediaListener
	{
		void addMedia(MediaGallery mediaGallery);
	}

	public interface RemoveMediaListener
	{
		void removeMedia(MediaUploadFile media);
	}

	public interface EditMediaListener
	{
		void editMedia(MediaUploadFile media);
	}


	public interface BackToMediaGalleriesListener
	{
		void backToMediaGallery();
	}

	public interface OnMediaGalleryClickListener
	{
		void onMediaGalleryClicked(MediaGallery mediaGallery);
	}

	public interface OnMediaClickListener
	{
		void onMediaClicked(Map<MediaUploadFile, List<MediaUploadFile>> medias);
	}

	public interface OnMediaSelectListener
	{
		void onMediaSelected(MediaUploadFile mediaUploadFile);
	}

	private AddMediaGalleryListener addMediaGalleryListener = null;
	private RemoveMediaGalleryListener removeMediaGalleryListener = null;
	private EditMediaGalleryListener editMediaGalleryListener = null;
	private AddMediaListener addMediaListener = null;
	private RemoveMediaListener removeMediaListener = null;
	private EditMediaListener editMediaListener = null;
	private BackToMediaGalleriesListener backToMediaGalleriesListener = null;
	private OnMediaGalleryClickListener onMediaGalleryClickListener = null;
	private OnMediaClickListener onMediaClickListener = null;
	private OnMediaSelectListener onMediaSelectListener = null;

	private boolean autoPreview = true;
	private final Label galleryMainLabel = new Label();

	private HorizontalLayout addMediaContainer;

	private List<MediaUploadFile> mediaUploadFiles;
	private List<Avatar> avatars;

	public MediaGalleryComponent()
	{
        galleryMainLabel.setVisible(false);
	}


	/**
	 * Create special icon to add new mediaGallery
	 *
	 * @return
	 */
	private Component createAddGalleryComponent()
	{
		final VerticalLayout layout = new VerticalLayout();

		if (addMediaGalleryListener == null) return layout;

		layout.setWidth("225px");
		layout.setHeight("225px");
		layout.setMargin(true);
		layout.setStyleName(CssStyle.BUTTON_ADD.getStyleName());

		final Button addGallery = new Button();
		addGallery.setIcon(FontAwesome.PLUS_CIRCLE);
		addGallery.addClickListener(event ->  addMediaGalleryListener.addMediaGallery());
		addGallery.setSizeUndefined();

		final Label label = new Label(I18N.MEDIAGALLERY_NEWGALLERY.msg());
		label.setSizeUndefined();

		layout.addComponents(addGallery, label);
		layout.setComponentAlignment(addGallery, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

		return layout;
	}

	/**
	 * Create special icon to add new media.
	 *
	 * @return
	 */
	private Component createAddMediaComponent()
	{
		addMediaContainer = new HorizontalLayout();
		addMediaContainer.setStyleName(CssStyle.ADD_MEDIA.getStyleName());

		if (addMediaListener == null) return addMediaContainer;

		addMediaContainer.setStyleName(CssStyle.BUTTON_ADD.getStyleName());
		addMediaContainer.setWidth("225px");
		addMediaContainer.setHeight("225px");
		addMediaContainer.setMargin(true);

		final Label label = new Label(I18N.MEDIAGALLERY_NEWPICTURE.msg());
		addMediaContainer.addComponent(label);
		addMediaContainer.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

		return addMediaContainer;
	}

	/**
	 * Set the mediaGalleries and display them directly.
	 *
	 * @param mediaGalleries list of displayed media galleries
	 */
	public void setMediaGalleryList(Map<MediaGallery, MediaUploadFile> mediaGalleries)
	{
	    final VerticalLayout rootLayout = new VerticalLayout();
	    rootLayout.setSizeFull();


		final CssLayout layout = new CssLayout();
		layout.setSizeFull();
		layout.setPrimaryStyleName(CssStyle.MEDIA_GALLERY_LIST.getStyleName());

		rootLayout.addComponent(galleryMainLabel);

		rootLayout.addComponent(layout);
		setCompositionRoot(rootLayout);

		if (mediaGalleries != null)
		{
			for (Map.Entry<MediaGallery, MediaUploadFile> mediaGallery : mediaGalleries.entrySet())
			{
				final Component mediaGalleryComponent = createMediaGalleryComponent(mediaGallery.getKey(), mediaGallery.getValue());
				layout.addComponent(mediaGalleryComponent);
			}
		}

        layout.addComponent(createAddGalleryComponent());

		// invisible placeholder for flexbox left alignment of last row workaround.
        for (int i = 0; i < 3; i++) {
            layout.addComponent(createInvisiblePlaceholder());
        }
	}

	/**
	 * Set the media-list and display them directly.
	 *
	 * @param mediaGallery the mediaGallery of all medias
	 * @param mediaList    displayed medias
	 */
	public void setMediaList(MediaGallery mediaGallery, List<MediaUploadFile> mediaList)
	{
		mediaUploadFiles = mediaList;
		final VerticalLayout layout = new VerticalLayout();
		final CssLayout mediaLayout = new CssLayout();
		mediaLayout.setPrimaryStyleName(CssStyle.MEDIA_WRAPPER.getStyleName());
		mediaLayout.setSizeUndefined();

		layout.addComponent(new Label(I18N.MEDIAGALLERY_GALLERYNAME.msg(mediaGallery.getName())));

		setCompositionRoot(layout);

		if (mediaUploadFiles != null)
		{
			for (MediaUploadFile media : mediaUploadFiles)
			{
				mediaLayout.addComponent(createMediaComponent(mediaList, media));
			}
		}

		mediaLayout.addComponent(createAddMediaComponent());

		if (mediaGallery.isAvatarGallery())
			addMediaContainer.setVisible(false);

        // invisible placeholder for flexbox left alignment of last row workaround.
		for (int i = 0; i < 3; i++) {
		    mediaLayout.addComponent(createInvisiblePlaceholder());
        }

		layout.addComponent(mediaLayout);

		if (backToMediaGalleriesListener != null)
		{
			final FooterCommandBar commandBar = new FooterCommandBar();

			final Button backToGalleriesButton = new Button(I18N.MEDIAGALLERY_COMPONENT_BACKBTN.msg());
			backToGalleriesButton.setIcon(FontAwesome.CHEVRON_LEFT);
			backToGalleriesButton.addClickListener(event -> backToMediaGalleriesListener.backToMediaGallery());
			commandBar.addButton(backToGalleriesButton, FooterCommandBar.Position.MIDDLE);

			layout.addComponent(commandBar);
		}
	}

	/**
	 * Create one clickable media icon. Automatically a preview will be opened, when no {@link #onMediaClickListener} was set.
	 *
	 * @param mediaUploadFiles all medias from the same mediagalley (for preview)
	 * @param mediaUploadFile  current media
	 * @return
	 */
	private Component createMediaComponent(List<MediaUploadFile> mediaUploadFiles, MediaUploadFile mediaUploadFile)
	{
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setWidth("225px");
		layout.setHeight("225px");
		layout.setStyleName(CssStyle.MEDIA_COMPONENT.getStyleName());

		final Image image = createImage(getImageResource(mediaUploadFile));
		image.setStyleName(CssStyle.MEDIA_COMPONENT_IMAGE.getStyleName());

		if (avatars != null)
		{
			int cnt = 0;
			for (Avatar a : avatars)
			{
				if (a.getMediafile() != null && a.getMediafile().getId().equals(mediaUploadFile.getFileEntity().getId()))
				{
					final Label label = new Label();
					label.setIcon(a.getCategory().getIcon());
					label.setStyleName(CssStyle.AVATAR_LABEL.getStyleName());
					layout.addComponent(label);
					cnt++;
					image.addStyleName("rewind-" +cnt);
				}
			}
		}


		Map<MediaUploadFile, List<MediaUploadFile>> medias = new HashMap<>();
		medias.put(mediaUploadFile, mediaUploadFiles);

		if (onMediaClickListener != null)
		{
			image.addClickListener(event -> onMediaClickListener.onMediaClicked(medias));
		}
		if (autoPreview)
		{
			image.addClickListener(event -> {
				onMediaClickListener.onMediaClicked(medias);
			});
		}

		if (onMediaSelectListener != null)
		{
			image.addClickListener(event -> onMediaSelectListener.onMediaSelected(mediaUploadFile));
		}

		layout.addComponent(image);
		
		final Label name = new Label();

		name.setValue(mediaUploadFile.getClearDescription(16));

		name.addStyleName(CssStyle.MEDIA_NAME_LABEL.getStyleName());
		name.setWidth("60%");
		layout.addComponent(name);
		layout.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(name, Alignment.BOTTOM_RIGHT);

		if (removeMediaListener != null)
		{
			final Button removeButton = new Button();
			removeButton.setIcon(FontAwesome.TRASH_O);
			removeButton.setWidth("45px");
			removeButton.setStyleName(CssStyle.BUTTON_REMOVE_MEDIA.getStyleName());
			removeButton.addClickListener(event ->
			{
				boolean isAvatar = false;

				if (avatars != null)
				{
					for (Avatar a : avatars)
					{
						if (a.getFile().getName().equals(mediaUploadFile.getFileEntity().getName()))
						{
							isAvatar = true;
						}
					}
				}

				if (isAvatar && mediaUploadFile.getMedia().getMediaGallery().isAvatarGallery())
					MessageBox.show(I18N.MEDIAGALLERY_REMOVEAVATAR.msg(), MessageBox.MessageBoxButtons.YES_NO, MessageBox.MessageBoxStyle.QUESTION, r -> doDeleteMedia(r, mediaUploadFile));
				else
					MessageBox.show(I18N.MEDIAGALLERY_REMOVEMEDIA.msg(), MessageBox.MessageBoxButtons.YES_NO, MessageBox.MessageBoxStyle.QUESTION, r -> doDeleteMedia(r, mediaUploadFile));
			});
			layout.addComponent(removeButton);
			layout.setComponentAlignment(removeButton, Alignment.BOTTOM_RIGHT);
		}

		if (editMediaListener != null)
		{
			final Button editButton = new Button();
			editButton.setIcon(FontAwesome.PENCIL);
			editButton.setStyleName(CssStyle.BTN_EDIT_MEDIA.getStyleName());
			editButton.setWidth("45px");
			layout.addComponent(editButton);
			editButton.addClickListener(event -> editMediaListener.editMedia(mediaUploadFile) );
		}

		return layout;
	}

	private void doDeleteMedia(MessageBox.DialogResult dialogResult, MediaUploadFile file)
	{
		if (dialogResult == MessageBox.DialogResult.YES)
		{
			removeMediaListener.removeMedia(file);
			Notification.show(I18N.MEDIAGALLERY_MEDIAGALLERY.msg(), I18N.MEDIAGALLERY_MEDIADELETED.msg(), Notification.Type.TRAY_NOTIFICATION);
		}
	}



	/**
	 * Create one media gallery icon. If {@link #onMediaGalleryClickListener} was set, the icon is clickable.
	 *
	 * @param mediaGallery current mediaGallery entity with the medias
	 * @param previewImage image for the icon
	 * @return
	 */
	private Component createMediaGalleryComponent(MediaGallery mediaGallery, MediaUploadFile previewImage)
	{
		final CssLayout layout = new CssLayout();
		layout.setPrimaryStyleName(CssStyle.MEDIA_GALLERY_COMPONENT.getStyleName());

		final CssLayout imageWrapper = new CssLayout();
		imageWrapper.setPrimaryStyleName(CssStyle.CROP.getStyleName());

		final CssLayout commandWrapper = new CssLayout();
		commandWrapper.setPrimaryStyleName(CssStyle.MEDIA_COMMAND.getStyleName());

		final Image image = createImage(getImageResource(previewImage));

		if (onMediaGalleryClickListener != null && previewImage != null )
		{
			image.addClickListener(event -> onMediaGalleryClickListener.onMediaGalleryClicked(mediaGallery));
			imageWrapper.addComponent(image);
		}
		else if (onMediaGalleryClickListener != null && previewImage == null)
		{
			final Button emptyGallery = new Button();
			emptyGallery.setIcon(FontAwesome.CAMERA);
			emptyGallery.setStyleName(CssStyle.EMPTY_GALLERY.getStyleName());
			emptyGallery.addClickListener(event -> onMediaGalleryClickListener.onMediaGalleryClicked(mediaGallery));
			imageWrapper.addComponent(emptyGallery);
		}

		layout.addComponent(imageWrapper);

		final Label name = new Label();
		if (mediaGallery.getName().length() >= 19)
			name.setValue(mediaGallery.getName().substring(0, 16) +"...");
		else
			name.setValue(mediaGallery.getName());

		name.setPrimaryStyleName(CssStyle.GALLERY_NAME.getStyleName());
		name.setWidth("60%");
		commandWrapper.addComponent(name);

		if (removeMediaGalleryListener != null && !mediaGallery.isAvatarGallery())
		{
			final Button removeButton = new Button();
			removeButton.setIcon(FontAwesome.TRASH_O);
			removeButton.setPrimaryStyleName(CssStyle.BTN_REMOVE.getStyleName());
			removeButton.setWidth("45px");
			removeButton.addClickListener(event -> {
				MessageBox.show(I18N.MEDIAGALLERY_REMOVEGALLERY.msg(), MessageBox.MessageBoxButtons.YES_NO, MessageBox.MessageBoxStyle.QUESTION, r -> doDeleteGallery(r, mediaGallery));
			});
			commandWrapper.addComponent(removeButton);
		}

		if (editMediaGalleryListener != null && !mediaGallery.isAvatarGallery())
		{
			final Button editButton = new Button();
			editButton.setIcon(FontAwesome.PENCIL);
			editButton.setPrimaryStyleName(CssStyle.BTN_EDIT.getStyleName());
			editButton.setWidth("45px");
			editButton.addClickListener(event -> editMediaGalleryListener.editMediaGallery(mediaGallery));
			commandWrapper.addComponent(editButton);
		}

		imageWrapper.addComponent(commandWrapper);

		return layout;
	}

	private Component createInvisiblePlaceholder() {
		final CssLayout layout = new CssLayout();
		layout.setPrimaryStyleName(CssStyle.INVISIBLE_PLACEHOLDER.getStyleName());

		return layout;
	}

	private void doDeleteGallery(MessageBox.DialogResult dialogResult, MediaGallery gallery)
	{
		if (dialogResult == MessageBox.DialogResult.YES)
		{
			removeMediaGalleryListener.removeMediaGallery(gallery);
			Notification.show(I18N.MEDIAGALLERY_MEDIAGALLERY.msg(), I18N.MEDIAGALLERY_GALLERYDELETED.msg(), Notification.Type.TRAY_NOTIFICATION);
		}
	}

	public void setMediaUploadComponent(MediaUploadComponent upload)
	{
		addMediaContainer.addComponent(upload, 0);
		addMediaContainer.setComponentAlignment(upload, Alignment.MIDDLE_CENTER);
	}


	public void setAddMediaGalleryListener(AddMediaGalleryListener addMediaGalleryListener)
	{
		this.addMediaGalleryListener = addMediaGalleryListener;
	}

	public void setEditMediaGalleryListener(EditMediaGalleryListener editMediaGalleryListener)
	{
		this.editMediaGalleryListener = editMediaGalleryListener;
	}

	public void setOnMediaGalleryClickListener(OnMediaGalleryClickListener onMediaGalleryClickListener)
	{
		this.onMediaGalleryClickListener = onMediaGalleryClickListener;
	}

	public void setAddMediaListener(AddMediaListener addMediaListener)
	{
		this.addMediaListener = addMediaListener;
	}

	public void setRemoveMediaListener(RemoveMediaListener removeMediaListener)
	{
		this.removeMediaListener = removeMediaListener;
	}

	public void setRemoveMediaGalleryListener(RemoveMediaGalleryListener removeMediaGalleryListener)
	{
		this.removeMediaGalleryListener = removeMediaGalleryListener;
	}

	public void setBackToMediaGalleriesListener(BackToMediaGalleriesListener backToMediaGalleriesListener)
	{
		this.backToMediaGalleriesListener = backToMediaGalleriesListener;
	}

	public void setOnMediaClickListener(OnMediaClickListener onMediaClickListener)
	{
		this.onMediaClickListener = onMediaClickListener;
		this.autoPreview = onMediaClickListener == null;
	}

	public void setOnMediaSelectListener(OnMediaSelectListener onMediaSelectListener)
	{
		this.onMediaSelectListener = onMediaSelectListener;
	}

	public void setEditMediaListener(EditMediaListener editMediaListener)
	{
		this.editMediaListener = editMediaListener;
	}

	public void updateEditedMediaUploadFile(MediaUploadFile mediaUploadFile)
	{
		int i = 0;
		for (MediaUploadFile file : mediaUploadFiles)
		{
			if (file.getPath() == mediaUploadFile.getPath())
			{
				mediaUploadFiles.remove(file);
				mediaUploadFiles.add(i, mediaUploadFile);
			}
			i++;
		}

		setMediaList(mediaUploadFile.getMedia().getMediaGallery(), mediaUploadFiles);
	}

	private Image createImage(Resource resource)
	{
		final Image image = new Image();
		image.setSource(resource);

		return image;
	}

	private Resource getImageResource(MediaUploadFile mediaUploadFile)
	{
		if (mediaUploadFile == null || mediaUploadFile.getPath() == null)
			return null;

		return new FileResource(mediaUploadFile.toFile());
	}

	public void setMainCaption(String caption) {
	    galleryMainLabel.setValue(caption);
	    galleryMainLabel.setVisible(!Strings.isNullOrEmpty(caption));
    }

    public void setAutoPreview(boolean autoPreview)
	{
		this.autoPreview = autoPreview;
	}

	public void setAvatars(List<Avatar> avatars)
	{
		this.avatars = avatars;
	}
}
