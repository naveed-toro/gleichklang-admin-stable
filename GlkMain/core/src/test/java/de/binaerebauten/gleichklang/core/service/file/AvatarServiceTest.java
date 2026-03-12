package de.binaerebauten.gleichklang.core.service.file;

import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AvatarRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AvatarServiceTest
{
	@InjectMocks
	private AvatarService avatarService;
	
	@Mock
	private AvatarRepository avatarRepository;
	
	@Mock
	private FileService fileService;
	
	@Test
	public void getAvatarsTest()
	{
		final User user = new User();
		final Avatar avatar = new Avatar();
		
		avatar.setFile(new FileEntity());
		
		when(avatarRepository.findByUser(user)).thenReturn(Collections.singletonList(avatar));
		
		final List<AvatarUploadFile> avatarUploadFiles = avatarService.getAvatars(user);
		
		assertThat(avatarUploadFiles.size(), equalTo(1));
		assertThat(avatarUploadFiles.iterator().next().getAvatar(), equalTo(avatar));
	}
	
	@Test
	public void getAvatarTest()
	{
		final User user = new User();
		final Avatar avatar = new Avatar();
		final RecommendationCategory category = RecommendationCategory.PARTNERSHIP;
		
		avatar.setFile(new FileEntity());
		
		when(avatarRepository.findByUserAndCategory(user, category)).thenReturn(avatar);
		
		final AvatarUploadFile avatarUploadFile = avatarService.getAvatar(user, category);
		
		assertThat(avatarUploadFile, notNullValue());
		assertThat(avatarUploadFile.getAvatar(), equalTo(avatar));
	}
}
