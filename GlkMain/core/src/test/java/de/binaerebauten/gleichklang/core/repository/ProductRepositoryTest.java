package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.RenewalOffer;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Unit tests for {@link ProductRepository}.
 */
public class ProductRepositoryTest
		extends AbstractRepositoryTest<RenewalOffer>
{
	@Autowired
	private ProductRepository<RenewalOffer> productRepository;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	private final LocalDate begin = LocalDate.of(2015, Month.JANUARY, 1);

	@Override
	protected Collection<RenewalOffer> getPersistedEntities()
	{
		List<RenewalOffer> allOffers = new ArrayList<>();

		LocalDateTime beginDateTime = begin.atStartOfDay();

		allOffers.add(paymentEntityFactory
				.createRenewalOffer("FriendshipOffer", beginDateTime, 12, RecommendationCategory.FRIENDSHIP));

		allOffers.add(paymentEntityFactory
				.createRenewalOffer("PartnershipOffer", beginDateTime, 12, RecommendationCategory.PARTNERSHIP));

		allOffers.add(paymentEntityFactory
				.createRenewalOffer("AllCategoriesOffer", beginDateTime, 12, RecommendationCategory.values()));

		productRepository.save(allOffers);

		return allOffers;
	}

	@Override
	protected JpaRepository<RenewalOffer, Long> getRepository()
	{
		return productRepository;
	}
}
