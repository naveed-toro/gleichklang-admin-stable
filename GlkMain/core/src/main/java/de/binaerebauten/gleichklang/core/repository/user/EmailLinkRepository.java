package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.EmailLink;
import de.binaerebauten.gleichklang.core.model.user.EmailLink.EmailLinkContext;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailLinkRepository extends JpaRepository<EmailLink, Long>
{
	@Query("SELECT id FROM EmailLink WHERE createDate IS NULL OR createDate < ?1")
	List<Long> findExpiredIds(LocalDateTime expireDate);
	
	@Transactional
	@Modifying
	void deleteByIdIn(@Param("ids") Iterable<Long> ids);
	
	EmailLink findByUniqueToken(String uniqueToken);
	
	@Transactional
	void deleteByUserAndContext(SignableUser user, EmailLinkContext context);
}
