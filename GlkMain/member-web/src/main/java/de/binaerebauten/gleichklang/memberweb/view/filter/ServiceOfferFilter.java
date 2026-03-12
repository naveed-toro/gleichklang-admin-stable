package de.binaerebauten.gleichklang.memberweb.view.filter;

import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.filter.ProductValidFilter;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This filter filters all service offers that can be offered for the given subscription offer.
 */
public class ServiceOfferFilter extends ProductValidFilter<ServiceOffer>
{
	private final Set<RecommendationCategory> categories;

	public ServiceOfferFilter(LocalDate date, SubscriptionOffer subscriptionOffer)
	{
		super(date, ProductType.SERVICE_OFFER);
		this.categories = subscriptionOffer != null ? subscriptionOffer.getCategories()
				.stream().map(SubscriptionOfferCategory::getCategory)
				.collect(Collectors.toSet()) : Collections.emptySet();
	}

	@Override
	public Predicate toPredicate(Root<ServiceOffer> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		Predicate productIsValid = super.toPredicate(root, query, cb);

		Predicate requiredCategoriesSize = cb.lessThanOrEqualTo(
				cb.size(root.get(ServiceOffer_.requiredCategories)), this.categories.size());

		Predicate categoriesPredicate;

		if (this.categories.size() > 0)
		{
			Predicate inCategories = root.join(ServiceOffer_.requiredCategories, JoinType.LEFT)
					.get(ServiceOfferRequiredCategory_.category)
					.in(this.categories);
			Predicate noRequiredCategories = cb.isEmpty(root.get(ServiceOffer_.requiredCategories));
			categoriesPredicate = cb.or(noRequiredCategories, cb.and(requiredCategoriesSize, inCategories));
		}
		else
		{
			categoriesPredicate = requiredCategoriesSize;
		}

		return cb.and(productIsValid, categoriesPredicate);
	}
}
