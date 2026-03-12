package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.IdentificationRequestType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.RequestType;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBElement;
import java.time.LocalDateTime;
import java.util.Optional;

import static de.binaerebauten.gleichklang.core.utils.FunctionalUtils.nullSafe;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { HeidelpayClientTestConfig.class, PaymentEntityFactory.class,
		HeidelpayQueryBuilder.class })
public class HeidelPayQueryBuilderTest
{
	private final static Logger LOG = LoggerFactory.getLogger(HeidelPayQueryBuilderTest.class);

	private static final long USER_ID = 1L;

	private static final long SUBSCRIPTION_ID = 2L;

	private static final long EXTERNAL_PAYMENT_ID = 3L;

	public static final String REGISTRATION_ID = "registration-id";

	@Autowired
	private HeidelpayQueryBuilder queryBuilder;

	@Autowired
	private PaymentEntityFactory paymentEntityFactory;

	private SubscriptionOffer initialSubscriptionOffer;

	private ExternalPaymentRegistration externalPaymentRegistration;

	private User user;

	private Subscription subscription;

	private RenewalOffer autoRenewalOffer;

	private ExternalPayment externalPayment;

	@Before
	public void setup()
	{
		user = DefaultStaticEntityFactory
				.createDefaultUser(DefaultStaticEntityFactory.DEFAULT_EMAIL,
						DefaultStaticEntityFactory.DEFAULT_ALIAS, RecommendationCategory.FRIENDSHIP);
		user.setId(USER_ID);

		initialSubscriptionOffer =
				paymentEntityFactory.createIntialSubscriptionOffer("Test-Initial-Offer", LocalDateTime.now(), 12,
						RecommendationCategory.FRIENDSHIP);
		autoRenewalOffer =
				paymentEntityFactory.createRenewalOffer("Test-Auto-Renewal-Offer", LocalDateTime.now(),
						initialSubscriptionOffer.getDuration() / 2,
						RecommendationCategory.FRIENDSHIP);

		initialSubscriptionOffer.setAutoRenewalOffer(autoRenewalOffer);

		subscription = paymentEntityFactory.createSubscription(user, initialSubscriptionOffer, LocalDateTime.now());
		subscription.setId(SUBSCRIPTION_ID);

		externalPayment = paymentEntityFactory.createExternalPayment(user, PaymentState.PENDING);
		externalPayment.setId(EXTERNAL_PAYMENT_ID);

		externalPaymentRegistration =
				paymentEntityFactory.createExternalPaymentRegistration(user, REGISTRATION_ID);
	}

	@Test
	public void testCreatePaymentRequest() throws PaymentException
	{
		JAXBElement<RequestType> paymentRequestElement =
				queryBuilder.createPaymentRequest(externalPayment, externalPaymentRegistration);
		LOG.debug(queryBuilder.encodeAsXml(paymentRequestElement));

		RequestType paymentRequest = paymentRequestElement.getValue();

		Optional<String> accountRegistration =
				nullSafe(() -> paymentRequest.getTransaction().getAccount().getRegistration());

		assertThat(accountRegistration.isPresent(), is(true));
		assertThat(accountRegistration.get(), is(REGISTRATION_ID));

		Optional<IdentificationRequestType> identificationRequestOptional =
				nullSafe(() -> paymentRequest.getTransaction().getIdentification());

		assertThat(identificationRequestOptional.isPresent(), is(true));
		IdentificationRequestType identificationRequest = identificationRequestOptional.get();

		TransactionID transactionID = TransactionID.toTransactionID(externalPayment, TransactionID.Type.PAYMENT);
		assertThat(identificationRequest.getTransactionID(), is(transactionID.toString()));
	}
}
