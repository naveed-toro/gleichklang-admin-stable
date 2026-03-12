package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MediaGalleryRepositoryTest extends AbstractRepositoryTest<MediaGallery>
{
	@Autowired
	private MediaGalleryRepository mediaGalleryRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	private User author;

	public MediaGalleryRepositoryTest()
	{
	}

	@Override
	protected Collection<MediaGallery> getPersistedEntities()
	{
		final MediaGallery mediaGallery = entityFactory.persistDefaultMediaGallery();
		author = mediaGallery.getAuthor();
		
		return Collections.singletonList(mediaGallery);
	}
	
	private MediaGallery createMediaGallery(User author)
	{
		final MediaGallery mediaGallery = new MediaGallery();
		mediaGallery.setAuthor(author);
		mediaGallery.setName(UUID.randomUUID().toString());
		mediaGallery.setVisibleAffiliation(Affiliation.POSITIVE);
		mediaGallery.setVisibleCategory(RecommendationCategory.FRIENDSHIP);
		return mediaGallery;
	}
	
	private MediaGallery persistDeletedMediaGallery(User author)
	{
		final MediaGallery mediaGallery = createMediaGallery(author);
		mediaGallery.setDeleted(true);
		return mediaGalleryRepository.save(mediaGallery);
	}

	@Override
	protected JpaRepository<MediaGallery, Long> getRepository()
	{
		return mediaGalleryRepository;
	}
	
	@Test
	public void findByAuthorWithDeletedTest()
	{
		persistDeletedMediaGallery(author);
		
		final List<MediaGallery> mediaGalleryList = mediaGalleryRepository.findByAuthorWithDeleted(author);
		assertThat(mediaGalleryList.size(), equalTo(2));
	}

	@Test
	public void findByAuthorTest()
	{
		persistDeletedMediaGallery(author);
		
		final List<MediaGallery> mediaGalleryList = mediaGalleryRepository.findByAuthor(author);
		assertThat(mediaGalleryList.size(), equalTo(1));
	}

	@Test
	public void findByAuthorTestRestricted()
	{
		final Relationship relationship = entityFactory.persistDefaultRelationship(author, entityFactory.persistDefaultUser("viewer"), RecommendationCategory.PARTNERSHIP);
		mediaGalleryRepository.deleteAll();

		final MediaGallery mediaGallery = createMediaGallery(author);
		mediaGallery.setVisibleAffiliation(null);
		mediaGallery.setVisibleCategory(null);
		mediaGallery.setDeleted(false);
		mediaGallery.setSecret(false);
		mediaGallery.setVisibleRelationships(null);
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, true);

		mediaGallery.setVisibleAffiliation(Affiliation.NEUTRAL);
		mediaGallery.setVisibleCategory(RecommendationCategory.PARTNERSHIP);
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, true);

		mediaGallery.setVisibleAffiliation(Affiliation.POSITIVE);
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, false);

		mediaGallery.setVisibleCategory(RecommendationCategory.FRIENDSHIP);
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, false);

		mediaGallery.setVisibleAffiliation(null);
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, false);

		mediaGallery.setVisibleCategory(null);
		mediaGallery.setSecret(true);
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, false);

		mediaGallery.setVisibleRelationships(Collections.singleton(relationship));
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, true);

		mediaGallery.setVisibleAffiliation(Affiliation.POSITIVE);
		mediaGallery.setVisibleCategory(RecommendationCategory.FRIENDSHIP);
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, true);

		mediaGallery.setVisibleAffiliation(Affiliation.NEUTRAL);
		mediaGallery.setVisibleCategory(RecommendationCategory.PARTNERSHIP);
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, true);

		mediaGallery.setVisibleRelationships(null);
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, false);

		mediaGallery.setDeleted(true);
		mediaGallery.setSecret(false);
		mediaGalleryRepository.save(mediaGallery);
		assertForViewer(relationship, false);
	}

	private void assertForViewer(Relationship relationship, boolean accepted)
	{
		assertThat(mediaGalleryRepository.findByAuthor(relationship.getSourceUser(), relationship.getTargetUser(), relationship.getCategories(), relationship.getAffiliation()).size(), accepted ? equalTo(1) : equalTo(0));
		assertThat(mediaGalleryRepository.countByAuthor(relationship.getSourceUser(), relationship.getTargetUser(), relationship.getCategories(), relationship.getAffiliation()), accepted ? equalTo(1) : equalTo(0));
	}

	@Test
	public void countByAuthor()
	{
		assertThat(mediaGalleryRepository.countByAuthor(author), equalTo(1));
	}
}
