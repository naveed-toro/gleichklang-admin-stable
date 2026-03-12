package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link PaymentCode}.
 */
public class PaymentCodeTest
{
	@Test
	public void testParse()
	{
		List<String> validPaymentCodes = Arrays.asList("CC.DR", "DD.DR", "CC.DB", "DD.DB", "DD.DR");

		for (String validPaymentCodeStr : validPaymentCodes)
		{
			final PaymentCode parsedPaymentCode = PaymentCode.parse(validPaymentCodeStr);

			assertThat(parsedPaymentCode.toString(), is(parsedPaymentCode.toString()));
		}
	}
}
