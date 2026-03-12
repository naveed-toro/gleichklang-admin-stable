//package de.binaerebauten.gleichklang.core.service.payment;
//
//import de.binaerebauten.gleichklang.core.model.payment.*;
//import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
//import de.binaerebauten.gleichklang.core.model.user.User;
//import de.binaerebauten.gleichklang.core.repository.*;
//import de.binaerebauten.gleichklang.core.service.SubscriptionService;
//import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
//import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.*;
//
//import java.math.BigDecimal;
//import java.util.HashSet;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.assertThat;
//import static org.mockito.Matchers.eq;
//import static org.mockito.Mockito.*;
//
///**
// * Unit test for {@link InvoiceService} which uses spies to verify
// * the correct order of method calls.
// */
//public class InvoiceServiceTest
//{
//	@InjectMocks
//	private InvoiceService invoiceService;
//
//	@Mock
//	private InvoiceRepository invoiceRepository;
//
//	@Mock
//	private PaymentRepository paymentRepository;
//
//	@Mock
//	private SubscriptionService subscriptionService;
//
//	@Mock
//	private PaymentService paymentService;
//
//	@Mock
//	private ExternalPaymentService externalPaymentService;
//
//	@Mock
//	private ExternalPaymentRepository externalPaymentRepository;
//
//	@Mock
//	private ProductService productService;
//
//	@Mock
//	private User user;
//
//	@Mock
//	private Product product;
//
//	@Mock
//	private ExternalPayment externalPayment;
//
//	@Mock
//	private ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;
//
//	@Mock
//	private Prepayment prepayment;
//
//	@Mock
//	private ChargebackRepository chargebackRepository;
//
//	@Mock
//	private UserMailTemplateService userMailTemplateService;
//
//	@Mock
//	private MailSendService mailSendService;
//
//	@Mock
//	private Chargeback chargeback;
//
//	@Before
//	public void setup()
//	{
//		invoiceService = spy(new InvoiceService());
//		MockitoAnnotations.initMocks(this);
//
//		when(product.getProductType()).thenReturn(ProductType.INITIAL_SUBSCRIPTION_OFFER);
//	}
//
//	@Test
//	public void testCreateAndSaveInvoice()
//	{
//		MonetaryAmount amount = new MonetaryAmount(BigDecimal.TEN, AvailableCurrency.EUR);
//		{
//			when(productService.getPrice(user, product)).thenReturn(amount);
//		}
//
//		InOrder inOrder = inOrder(invoiceService, invoiceRepository, paymentService);
//
//		Invoice invoice = invoiceService.createAndSaveInvoice(user, product, PaymentMethod.CREDIT_CARD);
//
//		{
//			inOrder.verify(invoiceRepository).save(invoice);
//			inOrder.verify(paymentService).createAndSavePayment(eq(invoice), eq(PaymentMethod.CREDIT_CARD), eq(amount), eq(true), eq(false));
//		}
//	}
//
//	@Test
//	public void testPaymentReceived_RefundPayment() throws PaymentException
//	{
//		Invoice invoice = new Invoice();
//
//		Prepayment paymentToRefund = new Prepayment();
//		paymentToRefund.setInvoice(invoice);
//		MonetaryAmount initialAmount = new MonetaryAmount(BigDecimal.valueOf(100), AvailableCurrency.EUR);
//		paymentToRefund.setAmount(initialAmount);
//
//		Prepayment refundPayment = new Prepayment();
//		refundPayment.setInvoice(invoice);
//		refundPayment.setState(PaymentState.PENDING);
//		refundPayment.setPaymentToRefund(paymentToRefund);
//		MonetaryAmount refundAmount = new MonetaryAmount(BigDecimal.valueOf(-90), AvailableCurrency.EUR);
//		refundPayment.setAmount(refundAmount);
//
//		invoiceService.paymentReceived(refundPayment);
//
//		THEN:
//		{
//			assertThat(refundPayment.getState(), is(PaymentState.PAID));
//			assertThat(paymentToRefund.isRefunded(), is(true));
//
//			verify(invoiceRepository).save(invoice);
//		}
//	}
//
//	@Test
//	public void testCancelExternalPaymentAndCreatePrepayment()
//	{
//		MonetaryAmount monetaryAmount = new MonetaryAmount(BigDecimal.TEN, AvailableCurrency.EUR);
//		when(externalPayment.getAmount()).thenReturn(monetaryAmount);
//
//		when(productService.getPrice(user, product)).thenReturn(monetaryAmount);
//		Invoice invoice = invoiceService.createAndSaveInvoice(user, product, PaymentMethod.CREDIT_CARD);
//		when(externalPayment.getInvoice()).thenReturn(invoice);
//
//		when(paymentService.createAndSavePayment(anyObject(), eq(PaymentMethod.PREPAYMENT), eq(monetaryAmount), eq(true))).thenReturn(prepayment);
//
//		THEN:
//		{
//			assertThat(invoiceService.cancelExternalPaymentAndCreatePrepayment(externalPayment, ChargebackReason.INSUFFICIENT), is(prepayment));
//
//			verify(externalPayment).setState(PaymentState.CANCELED);
//			verify(paymentService).createAndSavePayment(anyObject(), eq(PaymentMethod.PREPAYMENT), eq(monetaryAmount), eq(true));
//		}
//	}
//
//	@Test
//	public void testCancelExternalPaymentAndChargeback()
//	{
//		MonetaryAmount monetaryAmount = new MonetaryAmount(BigDecimal.TEN, AvailableCurrency.EUR);
//		when(externalPayment.getAmount()).thenReturn(monetaryAmount);
//		when(externalPayment.getMethod()).thenReturn(PaymentMethod.CREDIT_CARD);
//
//		Invoice invoice = new Invoice();
//		invoice.setItems(new HashSet<>());
//		when(externalPayment.getInvoice()).thenReturn(invoice);
//
//		MonetaryAmount chargebackAmount = new MonetaryAmount(BigDecimal.ONE, AvailableCurrency.EUR);
//		when(chargebackRepository.findByForMethod(eq(PaymentMethod.CREDIT_CARD))).thenReturn(chargeback);
//		when(chargeback.getAmount()).thenReturn(chargebackAmount);
//
//		when(paymentService.createAndSavePayment(anyObject(), eq(PaymentMethod.PREPAYMENT), eq(chargebackAmount), eq(false))).thenReturn(prepayment);
//
//		THEN:
//		{
//			assertThat(invoiceService.cancelExternalPaymentAndChargeback(externalPayment), is(prepayment));
//			verify(externalPayment).setState(PaymentState.CANCELED);
//			verify(paymentService).createAndSavePayment(anyObject(), eq(PaymentMethod.PREPAYMENT), eq(chargebackAmount), eq(false));
//		}
//	}
//
//	@Test
//	public void testRefundPayment() throws PaymentException
//	{
//		runTestRefundPayment(PaymentMethod.PREPAYMENT, true);
//		runTestRefundPayment(PaymentMethod.PREPAYMENT, false);
//		runTestRefundPayment(PaymentMethod.DIRECT_DEBIT, true);
//		runTestRefundPayment(PaymentMethod.DIRECT_DEBIT, false);
//		runTestRefundPayment(PaymentMethod.CREDIT_CARD, true);
//		runTestRefundPayment(PaymentMethod.CREDIT_CARD, false);
//	}
//
//	private void runTestRefundPayment(PaymentMethod paymentMethod, boolean usePrepayment) throws PaymentException
//	{
//		Mockito.reset(paymentService);
//		Mockito.reset(invoiceRepository);
//		Mockito.reset(externalPaymentService);
//
//		final AbstractPayment paymentToRefund;
//
//		switch(paymentMethod)
//		{
//			case DIRECT_DEBIT:
//			case CREDIT_CARD:
//				paymentToRefund = new ExternalPayment();
//				((ExternalPayment) paymentToRefund).setMethod(paymentMethod);
//				break;
//			case PREPAYMENT:
//			default:
//				paymentToRefund = new Prepayment();
//				break;
//		}
//
//		final Invoice invoice = new Invoice();
//		paymentToRefund.setInvoice(invoice);
//
//		final MonetaryAmount initialAmount = new MonetaryAmount(BigDecimal.valueOf(100), AvailableCurrency.EUR);
//		paymentToRefund.setAmount(initialAmount);
//
//		final MonetaryAmount refundAmount = new MonetaryAmount(BigDecimal.valueOf(-50), AvailableCurrency.EUR);
//
//		final Prepayment refundPrepayment = new Prepayment();
//		final ExternalPayment refundExternalPayment = new ExternalPayment();
//
//		when(paymentService.createAndSavePayment(anyObject(), eq(PaymentMethod.PREPAYMENT), eq(refundAmount), eq(false))).thenReturn(refundPrepayment);
//		when(paymentService.createAndSavePayment(anyObject(), eq(PaymentMethod.CREDIT_CARD), eq(refundAmount), eq(false))).thenReturn(refundExternalPayment);
//		when(paymentService.createAndSavePayment(anyObject(), eq(PaymentMethod.DIRECT_DEBIT), eq(refundAmount), eq(false))).thenReturn(refundExternalPayment);
//		when(externalPaymentRegistrationRepository.findByUser(any(User.class))).thenReturn(new ExternalPaymentRegistration());
//
//		THEN:
//		{
//			final AbstractPayment refundPayment = invoiceService.refundPayment(paymentToRefund, refundAmount, usePrepayment);
//
//			if(usePrepayment || paymentMethod == PaymentMethod.PREPAYMENT)
//			{
//				assertThat(refundPayment, is(refundPrepayment));
//				Mockito.verifyZeroInteractions(externalPaymentService);
//			}
//			else
//			{
//				assertThat(refundPayment, is(refundExternalPayment));
//				verify(externalPaymentService).requestRefund((ExternalPayment) paymentToRefund, (ExternalPayment) refundPayment);
//			}
//
//			assertThat(refundPayment.getPaymentToRefund(), is(paymentToRefund));
//			verify(paymentService).createAndSavePayment(any(Invoice.class), any(PaymentMethod.class), eq(refundAmount), eq(false));
//			verify(invoiceRepository).save(invoice);
//		}
//	}
//}
