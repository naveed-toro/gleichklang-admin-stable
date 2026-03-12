package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.UserPin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPinRepository extends JpaRepository<UserPin, Long> {
    UserPin findByUserId(Long userId);
}
