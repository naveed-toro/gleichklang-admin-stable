package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import com.google.common.base.Joiner;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID.Type;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link TransactionID}.
 */
public class TransactionIDTest
{
	private final static String PAYMENT_ID = "2";

	private final static String REGISTRATION_ID = "3";

	private ExternalPayment externalPayment;

	private ExternalPaymentRegistration externalPaymentRegistration;

	private String hostName;

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Before
	public void setup()
	{
		User user = new User();

		externalPayment = new ExternalPayment();
		externalPayment.setExternalReferenceId(PAYMENT_ID);
		externalPayment.setUser(user);

		hostName = "mymachine";

		externalPaymentRegistration = new ExternalPaymentRegistration();
		TransactionID transactionID = TransactionID.parse(Type.REGISTRATION.toString() + ":" + REGISTRATION_ID + ":" + hostName);
		externalPaymentRegistration.setExternalReferenceId(transactionID.toString());
	}

	@Test
	public void testParse_PAYMENT()
	{
		String transactionIdString = Joiner.on(":").join(TransactionID.Type.PAYMENT, PAYMENT_ID, hostName);
		TransactionID transactionID = TransactionID.parse(transactionIdString);

		assertThat(transactionID.getType(), is(TransactionID.Type.PAYMENT));
		assertThat(transactionID.getId(), is(PAYMENT_ID));
	}

	@Test
	public void testParse_REGISTRATION()
	{
		String transactionIdString = Joiner.on(":").join(TransactionID.Type.REGISTRATION, REGISTRATION_ID, hostName);
		TransactionID transactionID = TransactionID.parse(transactionIdString);

		assertThat(transactionID.getType(), is(TransactionID.Type.REGISTRATION));
		assertThat(transactionID.getId(), is(REGISTRATION_ID));

		transactionIdString = Joiner.on(":").join(TransactionID.Type.REGISTRATION, "", hostName);
		transactionID = TransactionID.parse(transactionIdString);

		assertThat(transactionID.getType(), is(TransactionID.Type.REGISTRATION));
		assertThat(transactionID.getId(), nullValue());
	}

	@Test
	public void testToTransactionID_PAYMENT()
	{
		TransactionID transactionID = TransactionID.toTransactionID(externalPayment, TransactionID.Type.PAYMENT);

		assertThat(transactionID.getType(), is(TransactionID.Type.PAYMENT));
		assertThat(transactionID.getId(), is(PAYMENT_ID));
	}

	@Test
	public void testToTransactionID_REGISTRATION()
	{
		TransactionID transactionID = TransactionID.toTransactionID(externalPayment, TransactionID.Type.REGISTRATION);

		assertThat(transactionID.getType(), is(TransactionID.Type.REGISTRATION));
		assertThat(transactionID.getId(), is(PAYMENT_ID));
	}

	@Test
	public void testToTransactionID_CHANGE_REGISTRATION()
	{
		TransactionID transactionID = TransactionID.toTransactionID(externalPaymentRegistration);

		assertThat(transactionID.getType(), is(TransactionID.Type.CHANGE_REGISTRATION));
		assertThat(transactionID.getId(), is(REGISTRATION_ID));
	}
}
