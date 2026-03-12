package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOfferCategory;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOfferCategory_;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer_;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * Filter for subscription offer that extends the {@link ProductValidFilter}
 * with additional filtering based on the given tariff and recommendation categories.
 *
 * @param <T> the  subscription offer type
 */
public abstract class SubscriptionOfferFilter<T extends SubscriptionOffer>
		extends ProductValidFilter<T>
{
	private final Set<RecommendationCategory> categories;

	/**
	 * Creates a new product filter which filters product entities if they're valid on the given date.
	 *
	 * @param date       the date for which the products should be valid, may be null
	 * @param categories the categories of the offer
	 * @param types      restricts the set of the result to these sub types of Product
	 */
	public SubscriptionOfferFilter(LocalDate date, Set<RecommendationCategory> categories, ProductType... types)
	{
		super(date, types);
		this.categories = categories;
	}

	public Set<RecommendationCategory> getCategories()
	{
		return categories;
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb)
	{
		Predicate productValidFilter = super.toPredicate(root, query, cb);

		Root<SubscriptionOffer> typedRoot = (Root<SubscriptionOffer>) root;

		Expression<Set<SubscriptionOfferCategory>> categoriesExpression =
				typedRoot.get(SubscriptionOffer_.categories);

		Predicate equalCategoriesSize = cb.equal(
				cb.size(categoriesExpression), this.categories.size());

		Predicate categoriesPredicate;

		if (this.categories.size() > 0)
		{
			Predicate inCategories = root.join(SubscriptionOffer_.categories)
					.get(SubscriptionOfferCategory_.category)
					.in(this.categories);
			categoriesPredicate = cb.and(equalCategoriesSize, inCategories);
		}
		else
		{
			categoriesPredicate = equalCategoriesSize;
		}

		Predicate filterPredicate = cb.and(
				productValidFilter,
				categoriesPredicate);

		return filterPredicate;
	}
}
