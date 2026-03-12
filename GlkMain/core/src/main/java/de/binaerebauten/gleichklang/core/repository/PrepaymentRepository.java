package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.Invoice;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.payment.Prepayment;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * This repository provides access to {@link Prepayment} objects.
 */
public interface PrepaymentRepository extends JpaRepository<Prepayment, Long>
{
	/**
	 * Finds all prepayment with the given payment state.
	 *
	 * @param date     the date
	 * @param pageable the pageable
	 * @return the page with the prepayment entities
	 */
	@Query("FROM Prepayment p WHERE p.state = de.binaerebauten.gleichklang.core.model.payment.PaymentState.PENDING "
			+ "AND p.amount.amount > 0 "
			+ "AND (p.nextReminderDate IS NULL OR p.nextReminderDate < :date)")
	Page<Prepayment> findPendingPrepaymentsToRemind(@Param("date") LocalDateTime date, Pageable pageable);

	@Query("FROM Prepayment p WHERE p.invoice = ?1 and p.state=?2 and p.method=?3")
	Prepayment findByUser(Invoice invoice, PaymentState paymentState, PaymentMethod paymentMethod);
}
