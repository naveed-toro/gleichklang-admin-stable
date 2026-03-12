package de.binaerebauten.gleichklang.core.service.file;

import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.media.Media;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.MediaGalleryRepository;
import de.binaerebauten.gleichklang.core.repository.MediaRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MediaServiceTest
{
	@InjectMocks
	private MediaService mediaService;
	
	@Mock
	private MediaGalleryRepository mediaGalleryRepository;
	
	@Mock
	private MediaRepository mediaRepository;
	
	@Mock
	private FileService fileService;
	
	@Test
	public void deleteAllMediasTest()
	{
		final User user = new User();
		final MediaGallery mediaGallery = new MediaGallery();
		final Media media = new Media();
		
		media.setFile(new FileEntity());
		
		when(mediaGalleryRepository.findByAuthorWithDeleted(user)).thenReturn(Collections.singletonList(mediaGallery));
		when(mediaRepository.findByMediaGalleryWithDeleted(mediaGallery)).thenReturn(Collections.singletonList(media));
		
		mediaService.deleteAllMedias(user);

		verify(fileService).deleteMediaUploadFile(any(MediaUploadFile.class));
		verify(mediaGalleryRepository).delete(Collections.singletonList(mediaGallery));
	}
	
	@Test
	public void getMediasTest()
	{
		final User user = new User();
		
		final Media media = new Media();
		media.setFile(new FileEntity());
		
		final List<Media> medias = new ArrayList<>();
		medias.add(media);
		
		when(mediaRepository.findByMediaGalleryAuthor(user)).thenReturn(medias);
		
		final List<MediaUploadFile> mediaUploadFiles = mediaService.getMedias(user);
		
		assertThat(mediaUploadFiles.size(), equalTo(1));
		assertThat(mediaUploadFiles.iterator().next().getMedia(), equalTo(media));
	}
}
