package de.binaerebauten.gleichklang.adminweb.service.payment;

import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.repository.ChargebackRepository;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExternalPaymentAdminService} which is not using spring and just uses mockito.
 */
public class ExternalPaymentAdminServiceTest
{
	@InjectMocks
	private ExternalPaymentAdminService externalPaymentAdminService;

	@Mock
	private PaymentService paymentService;

	@Mock
	private ExternalPaymentRepository externalPaymentRepository;

	@Mock
	private ChargebackRepository chargebackRepository;

	@Mock
	private ExternalPayment externalPayment;

	@Before
	public void setup()
	{
		externalPaymentAdminService = new ExternalPaymentAdminService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testSynchronizeExternalPayment()
	{
		long paymentId = 1L;

		when(externalPayment.getId()).thenReturn(paymentId);
		when(externalPaymentRepository.getOne(paymentId)).thenReturn(externalPayment);
		when(externalPayment.getSynchronizationCount()).thenReturn(0);

		THEN:
		{
			externalPaymentAdminService.synchronizeExternalPayment(externalPayment, PaymentState.PAID);

			verify(externalPayment).setState(PaymentState.PAID);
			verify(externalPaymentRepository).save(externalPayment);
		}
	}

	@Test
	public void testSynchronizeExternalPayment_IncrementSynchronizationCount()
	{
		long paymentId = 1L;

		when(externalPayment.getId()).thenReturn(paymentId);
		when(externalPaymentRepository.getOne(paymentId)).thenReturn(externalPayment);
		when(externalPayment.getSynchronizationCount()).thenReturn(0);

		THEN:
		{
			externalPaymentAdminService.synchronizeExternalPayment(externalPayment, null);

			verify(externalPayment).setSynchronizationCount(1);
			verify(externalPaymentRepository).save(externalPayment);
		}
	}
}
