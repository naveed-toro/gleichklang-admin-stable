package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.HeidelpayTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This repository provides access to the {@link HeidelpayTransaction} entities.
 */
@Repository
public interface HeidelpayTransactionRepository
		extends JpaRepository<HeidelpayTransaction, Long>
{
	/**
	 * Finds all heidelpay event for the given payment.
	 *
	 * @param payment  the payment
	 * @param pageable the pageable
	 * @return the page of heidelpay events
	 */
	Page<HeidelpayTransaction> findByPaymentOrderByCreateDateDesc(ExternalPayment payment, Pageable pageable);
}
