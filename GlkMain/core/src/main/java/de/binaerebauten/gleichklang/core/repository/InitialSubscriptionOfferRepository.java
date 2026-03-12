package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer_;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository for {@link InitialSubscriptionOffer} entities.
 *
 * This specialization is required in addition to the generic {@link ProductRepository}
 * because we need a specific repository for filtering the results via {@link #DEFAULT_SORT}.
 */
public interface InitialSubscriptionOfferRepository
		extends JpaRepository<InitialSubscriptionOffer, Long>, JpaSpecificationExecutor<InitialSubscriptionOffer>
{
	/**
	 * The default sort:
	 *    first order by tariff, then order by "additional" attribute and duration
	 */
	Sort DEFAULT_SORT = new Sort(
			new Order(Direction.ASC, InitialSubscriptionOffer_.tariff.getName()),
			new Order(Direction.ASC, InitialSubscriptionOffer_.additional.getName()),
			new Order(Direction.ASC, InitialSubscriptionOffer_.duration.getName())
	);
}
