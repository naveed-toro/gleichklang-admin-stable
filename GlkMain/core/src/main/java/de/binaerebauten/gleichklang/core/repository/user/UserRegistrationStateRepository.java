package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserRegistrationState;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

public interface UserRegistrationStateRepository extends JpaRepository<UserRegistrationState, Long>
{
	UserRegistrationState findByUser(User user);
	
	@Transactional
	void deleteByUser(User user);
}
