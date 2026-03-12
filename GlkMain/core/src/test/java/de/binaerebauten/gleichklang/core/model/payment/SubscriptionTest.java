package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.SubscriptionRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit test to make sure that setting the state of a subscription works correctly.
 */
public class SubscriptionTest extends BasePersistenceTest
{
	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private Subscription persistedSubscription;

	@Before
	public void setup()
	{
		User user = defaultEntityFactory.persistDefaultUser("test");
		SubscriptionOffer subscriptionOffer = paymentEntityFactory.persistInitialSubscriptionOffer("Test", LocalDateTime.now(), RecommendationCategory.FRIENDSHIP);

		persistedSubscription = paymentEntityFactory.persistSubscription(user, subscriptionOffer, LocalDateTime.now());
	}

	@After
	public void teardown()
	{
		defaultEntityFactory.reset();
	}

	@Test
	public void testSetActive()
	{
		Subscription subscription = new Subscription();

		assertThat(subscription.getCurrent(), is(Boolean.TRUE));

		subscription.setCurrent(null);

		assertThat(subscription.getCurrent(), nullValue());

		expectedException.expect(IllegalArgumentException.class);
		subscription.setCurrent(Boolean.FALSE);
	}

	@Test
	public void testIsAutoRenewalChanged()
	{
		persistedSubscription = subscriptionRepository.findOne(persistedSubscription.getId());
		persistedSubscription.setAutomaticRenewal(true);
		assertThat(persistedSubscription.isAutomaticRenewalChanged(), equalTo(false));

		persistedSubscription = subscriptionRepository.findOne(persistedSubscription.getId());
		persistedSubscription.setAutomaticRenewal(false);
		assertThat(persistedSubscription.isAutomaticRenewalChanged(), equalTo(true));

		subscriptionRepository.save(persistedSubscription);
		persistedSubscription = subscriptionRepository.findOne(persistedSubscription.getId());
		persistedSubscription.setAutomaticRenewal(true);
		assertThat(persistedSubscription.isAutomaticRenewalChanged(), equalTo(true));

		subscriptionRepository.save(persistedSubscription);
		persistedSubscription = subscriptionRepository.findOne(persistedSubscription.getId());
		persistedSubscription.setAutomaticRenewal(true);
		assertThat(persistedSubscription.isAutomaticRenewalChanged(), equalTo(false));
	}
}
