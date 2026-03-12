package de.binaerebauten.gleichklang.memberweb.service;

import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.view.filter.InitialSubscriptionOfferFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Set;

/**
 * Provides services related to {@link de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer}
 * entities.
 */
@Service
public class SubscriptionOfferService
{
	private final ProductRepository<InitialSubscriptionOffer> initialSubscriptionOfferRepository;

	/**
	 * This service uses constructor based dependency injection to ease testing.
	 *
	 * @param initialSubscriptionOfferRepository
	 */
	@Autowired
	public SubscriptionOfferService(ProductRepository<InitialSubscriptionOffer> initialSubscriptionOfferRepository)
	{
		this.initialSubscriptionOfferRepository = initialSubscriptionOfferRepository;
	}

	/**
	 * Checks if at least one subscription offer that is valid now exists for the given parameters.
	 *
	 * @param actionCode the nullable action code
	 * @param categories the non-null recommendation categories
	 * @return true iff. the given action code is valid now
	 */
	@Transactional
	public boolean isValidActionCode(String actionCode, Set<RecommendationCategory> categories)
	{
		InitialSubscriptionOfferFilter subscriptionOfferFilter = new InitialSubscriptionOfferFilter(LocalDate.now(), categories, actionCode, true);

		long subscriptionOffers = initialSubscriptionOfferRepository.count(subscriptionOfferFilter);

		return subscriptionOffers > 0;
	}
}
