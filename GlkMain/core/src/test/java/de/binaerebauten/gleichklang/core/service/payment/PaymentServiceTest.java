package de.binaerebauten.gleichklang.core.service.payment;

import de.binaerebauten.gleichklang.core.model.payment.InitialSubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.UserPaymentSettingsRepository;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentService}.
 */
public class PaymentServiceTest
{
	private PaymentService paymentService;

	@Mock
	private UserPaymentSettingsRepository userPaymentSettingsRepository;

	@Mock
	private ExternalPaymentService externalPaymentService;

	@Mock
	private SubscriptionService subscriptionService;

	private User user;

	private Subscription subscription;

	private PaymentEntityFactory paymentEntityFactory = new PaymentEntityFactory();

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);

		paymentService = new PaymentService(null, null, externalPaymentService, null, null, userPaymentSettingsRepository, null,null,null);

		user = DefaultStaticEntityFactory.createDefaultUser("test@example.com", "test");

		InitialSubscriptionOffer testOffer = paymentEntityFactory.createIntialSubscriptionOffer("TestOffer", LocalDateTime.now(), 12);

		subscription = paymentEntityFactory.createSubscription(user, testOffer, LocalDateTime.now());
	}

	@Test
	public void testUpdatePaymentSettings_CREDIT_CARD_TO_PREPAYMENT() throws PaymentException
	{
		UserPaymentSettings userPaymentSettings = createUserPaymentSettings(PaymentMethod.CREDIT_CARD);
		
		when(subscriptionService.findAllSubscriptions(user)).thenReturn(Arrays.asList(subscription));
		when(subscriptionService.findCurrentSubscription(user)).thenReturn(Optional.ofNullable(subscription));

		updatePaymentMethod(PaymentMethod.PREPAYMENT);

		THEN:
		{
			assertThat(userPaymentSettings.getPaymentMethod(), is(PaymentMethod.PREPAYMENT));

			verify(userPaymentSettingsRepository).save(userPaymentSettings);
			
			verify(externalPaymentService).deregister(user);
			verifyNoMoreInteractions(externalPaymentService);
		}
	}

	@Test
	public void testUpdatePaymentSettings_DIRECT_DEBIT_TO_PREPAYMENT() throws PaymentException
	{
		UserPaymentSettings userPaymentSettings = createUserPaymentSettings(PaymentMethod.DIRECT_DEBIT);
		
		when(subscriptionService.findAllSubscriptions(user)).thenReturn(Arrays.asList(subscription));
		when(subscriptionService.findCurrentSubscription(user)).thenReturn(Optional.ofNullable(subscription));

		updatePaymentMethod(PaymentMethod.PREPAYMENT);

		THEN:
		{
			assertThat(userPaymentSettings.getPaymentMethod(), is(PaymentMethod.PREPAYMENT));

			verify(userPaymentSettingsRepository).save(userPaymentSettings);
			
			verify(externalPaymentService).deregister(user);
			verifyNoMoreInteractions(externalPaymentService);
		}
	}

	@Test
	public void testUpdatePaymentSettings_CREDIT_CARD_TO_DIRECT_DEBIT() throws PaymentException
	{
		UserPaymentSettings userPaymentSettings = createUserPaymentSettings(PaymentMethod.CREDIT_CARD);
		
		when(subscriptionService.findAllSubscriptions(user)).thenReturn(Arrays.asList(subscription));
		when(subscriptionService.findCurrentSubscription(user)).thenReturn(Optional.ofNullable(subscription));

		updatePaymentMethod(PaymentMethod.DIRECT_DEBIT);

		THEN:
		{
			assertThat(userPaymentSettings.getPaymentMethod(), is(PaymentMethod.DIRECT_DEBIT));

			verify(userPaymentSettingsRepository).save(userPaymentSettings);

			verifyNoMoreInteractions(externalPaymentService);
		}
	}

	@Test
	public void testUpdatePaymentSettings_DIRECT_DEBIT_TO_CREDIT_CARD() throws PaymentException
	{
		UserPaymentSettings userPaymentSettings = createUserPaymentSettings(PaymentMethod.DIRECT_DEBIT);

		when(subscriptionService.findCurrentSubscription(user)).thenReturn(Optional.ofNullable(subscription));

		updatePaymentMethod(PaymentMethod.CREDIT_CARD);

		THEN:
		{
			assertThat(userPaymentSettings.getPaymentMethod(), is(PaymentMethod.CREDIT_CARD));

			verify(userPaymentSettingsRepository).save(userPaymentSettings);

			verifyNoMoreInteractions(externalPaymentService);
		}
	}

	@Test
	public void testUpdatePaymentSettings_PREPAYMENT_TO_CREDIT_CARD() throws PaymentException
	{
		UserPaymentSettings userPaymentSettings = createUserPaymentSettings(PaymentMethod.PREPAYMENT);

		when(subscriptionService.findCurrentSubscription(user)).thenReturn(Optional.ofNullable(subscription));

		updatePaymentMethod(PaymentMethod.CREDIT_CARD);

		THEN:
		{
			assertThat(userPaymentSettings.getPaymentMethod(), is(PaymentMethod.CREDIT_CARD));

			verify(userPaymentSettingsRepository).save(userPaymentSettings);

			verifyNoMoreInteractions(externalPaymentService);
		}
	}

	@Test
	public void testUpdatePaymentSettings_PREPAYMENT_TO_DIRECT_DEBIT() throws PaymentException
	{
		UserPaymentSettings userPaymentSettings = createUserPaymentSettings(PaymentMethod.PREPAYMENT);

		when(subscriptionService.findCurrentSubscription(user)).thenReturn(Optional.ofNullable(subscription));

		updatePaymentMethod(PaymentMethod.DIRECT_DEBIT);

		THEN:
		{
			assertThat(userPaymentSettings.getPaymentMethod(), is(PaymentMethod.DIRECT_DEBIT));

			verify(userPaymentSettingsRepository).save(userPaymentSettings);

			verifyNoMoreInteractions(externalPaymentService);
		}
	}

	private UserPaymentSettings createUserPaymentSettings(PaymentMethod paymentMethod)
	{
		UserPaymentSettings userPaymentSettings = paymentEntityFactory.createUserPaymentSettings(user, paymentMethod);
		when(userPaymentSettingsRepository.findByUser(user)).thenReturn(Optional.of(userPaymentSettings));

		return userPaymentSettings;
	}

	private UserPaymentSettings updatePaymentMethod(PaymentMethod paymentMethod)
			throws PaymentException
	{
		return paymentService.updateUserPaymentSettings(user, paymentMethod);
	}
}
