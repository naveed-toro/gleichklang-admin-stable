//package de.binaerebauten.gleichklang.memberweb.service;
//
//import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
//import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
//import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
//import de.binaerebauten.gleichklang.core.repository.ProductRepository;
//import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
//import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.time.LocalDateTime;
//import java.util.EnumSet;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.assertThat;
//
///**
// * Unit tests for {@link SubscriptionOfferService}.
// */
//public class SubscriptionOfferServiceTest extends BasePersistenceTest
//{
//	private static final String ACTION_CODE = "ACTION_CODE";
//
//	private SubscriptionOfferService subscriptionOfferService;
//
//	@Autowired
//	private ProductRepository<InitialSubscriptionOffer> initialSubscriptionOfferRepository;
//
//	@Autowired
//	private DefaultEntityFactory defaultEntityFactory;
//
//	@Autowired
//	private PaymentEntityFactory paymentEntityFactory;
//
//	@Before
//	public void setup()
//	{
//		subscriptionOfferService = new SubscriptionOfferService(initialSubscriptionOfferRepository);
//
//		LocalDateTime begin = LocalDateTime.now().minusDays(1L);
//
//		paymentEntityFactory.persistInitialSubscriptionOffer("TestOffer", begin, RecommendationCategory.PARTNERSHIP);
//
//		InitialSubscriptionOffer testOfferWithActionCode =
//				paymentEntityFactory.persistInitialSubscriptionOffer("TestOfferWithActionCode", begin, RecommendationCategory.PARTNERSHIP);
//
//		testOfferWithActionCode.setActionCode(ACTION_CODE);
//		initialSubscriptionOfferRepository.save(testOfferWithActionCode);
//	}
//
//	@After
//	public void teardown()
//	{
//		defaultEntityFactory.reset();
//	}
//
//	@Test
//	public void testIsValidActionCode()
//	{
//		assertThat(subscriptionOfferService.isValidActionCode(ACTION_CODE,
//				EnumSet.of(RecommendationCategory.PARTNERSHIP)), is(true));
//
//		assertThat(subscriptionOfferService.isValidActionCode(ACTION_CODE,
//				EnumSet.of(RecommendationCategory.FRIENDSHIP)), is(false));
//
//		assertThat(subscriptionOfferService.isValidActionCode(ACTION_CODE + "2",
//				EnumSet.of(RecommendationCategory.PARTNERSHIP)), is(false));
//
//		assertThat(subscriptionOfferService.isValidActionCode(ACTION_CODE,
//				EnumSet.allOf(RecommendationCategory.class)), is(false));
//	}
//}
