package de.binaerebauten.gleichklang.core.service.file;

import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.media.Media;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.utils.ImageUtil;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;

public class MediaUploadFile extends AbstractUploadFile
{
	private static final Logger LOG = LoggerFactory.getLogger(MediaUploadFile.class);

	private Media media;
	public static final String dummyMedia = "img/empty.png";
	public static final String addMedia = "img/add.png";

	public MediaUploadFile(Media media, FileService fileService)
	{
		super(fileService);

		Objects.requireNonNull(media);
		Objects.requireNonNull(media.getFile());

		this.media = media;
	}

	public MediaUploadFile(MediaGallery mediaGallery, FileService fileService)
	{
		super(fileService);

		Objects.requireNonNull(mediaGallery);

		media = new Media();
		media.setMediaGallery(mediaGallery);
		media.setFile(new FileEntity());
	}

	public MediaUploadFile(MediaGallery mediaGallery, FileService fileService, FileEntity file)
	{
		super(fileService);

		Objects.requireNonNull(mediaGallery);

		media = new Media();
		media.setMediaGallery(mediaGallery);
		media.setFile(file);
	}

	public Media getMedia()
	{
		return media;
	}

	@Override
	public FileEntity getFileEntity()
	{
		return media.getFile();
	}

	@Override
	protected void saveConcrete()
	{
		Objects.requireNonNull(media.getMediaGallery(), "Set media gallery before save");
		media = fileService.saveMediaUploadFile(this);
	}
	
	@Override
	public void delete()
	{
		fileService.deleteMediaUploadFile(this);
	}
	
	@Override
	protected boolean afterUploadSucceeded(Path path)
	{
		final int maxHeight = 1000;
		final int maxWidth = 1000;

		try
		{
			ImageUtil.convertImage(path, maxHeight, maxWidth);
			return true;
		}
		catch (Exception e)
		{
			LOG.error("Image convert failed", e);
			return false;
		}
	}
	
	@Override
	public String getFilename()
	{
		if (media.getFile().getId() != null)
			return StringUtils.removeInvalidFilePathSigns(media.getMediaGallery().getAuthor().getAlias()) + "_" + media.getFile().getId() + "." + ImageUtil.JPG_FORMAT;
		
		if (path != null)
			return path.getFileName().toString();
		
		return null;
	}
}
