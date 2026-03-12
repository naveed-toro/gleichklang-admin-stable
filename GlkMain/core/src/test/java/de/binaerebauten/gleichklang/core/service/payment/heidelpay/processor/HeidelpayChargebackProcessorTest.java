package de.binaerebauten.gleichklang.core.service.payment.heidelpay.processor;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.IdentificationResponseType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ProcessingType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ReturnType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayTransactionService;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionReturnCode;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link HeidelpayChargebackProcessor}.
 */
public class HeidelpayChargebackProcessorTest
{
	private HeidelpayChargebackProcessor heidelpayChargebackProcessor;
	
	private PaymentEntityFactory paymentEntityFactory;
	
	@Mock
	private ExternalPaymentRepository externalPaymentRepository;

	@Mock
	private HeidelpayTransactionService heidelpayTransactionService;

	@Mock
	private InvoiceService invoiceService;
	
	@Mock
	private SubscriptionService subscriptionService;

	private User user;

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);

		heidelpayChargebackProcessor = new HeidelpayChargebackProcessor(externalPaymentRepository, heidelpayTransactionService, invoiceService, null, null, subscriptionService);
		paymentEntityFactory = new PaymentEntityFactory();

		user = DefaultStaticEntityFactory.createDefaultUser("test@example.com", "test");
	}

	@Test
	public void testProcess_CancelExternalPaymentAndChargeback() throws PaymentException
	{
		TransactionResponseType transactionResponse = new TransactionResponseType();

		IdentificationResponseType identification = new IdentificationResponseType();
		identification.setTransactionID("PAYMENT:c190e3a0-3ad5-45f4-9f85-e53a2f2396e3:test-machine");
		identification.setUniqueID("31HA07BC810ABF37833E735FCC6F852C");
		identification.setReferenceID("referenceId");
		transactionResponse.setIdentification(identification);

		ProcessingType processingResult = new ProcessingType();
		processingResult.setResult(ProcessingResultType.ACK.name());
		ReturnType returnType = new ReturnType();
		returnType.setCode(TransactionReturnCode.REVOCATION_OR_DISPUTE.getCode());
		processingResult.setReturn(returnType);
		transactionResponse.setProcessing(processingResult);

		ExternalPayment payment = new ExternalPayment();
		payment.setState(PaymentState.PAID);
		payment.setUser(user);
		Invoice invoice = paymentEntityFactory.createDefaultInvoice(user);
		InitialSubscriptionOffer initialSubscriptionOffer = paymentEntityFactory
				.createIntialSubscriptionOffer("Initial", LocalDateTime.now(), 12, RecommendationCategory.FRIENDSHIP, RecommendationCategory.PARTNERSHIP);
		invoice.getItems().add(paymentEntityFactory.createDefaultInvoiceItem(invoice, initialSubscriptionOffer));
		payment.setInvoice(invoice);
		
		when(externalPaymentRepository.findByExternalId(identification.getUniqueID())).thenReturn(Optional.empty());
		when(heidelpayTransactionService.getPayment(identification.getReferenceID())).thenReturn(Optional.of(payment));
		when(subscriptionService.findCurrentSubscription(user)).thenReturn(Optional.of(new Subscription()));

		heidelpayChargebackProcessor.process(transactionResponse);

		THEN:
		{
			verify(subscriptionService).cancelSubscription(any(Subscription.class));
		}
	}

}
