package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import de.binaerebauten.gleichklang.core.config.HeidelpayControllerConfig;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ObjectFactory;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.user.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This is a more integration like test for the {@link HeidelpayTransactionResponseController}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@WebAppConfiguration
@ActiveProfiles("heidelpay")
@ContextConfiguration(classes = { HeidelpayControllerConfig.class, HeidelpayControllerTestConfig.class, HeidelpayTransactionResponseController.class })
public class HeidelpayTransactionResponseControllerTest
{
	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	private ObjectFactory jaxbFacory;

	private TransactionID transactionID;

	@Before
	public void setup()
	{
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

		jaxbFacory = new ObjectFactory();

		User user = new User();
		user.setId(0L);

		ExternalPayment payment = new ExternalPayment();
		payment.setId(1L);
		payment.setUser(user);

		transactionID = TransactionID.toTransactionID(payment, TransactionID.Type.PAYMENT);
	}

	@Test
	public void testHeidelpayPush_ACK() throws Exception
	{

		MediaType mediaType = MediaType.parseMediaType(HeidelpayControllerConfig.TRANSACTION_MEDIA_TYPE_VALUE);
		String content = createResponseContent(transactionID, ProcessingResultType.ACK);
		MockHttpServletRequestBuilder requestBuilder = post("/heidelpay/push")
				.contentType(mediaType)
				.content(content);

		this.mockMvc.perform(requestBuilder)
				.andExpect(status().isOk());
	}

	@Test
	public void testHeidelpayPush_NOK() throws Exception
	{
		MediaType mediaType = MediaType.parseMediaType(HeidelpayControllerConfig.TRANSACTION_MEDIA_TYPE_VALUE);
		String content = createResponseContent(transactionID, ProcessingResultType.NOK);
		MockHttpServletRequestBuilder requestBuilder = post("/heidelpay/push")
				.contentType(mediaType)
				.content(content);

		this.mockMvc.perform(requestBuilder)
				.andExpect(status().isOk());
	}

	private String createResponseContent(TransactionID transactionID, ProcessingResultType resultType)
	{
		return String.format(
				"<Response>" +
						"<Transaction>" +
						"<Identification><TransactionID>%s</TransactionID></Identification>" +
						"<Payment code=\"CC.SD\"/>" +
						"<Processing>" +
						"<Result>%s</Result>" +
						"</Processing>" +
						"</Transaction>"
						+ "</Response>",
				transactionID, resultType);
	}
}
