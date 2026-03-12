package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.HeidelpayTransaction;
import de.binaerebauten.gleichklang.core.model.payment.Invoice;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class HeidelpayTransactionRepositoryTest
		extends AbstractRepositoryTest<HeidelpayTransaction>
{
	@Autowired
	private HeidelpayTransactionRepository heidelpayTransactionRepository;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	private ExternalPayment payment;

	private HeidelpayTransaction firstEvent;

	private HeidelpayTransaction lastEvent;

	@Test
	public void testFindByPaymentOrderByCreateDateDesc()
	{
		Page<HeidelpayTransaction> heidelpayEventPage = heidelpayTransactionRepository.findByPaymentOrderByCreateDateDesc(payment, new PageRequest(0, 1));

		assertThat(heidelpayEventPage.getTotalElements(), is(2L));
		assertThat(heidelpayEventPage.getNumberOfElements(), is(1));

		HeidelpayTransaction heidelpayTransaction = heidelpayEventPage.getContent().get(0);

		assertThat(heidelpayTransaction, is(lastEvent));
		assertThat(heidelpayTransaction.getCreateDate(), greaterThan(firstEvent.getCreateDate()));
	}

	@Override
	protected Collection<HeidelpayTransaction> getPersistedEntities()
	{
		User user = defaultEntityFactory.persistDefaultUser(DefaultStaticEntityFactory.DEFAULT_ALIAS);
		Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);

		payment = paymentEntityFactory.persistExternalPayment(invoice, PaymentState.PENDING);

		firstEvent = paymentEntityFactory.persistDefaultHeidelpayEvent(payment);

		firstEvent.setCreateDate(LocalDateTime.from(LocalDateTime.of(1980, Month.JANUARY, 1, 0, 0)));
		heidelpayTransactionRepository.save(firstEvent);

		lastEvent = paymentEntityFactory.persistDefaultHeidelpayEvent(payment);

		return Arrays.asList(firstEvent, lastEvent);
	}

	@Override
	protected JpaRepository<HeidelpayTransaction, Long> getRepository()
	{
		return heidelpayTransactionRepository;
	}
}
