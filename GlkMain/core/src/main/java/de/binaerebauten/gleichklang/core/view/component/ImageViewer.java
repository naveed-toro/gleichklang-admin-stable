package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.List;
import java.util.Objects;

public class ImageViewer extends CustomComponent
{
	private final List<MediaUploadFile> mediaUploadFiles;

	private final ComponentReplacer<Image> image = new ComponentReplacer<>();
	private final Label name = new Label();
	private final Button backButton;
	private final Button forwardButton;

	private int currentFile;

	public ImageViewer(List<MediaUploadFile> mediaUploadFiles, MediaUploadFile mediaUploadFile)
	{
		Objects.requireNonNull(mediaUploadFiles);
		Objects.requireNonNull(mediaUploadFile);
		if(!mediaUploadFiles.contains(mediaUploadFile)) throw new IllegalArgumentException();

		this.mediaUploadFiles = mediaUploadFiles;
		currentFile = mediaUploadFiles.indexOf(mediaUploadFile);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();

		backButton = new Button("", event -> refresh(mediaUploadFiles.get(--currentFile)));
		backButton.setIcon(FontAwesome.CHEVRON_LEFT);

		forwardButton = new Button("", event -> refresh(mediaUploadFiles.get(++currentFile)));
		forwardButton.setIcon(FontAwesome.CHEVRON_RIGHT);

		final VerticalLayout imageWrapper = new VerticalLayout();
		imageWrapper.setSizeUndefined();

		imageWrapper.addComponents(image, name);
		image.setHeight(100, Unit.PERCENTAGE);
		image.addStyleName(CssStyle.IMAGE_VIEWER_IMAGE.getStyleName());
		imageWrapper.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
		imageWrapper.setComponentAlignment(name, Alignment.MIDDLE_CENTER);

		layout.addComponents(backButton, imageWrapper, forwardButton);
		layout.setComponentAlignment(backButton, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(imageWrapper, Alignment.MIDDLE_CENTER);
		layout.setComponentAlignment(forwardButton, Alignment.MIDDLE_CENTER);
		layout.setExpandRatio(backButton, 0.15f);
		layout.setExpandRatio(imageWrapper, 0.7f);
		layout.setExpandRatio(forwardButton, 0.15f);
        layout.setHeight(100, Unit.PERCENTAGE);
		setCompositionRoot(layout);

		refresh(mediaUploadFile);
		setHeight(100, Unit.PERCENTAGE);
	}

	private void refresh(MediaUploadFile mediaUploadFile)
	{
		backButton.setEnabled(currentFile > 0);
		forwardButton.setEnabled(currentFile < mediaUploadFiles.size() - 1);
		image.setComponent(new Image(mediaUploadFile.getDescription(), getImageResource(mediaUploadFile)));
		name.setValue(mediaUploadFile.getDescription());
	}

	private Resource getImageResource(MediaUploadFile mediaUploadFile)
	{
		if (mediaUploadFile == null || mediaUploadFile.getPath() == null)
			return new ThemeResource(MediaUploadFile.dummyMedia);

		return new FileResource(mediaUploadFile.toFile());
	}
}
