package de.binaerebauten.gleichklang.core.service.file;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.utils.ImageUtil;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public abstract class AbstractUploadFile extends AbstractUpload
{
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractUploadFile.class);

	protected final FileService fileService;
	protected Path path;
	protected boolean changedData = false;
	private Path tmpPath;

	public AbstractUploadFile(FileService fileService)
	{
		Objects.requireNonNull(fileService);

		this.fileService = fileService;
	}

	@Override
	public OutputStream startUpload(String filename, String mimeType)
	{
		if (Strings.isNullOrEmpty(filename)) return null;

		fileService.deleteTempPath(path);
		path = null;

		if (mimeType.toLowerCase().contains("image"))
			filename = StringUtils.replaceFileExtension(filename, ImageUtil.JPG_FORMAT);

		tmpPath = fileService.createTempFilePath(filename);
		changedData = true;

		try
		{
			return Files.newOutputStream(tmpPath);
		}
		catch (IOException e)
		{
			return null;
		}
	}
	
	public void setDeleted()
	{
		fileService.deleteTempPath(path);
		path = null;
		changedData = true;
	}

	@Override
	public UploadResult onUploadFailed()
	{
		fileService.deleteTempPath(tmpPath);
		tmpPath = null;

		return UploadResult.FAILED;
	}

	@Override
	public UploadResult onUploadSucceeded()
	{
		UploadResult result;
		try
		{
			boolean virusFree = fileService.isVirusFree(tmpPath);
			result = !virusFree ? UploadResult.VIRUS : !afterUploadSucceeded(tmpPath) ? UploadResult.FAILED : UploadResult.SUCCESS;
		}
		catch (Exception e)
		{
			result = UploadResult.FAILED;
			LOG.error("Upload failed. IOException", e);
		}

		if (UploadResult.SUCCESS.equals(result))
		{
			path = tmpPath;
			tmpPath = null;
		}
		else
		{
			fileService.deleteTempPath(tmpPath);
			tmpPath = null;
		}

		return result;
	}

	protected abstract boolean afterUploadSucceeded(Path path);

	/**
	 * If new data available from an upload, this returns true, otherwise
	 * false.
	 *
	 * @return true if new data available, otherwise false.
	 */
	public boolean isChangedData()
	{
		return changedData;
	}

	/**
	 * Set the status, so no new uploads are present and called {@link
	 * #saveConcrete()}
	 */
	public void save()
	{
		saveConcrete();

		changedData = false;
		path = null;
	}

	/**
	 * Set the status, so new uploads are present and called {@link
	 * #saveConcrete()}
	 */
	public void saveWithUpload()
	{
		saveConcrete();

		changedData = false;
	}

	/**
	 * To persist the concrete entity and the file with the help of the {@link
	 * FileService}.
	 */
	protected abstract void saveConcrete();
	
	public abstract void delete();

	/**
	 * Necessary for the {@link FileService} so that all files could be saved in
	 * the same way.
	 *
	 * @return
	 */
	public abstract FileEntity getFileEntity();
	
	public abstract String getFilename();

	/**
	 * Get the path of the file. This can be the persisted path or in the
	 * tmp-directory, if a new upload is available.
	 *
	 * @return
	 */
	public Path getPath()
	{
		if (changedData) return path;
		return fileService.getFilePath(getFileEntity());
	}
	
	public String getDescription()
	{
		if(getFileEntity() != null && getFileEntity().getDescription() != null) return getFileEntity().getDescription();
		return getFilename();
	}
	
	public String getClearName()
	{
		String name = getFilename();
		if(name == null) return null;
		
		final int extensionIndex = name.lastIndexOf(".");
		if(extensionIndex >= 0) name = name.substring(0, extensionIndex);
		
		name = name.replace("-", " ");
		name = name.replace("_", " ");
		
		return name;
	}

	public String getClearDescription(int maxSize)
	{
		return StringUtils.cutString(getDescription(), maxSize);
	}

	public String getClearName(int maxSize)
	{
		return StringUtils.cutString(getClearName(), maxSize);
	}
	
	public File toFile()
	{
		if (changedData) return path.toFile();
		return fileService.getFile(getFileEntity(), getFilename());
	}
}
