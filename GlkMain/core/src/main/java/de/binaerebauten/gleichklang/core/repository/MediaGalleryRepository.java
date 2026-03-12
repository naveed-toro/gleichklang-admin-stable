package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.matching.Relationship.Affiliation;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MediaGalleryRepository extends JpaRepository<MediaGallery, Long>, JpaSpecificationExecutor<MediaGallery>, DeleteRepository<MediaGallery, Long>
{
	@Query("FROM MediaGallery mg WHERE mg.author = ?1")
	List<MediaGallery> findByAuthorWithDeleted(User author);
	
	@Query("FROM MediaGallery mg WHERE mg.author = ?1 AND mg.deleted = false")
	List<MediaGallery> findByAuthor(User author);
	
	@Query("SELECT DISTINCT mg FROM MediaGallery mg LEFT JOIN mg.visibleRelationships vr WHERE "
			+ "mg.author = :author AND "
			+ "(mg.visibleCategory IN :rc OR mg.visibleCategory IS NULL) AND "
			+ "mg.deleted = false")
	List<MediaGallery> findByAuthor(@Param("author") User author, @Param("rc") RecommendationCategory category);

	@Query("SELECT DISTINCT mg FROM MediaGallery mg LEFT JOIN mg.visibleRelationships vr WHERE "
			+ "mg.author = :author AND "
			+ "(mg.visibleCategory IN :rc OR mg.visibleCategory IS NULL OR secret = true) AND "
			+ "(mg.visibleAffiliation = :a OR mg.visibleAffiliation IS NULL OR secret = true) AND "
			+ "(secret = false OR vr.targetUser = :viewer) AND "
			+ "mg.deleted = false")
	List<MediaGallery> findByAuthor(@Param("author") User author, @Param("viewer") User viewer, @Param("rc") Collection<RecommendationCategory> recommendationCategories, @Param("a") Affiliation affiliation);
	
	@Query("SELECT COUNT(mg) FROM MediaGallery mg WHERE mg.author = ?1 AND mg.deleted = false")
	int countByAuthor(User author);
	
	@Query("SELECT COUNT(DISTINCT mg) FROM MediaGallery mg LEFT JOIN mg.visibleRelationships vr WHERE "
			+ "mg.author = :author AND "
			+ "(mg.visibleCategory IN :rc OR mg.visibleCategory IS NULL OR secret = true) AND "
			+ "(mg.visibleAffiliation = :a OR mg.visibleAffiliation IS NULL OR secret = true) AND "
			+ "(secret = false OR vr.targetUser = :viewer) AND "
			+ "mg.deleted = false")
	int countByAuthor(@Param("author") User author, @Param("viewer") User viewer, @Param("rc") Collection<RecommendationCategory> recommendationCategories, @Param("a") Affiliation affiliation);

	@Query("FROM MediaGallery mg WHERE mg.author = ?1 AND mg.avatarGallery = true")
	MediaGallery findAvatarGalleryByAuthor(User author);
}
