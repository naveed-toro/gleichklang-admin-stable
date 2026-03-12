package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.*;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link HeidelpayTransactionService}.
 */
public class HeidelpayTransactionServiceTest extends BasePersistenceTest
{
	private HeidelpayTransactionService heidelpayTransactionService;

	@Autowired
	private HeidelpayTransactionRepository heidelpayTransactionRepository;

	@Autowired
	private ExternalPaymentRepository externalPaymentRepository;

	@Autowired
	private ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	private ExternalPayment externalPayment;
	private Subscription subscription;
	private ExternalPaymentRegistration externalPaymentRegistration;

	@Before
	public void setup()
	{
		heidelpayTransactionService = new HeidelpayTransactionService(heidelpayTransactionRepository,
				externalPaymentRepository, externalPaymentRegistrationRepository);

		User user = defaultEntityFactory.persistDefaultUser("test");

		InitialSubscriptionOffer subscriptionOffer = paymentEntityFactory.persistInitialSubscriptionOffer("Test", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);
		subscription = paymentEntityFactory.persistSubscription(user, subscriptionOffer, LocalDateTime.now());

		externalPaymentRegistration = paymentEntityFactory.persistExternalPaymentRegistration(user, "REGISTRATION_ID");

		Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);

		InvoiceItem invoiceItem = new InvoiceItem();

		invoiceItem.setAmount(subscriptionOffer.getAmount());
		invoiceItem.setProduct(subscriptionOffer);
		invoiceItem.setSubscription(subscription);

		invoiceItem.setInvoice(invoice);
		invoice.getItems().add(invoiceItem);

		invoiceRepository.save(invoice);

		externalPayment = paymentEntityFactory.persistExternalPayment(invoice, PaymentState.PAID);
	}

	@After
	public void teardown()
	{
		defaultEntityFactory.reset();
	}

	@Test
	public void testGetPayment()
	{
		TransactionID transactionID = TransactionID.toTransactionID(externalPayment, TransactionID.Type.PAYMENT);
		ExternalPayment payment = heidelpayTransactionService.getPayment(transactionID);

		assertThat(externalPayment, is(payment));

		transactionID = TransactionID.toTransactionID(externalPayment, TransactionID.Type.REGISTRATION);
		payment = heidelpayTransactionService.getPayment(transactionID);

		assertThat(externalPayment, is(payment));
	}

	@Test
	public void testGetExternalPaymentRegistration()
	{
		TransactionID transactionID = TransactionID.toTransactionID(externalPaymentRegistration);

		ExternalPaymentRegistration foundExternalPaymentRegistration = heidelpayTransactionService.getExternalPaymentRegistration(transactionID);

		assertThat(foundExternalPaymentRegistration, is(externalPaymentRegistration));
	}
}
