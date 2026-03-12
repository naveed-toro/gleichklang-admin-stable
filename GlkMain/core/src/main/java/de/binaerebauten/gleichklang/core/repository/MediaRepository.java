package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.media.Media;
import de.binaerebauten.gleichklang.core.model.media.MediaGallery;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long>, JpaSpecificationExecutor<Media>, DeleteRepository<Media, Long>
{
	@Query("FROM Media m WHERE m.mediaGallery = ?1 ORDER BY m.id")
	List<Media> findByMediaGalleryWithDeleted(MediaGallery mediaGallery);
	
	@Query("FROM Media m WHERE m.mediaGallery = ?1 AND m.deleted = false ORDER BY m.id")
	List<Media> findByMediaGallery(MediaGallery mediaGallery);

	@Query("FROM Media m WHERE m.mediaGallery = ?1 AND m.deleted = false ORDER BY m.id")
	List<Media> findByMediaGallery(MediaGallery mediaGallery, Pageable pageable);

	@Query("SELECT COUNT(m) FROM Media m WHERE m.mediaGallery = ?1 AND m.deleted = false")
	int countByMediaGallery(MediaGallery mediaGallery);
	
	List<Media> findByMediaGalleryAuthor(User author);
}
