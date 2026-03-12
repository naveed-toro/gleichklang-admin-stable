package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.Tariff;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link InitialSubscriptionOffer}.
 */
public class InitialSubscriptionOfferRepositoryTest extends AbstractRepositoryTest<InitialSubscriptionOffer>
{
	@Autowired
	private InitialSubscriptionOfferRepository initialSubscriptionOfferRepository;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	private InitialSubscriptionOffer socialOfferOneYear;
	private InitialSubscriptionOffer standardOfferOneYear;
	private InitialSubscriptionOffer standardOfferTwoYears;
	private InitialSubscriptionOffer socialOfferTwoYears;

	@Test
	public void testFindAll_DEFAULT_SORT()
	{
		List<InitialSubscriptionOffer> findAllDefaulSort = initialSubscriptionOfferRepository.findAll(InitialSubscriptionOfferRepository.DEFAULT_SORT);

		assertThat(findAllDefaulSort, is(Arrays.asList(socialOfferOneYear, socialOfferTwoYears, standardOfferOneYear, standardOfferTwoYears)));
	}

	@Override
	protected Collection<InitialSubscriptionOffer> getPersistedEntities()
	{
		List<InitialSubscriptionOffer> allOffers = new ArrayList<>();

		socialOfferOneYear = paymentEntityFactory.persistInitialSubscriptionOffer("Social1", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);
		socialOfferOneYear.setTariff(Tariff.SOCIAL);
		allOffers.add(socialOfferOneYear);

		socialOfferTwoYears = paymentEntityFactory.persistInitialSubscriptionOffer("Social2", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);
		socialOfferTwoYears.setTariff(Tariff.SOCIAL);
		socialOfferTwoYears.setDuration(24);
		allOffers.add(socialOfferTwoYears);

		standardOfferOneYear = paymentEntityFactory.persistInitialSubscriptionOffer("Standard1", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);
		allOffers.add(standardOfferOneYear);

		standardOfferTwoYears = paymentEntityFactory.persistInitialSubscriptionOffer("Standard2", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);
		standardOfferTwoYears.setDuration(24);
		allOffers.add(standardOfferTwoYears);

		initialSubscriptionOfferRepository.save(allOffers);

		return allOffers;
	}

	@Override
	protected JpaRepository<InitialSubscriptionOffer, Long> getRepository()
	{
		return initialSubscriptionOfferRepository;
	}
}
