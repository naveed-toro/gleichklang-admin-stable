package de.binaerebauten.gleichklang.memberweb.view.filter;

import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.UpgradeOffer;
import de.binaerebauten.gleichklang.core.model.payment.UpgradeOffer_;
import de.binaerebauten.gleichklang.core.view.filter.ProductValidFilter;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * This filter filters all upgrade offers that can upgrade the given subscription offer.
 */
public class UpgradeOfferFilter extends ProductValidFilter<UpgradeOffer>
{
	private final SubscriptionOffer subscriptionOffer;

	public UpgradeOfferFilter(LocalDate date, SubscriptionOffer subscriptionOffer)
	{
		super(date, ProductType.UPGRADE_OFFER);
		this.subscriptionOffer = subscriptionOffer;
	}

	@Override
	public Predicate toPredicate(Root<UpgradeOffer> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		Predicate productIsValid = super.toPredicate(root, query, cb);

		Expression<Set<SubscriptionOffer>> subscriptionOffers = root.get(UpgradeOffer_.subscriptionOffers);
		Predicate canUpgradeSubscriptionOffer = cb.isMember(subscriptionOffer, subscriptionOffers);

		return cb.and(productIsValid, canUpgradeSubscriptionOffer);
	}
}
