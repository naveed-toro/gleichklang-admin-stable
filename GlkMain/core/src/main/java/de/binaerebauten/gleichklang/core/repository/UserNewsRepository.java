package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.news.News;
import de.binaerebauten.gleichklang.core.model.news.UserNews;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface UserNewsRepository extends JpaRepository<UserNews, Long>, JpaSpecificationExecutor<UserNews>
{
	@Query("FROM UserNews un WHERE un.user = ?1 AND NOW() >= un.news.validFrom AND NOW() <= un.news.validTo AND un.hide = false ORDER BY un.news.validFrom DESC")
	List<UserNews> findByUser(User currentUser);

	@Query("SELECT un.user.id FROM UserNews un WHERE un.news = ?1")
	Set<Long> findUserIdsByNews(News news);
	
	@Query("SELECT un FROM UserNews un JOIN un.news n WHERE "
			+ "NOW() >= n.validFrom AND "
			+ "NOW() <= n.validTo AND "
			+ "n.emailNotification = true AND "
			+ "un.notified = false AND "
			+ "un.user = ?1")
	List<UserNews> findNewsToSendViaEmail(User user);
}
