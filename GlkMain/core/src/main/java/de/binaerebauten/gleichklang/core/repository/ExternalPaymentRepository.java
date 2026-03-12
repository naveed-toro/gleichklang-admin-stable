package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * This repository is responsible for the {@link ExternalPayment} entities.
 */
@Repository
public interface ExternalPaymentRepository
		extends JpaRepository<ExternalPayment, Long>
{
	/**
	 * Find all external payments with the given state which synchronization count
	 * is less than the given one.
	 *
	 * @param state                the payment state
	 * @param pageable             the pageable
	 * @param synchronizationCount the synchronization count
	 * @return the page for iterating over the results
	 */
	Page<ExternalPayment> findByStateAndSynchronizationCountLessThan(PaymentState state, int synchronizationCount,
			Pageable pageable);

	/**
	 * Finds the payment with the given external reference id.
	 *
	 * @param externalReferenceId the external reference id {@link ExternalPayment#externalReferenceId}
	 * @return the matching payment or null
	 */
	ExternalPayment findByExternalReferenceId(String externalReferenceId);
	
	/**
	 * Retrieves the payment with the given external ID.
	 *
	 * @param externalId
	 */
	Optional<ExternalPayment> findByExternalId(String externalId);

	/**
	 * Finds external payments assocaited with the given subscription.
	 *
	 * @param subscription
	 * @return the list of external payments associated with the given subscription
	 */
	@Query("SELECT p FROM ExternalPayment p WHERE EXISTS (SELECT i FROM InvoiceItem i WHERE p.invoice = i.invoice AND i.subscription = ?1)")
	List<ExternalPayment> findBySubscription(Subscription subscription);
	
	List<ExternalPayment> findByUser(User user);
}
