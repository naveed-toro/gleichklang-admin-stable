package de.binaerebauten.gleichklang.core.service.file;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.media.Media;
import de.binaerebauten.gleichklang.core.model.message.MessageAttachment;
import de.binaerebauten.gleichklang.core.repository.AvatarRepository;
import de.binaerebauten.gleichklang.core.repository.FileRepository;
import de.binaerebauten.gleichklang.core.repository.MediaRepository;
import de.binaerebauten.gleichklang.core.repository.message.MessageAttachmentRepository;
import de.binaerebauten.gleichklang.core.utils.ImageUtil;
import de.binaerebauten.gleichklang.core.utils.NamedFile;
import fi.solita.clamav.ClamAVClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Service
public class FileService
{
	private static final Logger LOG = LoggerFactory.getLogger(FileService.class);
	private static final int THUMBNAIL_MAX_HEIGHT = 120;
	private static final int THUMBNAIL_MAX_WIDTH = 120;

	@Autowired
	private AvatarRepository avatarRepository;

	@Autowired
	private MediaRepository mediaRepository;

	@Autowired
	private MessageAttachmentRepository messageAttachmentRepository;

	@Value("${file.path}")
	private String filePath;

	@Value("${file.clamav.url}")
	private String clamAvUrl;

	@Value("${file.clamav.port}")
	private int clamAvPort;

	@Value("${file.clamav.timeout}")
	private int clamAvTimeout;

	@Autowired
	private FileRepository fileRepository;
	
	private void deleteUploadFile(AbstractUploadFile uploadFile)
	{
		final FileEntity fileEntity = uploadFile.getFileEntity();
		if(fileEntity.getId() != null) fileRepository.delete(fileEntity);
		cleanUp(getDirectoryPath(fileEntity));
	}

	private void deleteAvatarUploadFile(FileEntity fileEntity)
	{
		if(fileEntity != null && fileEntity.getId() != null) fileRepository.delete(fileEntity);
		cleanUp(getDirectoryPath(fileEntity));
	}

	private FileEntity saveUploadFile(AbstractUploadFile uploadFile)
	{
		final FileEntity persistedFileEntity = fileRepository.save(uploadFile.getFileEntity());

		if (uploadFile.isChangedData())
		{
			final Path path = uploadFile.getPath();
			String filename = path != null ? path.getFileName().toString() : null;
			final Path targetPath = getFilePath(persistedFileEntity.getId(), filename);

			// cleanUp the targetDirectory, so that all old files are removed
			cleanUp(getDirectoryPath(persistedFileEntity));

			// if path or targetPath are null, as new data nothing was selected
			if (path != null && targetPath != null)
			{
				try
				{
					Files.createDirectories(targetPath.getParent());
					Files.move(path, targetPath);
					Files.delete(path.getParent());
				}
				catch (IOException e)
				{
					LOG.error("Persist file error", e);
					throw new RuntimeException("END TRANSACTION");
				}
			}

			persistedFileEntity.setName(filename);
		}

		return persistedFileEntity;
	}

	public boolean isVirusFree(Path path) throws IOException
	{
		final ClamAVClient cl = new ClamAVClient(clamAvUrl, clamAvPort, clamAvTimeout);

		final byte[] reply = cl.scan(Files.newInputStream(path));
		return ClamAVClient.isCleanReply(reply);
	}

	/**
	 * Delete a path recursively without exceptions.
	 *
	 * @param oldPath
	 */
	private void cleanUp(Path oldPath)
	{
		if (oldPath == null)
			return;

		try
		{
			deletePath(oldPath);
		}
		catch (IOException e)
		{
			LOG.error("Delete directory " + oldPath.toString() + " error", e);
		}
	}

