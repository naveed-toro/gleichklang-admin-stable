package de.binaerebauten.gleichklang.core.service.file;

import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.media.Media;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.MediaGalleryRepository;
import de.binaerebauten.gleichklang.core.repository.MediaRepository;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MediaService
{
	private static final int MAX_GALLERIES = 10;
	private static final int MAX_MEDIA = 10;

	@Autowired
	private MediaGalleryRepository mediaGalleryRepository;

	@Autowired
	private MediaRepository mediaRepository;

	@Autowired
	private FileService fileService;

	@Transactional
	public void saveWithoutValidation(MediaUploadFile media)
	{
		media.save();
	}

	@Transactional
	public void save(MediaGallery mediaGallery, User currentUser) throws ValidationException
	{
		validateMediaGallery(mediaGallery, currentUser);
		mediaGalleryRepository.save(mediaGallery);
	}

	@Transactional
	public void save(MediaUploadFile media) throws ValidationException
	{
		validateMedia(media.getMedia(), media.getMedia().getMediaGallery());
		media.save();
	}

	@Transactional
	public Map<MediaGallery, MediaUploadFile> getMediaGalleryWithPreview(User user)
	{
		final List<MediaGallery> mediaGalleryList = mediaGalleryRepository.findByAuthor(user);
		final Map<MediaGallery, MediaUploadFile> result = new HashMap<>();
		mediaGalleryList.forEach(value -> result.put(value, getPreviewMedia(value)));
		return result;
	}
	
	public Map<MediaGallery, MediaUploadFile> getMediaGalleryWithPreview(User user, RecommendationCategory category)
	{
		final Map<MediaGallery, MediaUploadFile> result = new HashMap<>();
		if(user == null || category == null) return result;
		
		final List<MediaGallery> mediaGalleryList = mediaGalleryRepository.findByAuthor(user, category);
		mediaGalleryList.forEach(value -> result.put(value, getPreviewMedia(value)));
		return result;
	}

	@Transactional
	public Map<MediaGallery, MediaUploadFile> getMediaGalleryWithPreview(User user, User viewer, Collection<RecommendationCategory> recommendationCategories, Affiliation affiliation)
	{
		final Map<MediaGallery, MediaUploadFile> result = new HashMap<>();
		if(user == null || viewer == null || recommendationCategories == null || recommendationCategories.isEmpty() || affiliation == null) return result;

		final List<MediaGallery> mediaGalleryList = mediaGalleryRepository.findByAuthor(user, viewer, recommendationCategories, affiliation);
		mediaGalleryList.forEach(value -> result.put(value, getPreviewMedia(value)));
		return result;
	}
	
	@Transactional
	public boolean isExistsMediaGallery(Relationship relationship, Relationship inverseRelationship)
	{
		return mediaGalleryRepository.countByAuthor(relationship.getTargetUser(), relationship.getSourceUser(), relationship.getCategories(), inverseRelationship.getAffiliation()) > 0;
	}

	@Transactional
	public List<MediaUploadFile> getMedias(MediaGallery mediaGallery)
	{
		if (mediaGallery == null) return new ArrayList<>();
		return mediaRepository.findByMediaGallery(mediaGallery).stream().map(value -> new MediaUploadFile(value, fileService)).collect(Collectors.toList());
	}
	
	public List<MediaUploadFile> getMedias(User user)
	{
		if (user == null) return new ArrayList<>();
		return mediaRepository.findByMediaGalleryAuthor(user).stream().map(value -> new MediaUploadFile(value, fileService)).collect(Collectors.toList());
	}

	private MediaUploadFile getPreviewMedia(MediaGallery mediaGallery)
	{
		final Media preview = mediaRepository.findByMediaGallery(mediaGallery, new PageRequest(0, 1)).stream().findFirst().orElse(null);
		return preview != null ? new MediaUploadFile(preview, fileService) : null;
	}

	public void validateMediaGallery(MediaGallery mediaGallery, User currentUser) throws ValidationException
	{
		if (mediaGallery == null || mediaGallery.getId() == null)
		{
			if (mediaGalleryRepository.countByAuthor(currentUser) >= MAX_GALLERIES)
			{
				throw new ValidationException("Maximale Galerienanzahl erreicht");
			}
		}
	}

	public void validateMedia(Media media, MediaGallery mediaGallery) throws ValidationException
	{
		if (media == null || media.getId() == null)
		{
			if (mediaRepository.countByMediaGallery(mediaGallery) >= MAX_MEDIA)
			{
				throw new ValidationException("Maximale Bilderanzahl in der Galerie überschritten");
			}
		}
	}
	
	@Transactional
	public void deleteAllMedias(User user)
	{
		final List<MediaGallery> mediaGalleryList = mediaGalleryRepository.findByAuthorWithDeleted(user);
		for (MediaGallery mediaGallery : mediaGalleryList)
		{
			mediaRepository.findByMediaGalleryWithDeleted(mediaGallery).stream()
					.map(media -> new MediaUploadFile(media, fileService))
					.forEach(MediaUploadFile::delete);
		}
		mediaGalleryRepository.delete(mediaGalleryList);
	}
}
