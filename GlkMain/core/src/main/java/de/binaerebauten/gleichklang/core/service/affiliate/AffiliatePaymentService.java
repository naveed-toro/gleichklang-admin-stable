package de.binaerebauten.gleichklang.core.service.affiliate;

import de.binaerebauten.gleichklang.core.model.affiliate.AffiliatePartner;
import de.binaerebauten.gleichklang.core.model.affiliate.AffiliatePaymentState;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.repository.AffiliatePaymentStateRepository;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides common operations for affiliate payments.
 */
@Service
public class AffiliatePaymentService
{
	private static final Logger LOG = LoggerFactory.getLogger(AffiliatePaymentService.class);
	
	private final AffiliatePaymentStateRepository affiliatePaymentStateRepository;
	
	private final PaymentRepository paymentRepository;
	
	@Autowired
	public AffiliatePaymentService(PaymentRepository paymentRepository,
			AffiliatePaymentStateRepository affiliatePaymentStateRepository)
	{
		this.paymentRepository = paymentRepository;
		this.affiliatePaymentStateRepository = affiliatePaymentStateRepository;
	}
	
	/**
	 * Creates a new affiliate payment record with the state "not paid" and
	 * with the same external id as the previous one.
	 * Does nothing if no previous records found.
	 *
	 * @param currentSubscription an active subscription before prolongation
	 * @param newPayment      a new payment
	 */
	@Transactional
	public void prolong(Subscription currentSubscription, AbstractPayment newPayment)
	{
		final List<AbstractPayment> previousPayments = paymentRepository.findBySubscription(currentSubscription);
		if (previousPayments.isEmpty())
		{
			LOG.error("no previous payment for current subscription, this should not be happen");
			return;
		}
		
		final Set<AffiliatePaymentState> affiliatePaymentStates = affiliatePaymentStateRepository.findByPaymentIn(previousPayments);
		final EnumSet<AffiliatePartner> usedAffiliatePartners = EnumSet.noneOf(AffiliatePartner.class);
		
		for (AffiliatePaymentState affiliatePaymentState : affiliatePaymentStates)
		{
			if (!usedAffiliatePartners.contains(affiliatePaymentState.getPartner()))
			{
				create(affiliatePaymentState, newPayment);
				usedAffiliatePartners.add(affiliatePaymentState.getPartner());
			}
			else
			{
				LOG.error("more than one affiliate partner for the last subscription found, this should not be happen");
			}
		}
	}
	
	private void create(AffiliatePaymentState existingAffiliatePaymentState, AbstractPayment payment)
	{
		final AffiliatePaymentState affiliatePaymentState = new AffiliatePaymentState();
		affiliatePaymentState.setPartner(existingAffiliatePaymentState.getPartner());
		affiliatePaymentState.setExternalId(existingAffiliatePaymentState.getExternalId());
		affiliatePaymentState.setPayment(payment);
		affiliatePaymentState.setPaidToPartner(false);
		
		affiliatePaymentStateRepository.save(affiliatePaymentState);
	}
}
