package de.binaerebauten.gleichklang.adminweb.service.payment.heidelpay;

import com.google.common.collect.Iterators;
import de.binaerebauten.gleichklang.adminweb.service.payment.ExternalPaymentAdminService;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ObjectFactory;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ProcessingType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ResponseType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRegistrationRepository;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayQueryApiConnector;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayTransactionService;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for{@link HeidelpayBackofficePaymentService}.
 */
public class HeidelpayBackofficePaymentServiceTest
{
	@InjectMocks
	private HeidelpayBackofficePaymentService heidelpayBackofficePaymentService;

	@Mock
	private TransactionTemplate transactionTemplate;

	@Mock
	private HeidelpayQueryApiConnector heidelpayQueryApiConnector;

	@Mock
	private ExternalPaymentRepository externalPaymentRepository;

	@Mock
	private HeidelpayTransactionService heidelpayTransactionService;

	@Mock
	private ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;

	@Mock
	private ExternalPaymentAdminService externalPaymentAdminService;

	@Mock
	private ExternalPayment externalPayment;

	private ObjectFactory jaxbFactory = new ObjectFactory();

	@Before
	public void setup()
	{
		heidelpayBackofficePaymentService = new HeidelpayBackofficePaymentService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testSynchronizePendingExternalPayments_External_PaymentState_Unknown()
			throws PaymentException
	{
		Page<ExternalPayment> page = mockExternalPaymentPage();
		when(externalPaymentRepository.
				findByStateAndSynchronizationCountLessThan(eq(PaymentState.PENDING), anyInt(), anyObject()))
				.thenReturn(page);

		when(heidelpayQueryApiConnector.getTransaction(externalPayment, TransactionID.Type.PAYMENT)).thenThrow(new PaymentException("", ""));

		THEN:
		{
			heidelpayBackofficePaymentService.synchronizePendingExternalPayments();
		}
	}

	@Test
	public void testSynchronizePendingExternalPayments_External_PaymentState_PAID()
			throws PaymentException
	{
		Page<ExternalPayment> page = mockExternalPaymentPage();
		when(externalPaymentRepository.
				findByStateAndSynchronizationCountLessThan(eq(PaymentState.PENDING), anyInt(), anyObject()))
				.thenReturn(page);

		TransactionResponseType succesfulTransactionResponse = createTransactionResponse(ProcessingResultType.ACK);
		when(heidelpayQueryApiConnector.getTransaction(externalPayment, TransactionID.Type.PAYMENT)).thenReturn(succesfulTransactionResponse);

		THEN:
		{
			heidelpayBackofficePaymentService.synchronizePendingExternalPayments();

			verify(externalPaymentAdminService).synchronizeExternalPayment(externalPayment, PaymentState.PAID);
			verify(heidelpayTransactionService).log(externalPayment, getProcessing(succesfulTransactionResponse));
		}
	}

	@Test
	public void testSynchronizePendingExternalPayments_External_PaymentState_FAILED()
			throws PaymentException
	{
		Page<ExternalPayment> page = mockExternalPaymentPage();
		when(externalPaymentRepository.
				findByStateAndSynchronizationCountLessThan(eq(PaymentState.PENDING), anyInt(), anyObject()))
				.thenReturn(page);

		TransactionResponseType failedTransactionResponse = createTransactionResponse(ProcessingResultType.NOK);
		when(heidelpayQueryApiConnector.getTransaction(externalPayment, TransactionID.Type.PAYMENT)).thenReturn(failedTransactionResponse);

		THEN:
		{
			heidelpayBackofficePaymentService.synchronizePendingExternalPayments();

			verify(externalPaymentAdminService).synchronizeExternalPayment(externalPayment, PaymentState.FAILED);
			verify(heidelpayTransactionService).log(externalPayment, getProcessing(failedTransactionResponse));
		}
	}

	private ProcessingType getProcessing(TransactionResponseType transactionResponse)
	{
		return transactionResponse.getProcessing();
	}

	private Page<ExternalPayment> mockExternalPaymentPage()
	{
		Page<ExternalPayment> page = mock(Page.class);
		when(page.iterator()).thenReturn(Iterators.forArray(externalPayment));
		return page;
	}

	private ResponseType createErrorResponse()
	{
		ResponseType responseType = createResponseType();
		responseType.setError(jaxbFactory.createErrorType());
		return responseType;
	}

	private TransactionResponseType createTransactionResponse(ProcessingResultType processingResult)
	{
		ProcessingType processingType = createProcessingType(processingResult);

		TransactionResponseType transactionResponseType = jaxbFactory.createTransactionResponseType();
		transactionResponseType.setProcessing(processingType);

		return transactionResponseType;
	}

	private ProcessingType createProcessingType(ProcessingResultType result)
	{
		ProcessingType processingType = jaxbFactory.createProcessingType();
		processingType.setResult(result.name());
		return processingType;
	}

	private ResponseType createResponseType()
	{
		return jaxbFactory.createResponseType();
	}
}
