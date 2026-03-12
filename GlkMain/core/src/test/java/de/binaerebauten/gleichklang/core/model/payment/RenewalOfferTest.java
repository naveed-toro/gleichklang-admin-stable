package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link RenewalOffer}.
 */
public class RenewalOfferTest extends BaseSubscriptionOfferTest
{
	private RenewalOffer renewalOffer;

	@Override
	public void setup()
	{
		super.setup();

		renewalOffer = paymentEntityFactory
				.createRenewalOffer("Renewal", LocalDateTime.now(), 12, RecommendationCategory.FRIENDSHIP, RecommendationCategory.PARTNERSHIP);
	}

	@Test
	public void testCreateSubscription()
	{
		LocalDateTime renewalDate = LocalDateTime.of(2015, Month.JANUARY, 1, 0, 0);
		final Subscription renewedSubscription = renewalOffer.createSubscription(renewalDate, null);

		assertThat(renewedSubscription.getBegin(), is(renewalDate));
		assertThat(renewedSubscription.getEnd(), is(renewalDate.plusMonths(renewalOffer.getDuration())));
	}

	@Test
	public void testGetAutoRenewalOffer()
	{
		assertThat(renewalOffer.getAutoRenewalOffer(), is(renewalOffer));

		RenewalOffer anotherRenewalOffer = paymentEntityFactory
				.createRenewalOffer("Renewal", LocalDateTime.now(), 12, RecommendationCategory.FRIENDSHIP, RecommendationCategory.PARTNERSHIP);

		renewalOffer.setAutoRenewalOffer(anotherRenewalOffer);
		assertThat(renewalOffer.getAutoRenewalOffer(), is(anotherRenewalOffer));
	}
}
