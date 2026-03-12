package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.affiliate.AffiliatePartner;
import de.binaerebauten.gleichklang.core.model.affiliate.AffiliatePaymentState;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import de.binaerebauten.gleichklang.core.model.payment.Invoice;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AffiliatePaymentStateRepositoryTest extends AbstractRepositoryTest<AffiliatePaymentState>
{
	@Autowired
	private PaymentEntityFactory paymentEntityFactory;
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	@Autowired
	private AffiliatePaymentStateRepository affiliatePaymentStateRepository;
	
	private AbstractPayment payment;

	@Override
	protected Collection<AffiliatePaymentState> getPersistedEntities()
	{
		final Invoice invoice = paymentEntityFactory.persistDefaultInvoice(defaultEntityFactory.persistDefaultUser("test"));
		payment = paymentEntityFactory.persistExternalPayment(invoice, PaymentState.PAID);
		
		final AffiliatePaymentState affiliatePaymentState = new AffiliatePaymentState();
		affiliatePaymentState.setExternalId(UUID.randomUUID().toString());
		affiliatePaymentState.setPaidToPartner(false);
		affiliatePaymentState.setPayment(payment);
		affiliatePaymentState.setPartner(AffiliatePartner.SUPERCLIX);

		return Collections.singletonList(affiliatePaymentStateRepository.save(affiliatePaymentState));
	}
	
	@Test
	public void findByPaymentInTest()
	{
		final Set<AffiliatePaymentState> affiliatePaymentStates = affiliatePaymentStateRepository.findByPaymentIn(Collections.singleton(payment));
		
		assertThat(affiliatePaymentStates.size(), equalTo(1));
	}

	@Override
	protected JpaRepository<AffiliatePaymentState, Long> getRepository()
	{
		return affiliatePaymentStateRepository;
	}
}
