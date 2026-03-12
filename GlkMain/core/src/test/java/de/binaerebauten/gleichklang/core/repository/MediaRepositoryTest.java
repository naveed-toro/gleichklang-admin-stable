package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.media.Media;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

public class MediaRepositoryTest extends AbstractRepositoryTest<Media>
{
	@Autowired
	private MediaRepository mediaRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;
	
	private Media media;
	
	public MediaRepositoryTest()
	{
	}

	@Override
	protected Collection<Media> getPersistedEntities()
	{
		media = entityFactory.persistDefaultMedia();
		return Collections.singletonList(media);
	}

	@Override
	protected JpaRepository<Media, Long> getRepository()
	{
		return mediaRepository;
	}
	
	@Test
	public void findByMediaGalleryWithDeleted() throws Exception
	{
		List<Media> mediaList;
		
		mediaList = mediaRepository.findByMediaGalleryWithDeleted(media.getMediaGallery());
		shouldContainMedia(mediaList);
		
		markMediaDeleted();
		
		mediaList = mediaRepository.findByMediaGalleryWithDeleted(media.getMediaGallery());
		shouldContainMedia(mediaList);
	}
	
	@Test
	public void findByMediaGallery() throws Exception
	{
		List<Media> mediaList;
		
		mediaList = mediaRepository.findByMediaGallery(media.getMediaGallery());
		shouldContainMedia(mediaList);
		
		markMediaDeleted();
		
		mediaList = mediaRepository.findByMediaGallery(media.getMediaGallery());
		shouldBeEmpty(mediaList);
	}
	
	@Test
	public void findByMediaGalleryPageable() throws Exception
	{
		List<Media> mediaList;
		final PageRequest pageable = new PageRequest(0, 10);
		
		mediaList = mediaRepository.findByMediaGallery(media.getMediaGallery(), pageable);
		shouldContainMedia(mediaList);
		
		markMediaDeleted();
		
		mediaList = mediaRepository.findByMediaGallery(media.getMediaGallery(), pageable);
		shouldBeEmpty(mediaList);
	}
	
	@Test
	public void countByMediaGallery() throws Exception
	{
		assertThat(mediaRepository.countByMediaGallery(media.getMediaGallery()), equalTo(1));
		
		markMediaDeleted();
		
		assertThat(mediaRepository.countByMediaGallery(media.getMediaGallery()), equalTo(0));
	}
	
	private void markMediaDeleted()
	{
		media.setDeleted(true);
		mediaRepository.save(media);
	}
	
	private void shouldBeEmpty(List<Media> mediaList)
	{
		assertThat(mediaList.size(), equalTo(0));
	}
	
	private void shouldContainMedia(List<Media> mediaList)
	{
		assertThat(mediaList.size(), equalTo(1));
		assertThat(mediaList, hasItem(media));
	}
	
	@Test
	public void findByMediaGalleryAuthorTest()
	{
		final User author = media.getMediaGallery().getAuthor();
		
		assertThat(mediaRepository.findByMediaGalleryAuthor(author).iterator().next(), equalTo(media));
	}
}
