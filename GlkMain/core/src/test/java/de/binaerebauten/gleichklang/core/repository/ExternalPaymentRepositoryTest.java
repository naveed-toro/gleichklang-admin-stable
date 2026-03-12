package de.binaerebauten.gleichklang.core.repository;

import java.util.List;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link PrepaymentRepository}.
 */
public class ExternalPaymentRepositoryTest
		extends AbstractRepositoryTest<ExternalPayment>
{
	private final LocalDateTime begin = LocalDateTime.of(2015, Month.JANUARY, 1, 0, 0);

	@Autowired
	private ExternalPaymentRepository externalPaymentRepository;

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	@Autowired
	private DefaultEntityFactory entityFactory;

	private ExternalPayment pendingExternalPayment;

	private Subscription subscription;

	@Test
	public void testFindByExternalReferenceId()
	{
		ExternalPayment externalPayment = externalPaymentRepository.findByExternalReferenceId(pendingExternalPayment.getExternalReferenceId());

		assertThat(externalPayment, is(pendingExternalPayment));
	}

	@Test
	public void testFindBySubscription()
	{
		List<ExternalPayment> externalPayments = externalPaymentRepository.findBySubscription(subscription);
		assertThat(externalPayments, hasItem(pendingExternalPayment));
	}

	@Override
	protected Collection<ExternalPayment> getPersistedEntities()
	{
		User user = entityFactory.persistDefaultUser(DefaultStaticEntityFactory.DEFAULT_ALIAS);
		Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);

		pendingExternalPayment = paymentEntityFactory.persistExternalPayment(invoice, PaymentState.PENDING);

		InitialSubscriptionOffer subscriptionOffer = paymentEntityFactory.persistInitialSubscriptionOffer("Test", LocalDateTime.now(), RecommendationCategory.PARTNERSHIP);

		subscription = paymentEntityFactory.persistSubscription(user, subscriptionOffer, LocalDateTime.now());

		InvoiceItem invoiceItem = new InvoiceItem();
		invoiceItem.setInvoice(invoice);
		invoiceItem.setProduct(subscriptionOffer);
		invoiceItem.setAmount(subscriptionOffer.getAmount());
		invoiceItem.setSubscription(subscription);
		invoice.getItems().add(invoiceItem);
		invoiceRepository.save(invoice);

		return Arrays.asList(pendingExternalPayment);
	}

	@Override
	protected JpaRepository<ExternalPayment, Long> getRepository()
	{
		return externalPaymentRepository;
	}
}
