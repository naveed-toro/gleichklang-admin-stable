package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link InitialSubscriptionOffer}.
 */
public class InitialSubscriptionOfferTest extends BaseSubscriptionOfferTest
{
	@Test
	public void testCreateSubscription()
	{
		InitialSubscriptionOffer initialSubscriptionOffer = paymentEntityFactory.createIntialSubscriptionOffer(
				"Initial", LocalDateTime.now(), 12, RecommendationCategory.FRIENDSHIP, RecommendationCategory.PARTNERSHIP);

		LocalDateTime renewalDate = LocalDateTime.of(2015, Month.JANUARY, 1, 0, 0);
		final Subscription renewedSubscription = initialSubscriptionOffer.createSubscription(renewalDate, null);

		assertThat(renewedSubscription.getBegin(), is(renewalDate));
		assertThat(renewedSubscription.getEnd(), is(renewalDate.plusMonths(initialSubscriptionOffer.getDuration())));
	}
}
