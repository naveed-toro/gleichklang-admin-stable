package de.binaerebauten.gleichklang.adminweb.view.filter;

import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.Product;
import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.payment.RenewalOffer;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import de.binaerebauten.gleichklang.core.view.filter.AbstractFilterTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class ProductAdminFilterTest
		extends AbstractFilterTest<Product, ProductAdminFilter<Product>>
{
	@Autowired
	private ProductRepository<Product> productRepository;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	private InitialSubscriptionOffer actionCodeOffer;
	private InitialSubscriptionOffer offerWithoutActionCode;
	private RenewalOffer renewalOffer;

	private final ProductAdminFilter<Product> emptyFilter = new ProductAdminFilter<>();

	@Test
	public void testWithActionCodePrefix()
	{
		ProductAdminFilter<Product> actionCodePrefixFilter = emptyFilter.withActionCodePrefix("A");

		List<Product> offers = findAll(actionCodePrefixFilter);

		assertThat(offers.size(), is(1));
		assertThat(offers, hasItem(actionCodeOffer));
	}

	@Test
	public void testWithDontCareActionCodePrefix()
	{
		ProductAdminFilter<Product> actionCodePrefixFilter = emptyFilter.withActionCodePrefix("");

		List<Product> offers = findAll(actionCodePrefixFilter);

		assertThat(offers.size(), is(3));
		assertThat(offers, hasItems(actionCodeOffer, offerWithoutActionCode, renewalOffer));

		actionCodePrefixFilter = emptyFilter.withActionCodePrefix(null);

		offers = findAll(actionCodePrefixFilter);

		assertThat(offers.size(), is(3));
		assertThat(offers, hasItems(actionCodeOffer, offerWithoutActionCode, renewalOffer));
	}

	@Test
	public void testWithNamePrefix()
	{
		ProductAdminFilter<Product> namePrefixFilter = emptyFilter.withNamePrefix("A");

		List<Product> offers = findAll(namePrefixFilter);

		assertThat(offers.size(), is(1));
		assertThat(offers, hasItem(actionCodeOffer));

		namePrefixFilter = emptyFilter.withNamePrefix("O");

		offers = findAll(namePrefixFilter);

		assertThat(offers.size(), is(1));
		assertThat(offers, hasItem(offerWithoutActionCode));
	}

	@Test
	public void testWithTypes()
	{
		ProductAdminFilter<Product> typeFilter = emptyFilter.withTypes(ProductType.RENEWAL_OFFER);

		List<Product> offers = findAll(typeFilter);

		assertThat(offers.size(), is(1));
		assertThat(offers, hasItem(renewalOffer));

		typeFilter = emptyFilter.withTypes(ProductType.INITIAL_SUBSCRIPTION_OFFER);
		offers = findAll(typeFilter);

		assertThat(offers.size(), is(2));
		assertThat(offers, hasItem(actionCodeOffer));
		assertThat(offers, hasItem(offerWithoutActionCode));
	}

	@Override
	protected Collection<Product> getPersistedEntities()
	{
		List<Product> subscriptionOffers = new ArrayList<>();

		renewalOffer = paymentEntityFactory.createRenewalOffer("Renewal", LocalDateTime.now(), 12, RecommendationCategory.FRIENDSHIP);
		subscriptionOffers.add(renewalOffer);

		actionCodeOffer = paymentEntityFactory.createIntialSubscriptionOffer("ActionCodeOffer", LocalDateTime.now(), 12,
				RecommendationCategory.FRIENDSHIP);
		actionCodeOffer.setActionCode("ActionCode");
		actionCodeOffer.setAutoRenewalOffer(renewalOffer);
		subscriptionOffers.add(actionCodeOffer);

		offerWithoutActionCode = paymentEntityFactory.createIntialSubscriptionOffer("OfferWithoutActionCode", LocalDateTime.now(), 12,
				RecommendationCategory.FRIENDSHIP);
		offerWithoutActionCode.setAutoRenewalOffer(renewalOffer);
		subscriptionOffers.add(offerWithoutActionCode);

		productRepository.save(subscriptionOffers);

		return subscriptionOffers;
	}

	@Override
	protected JpaSpecificationExecutor<Product> getSpecificationExecutor()
	{
		return productRepository;
	}
}
