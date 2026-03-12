package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ActivateDeactiveSubscriptionRepository;
import de.binaerebauten.gleichklang.core.repository.SubscriptionRepository;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link SubscriptionService}.
 */
public class SubscriptionServiceTest
{
	private final static int EXPIRATION_PERIOD_IN_DAYS = 14;

	private SubscriptionService subscriptionService;

	@Mock
	private SubscriptionRepository subscriptionRepository;

	@Mock
	private ExternalPaymentService externalPaymentService;

	@Mock
	ActivateDeactiveSubscriptionRepository activateDeactiveSubscriptionRepository;

	@Mock
	private UserActivityService userActivityService;

	private User user;
	private Subscription subscription;

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		subscriptionService = new SubscriptionService(subscriptionRepository, null,
				externalPaymentService, userActivityService, null, null,
				EXPIRATION_PERIOD_IN_DAYS,activateDeactiveSubscriptionRepository);
		user = new User();

		subscription = new Subscription();
		LocalDateTime end = LocalDateTime.of(2016, Month.APRIL, 1, 0, 0);
		subscription.setEnd(end);
		subscription.setExpirationDate(end.plusDays(EXPIRATION_PERIOD_IN_DAYS));
	}

	@Test
	public void testCreateAndSaveSubscription()
	{
		SubscriptionOffer subscriptionOffer = new InitialSubscriptionOffer();

		int duration = 12;

		subscriptionOffer.setDuration(duration);
		subscriptionOffer.setDurationUnit(DurationUnit.MONTHS);
		
		when(subscriptionRepository.findCurrentSubscription(eq(user))).thenReturn(Optional.empty());

		THEN:
		{
			Subscription subscription = subscriptionService.createAndSaveSubscription(user, subscriptionOffer);

			assertThat(subscription.getBegin().plusMonths(duration), is(subscription.getEnd()));
			assertThat(subscription.getEnd().plusDays(EXPIRATION_PERIOD_IN_DAYS), is(subscription.getExpirationDate()));

			verify(subscriptionRepository).save(subscription);
		}
	}

	@Test
	public void testGetCurrentSubscriptionOfferCategories()
	{
		EnumSet<RecommendationCategory> categories = EnumSet.of(RecommendationCategory.FRIENDSHIP);

		when(subscriptionRepository.findCurrentSubscriptionOfferCategories(eq(user))).
				thenReturn(categories);

		THEN:
		{
			Set<RecommendationCategory> activeSubscriptionOfferCategories =
					subscriptionService.getCurrentSubscriptionOfferCategories(user);

			assertThat(activeSubscriptionOfferCategories, equalTo(categories));
		}
	}
	
	@Test
	public void testGetLastSubscriptionOfferCategories()
	{
		EnumSet<RecommendationCategory> categories = EnumSet.of(RecommendationCategory.FRIENDSHIP);
		
		when(subscriptionRepository.findLastSubscriptionOfferCategories(eq(user.getId()))).
				thenReturn(categories.stream().map(Enum::name).collect(Collectors.toList()));
		
		THEN:
		{
			Set<RecommendationCategory> activeSubscriptionOfferCategories =
					subscriptionService.getLastSubscriptionOfferCategories(user.getId());
			
			assertThat(activeSubscriptionOfferCategories, equalTo(categories));
		}
	}

	@Test
	public void testGetCurrentSubscriptionWithState_PENDING()
	{
		when(subscriptionRepository.findCurrentSubscription(eq(user))).thenReturn(Optional.empty());

		assertThat(subscriptionService.getCurrentSubscriptionWithState(user, LocalDateTime.now()),
				is(new SubscriptionWithState(null)));
	}

	@Test
	public void testGetCurrentSubscriptionWithState_ACTIVE()
	{
		when(subscriptionRepository.findCurrentSubscription(eq(user))).thenReturn(Optional.ofNullable(subscription));

		THEN:
		{
			assertThat(subscriptionService.getCurrentSubscriptionWithState(user, subscription.getEnd().minusDays(1)),
					is(new SubscriptionWithState(subscription)));
		}
	}

	@Test
	public void testGetCurrentSubscriptionWithState_EXPIRING()
	{
		when(subscriptionRepository.findCurrentSubscription(eq(user))).thenReturn(Optional.ofNullable(subscription));

		THEN:
		{
			assertThat(subscriptionService.getCurrentSubscriptionWithState(user, subscription.getEnd().plusDays(1)),
					is(new SubscriptionWithState(subscription)));

			assertThat(subscriptionService.getCurrentSubscriptionWithState(user, subscription.getEnd().plusDays(EXPIRATION_PERIOD_IN_DAYS - 1)),
					is(new SubscriptionWithState(subscription)));
		}
	}

	@Test
	public void testGetCurrentSubscriptionWithState_EXPIRED()
	{
		when(subscriptionRepository.findCurrentSubscription(eq(user))).thenReturn(Optional.ofNullable(subscription));

		THEN:
		{
			assertThat(subscriptionService.getCurrentSubscriptionWithState(user, subscription.getEnd().plusDays(EXPIRATION_PERIOD_IN_DAYS)),
					is(new SubscriptionWithState(subscription)));

			assertThat(subscriptionService.getCurrentSubscriptionWithState(user, subscription.getEnd().plusDays(EXPIRATION_PERIOD_IN_DAYS + 1)),
					is(new SubscriptionWithState(subscription)));
		}
	}

	@Test
	public void testGetCurrentSubscriptionWithState_CANCELED()
	{
		subscription.setExpirationDate(subscription.getEnd());
		when(subscriptionRepository.findCurrentSubscription(eq(user))).thenReturn(Optional.ofNullable(subscription));

		THEN:
		{
			assertThat(subscriptionService.getCurrentSubscriptionWithState(user, subscription.getEnd().plusDays(EXPIRATION_PERIOD_IN_DAYS)),
					is(new SubscriptionWithState(subscription)));

			assertThat(subscriptionService.getCurrentSubscriptionWithState(user, subscription.getEnd().plusDays(EXPIRATION_PERIOD_IN_DAYS + 1)),
					is(new SubscriptionWithState(subscription)));
		}
	}
}
