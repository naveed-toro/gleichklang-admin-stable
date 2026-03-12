package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News>
{
	@Query("SELECT n FROM News AS n WHERE "
			+ "NOW() >= n.validFrom AND NOW() <= n.validTo AND "
			+ "n.active = true AND "
			+ "NOT EXISTS (FROM UserNews un WHERE un.news = n AND un.user = ?1) "
			+ "ORDER BY n.validFrom DESC")
	List<News> findNewNews(User currentUser);
}
