package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.payment.Chargeback;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link ChargebackRepository}
 */
public class ChargebackRepositoryTest extends BasePersistenceTest
{
	@Autowired
	private ChargebackRepository chargebackRepository;

	@Autowired
	private PaymentEntityFactory entityFactory;

	private Chargeback chargebackForCreditCard;

	private Chargeback chargebackForDirectDebit;

	@Before
	public void setup()
	{
		chargebackForCreditCard = entityFactory.persistChargeback(PaymentMethod.CREDIT_CARD, BigDecimal.TEN);
		chargebackForDirectDebit = entityFactory.persistChargeback(PaymentMethod.DIRECT_DEBIT, BigDecimal.TEN);
	}

	@Test
	public void testFindByForMethod()
	{
		Chargeback chargeback = chargebackRepository.findByForMethod(PaymentMethod.CREDIT_CARD);

		assertThat(chargeback, is(chargebackForCreditCard));

		chargeback = chargebackRepository.findByForMethod(PaymentMethod.DIRECT_DEBIT);

		assertThat(chargeback, is(chargebackForDirectDebit));
	}
}
