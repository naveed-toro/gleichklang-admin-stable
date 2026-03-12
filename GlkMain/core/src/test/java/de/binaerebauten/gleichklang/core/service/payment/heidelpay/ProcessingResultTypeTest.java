package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.ObjectFactory;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ProcessingType;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link ProcessingResultType}.
 */
public class ProcessingResultTypeTest
{
	@Test
	public void testFrom()
	{
		ProcessingType processingType = new ObjectFactory().createProcessingType();

		assertThat(ProcessingResultType.from(processingType), is(ProcessingResultType.UNKNOWN));

		processingType.setResult(UUID.randomUUID().toString());
		assertThat(ProcessingResultType.from(processingType), is(ProcessingResultType.UNKNOWN));

		processingType.setResult(ProcessingResultType.ACK.name());
		assertThat(ProcessingResultType.from(processingType), is(ProcessingResultType.ACK));

		processingType.setResult(ProcessingResultType.NOK.name());
		assertThat(ProcessingResultType.from(processingType), is(ProcessingResultType.NOK));
	}
}
