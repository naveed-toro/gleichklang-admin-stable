package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing {@link de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration}
 */
@Repository
public interface ExternalPaymentRegistrationRepository extends JpaRepository<ExternalPaymentRegistration, Long>, JpaSpecificationExecutor<ExternalPaymentRegistration>
{
	/**
	 * Finds the external payment registration of the given user and registration id.
	 *
	 * @param user the user
	 * @param registrationId the registration id
	 *
	 * @return the external payment registration of the user or null if it doesn't exist
	 */
	ExternalPaymentRegistration findByUserAndRegistrationId(User user, String registrationId);

	/**
	 * Finds the external payment registration of the given user.
	 *
	 * @param user the user
	 * @return the external payment registration of the user or null if it doesn't exist
	 */
	ExternalPaymentRegistration findByUser(User user);

	/**
	 * Finds the external payment registration with the given external reference id.
	 *
	 * @param externalReferenceId the external reference id {@link ExternalPaymentRegistration#externalReferenceId}
	 *
	 * @return the matching external payment registration or null
	 */
	ExternalPaymentRegistration findByExternalReferenceId(String externalReferenceId);
	
	ExternalPaymentRegistration findByRegistrationId(String registrationId);
}
