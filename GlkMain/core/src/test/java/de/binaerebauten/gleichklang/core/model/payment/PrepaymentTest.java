package de.binaerebauten.gleichklang.core.model.payment;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link Prepayment}.
 */
public class PrepaymentTest
{
	@Test
	public void testCreateExternalReferenceId()
	{
		Prepayment prepayment = new Prepayment();

		prepayment.setId(999999999999L);
		assertThat(prepayment.createExternalReferenceId(), is("9999-9999-9999"));

		prepayment.setId(1L);
		assertThat(prepayment.createExternalReferenceId(), is("0001-0000-0000"));
	}
}
