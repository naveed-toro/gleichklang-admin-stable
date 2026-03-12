package de.binaerebauten.gleichklang.core.view.filter;

import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.Tariff;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link InitialSubscriptionOfferFilter}.
 */
public class InitialSubscriptionOfferFilterTest
		extends AbstractFilterTest<InitialSubscriptionOffer, InitialSubscriptionOfferFilter>
{
	public static final String SOCIAL_ACTION_CODE = "SZ-2016";
	@Autowired
	private ProductRepository<InitialSubscriptionOffer> initialSubscriptionOfferRepository;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	private final LocalDate begin = LocalDate.of(2015, Month.JANUARY, 1);

	private final static String ACTION_CODE = "ACTION_CODE";

	private List<SubscriptionOffer> allOffers;

	private InitialSubscriptionOffer actionCodeOffer;

	private InitialSubscriptionOffer socialOffer;

	private InitialSubscriptionOffer friendshipOffer;

	private InitialSubscriptionOffer partnershipOffer;

	private InitialSubscriptionOffer allCategoriesOffer;

	private InitialSubscriptionOffer limitedFriendshipOffer;

	@Test
	public void testWithActionCode()
			throws Exception
	{
		InitialSubscriptionOfferFilter filter = createInitialSubscriptionOfferFilter(begin, ACTION_CODE,
				true, RecommendationCategory.FRIENDSHIP);

		List<InitialSubscriptionOffer> validInitialOffers = findAll(filter);
		assertThat(validInitialOffers, equalTo(Collections.singletonList(actionCodeOffer)));
	}

	@Test
	public void testWithSocialCode()
			throws Exception
	{
		InitialSubscriptionOfferFilter filter = createInitialSubscriptionOfferFilter(begin, SOCIAL_ACTION_CODE,
				true, RecommendationCategory.FRIENDSHIP);

		List<InitialSubscriptionOffer> validInitialOffers = findAll(filter);
		assertThat(validInitialOffers, equalTo(Collections.singletonList(socialOffer)));
	}

	@Test
	public void testWithInvalidSocialCode()
			throws Exception
	{
		InitialSubscriptionOfferFilter filter = createInitialSubscriptionOfferFilter(begin, "SZ-", false,
				RecommendationCategory.FRIENDSHIP);

		List<InitialSubscriptionOffer> validInitialOffers = findAll(filter);
		assertThat(validInitialOffers.size(), is(3));
		assertThat(validInitialOffers, hasItems(friendshipOffer, limitedFriendshipOffer, socialOffer));
	}

	@Test
	public void testWithCategories()
			throws Exception
	{
		InitialSubscriptionOfferFilter filter = createInitialSubscriptionOfferFilter(begin, RecommendationCategory.FRIENDSHIP);

		List<InitialSubscriptionOffer> validInitialOffers = findAll(filter);
		assertThat(validInitialOffers, hasItems(friendshipOffer, limitedFriendshipOffer));

		filter = createInitialSubscriptionOfferFilter(begin, RecommendationCategory.PARTNERSHIP);
		validInitialOffers = initialSubscriptionOfferRepository.findAll(filter);
		assertThat(validInitialOffers, equalTo(Collections.singletonList(partnershipOffer)));

		filter = createInitialSubscriptionOfferFilter(begin, RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);

		validInitialOffers = initialSubscriptionOfferRepository.findAll(filter);
		assertThat(validInitialOffers, equalTo(Collections.singletonList(allCategoriesOffer)));
	}

	@Override
	protected Collection<InitialSubscriptionOffer> getPersistedEntities()
	{
		List<InitialSubscriptionOffer> offers = new ArrayList<>();

		LocalDateTime beginDateTime = begin.atStartOfDay();

		friendshipOffer = paymentEntityFactory
				.persistInitialSubscriptionOffer("FriendshipOffer", beginDateTime, RecommendationCategory.FRIENDSHIP);
		offers.add(friendshipOffer);

		partnershipOffer = paymentEntityFactory
				.persistInitialSubscriptionOffer("PartnershipOffer", beginDateTime, RecommendationCategory.PARTNERSHIP);
		offers.add(partnershipOffer);

		allCategoriesOffer = paymentEntityFactory
				.persistInitialSubscriptionOffer("AllCategoriesOffer", beginDateTime, RecommendationCategory.values());
		offers.add(allCategoriesOffer);

		limitedFriendshipOffer = paymentEntityFactory
				.persistInitialSubscriptionOffer("LimitedOffer", beginDateTime, RecommendationCategory.FRIENDSHIP);
		limitedFriendshipOffer.setEnd(beginDateTime.plusWeeks(4));
		offers.add(limitedFriendshipOffer);

		actionCodeOffer = paymentEntityFactory
				.persistInitialSubscriptionOffer("ActionCodeOffer", beginDateTime, RecommendationCategory.FRIENDSHIP);
		actionCodeOffer.setActionCode(ACTION_CODE);
		offers.add(actionCodeOffer);

		socialOffer = paymentEntityFactory.persistInitialSubscriptionOffer("SocualOffer", beginDateTime, RecommendationCategory.FRIENDSHIP);
		socialOffer.setTariff(Tariff.SOCIAL);
		socialOffer.setActionCode(SOCIAL_ACTION_CODE);
		offers.add(socialOffer);

		initialSubscriptionOfferRepository.save(offers);

		return offers;
	}

	@Override
	protected JpaSpecificationExecutor<InitialSubscriptionOffer> getSpecificationExecutor()
	{
		return initialSubscriptionOfferRepository;
	}

	private InitialSubscriptionOfferFilter createInitialSubscriptionOfferFilter(LocalDate date, String actionCode, boolean isValidActionCode,
			RecommendationCategory... categories)
	{
		EnumSet<RecommendationCategory> categoriesAsSet = EnumSet.copyOf(Arrays.asList(categories));
		InitialSubscriptionOfferFilter filter = new InitialSubscriptionOfferFilter(date, categoriesAsSet, actionCode, isValidActionCode);

		return filter;

	}

	private InitialSubscriptionOfferFilter createInitialSubscriptionOfferFilter(LocalDate date, RecommendationCategory... categories)
	{
		return createInitialSubscriptionOfferFilter(date, null, false, categories);
	}
}
