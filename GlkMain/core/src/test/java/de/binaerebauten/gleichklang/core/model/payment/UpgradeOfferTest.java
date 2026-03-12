package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link UpgradeOffer}.
 */
public class UpgradeOfferTest extends BaseSubscriptionOfferTest
{
	private InitialSubscriptionOffer initialSubscriptionOffer;
	private LocalDateTime initialSubscriptionBegin;
	private Subscription initialSubscription;

	@Before
	public void setup()
	{
		super.setup();

		initialSubscriptionOffer = paymentEntityFactory.createIntialSubscriptionOffer("Initial", LocalDateTime.now(), 12,
				RecommendationCategory.FRIENDSHIP);
		initialSubscriptionBegin = LocalDateTime.of(2015, Month.JANUARY, 1, 0, 0);
		initialSubscription = paymentEntityFactory.createSubscription(user, initialSubscriptionOffer, initialSubscriptionBegin);
	}

	@Test
	public void testGetAmount_TariffChange()
	{
		UpgradeOffer tariffChange = paymentEntityFactory.createUpgradeOffer(UpgradeType.TARIFF_CHANGE,
				"TariffChange", LocalDateTime.now(), 12);

		BigDecimal upgradeAmount = initialSubscriptionOffer.getAmount().getAmount().add(BigDecimal.ONE);
		tariffChange.setAmount(new MonetaryAmount(upgradeAmount, AvailableCurrency.EUR));

		MonetaryAmount amount = tariffChange.getAmount(initialSubscriptionBegin, initialSubscription);

		assertThat(amount.getAmount(), is(BigDecimal.ONE));
	}

	@Test
	public void testGetAmount_Donation()
	{
		UpgradeOffer tariffChange = paymentEntityFactory.createUpgradeOffer(UpgradeType.DONATION,
				"Donation", LocalDateTime.now(), 12);

		BigDecimal donationAmount = initialSubscriptionOffer.getAmount().getAmount().add(BigDecimal.ONE);
		tariffChange.setAmount(new MonetaryAmount(donationAmount, AvailableCurrency.EUR));

		MonetaryAmount amount = tariffChange.getAmount(initialSubscriptionBegin, initialSubscription);

		assertThat(amount.getAmount(), is(donationAmount));
	}

	@Test
	public void testGetAmount_CategoryExtension()
	{
		UpgradeOffer categoryExtension = paymentEntityFactory.createUpgradeOffer(UpgradeType.CATEGORY_EXTENSION,
				"CategoryExtension", LocalDateTime.now(), 12);

		BigDecimal fullAmount = new BigDecimal(12);
		categoryExtension.setAmount(new MonetaryAmount(fullAmount, AvailableCurrency.EUR));

		MonetaryAmount amount = categoryExtension.getAmount(initialSubscriptionBegin, initialSubscription);
		assertThat(amount.getAmount(), is(fullAmount));

		amount = categoryExtension.getAmount(initialSubscriptionBegin.plusDays(180), initialSubscription);
		assertThat(amount.getAmount().floatValue(), is(7f));

		amount = categoryExtension.getAmount(initialSubscriptionBegin.plusDays(360), initialSubscription);
		assertThat(amount.getAmount().floatValue(), is(1f));
	}

	@Test
	public void testCreateSubscription_CATEGORY_EXTENSION()
	{
		UpgradeOffer categoryExtension = paymentEntityFactory.createUpgradeOffer(UpgradeType.CATEGORY_EXTENSION,
				"CategoryExtension", LocalDateTime.now(), 12, RecommendationCategory.FRIENDSHIP, RecommendationCategory.PARTNERSHIP);

		LocalDateTime upgradeBegin = initialSubscriptionBegin.plusMonths(categoryExtension.getDuration() / 2);
		final Subscription upgradedSubscription = categoryExtension.createSubscription(upgradeBegin, initialSubscription);

		assertThat(upgradedSubscription.getBegin(), is(upgradeBegin));
		assertThat(upgradedSubscription.getEnd(), is(initialSubscription.getEnd()));
	}

	@Test
	public void testCreateSubscription_TARIFF_CHANGE()
	{
		UpgradeOffer tarfiffChange = paymentEntityFactory.createUpgradeOffer(UpgradeType.TARIFF_CHANGE,
				"TariffChange", LocalDateTime.now(), 12, RecommendationCategory.FRIENDSHIP, RecommendationCategory.PARTNERSHIP);

		LocalDateTime upgradeBegin = initialSubscriptionBegin.plusMonths(tarfiffChange.getDuration() / 2);
		final Subscription upgradedSubscription = tarfiffChange.createSubscription(upgradeBegin, initialSubscription);

		assertThat(upgradedSubscription.getBegin(), is(upgradeBegin));
		assertThat(upgradedSubscription.getEnd(), is(initialSubscription.getEnd()));
	}

	@Test
	public void testCreateSubscription_DONATION()
	{
		UpgradeOffer donationOffer = paymentEntityFactory.createUpgradeOffer(UpgradeType.DONATION,
				"Donation", LocalDateTime.now(), 12, RecommendationCategory.FRIENDSHIP, RecommendationCategory.PARTNERSHIP);

		LocalDateTime upgradeBegin = initialSubscriptionBegin.plusMonths(donationOffer.getDuration() / 2);
		final Subscription upgradedSubscription = donationOffer.createSubscription(upgradeBegin, initialSubscription);

		assertThat(upgradedSubscription.getBegin(), is(upgradeBegin));
		assertThat(upgradedSubscription.getEnd(), is(upgradeBegin.plusMonths(donationOffer.getDuration())));
	}
}
