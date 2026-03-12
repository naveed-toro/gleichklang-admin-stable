package de.binaerebauten.gleichklang.core.service.affiliate;

import de.binaerebauten.gleichklang.core.model.affiliate.AffiliatePartner;
import de.binaerebauten.gleichklang.core.model.affiliate.AffiliatePaymentState;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AffiliatePaymentStateRepository;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.InvoiceRepository;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AffiliatePaymentServiceTest extends BasePersistenceTest
{
	@Autowired
	private AffiliatePaymentStateRepository affiliatePaymentStateRepository;
	
	@Autowired
	private InvoiceRepository invoiceRepository;
	
	@Autowired
	private AffiliatePaymentService affiliatePaymentService;
	
	@Autowired
	private PaymentService paymentService;
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	@Autowired
	private PaymentEntityFactory paymentEntityFactory;
	
	@Before
	public void setUp() throws Exception
	{
		defaultEntityFactory.reset();
	}
	
	@After
	public void tearDown() throws Exception
	{
		defaultEntityFactory.reset();
	}
	
	@Test
	public void testProlong()
	{
		final User user = defaultEntityFactory.persistDefaultUser("test user");
		
		// Create a test affiliate payment state record
		final Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);
		final AbstractPayment payment = paymentEntityFactory.persistExternalPayment(invoice, PaymentState.PAID);
		final List<AffiliatePaymentState> affiliatePaymentStates = createAffiliatePaymentState(payment);
		
		final SubscriptionOffer subscriptionOffer = paymentEntityFactory.persistInitialSubscriptionOffer("InitialOffer", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);
		final Subscription existingSubscription = paymentEntityFactory.persistSubscription(user, subscriptionOffer, LocalDateTime.now());
		
		final InvoiceItem invoiceItem = paymentEntityFactory.persistDefaultInvoiceItem(invoice, subscriptionOffer);
		invoiceItem.setSubscription(existingSubscription);
		invoiceRepository.save(invoice);
		
		final int expectedSize = AffiliatePartner.values().length;
		
		assertThat(expectedSize > 1, equalTo(true));
		assertThat(affiliatePaymentStates.size(), equalTo(expectedSize));
		assertThat("One affiliate payment state stored", affiliatePaymentStateRepository.findAll().size(), equalTo(expectedSize));
		
		// Simulate subscription prolongation
		final Invoice newInvoice = paymentEntityFactory.persistDefaultInvoice(user);
		final AbstractPayment newPayment = paymentService.createAndSavePayment(newInvoice, PaymentMethod.DIRECT_DEBIT, new MonetaryAmount(BigDecimal.ONE, AvailableCurrency.EUR), true);
		
		affiliatePaymentService.prolong(existingSubscription, newPayment);
		
		assertThat("Affiliate payment state stored for the next subscription", affiliatePaymentStateRepository.findAll().size(), equalTo(expectedSize * 2));
	}
	
	private List<AffiliatePaymentState> createAffiliatePaymentState(AbstractPayment payment)
	{
		final List<AffiliatePaymentState> affiliatePaymentStates = new ArrayList<>();
		for(AffiliatePartner affiliatePartner : AffiliatePartner.values())
		{
			final AffiliatePaymentState affiliatePaymentState = new AffiliatePaymentState();
			affiliatePaymentState.setPartner(affiliatePartner);
			affiliatePaymentState.setPayment(payment);
			affiliatePaymentState.setExternalId("external-id");
			affiliatePaymentState.setPaidToPartner(false);
			
			affiliatePaymentStates.add(affiliatePaymentState);
		}
		
		return affiliatePaymentStateRepository.save(affiliatePaymentStates);
	}
	
}