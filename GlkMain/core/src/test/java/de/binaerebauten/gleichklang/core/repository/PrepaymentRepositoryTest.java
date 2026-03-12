package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.payment.BankAccount;
import de.binaerebauten.gleichklang.core.model.payment.Invoice;
import de.binaerebauten.gleichklang.core.model.payment.Prepayment;
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
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for {@link PrepaymentRepository}.
 */
public class PrepaymentRepositoryTest extends AbstractRepositoryTest<Prepayment>
{
	private final LocalDateTime begin = LocalDateTime.of(2015, Month.JANUARY, 1, 0, 0);

	@Autowired
	private PrepaymentRepository prepaymentRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	private Prepayment prepayment;
	
	private Prepayment refund;

	@Test
	public void testFindPendingPrepaymentsToRemind()
	{
		PageRequest pageable = new PageRequest(0, 10);

		Page<Prepayment> pendingPrepaymentsPage = prepaymentRepository.findPendingPrepaymentsToRemind(LocalDateTime.now(), pageable);

		assertThat(pendingPrepaymentsPage.getTotalElements(), is(1L));
		assertThat(pendingPrepaymentsPage.getContent(), hasItem(prepayment));
		
		// refund should not be returned
	}

	@Override
	protected Collection<Prepayment> getPersistedEntities()
	{
		User user = entityFactory.persistDefaultUser(DefaultStaticEntityFactory.DEFAULT_ALIAS);
		final Country country = entityFactory.persistDefaultCountry();

		BankAccount bankAccount = paymentEntityFactory.persistDefaultBankAccount(country);
		Invoice invoice = paymentEntityFactory.persistDefaultInvoice(user);

		prepayment = paymentEntityFactory.persistPrepayment(user, bankAccount, invoice);
		prepayment.setCurrent(null);
		prepaymentRepository.saveAndFlush(prepayment);
		refund = paymentEntityFactory.persistRefund(prepayment);

		return Arrays.asList(prepayment, refund);
	}

	@Override
	protected JpaRepository<Prepayment, Long> getRepository()
	{
		return prepaymentRepository;
	}
}
