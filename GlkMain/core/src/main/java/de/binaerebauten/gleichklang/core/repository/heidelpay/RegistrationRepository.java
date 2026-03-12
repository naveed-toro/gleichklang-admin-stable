package de.binaerebauten.gleichklang.core.repository.heidelpay;

import de.binaerebauten.gleichklang.core.model.heidelpay.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long>, JpaSpecificationExecutor<Registration>
{
}
