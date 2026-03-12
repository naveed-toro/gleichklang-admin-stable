package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.file.FileService;
import de.binaerebauten.gleichklang.core.service.file.MediaUploadFile;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.ImageUtil;
import de.binaerebauten.gleichklang.core.view.component.MediaUploadComponent;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.ImageViewerPopup;
import de.binaerebauten.gleichklang.core.service.file.MediaService;
import de.binaerebauten.gleichklang.memberweb.view.I18N;
import de.binaerebauten.gleichklang.memberweb.view.MediaView;
import de.binaerebauten.gleichklang.memberweb.view.MediaView.MediaViewListener;
import de.binaerebauten.gleichklang.memberweb.view.popup.EditMediaPopup;
import de.binaerebauten.gleichklang.memberweb.view.popup.MediaGalleryPopup;
import de.binaerebauten.gleichklang.memberweb.view.popup.MediaGalleryPopup.MediaGalleryPopupCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MediaPresenter extends NavigatePresenter implements MediaViewListener, MediaGalleryPopupCallback, MediaUploadComponent.MediaUploadCallback
{
	private static final Logger LOG = LoggerFactory.getLogger(MediaPresenter.class);
	
	private final User currentUser;
	private final MediaView view;
	private final FileService fileService;
	private final MediaGalleryRepository mediaGalleryRepository;
	private final RelationshipRepository relationshipRepository;
	private final MediaRepository mediaRepository;
	private final MediaService mediaService;
	private final AvatarRepository avatarRepository;
	private final ClientInformation.Device device;
	
	public MediaPresenter(ApplicationContext ctx, MediaView view, ClientInformation.Device device)
	{
		super(view);
		this.view = view;
		this.device = device;
		
		fileService = ctx.getBean(FileService.class);
		mediaGalleryRepository = ctx.getBean(MediaGalleryRepository.class);
		mediaRepository = ctx.getBean(MediaRepository.class);
		currentUser = ctx.getBean(UserRepository.class).findOne(ctx.getBean(AuthenticationService.class).getAuthenticatedUserId());
		mediaService = ctx.getBean(MediaService.class);
		relationshipRepository = ctx.getBean(RelationshipRepository.class);
		avatarRepository = ctx.getBean(AvatarRepository.class);
		
		view.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		loadMediaGalleries();
	}
	
	@Override
	public void leave()
	{
		this.view.setMediaGalleryList(null);
		
		super.leave();
	}
	
	@Override
	public void loadMediaGalleries()
	{
		this.view.setMediaGalleryList(mediaService.getMediaGalleryWithPreview(currentUser));
	}
	
	@Override
	public void newMediaGallery()
	{
		try
		{
			mediaService.validateMediaGallery(null, currentUser);
			
			final MediaGallery mediaGallery = new MediaGallery();
			mediaGallery.setAuthor(currentUser);
			mediaGallery.setName(I18N.MEDIAGALLERY_CAPTION_DEFAULTNAME.msg());
			
			createMediaGalleryPopup(mediaGallery, I18N.MEDIAGALLERY_POPUP_CREATE);
		}
		catch (ValidationException e)
		{
			Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
		}
	}
	
	private void createMediaGalleryPopup(MediaGallery item, I18N caption)
	{
		final MediaGalleryPopup popup = new MediaGalleryPopup(item, currentUser.getOrderedCategories(), relationshipRepository.findAllRelationshipsForUser(currentUser), this, caption, device);
		popup.addStyleName(CssStyle.GENERIC_POPUP.getStyleName());
		popup.addCloseListener(event -> loadMediaGalleries());
		tryOpenPopup(popup);
	}
	
	@Override
	public void editMediaGallery(MediaGallery item)
	{
		createMediaGalleryPopup(item, I18N.MEDIAGALLERY_POPUP_EDIT);
	}
	
	@Override
	public void deleteMediaGallery(MediaGallery item)
	{
		mediaGalleryRepository.markAsDeleted(item);
		loadMediaGalleries();
	}
	
	@Override
	public void loadMediaList(MediaGallery mediaGallery)
	{
		view.setAvatars(avatarRepository.findByUser(currentUser));
		view.setMediaList(mediaGallery, mediaService.getMedias(mediaGallery));
		final MediaUploadFile media = new MediaUploadFile(mediaGallery, fileService);
		this.view.setMediaUploadComponent(new MediaUploadComponent(mediaGallery, media, this::save));
	}
	
	@Override
	public void deleteMedia(MediaUploadFile item)
	{
		final MediaGallery mediaGallery = item.getMedia().getMediaGallery();
		
		if (item.getMedia().getMediaGallery().isAvatarGallery())
		{
			List<Avatar> avatars = avatarRepository.findByUser(currentUser);
			for (Avatar a : avatars)
			{
				if (a.getMediafile() != null && a.getMediafile().getId().equals(item.getFileEntity().getId()))
				{
					a.setMediafile(null);
					avatarRepository.delete(a);
				}
				
			}
		}
		
		mediaRepository.markAsDeleted(item.getMedia());
		
		loadMediaList(mediaGallery);
	}
	
	@Override
	public void save(MediaGallery mediaGallery) throws ValidationException
	{
		mediaService.save(mediaGallery, currentUser);
	}
	
	@Override
	public void save(MediaGallery gallery, MediaUploadFile mediaUploadFile) throws ValidationException
	{
		try
		{
			mediaService.validateMedia(null, gallery);
			mediaService.save(mediaUploadFile);
			Notification.show(I18N.MEDIAGALLERY_NOTIFICATION.msg(), I18N.MEDIAGALLERY_NOTIFICATION_SAVED.msg(), Notification.Type.TRAY_NOTIFICATION);
			loadMediaList(gallery);
		}
		catch (ValidationException e)
		{
			Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
		}
		
	}
	
	@Override
	public void showMediaPopup(Map<MediaUploadFile, List<MediaUploadFile>> medias)
	{
		tryOpenPopup(new ImageViewerPopup(medias));
	}
	
	@Override
	public void editMedia(MediaUploadFile mediaUploadFile)
	{
		final Set<RecommendationCategory> categories = currentUser.getOrderedCategories();
		final List<Avatar> avatars = new ArrayList<>();
		
		for (RecommendationCategory c : categories)
		{
			avatars.add(avatarRepository.findByUserAndCategory(currentUser, c));
		}
		
		final EditMediaPopup popup = new EditMediaPopup(mediaUploadFile, currentUser.getOrderedCategories(), avatars);
		popup.setRotateMediaCallback(this::updateEditedMedia);
		popup.setSaveAvatarsCallback(this::saveRotatedAvatars);
		popup.setDescriptionChangedCallback((file) -> saveUpdatedGallery(mediaUploadFile.getMedia().getMediaGallery(), file));
		popup.setSetAsAvatarCallback((file, image, category, gallery) -> saveMediaAsAvatar(file, image, category, gallery, mediaUploadFile));
		tryOpenPopup(popup);
	}
	
	private void saveUpdatedGallery(MediaGallery gallery, MediaUploadFile mediaUploadFile)
	{
		try
		{
			mediaService.save(mediaUploadFile);
			Notification.show(I18N.MEDIAGALLERY_NOTIFICATION.msg(), I18N.MEDIAGALLERY_NOTIFICATION_SAVED.msg(), Notification.Type.TRAY_NOTIFICATION);
			view.setAvatars(avatarRepository.findByUser(currentUser));
			loadMediaList(gallery);
		}
		catch (ValidationException e)
		{
			Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
		}
		
	}
	
	private void saveRotatedAvatars(BufferedImage image, List<Avatar> avatars)
	{
		for (Avatar a : avatars)
		{
			final File file = fileService.getFile(a.getFile());
			final File thumbnail = fileService.getFile(a.getThumbnail());
			
			BufferedImage newThumbnail = null;
			try
			{
				newThumbnail = ImageUtil.scaleImage(image, 100, 100);
			}
			catch (IOException e)
			{
				LOG.error("rotate avatar", e);
			}
			
			try
			{
				ImageIO.write(image, ImageUtil.JPG_FORMAT, file);
				ImageIO.write(newThumbnail, ImageUtil.JPG_FORMAT, thumbnail);
				Notification.show(I18N.MEDIAGALLERY_NOTIFICATION.msg(), I18N.MEDIAGALLERY_NOTIFICATION_SAVED.msg(), Notification.Type.TRAY_NOTIFICATION);
			}
			catch (IOException e)
			{
				LOG.error("save rotated avatar", e);
				Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
			}
		}
		
	}
	
	private void saveMediaAsAvatar(File oldFile, BufferedImage image, RecommendationCategory category, MediaGallery mediaGallery, MediaUploadFile file)
	{
		FileEntity persistedFile = fileService.saveAvatar(oldFile, image);
		FileEntity persistedThumbnail = fileService.saveAvatarThumbnail(oldFile, image);
		
		Avatar oldAvatar = avatarRepository.findByUserAndCategory(currentUser, category);
		if (oldAvatar != null)
		{
			oldAvatar.setFile(persistedFile);
			oldAvatar.setThumbnail(persistedThumbnail);
			oldAvatar.setMediafile(file.getFileEntity());
			avatarRepository.save(oldAvatar);
		}
		else
		{
			Avatar avatar = new Avatar();
			avatar.setCategory(category);
			avatar.setUser(currentUser);
			
			avatar.setFile(persistedFile);
			avatar.setThumbnail(persistedThumbnail);
			avatar.setMediafile(file.getFileEntity());
			
			avatarRepository.save(avatar);
		}
		
		view.setAvatars(avatarRepository.findByUser(currentUser));
		loadMediaList(mediaGallery);
	}
	
	private void updateEditedMedia(MediaUploadFile mediaUploadFile)
	{
		view.setAvatars(avatarRepository.findByUser(currentUser));
		this.view.updateEditedMedia(mediaUploadFile);
		final MediaUploadFile media = new MediaUploadFile(mediaUploadFile.getMedia().getMediaGallery(), fileService);
		this.view.setMediaUploadComponent(new MediaUploadComponent(mediaUploadFile.getMedia().getMediaGallery(), media, this::save));
	}
	
}
