package de.binaerebauten.gleichklang.core.repository.mail;

import de.binaerebauten.gleichklang.core.model.mail.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Set;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long>, JpaSpecificationExecutor<Newsletter>
{
	Set<Newsletter> findByEmailIn(Collection<String> email);

	Newsletter findByEmail(String email);

	@Transactional
	void deleteByEmail(String email);
}
