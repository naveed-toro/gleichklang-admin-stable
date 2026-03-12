package de.binaerebauten.gleichklang.core.service.file;

import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AvatarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AvatarService
{
	@Autowired
	private FileService fileService;

	@Autowired
	private AvatarRepository avatarRepository;

	public SortedMap<RecommendationCategory, AvatarUploadFile> createAvatars(User user)
	{
		final Collection<Avatar> avatars = avatarRepository.findByUser(user);
		final SortedMap<RecommendationCategory, AvatarUploadFile> uploadFiles = new TreeMap<>();

		for (RecommendationCategory category : user.getCategories())
		{
			final AvatarUploadFile avatarUploadFile = avatars.stream()
					.filter(avatar -> category.equals(avatar.getCategory()))
					.findFirst()
					.map(categoryAvatar -> new AvatarUploadFile(categoryAvatar, fileService))
					.orElseGet(() -> new AvatarUploadFile(category, user, fileService));
			uploadFiles.put(category, avatarUploadFile);
		}

		return uploadFiles;
	}

	public List<RecommendationCategory> getMissingAvatars(User user)
	{
		final Collection<Avatar> avatars = avatarRepository.findByUser(user);
		return user.getOrderedCategories().stream()
				.filter(category -> avatars.stream().map(Avatar::getCategory).noneMatch(category::equals))
				.collect(Collectors.toList());
	}
	
	public AvatarUploadFile getAvatar(User user, RecommendationCategory category)
	{
		final Avatar avatar = avatarRepository.findByUserAndCategory(user, category);
		if(avatar == null) return null;
		
		return new AvatarUploadFile(avatar, fileService);
	}
	
	public List<AvatarUploadFile> getAvatars(User user)
	{
		final List<Avatar> avatars = avatarRepository.findByUser(user);
		return avatars.stream().map(a -> new AvatarUploadFile(a, fileService)).collect(Collectors.toList());
	}
}
