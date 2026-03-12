package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.Chargeback;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for accessing {@link Chargeback} entities.
 */
public interface ChargebackRepository extends JpaRepository<Chargeback, Long>
{
	/**
	 * Finds the chargeback for the given payment method.
	 *
	 * @param method the payment method for which to retrieve the chargeback
	 * @return the chargeback to use for the given payment method
	 */
	Chargeback findByForMethod(PaymentMethod method);
}
