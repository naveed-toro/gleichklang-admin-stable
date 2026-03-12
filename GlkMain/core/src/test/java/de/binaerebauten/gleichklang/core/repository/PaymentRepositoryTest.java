package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link PaymentRepository}.
 */
public class PaymentRepositoryTest extends BasePersistenceTest
{
	@Autowired
	private InvoiceRepository invoiceRepository;
	
	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	private User user;
	
	private ExternalPayment payment;
	
	private Subscription subscription;

	@Before
	public void setup()
	{
		user = defaultEntityFactory.persistDefaultUser("test");

		final Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);
		payment = paymentEntityFactory.persistExternalPayment(invoice, PaymentState.PAID);
		
		SubscriptionOffer subscriptionOffer = paymentEntityFactory.persistInitialSubscriptionOffer(
				"InitialOffer", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);
		subscription = paymentEntityFactory.persistSubscription(user, subscriptionOffer, LocalDateTime.now());
		
		InvoiceItem invoiceItem = paymentEntityFactory.persistDefaultInvoiceItem(invoice, subscriptionOffer);
		invoiceItem.setSubscription(subscription);
		invoiceRepository.save(invoice);
	}

	@After
	public void teardown()
	{
		defaultEntityFactory.reset();
	}
	
	@Test
	public void testExistsPendingPayments()
	{
		assertThat(paymentRepository.existsPendingPayments(user), equalTo(false));
		
		payment.setState(PaymentState.PENDING);
		paymentRepository.save(payment);
		
		assertThat(paymentRepository.existsPendingPayments(user), equalTo(true));
	}
	
	@Test
	public void testExistsByUser()
	{
		assertThat(paymentRepository.existsByUser(user), equalTo(true));
		
		payment.setUser(defaultEntityFactory.persistDefaultUser("newUser"));
		paymentRepository.save(payment);
		
		assertThat(paymentRepository.existsByUser(user), equalTo(false));
	}
	
	@Test
	public void testExistsByUserAndState()
	{
		assertThat(paymentRepository.existsByUserAndState(user, PaymentState.PAID), equalTo(true));
		assertThat(paymentRepository.existsByUserAndState(user, PaymentState.PENDING), equalTo(false));
	}
	
	@Test
	public void testFindCurrentPayment()
	{
		assertThat(paymentRepository.findCurrentPayment(user), is(Optional.empty()));
		
		payment.setCurrent(true);
		payment = paymentRepository.save(payment);
		
		assertThat(paymentRepository.findCurrentPayment(user), is(Optional.of(payment)));
	}
	
	@Test
	public void testFindBySubscription()
	{
		assertThat(paymentRepository.findBySubscription(subscription).stream().findFirst().get(), is(payment));
	}
	
	@Test
	public void testFindByUser()
	{
		assertThat(paymentRepository.findByUser(user).iterator().next(), is(payment));
	}
}
