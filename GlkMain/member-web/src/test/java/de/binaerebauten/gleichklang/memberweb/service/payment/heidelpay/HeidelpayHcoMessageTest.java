package de.binaerebauten.gleichklang.memberweb.service.payment.heidelpay;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.binaerebauten.gleichklang.core.model.payment.AvailableCurrency;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.MonetaryAmount;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayPaymentMethodCode;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author matthias.koester@binaere-bauten.de
 */
public class HeidelpayHcoMessageTest
{
	private static final String SECURITY_SENDER_VALUE = "securitySender";
	private static final String USER_LOGIN_VALUE = "userLogin";
	private static final String ACCOUNT_REGISTRATION = "accountRegistration";

	private final ObjectMapper mapper = new ObjectMapper();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testSerialize()
	{
		HeidelpayHcoMessage msg = new HeidelpayHcoMessage();

		msg.setSecuritySender(SECURITY_SENDER_VALUE);
		msg.setUserLogin(USER_LOGIN_VALUE);
		msg.setAccountRegistration(ACCOUNT_REGISTRATION);

		Map<String, String> map = msg.toMultiValueMap().toSingleValueMap();

		assertEquals(SECURITY_SENDER_VALUE, map.get(HeidelpayHcoParameters.SECURITY_SENDER));
		assertEquals(USER_LOGIN_VALUE, map.get(HeidelpayHcoParameters.USER_LOGIN));
		assertEquals(ACCOUNT_REGISTRATION, map.get(HeidelpayHcoParameters.ACCOUNT_REGISTRATION));
	}

	@Test
	public void testDeserialize()
	{
		Map<String, String> map = new HashMap<>();

		map.put(HeidelpayHcoParameters.SECURITY_SENDER, SECURITY_SENDER_VALUE);
		map.put(HeidelpayHcoParameters.USER_LOGIN, USER_LOGIN_VALUE);
		map.put(HeidelpayHcoParameters.ACCOUNT_REGISTRATION, ACCOUNT_REGISTRATION);

		HeidelpayHcoMessage msg = HeidelpayHcoMessage.fromMap(map);

		assertEquals(SECURITY_SENDER_VALUE, msg.getSecuritySender());
		assertEquals(USER_LOGIN_VALUE, msg.getUserLogin());
		assertEquals(ACCOUNT_REGISTRATION, msg.getAccountRegistration());
	}

	@Test
	public void testCreatePayment()
	{
		for (HeidelpayPaymentMethodCode paymentMethodCode : HeidelpayPaymentMethodCode.values())
		{
			HeidelpayHcoMessage msg = new HeidelpayHcoMessage();

			msg.setPaymentCode(paymentMethodCode.code);

			BigDecimal value = BigDecimal.TEN.multiply(BigDecimal.TEN).setScale(2);
			MonetaryAmount amount = new MonetaryAmount(value, AvailableCurrency.EUR);
			msg.setAmount(amount.getAmount().toPlainString());

			msg.setCurrency(amount.getCurrency().name());

			String uniqueId = "UniqueId";
			msg.setIdentificationUniqueId(uniqueId);

			ExternalPayment payment = msg.createPayment();

			assertThat(payment.getMethod(), equalTo(paymentMethodCode.paymentMethod));
			assertThat(payment.getAmount(), equalTo(amount));
		}
	}

	@Test
	public void testGetTransactionType()
	{
		HeidelpayHcoMessage msg = new HeidelpayHcoMessage();

		msg.setPaymentCode("CC.RG");
		assertThat(msg.getTransactionType(), is(TransactionType.REGISTRATION));

		msg.setPaymentCode("DD.RR");
		assertThat(msg.getTransactionType(), is(TransactionType.REREGISTRATION));

		msg.setPaymentCode("DD.DR");
		assertThat(msg.getTransactionType(), is(TransactionType.DEREGISTRATION));

		expectedException.expect(IllegalArgumentException.class);

		msg.setPaymentCode("DB.BB");
		msg.getTransactionType();
	}
}
