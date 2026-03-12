//package de.binaerebauten.gleichklang.core.repository;
//
//import de.binaerebauten.gleichklang.core.model.payment.Subscription;
//import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
//import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
//import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
//import de.binaerebauten.gleichklang.core.model.user.User;
//import de.binaerebauten.gleichklang.core.service.SubscriptionService;
//import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
//import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
//import org.hibernate.exception.ConstraintViolationException;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.Assert.assertThat;
//
//public class SubscriptionRepositoryTest extends AbstractRepositoryTest<Subscription>
//{
//	@Autowired
//	private ProductRepository<SubscriptionOffer> offerRespository;
//
//	@Autowired
//	private SubscriptionRepository subscriptionRepository;
//
//	@Autowired
//	private SubscriptionService subscriptionService;
//
//	@Autowired
//	private PaymentEntityFactory paymentEntityFactory;
//
//	@Autowired
//	private DefaultEntityFactory entityFactory;
//
//	private SubscriptionOffer offer;
//	private User activeSubscriptionUser;
//	private Subscription activeSubscription;
//
//	private final LocalDateTime begin = LocalDateTime.now().minus(6, ChronoUnit.MONTHS);
//
//
//	private final PageRequest pageRequest = new PageRequest(0, 5);
//
//	private final Map<SubscriptionState, Subscription> subscriptions = new HashMap<>();
//
//	@Rule
//	public ExpectedException expectedException = ExpectedException.none();
//
//	@Override
//	protected Collection<Subscription> getPersistedEntities()
//	{
//		offer = paymentEntityFactory.persistInitialSubscriptionOffer("InitialOffer", begin, RecommendationCategory.values());
//
//		for(SubscriptionState state : SubscriptionState.values())
//		{
//			final Subscription subscription = paymentEntityFactory.persistSubscription(entityFactory.persistDefaultUser(state.toString()), offer, begin);
//			subscription.setState(state);
//			subscriptions.put(state, subscriptionRepository.save(subscription));
//		}
//
//		activeSubscription = subscriptions.get(SubscriptionState.ACTIVE);
//		activeSubscriptionUser = activeSubscription.getUser();
//
//		return subscriptions.values();
//	}
//
//	@Override
//	protected JpaRepository<Subscription, Long> getRepository()
//	{
//		return subscriptionRepository;
//	}
//
//	@Test
//	public void testFindCurrentSubscription()
//	{
//		assertThat(subscriptionRepository.findCurrentSubscription(activeSubscriptionUser), equalTo(Optional.of(activeSubscription)));
//	}
//
//	@Test
//	public void testSaveAnotherCurrentSubscription()
//	{
//		final Subscription anotherActiveSubscription = paymentEntityFactory.createSubscription(activeSubscriptionUser, offer, begin);
//
//		expectedException.expect(DataIntegrityViolationException.class);
//		expectedException.expectCause(instanceOf(ConstraintViolationException.class));
//
//		subscriptionRepository.save(anotherActiveSubscription);
//	}
//
//	@Test
//	public void testSaveAnotherSubscriptionAndUpdateOldOne()
//	{
//		activeSubscription.setCurrent(null);
//		// unique indexes are non transactional in mysql
//		// so we first have to save the change in one transaction
//		subscriptionRepository.save(activeSubscription);
//
//		final Subscription newSubscription = paymentEntityFactory.createSubscription(activeSubscriptionUser, offer, begin);
//
//		// and then save the other subscriptionWithRegisteredUser separately
//		subscriptionRepository.save(newSubscription);
//
//		assertThat(subscriptionRepository.findCurrentSubscription(activeSubscriptionUser), equalTo(Optional.of(newSubscription)));
//	}
//
//	@Test
//	public void testFindExpiringCurrentSubscriptions()
//	{
//		final LocalDateTime end = activeSubscription.getEnd();
//
//		final Page<Subscription> subscriptionPage = subscriptionRepository.findSubscriptionStateTransitionToExpiring(end.plusSeconds(1), pageRequest);
//
//		assertThat(subscriptionPage.getTotalElements(), is(1L));
//	}
//
//	//@Test
//	public void testFindSubscriptionStateTransitionToExpired()
//	{
//		final LocalDateTime expiredDate = activeSubscription.getExpirationDate().plusDays(1);
//		final LocalDateTime expiringDate = activeSubscription.getExpirationDate().minusDays(1);
//
//		Page<Subscription> subscriptionPage = subscriptionRepository.findSubscriptionStateTransitionToExpired(expiredDate, pageRequest);
//		assertThat(subscriptionPage.getTotalElements(), is(1L));
//
//		subscriptionPage = subscriptionRepository.findSubscriptionStateTransitionToExpired(expiringDate, pageRequest);
//		assertThat(subscriptionPage.getTotalElements(), is(0L));
//	}
//
//	@Test
//	public void testFindCurrentSubscriptionOfferCategories()
//	{
//		Set<RecommendationCategory> categories = subscriptionRepository.findCurrentSubscriptionOfferCategories(activeSubscriptionUser);
//		assertThat(categories, equalTo(EnumSet.allOf(RecommendationCategory.class)));
//
//		final SubscriptionOffer partnerShipOffer = paymentEntityFactory.persistInitialSubscriptionOffer("InitialOffer", begin, RecommendationCategory.PARTNERSHIP);
//
//		activeSubscription.setOffer(partnerShipOffer);
//		subscriptionRepository.save(activeSubscription);
//
//		categories = subscriptionRepository.findCurrentSubscriptionOfferCategories(activeSubscriptionUser);
//		assertThat(categories, equalTo(EnumSet.of(RecommendationCategory.PARTNERSHIP)));
//	}
//
//	@Test
//	public void testFindCurrentSubscriptionOfferCategoriesWithDate()
//	{
//		Set<RecommendationCategory> categories = subscriptionRepository.findCurrentSubscriptionOfferCategories(activeSubscriptionUser.getId(), begin);
//		assertThat(categories, equalTo(EnumSet.allOf(RecommendationCategory.class)));
//
//		final SubscriptionOffer partnerShipOffer = paymentEntityFactory.persistInitialSubscriptionOffer("InitialOffer", begin, RecommendationCategory.PARTNERSHIP);
//
//		activeSubscription.setOffer(partnerShipOffer);
//		subscriptionRepository.save(activeSubscription);
//
//		categories = subscriptionRepository.findCurrentSubscriptionOfferCategories(activeSubscriptionUser.getId(), begin);
//		assertThat(categories, equalTo(EnumSet.of(RecommendationCategory.PARTNERSHIP)));
//	}
//
//	@Test
//	public void testFindLastSubscriptionOfferCategories()
//	{
//		Set<RecommendationCategory> categories = subscriptionRepository.findLastSubscriptionOfferCategories(activeSubscriptionUser.getId())
//				.stream().map(RecommendationCategory::valueOf).collect(Collectors.toSet());
//		assertThat(categories, equalTo(EnumSet.allOf(RecommendationCategory.class)));
//
//		final SubscriptionOffer partnerShipOffer = paymentEntityFactory.persistInitialSubscriptionOffer("InitialOffer", begin, RecommendationCategory.PARTNERSHIP);
//		activeSubscription.setOffer(partnerShipOffer);
//
//		subscriptionService.cancelSubscription(activeSubscription);
//
//		categories = subscriptionRepository.findLastSubscriptionOfferCategories(activeSubscriptionUser.getId())
//				.stream().map(RecommendationCategory::valueOf).collect(Collectors.toSet());
//		assertThat(categories, equalTo(EnumSet.of(RecommendationCategory.PARTNERSHIP)));
//	}
//
//	@Test
//	public void testCancelSubscription()
//	{
//		subscriptionService.cancelSubscription(activeSubscription);
//
//		assertThat(activeSubscription.getEnd(), is(activeSubscription.getExpirationDate()));
//		assertThat(activeSubscription.getEnd().isBefore(LocalDateTime.now()), is(true));
//	}
//}
