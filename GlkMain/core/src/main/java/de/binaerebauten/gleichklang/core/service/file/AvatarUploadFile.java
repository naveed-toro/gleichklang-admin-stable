package de.binaerebauten.gleichklang.core.service.file;

import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.ImageUtil;
import de.binaerebauten.gleichklang.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class AvatarUploadFile extends AbstractUploadFile
{
	public static final String dummyAvatar = "img/default_avatar.svg";
	public static final String canceledDummyAvatar = "img/canceled_avatar.svg";
	
	private static final Logger LOG = LoggerFactory.getLogger(AvatarUploadFile.class);
	
	private Avatar avatar;
	
	public AvatarUploadFile(Avatar avatar, FileService fileService)
	{
		super(fileService);
		
		Objects.requireNonNull(avatar);
		Objects.requireNonNull(avatar.getFile());
		
		this.avatar = avatar;
	}
	
	public AvatarUploadFile(RecommendationCategory category, User user, FileService fileService)
	{
		super(fileService);
		
		Objects.requireNonNull(category);
		Objects.requireNonNull(user);
		
		avatar = new Avatar();
		avatar.setCategory(category);
		avatar.setUser(user);
		avatar.setFile(new FileEntity());
	}
	
	public AvatarUploadFile(RecommendationCategory category, User user, FileService fileService, FileEntity file)
	{
		super(fileService);
		
		Objects.requireNonNull(category);
		Objects.requireNonNull(user);
		
		avatar = new Avatar();
		avatar.setCategory(category);
		avatar.setUser(user);
		avatar.setFile(file);
	}
	
	public Avatar getAvatar()
	{
		return avatar;
	}
	
	@Override
	public FileEntity getFileEntity()
	{
		return avatar.getFile();
	}
	
	@Override
	protected void saveConcrete()
	{
		
		avatar = fileService.saveAvatarUploadFile(this);
		
		if (this.getPath() != null)
		{
			
			final String filename = this.getFileEntity().getName();
			final Path targetPath = fileService.getFilePath(this.getFileEntity().getId(), filename);
			
			BufferedImage image = null;
			File file = new File(targetPath.toString());
			try
			{
				image = ImageIO.read(file);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			FileEntity persistedThumbnail = fileService.saveAvatarThumbnail(file, image);
			avatar.setThumbnail(persistedThumbnail);
			avatar = fileService.saveAvatarWithThumbnail(avatar);
		}
	}
	
	@Override
	public void delete()
	{
		fileService.deleteAvatarUploadFile(this);
	}
	
	@Override
	protected boolean afterUploadSucceeded(Path path)
	{
		final int maxHeight = 300;
		final int maxWidth = 300;
		
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
		if (avatar.getFile().getId() != null)
			return StringUtils.removeInvalidFilePathSigns(avatar.getUser().getAlias()) + "_" + avatar.getFile().getId() + "_avatar." + ImageUtil.JPG_FORMAT;
		
		if (path != null)
			return path.getFileName().toString();
		
		return null;
	}
	
	public File toThumbnailFile()
	{
		if (avatar.getThumbnail() == null) return toFile();
		return fileService.getFile(avatar.getThumbnail(), getFilename());
	}
}
