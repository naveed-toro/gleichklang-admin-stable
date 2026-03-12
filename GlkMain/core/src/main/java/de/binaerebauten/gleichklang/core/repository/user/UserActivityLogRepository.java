package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long>
{
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO user_activity_log (user_id, user_activity, category, create_date, change_date ) "
			+ "VALUES(?1, ?2, ?3, CURRENT_DATE, CURRENT_DATE)", nativeQuery = true)
	void insertUserActivityLog(Long userId, String userActivity, String category);
}
