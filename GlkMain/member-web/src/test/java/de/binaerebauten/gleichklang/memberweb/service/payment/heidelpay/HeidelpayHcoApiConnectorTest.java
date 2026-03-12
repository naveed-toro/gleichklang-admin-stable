package de.binaerebauten.gleichklang.memberweb.service.payment.heidelpay;

import com.google.common.collect.Maps;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ContactType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.CustomerType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.NameType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ObjectFactory;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPaymentRegistration;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.HeidelpayClientTestConfig;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.ProcessingResultType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { HeidelpayClientTestConfig.class, HeidelpayHcoApiConnector.class })
public class HeidelpayHcoApiConnectorTest
{
	private static URI PAYMENT_DATA_UPDATE_URL = URI.create("https://payment.com/data/update");

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private HeidelpayHcoApiConnector connector;

	@Value("${heidelpay.hco.url}")
	private URI hcoUrl;

	private MockRestServiceServer mockServer;

	private AllEncompassingFormHttpMessageConverter formMessageConverter;

	private User user;

	private ExternalPaymentRegistration externalPaymentRegistration;

	private ObjectFactory jaxbFactory = new ObjectFactory();

	@Before
	public void setup()
	{
		mockServer = MockRestServiceServer.createServer(restTemplate);
		formMessageConverter = new AllEncompassingFormHttpMessageConverter();

		user = new User();
		user.setId(1L);

		externalPaymentRegistration = new ExternalPaymentRegistration();
		externalPaymentRegistration.setExternalReferenceId("REGISTRATION:1:avior");
		externalPaymentRegistration.setUser(user);
	}

	@Test
	public void testSendMessage()
			throws PaymentException, IOException
	{
		String hcoResponse = createResponse("/payment/heidelpay_hco_response.properties");

		mockServer.expect(requestTo(hcoUrl))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess(hcoResponse, MediaType.APPLICATION_FORM_URLENCODED));

		HeidelpayHcoMessage heidelpayHcoMessage = connector.sendMessage(new HeidelpayHcoMessage());

		assertThat(heidelpayHcoMessage, notNullValue());

		mockServer.verify();
	}

	@Test
	public void testGetPaymentDataUpdateFormUrl()
			throws PaymentException, IOException
	{
		String hcoResponse = createAckResponse(PAYMENT_DATA_UPDATE_URL);

		mockServer.expect(requestTo(hcoUrl))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess(hcoResponse, MediaType.APPLICATION_FORM_URLENCODED));

		CustomerType customer = createCustomer();
		URI paymentDataUpdateFormUrl = connector.getPaymentDataUpdateFormUrl(customer, PaymentMethod.CREDIT_CARD, externalPaymentRegistration);

		assertThat(paymentDataUpdateFormUrl, notNullValue());
		assertThat(paymentDataUpdateFormUrl, is(PAYMENT_DATA_UPDATE_URL));

		mockServer.verify();
	}

	private String createAckResponse(URI url) throws IOException
	{
		String frontendRedirectUrl = url.toString();

		HeidelpayHcoMessage hcoResponseMessage = new HeidelpayHcoMessage();
		hcoResponseMessage.setPostValidation(ProcessingResultType.ACK.name());
		hcoResponseMessage.setFrontendRedirectUrl(frontendRedirectUrl);

		return createResponse(hcoResponseMessage);
	}

	private String createResponse(String propertiesResource) throws IOException
	{
		Properties properties = new Properties();
		properties.load(getClass().getResourceAsStream(propertiesResource));

		MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
		formParams.setAll(Maps.fromProperties(properties));

		return createResponse(formParams);
	}

	private String createResponse(HeidelpayHcoMessage message)
			throws IOException
	{
		MultiValueMap<String, String> formParams = message.toMultiValueMap();

		return createResponse(formParams);
	}

	private String createResponse(MultiValueMap<String, String> formParams)
			throws IOException
	{
		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		formMessageConverter.write(formParams, MediaType.APPLICATION_FORM_URLENCODED, outputMessage);

		return outputMessage.getBodyAsString();
	}

	private CustomerType createCustomer()
	{
		CustomerType customer = jaxbFactory.createCustomerType();

		NameType name = jaxbFactory.createNameType();
		customer.setName(name);

		ContactType contact = jaxbFactory.createContactType();
		customer.setContact(contact);

		return customer;
	}
}
