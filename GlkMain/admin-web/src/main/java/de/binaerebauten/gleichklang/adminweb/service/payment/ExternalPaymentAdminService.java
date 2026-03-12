package de.binaerebauten.gleichklang.adminweb.service.payment;

import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;

/**
 * Provides common operations for external payments related to the admin-web module.
 */
@Service
public class ExternalPaymentAdminService
{
	@Autowired
	private ExternalPaymentRepository externalPaymentRepository;

	/**
	 * Synchronizes the given external payment with the given external payment state.
	 * If the external payment state is null, increases the synchronization count of
	 * the external payment to limit the number of further synchronization attempts.
	 *
	 * @param externalPayment      the external payment
	 * @param externalPaymentState the external payment state or null
	 *
	 * @deprecated nicht sinnvoll, da nur der payment state umgesetzt wird, aber z.B. die subscription nicht aktiviert wird
	 */
	@Transactional
	@Deprecated
	public void synchronizeExternalPayment(ExternalPayment externalPayment, PaymentState externalPaymentState)
	{
		Objects.requireNonNull(externalPayment, "externalPayment == null");

		Long paymentId = externalPayment.getId();
		ExternalPayment attachedExternalPayment = externalPaymentRepository.getOne(paymentId);

		if (externalPaymentState != null)
		{
			attachedExternalPayment.setState(externalPaymentState);
		}
		else
		{
			int newSynchronizationCount = attachedExternalPayment.getSynchronizationCount() + 1;
			attachedExternalPayment.setSynchronizationCount(newSynchronizationCount);
		}
		externalPaymentRepository.save(attachedExternalPayment);
	}
}
