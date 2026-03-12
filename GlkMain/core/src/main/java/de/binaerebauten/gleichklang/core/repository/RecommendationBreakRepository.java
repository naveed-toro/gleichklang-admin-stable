package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.user.RecommendationBreak;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface RecommendationBreakRepository extends JpaRepository<RecommendationBreak, Long>
{
	@Query("FROM RecommendationBreak WHERE user = ?1 AND (endDate IS NULL OR endDate > CURRENT_DATE)")
	List<RecommendationBreak> findByUser(User currentUser);

	@Transactional
	void deleteByUser(User user);
}
