package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.media.Avatar;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long>
{
	List<Avatar> findByUser(User user);

	Avatar findByUserAndCategory(User user, RecommendationCategory category);
}
