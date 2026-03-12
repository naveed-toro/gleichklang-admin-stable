package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminRole;
import de.binaerebauten.gleichklang.core.repository.SignableUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends SignableUserRepository<Admin>
{
	List<Admin> findByRoles(AdminRole role);
}
