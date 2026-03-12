package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.affiliate.AffiliatePaymentState;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface AffiliatePaymentStateRepository extends JpaRepository<AffiliatePaymentState, Long>
{
	Set<AffiliatePaymentState> findByPaymentIn(Collection<AbstractPayment> payments);
}
