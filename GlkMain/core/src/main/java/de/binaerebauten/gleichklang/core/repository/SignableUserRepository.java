package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SignableUserRepository<T extends SignableUser> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T>
{
	T findByEmail(String email);
	T findByAlias(String alias);
}
