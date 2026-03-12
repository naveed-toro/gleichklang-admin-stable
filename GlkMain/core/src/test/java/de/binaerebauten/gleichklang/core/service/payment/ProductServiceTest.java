package de.binaerebauten.gleichklang.core.service.payment;

import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.payment.ServiceOffer;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.message.AdminWorkItemRepository;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.affiliate.AffiliatePaymentService;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * unit tests for {@link ProductService}.
 */
public class ProductServiceTest
{
	private ProductService productService;

	private DefaultEntityFactory defaultEntityFactory = new DefaultEntityFactory();

	private PaymentEntityFactory paymentEntityFactory = new PaymentEntityFactory();
	
	@Mock
	private AffiliatePaymentService affiliatePaymentService;

	@Mock
	private SubscriptionService subscriptionService;

	@Mock
	private ExternalPaymentService externalPaymentService;
	
	@Mock
	private MailQueueService mailQueueService;

	@Mock
	private MessageService messageService;

	@Mock
	private UserService userService;

	@Mock
	private AdminWorkItemRepository adminWorkItemRepository;

	private User user;

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);

		productService = new ProductService(affiliatePaymentService,
				subscriptionService,
				messageService,
				userService);

		user = DefaultStaticEntityFactory.createDefaultUser("test@example.com", "test");
	}

	@Test
	public void testActivate_ServiceOffer() throws ValidationException
	{
		ServiceOffer serviceOffer = paymentEntityFactory.createServiceOffer("TestServiceOffer", LocalDateTime.now(), 12);
		ExternalPayment externalPayment = paymentEntityFactory.createExternalPayment(user, PaymentState.PENDING);

		Message message = defaultEntityFactory.createDefaultMessage(user, null);
		
		when(subscriptionService.findCurrentSubscription(user)).thenReturn(Optional.empty());
		when(messageService.createNewMessage(user)).thenReturn(message);

		productService.activate(serviceOffer, externalPayment);

		THEN:
		{
			verify(messageService).sendMessageToAdmin(message, null);
		}
	}
}
