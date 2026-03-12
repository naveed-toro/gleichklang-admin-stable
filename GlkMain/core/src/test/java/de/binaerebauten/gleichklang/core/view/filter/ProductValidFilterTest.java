package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link ProductValidFilter}.
 */
public class ProductValidFilterTest
		extends AbstractFilterTest<SubscriptionOffer, ProductValidFilter<SubscriptionOffer>>
{
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	private final LocalDate begin = LocalDate.of(2015, Month.JANUARY, 1);

	private List<SubscriptionOffer> allOffers;

	private InitialSubscriptionOffer friendshipOffer;

	private InitialSubscriptionOffer partnershipOffer;

	private InitialSubscriptionOffer allCategoriesOffer;

	private InitialSubscriptionOffer limitedFriendshipOffer;

	@Test
	public void testWithDate() throws Exception
	{
		ProductValidFilter<SubscriptionOffer> filter = new ProductValidFilter<>(begin);

		List<SubscriptionOffer> validInitialOffers = findAll(filter);
		assertThat(validInitialOffers, hasItems(friendshipOffer, partnershipOffer, allCategoriesOffer, limitedFriendshipOffer));

		LocalDate end = limitedFriendshipOffer.getEnd().toLocalDate();
		filter = new ProductValidFilter<>(end);
		validInitialOffers = findAll(filter);
		assertThat(validInitialOffers, hasItems(friendshipOffer, partnershipOffer, allCategoriesOffer, limitedFriendshipOffer));

		filter = new ProductValidFilter<>(end.plusDays(1));
		validInitialOffers = findAll(filter);
		assertThat(validInitialOffers, hasItems(friendshipOffer, partnershipOffer, allCategoriesOffer));
		assertThat(validInitialOffers, not(hasItem(limitedFriendshipOffer)));

		filter = new ProductValidFilter<>(begin.minusDays(1));
		validInitialOffers = findAll(filter);
		assertThat(validInitialOffers.isEmpty(), is(true));
	}

	@Override
	protected Collection<SubscriptionOffer> getPersistedEntities()
	{
		allOffers = new ArrayList<>();

		LocalDateTime beginDateTime = begin.atStartOfDay();

		friendshipOffer = paymentEntityFactory
				.persistInitialSubscriptionOffer("FriendshipOffer", beginDateTime, RecommendationCategory.FRIENDSHIP);
		allOffers.add(friendshipOffer);

		partnershipOffer = paymentEntityFactory
				.persistInitialSubscriptionOffer("PartnershipOffer", beginDateTime, RecommendationCategory.PARTNERSHIP);
		allOffers.add(partnershipOffer);

		allCategoriesOffer = paymentEntityFactory
				.persistInitialSubscriptionOffer("AllCategoriesOffer", beginDateTime, RecommendationCategory.values());
		allOffers.add(allCategoriesOffer);
		limitedFriendshipOffer = paymentEntityFactory
				.persistInitialSubscriptionOffer("LimitedOffer", beginDateTime, RecommendationCategory.FRIENDSHIP);
		limitedFriendshipOffer.setEnd(beginDateTime.plusWeeks(4));
		allOffers.add(limitedFriendshipOffer);

		productRepository.save(allOffers);

		return allOffers;
	}

	@Override
	protected JpaSpecificationExecutor<SubscriptionOffer> getSpecificationExecutor()
	{
		return productRepository;
	}
}
