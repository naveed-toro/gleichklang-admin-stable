package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This repository can be used for the {@link de.binaerebauten.gleichklang.core.model.payment.ExternalPayment}
 * and {@link de.binaerebauten.gleichklang.core.model.payment.Prepayment} entities.
 */
@Repository
public interface PaymentRepository
		extends JpaRepository<AbstractPayment, Long>
{
	/**
	 * Retrieves an optional current payment of the given user.
	 *
	 * @param user the user
	 * @return the current payment or null
	 */
	@Query("FROM AbstractPayment p WHERE p.current = true AND p.user = :user")
	Optional<AbstractPayment> findCurrentPayment(@Param("user") User user);
	
	default boolean existsPendingPayments(User user)
	{
		return existsByUserAndState(user, PaymentState.PENDING);
	}
	
	// TODO ab spring.data 1.11 ist dies auch ohne @Query-Annotation verwendbar mit besserer Performance
	@Query("SELECT COUNT(p) > 0 FROM AbstractPayment p WHERE p.user = ?1")
	boolean existsByUser(User user);
	
	// TODO ab spring.data 1.11 ist dies auch ohne @Query-Annotation verwendbar mit besserer Performance
	@Query("SELECT COUNT(p) > 0 FROM AbstractPayment p WHERE p.user = ?1 AND p.state = ?2")
	boolean existsByUserAndState(User user, PaymentState state);
	
	default Set<AbstractPayment> findPendingPayments(User user)
	{
		return findByUserAndState(user, PaymentState.PENDING);
	}
	
	Set<AbstractPayment> findByUserAndState(User user, PaymentState state);
	
	/**
	 * Finds all payments, which reference to the subscription.
	 *
	 * @param subscription
	 * @return
	 */
	@Query("FROM AbstractPayment p JOIN FETCH p.invoice i JOIN FETCH i.items item JOIN FETCH item.subscription subscription WHERE subscription = :subscription")
	List<AbstractPayment> findBySubscription(@Param("subscription") Subscription subscription);

List<AbstractPayment> findByUser(User user);

// TODO Task 1 changes
	@Transactional
	@Modifying
	@Query("UPDATE AbstractPayment p SET "
			+ "p.comment = ?2 "
			+ "WHERE p = ?1")
	public void updatePaymentComments(AbstractPayment payment, String comment);

	@Query("SELECT COUNT(p) >= 1 FROM AbstractPayment p WHERE p.user = ?1 AND p.state = ?2")
	boolean paymentPending(User user, PaymentState state);

	@Query("FROM AbstractPayment p WHERE p.user = ?1 AND p.state = ?2")
	List<AbstractPayment> getUserPendingPaymentList(User user, PaymentState state);

	@Transactional
	@Modifying
	@Query("UPDATE AbstractPayment p SET p.revocationAmount= ?1, p.revocationDate = ?2 where p = ?3")
	public void updateRevocation(BigDecimal decimal, Date date, AbstractPayment abstractPayment);



}
