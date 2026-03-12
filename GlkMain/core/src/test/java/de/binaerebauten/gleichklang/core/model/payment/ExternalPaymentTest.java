package de.binaerebauten.gleichklang.core.model.payment;

import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link ExternalPayment}.
 */
public class ExternalPaymentTest
{
	@Test
	public void testCreateExternalReferenceId()
	{
		ExternalPayment externalPayment = new ExternalPayment();

		UUID uuid = UUID.fromString(externalPayment.createExternalReferenceId());

		assertThat(uuid, notNullValue());
	}
}
