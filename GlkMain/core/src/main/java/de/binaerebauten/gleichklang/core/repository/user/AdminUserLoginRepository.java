package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminUserLogin;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface AdminUserLoginRepository extends JpaRepository<AdminUserLogin, Long>
{
	@Transactional
	@Modifying
	@Query(value = "DELETE AdminUserLogin WHERE createDate IS NULL OR createDate < ?1")
	void deleteExpired(LocalDateTime expiredCreateDate);
	
	@Transactional
	void deleteByAdmin(Admin admin);
	
	AdminUserLogin findByUser(User user);
}