	/**
	 * Delete a path recursively.
	 *
	 * @param path path to be deleted
	 * @throws IOException
	 */
	private void deletePath(Path path) throws IOException
	{
		Files.walkFileTree(path, new SimpleFileVisitor<Path>()
		{
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
			{
				// try to delete the file anyway, even if its attributes
				// could not be read, since delete-only access is
				// theoretically possible
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
			{
				if (exc == null)
				{
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
				else
				{
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
	}

	@Transactional
	public Avatar saveAvatarWithThumbnail(Avatar avatar)
	{
		return avatarRepository.save(avatar);
	}

	@Transactional
	public Avatar saveAvatarUploadFile(AvatarUploadFile avatarUploadFile)
	{
		if(avatarUploadFile.getPath() == null)
		{
			deleteAvatarUploadFile(avatarUploadFile);
			return avatarUploadFile.getAvatar();
		}

		saveUploadFile(avatarUploadFile);
		return avatarRepository.save(avatarUploadFile.getAvatar());
	}

	@Transactional
	public Media saveMediaUploadFile(MediaUploadFile mediaUploadFile)
	{
		if(mediaUploadFile.getPath() == null)
		{
			deleteMediaUploadFile(mediaUploadFile);
			return mediaUploadFile.getMedia();
		}

		saveUploadFile(mediaUploadFile);
		return mediaRepository.save(mediaUploadFile.getMedia());
	}
	
	@Transactional
	public MessageAttachment saveMessageUploadFile(MessageUploadFile messageUploadFile)
	{
		if(messageUploadFile.getPath() == null)
		{
			deleteMessageUploadFile(messageUploadFile);
			return messageUploadFile.getMessageAttachment();
		}
		
		saveUploadFile(messageUploadFile);
		return messageAttachmentRepository.save(messageUploadFile.getMessageAttachment());
	}
	
	@Transactional
	public void deleteAvatarUploadFile(AvatarUploadFile avatarUploadFile)
	{
		final Avatar avatar = avatarUploadFile.getAvatar();

		if(avatar.getId() != null)
		{
			avatarRepository.delete(avatar);
			deleteAvatarUploadFile(avatarUploadFile.getAvatar().getThumbnail());
			deleteAvatarUploadFile(avatarUploadFile.getAvatar().getFile());
		}
	}
	
	@Transactional
	public void deleteMediaUploadFile(MediaUploadFile mediaUploadFile)
	{
		final Media media = mediaUploadFile.getMedia();
		if(media.getId() != null) mediaRepository.delete(media);
		deleteUploadFile(mediaUploadFile);
	}
	
	@Transactional
	public void deleteMessageUploadFile(MessageUploadFile messageUploadFile)
	{
		final MessageAttachment messageAttachment = messageUploadFile.getMessageAttachment();
		if(messageAttachment.getId() != null) messageAttachmentRepository.delete(messageAttachment);
		deleteUploadFile(messageUploadFile);
	}

	/**
	 * Creates a temporary directory.
	 *
	 * @param filename name of the file
	 * @return a path to the file in a temporary directory
	 */
	public Path createTempFilePath(String filename)
	{
		try
		{
			return Files.createTempDirectory("gk").resolve(filename);
		}
		catch (IOException e)
		{
			LOG.error("Create temp-directory error", e);
			return null;
		}
	}

	/**
	 * Delete the parent directory of this path recursively.
	 *
	 * @param path
	 */
	public void deleteTempPath(Path path)
	{
		if (path == null)
			return;
		cleanUp(path.getParent());
	}

	private Path getDirectoryPath(FileEntity fileEntity)
	{
		if (fileEntity == null || Strings.isNullOrEmpty(fileEntity.getName()))
			return null;

		return getDirectoryPath(fileEntity.getId());
	}

	/**
	 * Get the target directory dependent on the id. This is necessary to clean
	 * up the target directory before new upload is transfer to this directory.
	 *
	 * @param id the id of the FileEntity
	 * @return
	 */
	private Path getDirectoryPath(Long id)
	{
		if (id == null)
			return null;

		return Paths.get(filePath, id.toString());
	}
	
	public File getFile(FileEntity fileEntity)
	{
		return getFile(fileEntity, String.valueOf(fileEntity.getId()));
	}
	
	public File getFile(FileEntity fileEntity, String filename)
	{
		final Path path = getFilePath(fileEntity);
		return path != null ? new NamedFile(path.toString(), filename) : null;
	}

	public Path getFilePath(FileEntity fileEntity)
	{
		if (fileEntity == null)
			return null;

		return getFilePath(fileEntity.getId(), fileEntity.getName());
	}

	/**
	 * Same as {@link #getDirectoryPath(Long)} with filename. Necessary to
	 * compute the target for persisting the tmp-upload-file.
	 *
	 * @param id       the id of the FileEntity
	 * @param filename the name of the file
	 * @return composed targetDirectory and filename
	 */
	public Path getFilePath(Long id, String filename)
	{
		final Path targetDirectory = getDirectoryPath(id);

		if (Strings.isNullOrEmpty(filename) || targetDirectory == null)
			return null;

		return targetDirectory.resolve(filename);
	}



	/***
	 * Create copies of existing images
	 * @param file original file
	 * @param image Buffered image
	 * @return
	 */
	public FileEntity saveAvatar(File  file, BufferedImage image)
	{
		FileEntity newFile = new FileEntity();
		newFile.setName( file.getName());
		newFile.setType(FileEntity.FileType.IMAGE);

		final FileEntity persistedFileEntity = fileRepository.save(newFile);

		final String filename = persistedFileEntity.getName();
		final Path targetPath = getFilePath(persistedFileEntity.getId(), filename);

		try
		{
			Files.createDirectories(targetPath.getParent());

			try
			{
				JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
				jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				jpegParams.setCompressionQuality(0.85f);
				jpegParams.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);

				final ImageWriter writer = ImageIO.getImageWritersByFormatName(ImageUtil.JPG_FORMAT).next();
				writer.setOutput(new FileImageOutputStream(
						new File(targetPath.toString())));

				writer.write(null, new IIOImage(image, null, null), jpegParams);
			}
			catch (IOException e)
			{
				LOG.error("Write file error", e);
			}
		}
		catch (IOException e)
		{
			LOG.error("Persist file error", e);
			throw new RuntimeException("END TRANSACTION");
		}

		return persistedFileEntity;
	}

	public FileEntity saveAvatarThumbnail(File  file, BufferedImage image)
	{
		FileEntity thumbnail = new FileEntity();
		thumbnail.setName( file.getName());
		thumbnail.setType(FileEntity.FileType.IMAGE);

		final FileEntity persistedThumbnail = fileRepository.save(thumbnail);

		final String filename = persistedThumbnail.getName();
		final Path targetPath = getFilePath(persistedThumbnail.getId(), filename);

		try
		{
			Files.createDirectories(targetPath.getParent());
			try
			{

				BufferedImage scaled = ImageUtil.scaleImage(image, THUMBNAIL_MAX_HEIGHT, THUMBNAIL_MAX_WIDTH);

				JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
				jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				jpegParams.setCompressionQuality(0.85f);
				jpegParams.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);

				final ImageWriter writer = ImageIO.getImageWritersByFormatName(ImageUtil.JPG_FORMAT).next();
				writer.setOutput(new FileImageOutputStream(
						new File(targetPath.toString())));

				writer.write(null, new IIOImage(scaled, null, null), jpegParams);

			} catch (IOException e)
			{
				LOG.error("Write thumbnail error", e);
			}
		}
		catch (IOException e)
		{
			LOG.error("Persist thumbnail error", e);
			throw new RuntimeException("END TRANSACTION");
		}

		return persistedThumbnail;
	}
}
