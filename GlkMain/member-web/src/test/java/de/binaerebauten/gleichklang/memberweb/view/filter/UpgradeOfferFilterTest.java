//package de.binaerebauten.gleichklang.memberweb.view.filter;
//
//import de.binaerebauten.gleichklang.core.model.payment.*;
//import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
//import de.binaerebauten.gleichklang.core.repository.ProductRepository;
//import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
//import de.binaerebauten.gleichklang.core.view.filter.AbstractFilterTest;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.assertThat;
//
///**
// * Unit test for {@link UpgradeOfferFilter}
// */
//public class UpgradeOfferFilterTest extends AbstractFilterTest<UpgradeOffer, UpgradeOfferFilter>
//{
//	@Autowired
//	private ProductRepository<UpgradeOffer> upgradeOfferRespository;
//
//	@Autowired
//	private ProductRepository<InitialSubscriptionOffer> initialSubscriptionOfferRespository;
//
//	@Autowired
//	private PaymentEntityFactory paymentEntityFactory;
//
//	private InitialSubscriptionOffer upgradeableOffer;
//
//	private UpgradeOffer upgradeOfferWithUpgradeableOffer;
//
//	private UpgradeOffer upgradeOfferWithoutUpgradeableOffer;
//
//	@Before
//	public void setup()
//	{
//		upgradeableOffer = paymentEntityFactory.persistInitialSubscriptionOffer("UpgradeableOffer", LocalDateTime.now(),
//				RecommendationCategory.FRIENDSHIP);
//
//		super.setup();
//	}
//
//	@Test
//	public void testWithUpgradeableOffer()
//	{
//		UpgradeOfferFilter upgradeOfferFilter = new UpgradeOfferFilter(null, upgradeableOffer);
//
//		List<UpgradeOffer> upgradeOffers = upgradeOfferRespository.findAll(upgradeOfferFilter);
//
//		assertThat(upgradeOffers.size(), is(1));
//		assertThat(upgradeOffers, is(Collections.singletonList(upgradeOfferWithUpgradeableOffer)));
//	}
//
//	@Override
//	protected Collection<UpgradeOffer> getPersistedEntities()
//	{
//		List<UpgradeOffer> persistedEntities = new ArrayList<>();
//
//		upgradeOfferWithUpgradeableOffer = paymentEntityFactory.persistUpgradeOffer(UpgradeType.TARIFF_CHANGE, "WithUgradeableOffer", LocalDateTime.now(), 12,
//				RecommendationCategory.FRIENDSHIP);
//		upgradeOfferWithUpgradeableOffer.getSubscriptionOffers().add(upgradeableOffer);
//		persistedEntities.add(upgradeOfferWithUpgradeableOffer);
//
//		upgradeOfferWithoutUpgradeableOffer = paymentEntityFactory.persistUpgradeOffer(UpgradeType.TARIFF_CHANGE, "WithoutUgradeableOffer", LocalDateTime.now(), 12,
//				RecommendationCategory.FRIENDSHIP);
//		persistedEntities.add(upgradeOfferWithoutUpgradeableOffer);
//
//		upgradeOfferRespository.save(persistedEntities);
//
//		return persistedEntities;
//	}
//
//	@Override
//	protected JpaSpecificationExecutor<UpgradeOffer> getSpecificationExecutor()
//	{
//		return upgradeOfferRespository;
//	}
//}
