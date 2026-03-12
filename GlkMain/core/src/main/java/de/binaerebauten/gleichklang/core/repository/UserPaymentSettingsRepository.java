package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository provinding access to {@link UserPaymentSettings}.
 */
@Repository
public interface UserPaymentSettingsRepository extends JpaRepository<UserPaymentSettings, Long>
{
	/**
	 * Finds an optional payment settings of the given user
	 *
	 * @param user the user
	 * @return an optional users payment settings
	 */
	Optional<UserPaymentSettings> findByUser(User user);
}
