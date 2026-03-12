//package de.binaerebauten.gleichklang.memberweb.view.filter;
//
//import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
//import de.binaerebauten.gleichklang.core.model.payment.ServiceOffer;
//import de.binaerebauten.gleichklang.core.model.payment.UpgradeOffer;
//import de.binaerebauten.gleichklang.core.model.payment.UpgradeType;
//import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
//import de.binaerebauten.gleichklang.core.repository.ProductRepository;
//import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
//import de.binaerebauten.gleichklang.core.view.filter.AbstractFilterTest;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//
//import static org.hamcrest.CoreMatchers.hasItem;
//import static org.hamcrest.CoreMatchers.hasItems;
//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.assertThat;
//
///**
// * Unit test for {@link ServiceOfferFilter}
// */
//public class ServiceOfferFilterTest extends AbstractFilterTest<ServiceOffer, ServiceOfferFilter>
//{
//	@Autowired
//	private ProductRepository<ServiceOffer> serviceOfferRespository;
//
//	@Autowired
//	private ProductRepository<InitialSubscriptionOffer> initialSubscriptionOfferRespository;
//
//	@Autowired
//	private PaymentEntityFactory paymentEntityFactory;
//
//	private InitialSubscriptionOffer friendshipOffer;
//
//	private InitialSubscriptionOffer partnershipOffer;
//
//	private InitialSubscriptionOffer allCategoriesOffer;
//
//	private ServiceOffer withoutRequiredCategories;
//
//	private ServiceOffer withFriendshipRequired;
//
//	private ServiceOffer withPartnershipRequired;
//
//	private ServiceOffer withAllCategoriesRequired;
//
//	@Before
//	public void setup()
//	{
//		friendshipOffer = paymentEntityFactory.persistInitialSubscriptionOffer("FriendshipOffer", LocalDateTime.now(),
//				RecommendationCategory.FRIENDSHIP);
//
//		partnershipOffer = paymentEntityFactory.persistInitialSubscriptionOffer("PartnershipOffer", LocalDateTime.now(),
//				RecommendationCategory.PARTNERSHIP);
//
//		allCategoriesOffer = paymentEntityFactory.persistInitialSubscriptionOffer("AllCategoriesOffer", LocalDateTime.now(),
//				RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);
//
//		super.setup();
//	}
//
//	@Test
//	public void testWithFriendshipOffer()
//	{
//		ServiceOfferFilter serviceOfferFilter = new ServiceOfferFilter(null, friendshipOffer);
//
//		List<ServiceOffer> serviceOffers = serviceOfferRespository.findAll(serviceOfferFilter);
//
//		assertThat(serviceOffers.size(), is(2));
//		assertThat(serviceOffers, hasItems(withoutRequiredCategories, withFriendshipRequired));
//	}
//
//	@Test
//	public void testWithPartnershipOffer()
//	{
//		ServiceOfferFilter serviceOfferFilter = new ServiceOfferFilter(null, partnershipOffer);
//
//		List<ServiceOffer> serviceOffers = serviceOfferRespository.findAll(serviceOfferFilter);
//
//		assertThat(serviceOffers.size(), is(2));
//		assertThat(serviceOffers, hasItems(withoutRequiredCategories, withPartnershipRequired));
//	}
//
//	@Test
//	public void testWithAllCategoriesOffer()
//	{
//		ServiceOfferFilter serviceOfferFilter = new ServiceOfferFilter(null, allCategoriesOffer);
//
//		List<ServiceOffer> serviceOffers = serviceOfferRespository.findAll(serviceOfferFilter);
//
//		assertThat(serviceOffers.size(), is(4));
//		assertThat(serviceOffers, hasItems(withoutRequiredCategories, withAllCategoriesRequired, withPartnershipRequired, withFriendshipRequired));
//	}
//
//	@Override
//	protected Collection<ServiceOffer> getPersistedEntities()
//	{
//		List<ServiceOffer> persistedEntities = new ArrayList<>();
//
//		withoutRequiredCategories = paymentEntityFactory.createServiceOffer("WithoutRequiredCategories", LocalDateTime.now(), 12);
//		serviceOfferRespository.save(withoutRequiredCategories);
//		persistedEntities.add(withoutRequiredCategories);
//
//		withFriendshipRequired = paymentEntityFactory.createServiceOffer("WithFriendshipRequired", LocalDateTime.now(), 12,
//				RecommendationCategory.FRIENDSHIP);
//		serviceOfferRespository.save(withFriendshipRequired);
//		persistedEntities.add(withFriendshipRequired);
//
//		withPartnershipRequired = paymentEntityFactory.createServiceOffer("WithPartnershipRequired", LocalDateTime.now(), 12,
//				RecommendationCategory.PARTNERSHIP);
//		serviceOfferRespository.save(withPartnershipRequired);
//		persistedEntities.add(withPartnershipRequired);
//
//		withAllCategoriesRequired = paymentEntityFactory.createServiceOffer("WithAllCategoriesRequired", LocalDateTime.now(), 12,
//				RecommendationCategory.PARTNERSHIP, RecommendationCategory.FRIENDSHIP);
//		serviceOfferRespository.save(withAllCategoriesRequired);
//		persistedEntities.add(withAllCategoriesRequired);
//
//		return persistedEntities;
//	}
//
//	@Override
//	protected JpaSpecificationExecutor<ServiceOffer> getSpecificationExecutor()
//	{
//		return serviceOfferRespository;
//	}
//}
