package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import de.binaerebauten.gleichklang.core.model.heidelpay.query.*;
import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRegistrationRepository;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID.Type;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import de.binaerebauten.gleichklang.core.utils.PaymentEntityFactory;
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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Unit test for {@link HeidelpayQueryApiConnector}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { HeidelpayClientTestConfig.class,
		PaymentEntityFactory.class, HeidelpayQueryBuilder.class })
public class HeidelpayQueryApiConnectorTest
{
	private HeidelpayQueryApiConnector connector;
	
	private AllEncompassingFormHttpMessageConverter formMessageConverter;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private HeidelpayQueryBuilder queryBuilder;
	
	@Autowired
	private PaymentEntityFactory paymentEntityFactory;
	
	@Value("${heidelpay.query.url}")
	private URI queryUrl;
	
	private ObjectFactory jaxbFactory = new ObjectFactory();

	private MockRestServiceServer mockServer;

	@Before
	public void setup()
	{
		mockServer = MockRestServiceServer.createServer(restTemplate);
		formMessageConverter = new AllEncompassingFormHttpMessageConverter();

		connector = new HeidelpayQueryApiConnector(queryUrl, restTemplate,
				mock(ExternalPaymentRegistrationRepository.class),
				queryBuilder, mock(HeidelpayTransactionService.class));
	}

	@Test
	public void testPerformQueryForTransactionTypes()
			throws PaymentException, JAXBException, IOException
	{
		LocalDate to = LocalDate.now();
		LocalDate from = to.minusWeeks(1);

		String expectedRequestBody = createQueryRequestBody(to, from, TransactionType.CHARGEBACK);

		JAXBElement<ResponseType> response = createQueryResponse();
		String responseBody = queryBuilder.encodeAsXml(response);

		mockServer.expect(requestTo(queryUrl))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().string(expectedRequestBody))
				.andRespond(withSuccess(responseBody, MediaType.APPLICATION_XML));

		List<TransactionResponseType> transactions = connector.queryForTransactionTypes(from, to, TransactionType.CHARGEBACK, null);

		assertThat(transactions, notNullValue());
		assertThat(transactions.size(), equalTo(1));

		mockServer.verify();
	}

	@Test
	public void testRegister()
			throws PaymentException, JAXBException, IOException
	{
		User user = DefaultStaticEntityFactory.createDefaultUser(DefaultStaticEntityFactory.DEFAULT_EMAIL,
				DefaultStaticEntityFactory.DEFAULT_ALIAS, RecommendationCategory.FRIENDSHIP);

		Country defaultCountry = DefaultStaticEntityFactory.createDefaultCountry(DefaultStaticEntityFactory.createDefaultContinent());
		Address billingAddress = DefaultStaticEntityFactory.createDefaultAddress(defaultCountry);
		billingAddress.setPayment(true);
		user.addAddress(billingAddress);

		UserPaymentSettings userPaymentSettings = paymentEntityFactory.createUserPaymentSettings(user, PaymentMethod.CREDIT_CARD);

		ExternalPayment externalPayment = paymentEntityFactory.createExternalPayment(user, PaymentState.PAID);

		CreditCardAccount creditCard = new CreditCardAccount();
		creditCard.setHolder(String.format("%s %s", user.getFirstName(), user.getLastName()));
		creditCard.setNumber("4012888888881881");
		creditCard.setVerification("123");
		creditCard.setExpirationDate(YearMonth.of(2016, 10));
		creditCard.setBrand("VISA");

		String expectedRequestBody = createRegisterRequestBody(user, userPaymentSettings, externalPayment, creditCard);

		JAXBElement<ResponseType> response = createRegisterResponse();
		String responseBody = queryBuilder.encodeAsXml(response);

		mockServer.expect(requestTo(queryUrl))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().string(expectedRequestBody))
				.andRespond(withSuccess(responseBody, MediaType.APPLICATION_XML));

		connector.register(user, userPaymentSettings, externalPayment, creditCard);

		mockServer.verify();
	}

	private String createRegisterRequestBody(User user, UserPaymentSettings userPaymentSettings,
			ExternalPayment externalPayment, Account creditCard)
			throws PaymentException, IOException
	{
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		TransactionID transactionId = TransactionID.toTransactionID(externalPayment, Type.REGISTRATION);
		params.add("load", queryBuilder.encodeAsXml(queryBuilder.createRegisterRequest(user, userPaymentSettings, transactionId, creditCard)));

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		formMessageConverter.write(params, MediaType.APPLICATION_FORM_URLENCODED, outputMessage);

		return outputMessage.getBodyAsString();
	}

	private JAXBElement<ResponseType> createRegisterResponse()
	{
		ResponseType responseType = jaxbFactory.createResponseType();

		TransactionResponseType transaction = jaxbFactory.createTransactionResponseType();
		IdentificationResponseType identification = jaxbFactory.createIdentificationResponseType();
		identification.setUniqueID("unique-id");
		transaction.setIdentification(identification);
		responseType.setTransaction(transaction);

		return jaxbFactory.createResponse(responseType);
	}

	@Test
	public void testReregister()
			throws PaymentException, JAXBException, IOException
	{
		User user = DefaultStaticEntityFactory.createDefaultUser(DefaultStaticEntityFactory.DEFAULT_EMAIL,
				DefaultStaticEntityFactory.DEFAULT_ALIAS, RecommendationCategory.FRIENDSHIP);

		ExternalPaymentRegistration externalPaymentRegistration =
				paymentEntityFactory.createExternalPaymentRegistration(user, "registration-id");

		UserPaymentSettings userPaymentSettings = paymentEntityFactory.createUserPaymentSettings(user, PaymentMethod.CREDIT_CARD);

		String expectedRequestBody = createReregisterRequestBody(userPaymentSettings, externalPaymentRegistration);

		JAXBElement<ResponseType> response = createReregisterResponse();
		String responseBody = queryBuilder.encodeAsXml(response);

		mockServer.expect(requestTo(queryUrl))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().string(expectedRequestBody))
				.andRespond(withSuccess(responseBody, MediaType.APPLICATION_XML));

		connector.reregister(userPaymentSettings, externalPaymentRegistration);

		mockServer.verify();
	}

	private String createReregisterRequestBody(UserPaymentSettings userPaymentSettings, ExternalPaymentRegistration externalPaymentRegistration)
			throws PaymentException, IOException
	{
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		TransactionID transactionId = TransactionID.toTransactionID(externalPaymentRegistration);
		params.add("load", queryBuilder.encodeAsXml(queryBuilder.createReregisterRequest(userPaymentSettings, externalPaymentRegistration, transactionId)));

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		formMessageConverter.write(params, MediaType.APPLICATION_FORM_URLENCODED, outputMessage);

		return outputMessage.getBodyAsString();
	}

	private JAXBElement<ResponseType> createReregisterResponse()
	{
		ResponseType responseType = jaxbFactory.createResponseType();

		responseType.setTransaction(jaxbFactory.createTransactionResponseType());

		return jaxbFactory.createResponse(responseType);
	}

	@Test
	public void testDeregister()
			throws PaymentException, JAXBException, IOException
	{
		User user = DefaultStaticEntityFactory.createDefaultUser(DefaultStaticEntityFactory.DEFAULT_EMAIL,
				DefaultStaticEntityFactory.DEFAULT_ALIAS, RecommendationCategory.FRIENDSHIP);

		ExternalPaymentRegistration externalPaymentRegistration =
				paymentEntityFactory.createExternalPaymentRegistration(user, "registration-id");

		UserPaymentSettings userPaymentSettings = paymentEntityFactory.createUserPaymentSettings(user, PaymentMethod.CREDIT_CARD);

		String expectedRequestBody = createDeregisterRequestBody(userPaymentSettings, externalPaymentRegistration);

		JAXBElement<ResponseType> response = createDeregisterResponse();
		String responseBody = queryBuilder.encodeAsXml(response);

		mockServer.expect(requestTo(queryUrl))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().string(expectedRequestBody))
				.andRespond(withSuccess(responseBody, MediaType.APPLICATION_XML));

		connector.deregister(userPaymentSettings, externalPaymentRegistration);

		mockServer.verify();
	}

	private String createDeregisterRequestBody(UserPaymentSettings userPaymentSettings, ExternalPaymentRegistration externalPaymentRegistration)
			throws PaymentException, IOException
	{
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		TransactionID transactionId = TransactionID.toTransactionID(externalPaymentRegistration);
		params.add("load", queryBuilder.encodeAsXml(queryBuilder.createDeregisterRequest(userPaymentSettings, externalPaymentRegistration, transactionId)));

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		formMessageConverter.write(params, MediaType.APPLICATION_FORM_URLENCODED, outputMessage);

		return outputMessage.getBodyAsString();
	}

	private String createQueryRequestBody(LocalDate to, LocalDate from, TransactionType paymentType)
			throws PaymentException, IOException
	{
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("load", queryBuilder.encodeAsXml(queryBuilder.createQueryRequest(from, to, paymentType, null)));

		MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
		formMessageConverter.write(params, MediaType.APPLICATION_FORM_URLENCODED, outputMessage);

		return outputMessage.getBodyAsString();
	}

	private JAXBElement<ResponseType> createQueryResponse()
	{
		ResponseType responseType = jaxbFactory.createResponseType();

		ResultType resultType = jaxbFactory.createResultType();
		responseType.setResult(resultType);

		resultType.getTransaction().add(jaxbFactory.createTransactionResponseType());

		return jaxbFactory.createResponse(responseType);
	}

	private JAXBElement<ResponseType> createDeregisterResponse()
	{
		ResponseType responseType = jaxbFactory.createResponseType();

		responseType.setTransaction(jaxbFactory.createTransactionResponseType());

		return jaxbFactory.createResponse(responseType);
	}
	
	@Test
	public void transactionExistsTest()
	{
		ResponseType okResponse = getResponseType(ProcessingResultType.ACK, TransactionType.SCHEDULE, "", "");
		assertThat(connector.transactionExists(okResponse), is(true));
		
		ResponseType errorResponse = getResponseType(ProcessingResultType.NOK, TransactionType.SCHEDULE, "", "");
		assertThat(connector.transactionExists(errorResponse), is(false));
	}
	
	private ResponseType getResponseType(ProcessingResultType processingResultType,
			TransactionType transactionType, String uniqueId, String referenceId)
	{
		ResponseType responseType = jaxbFactory.createResponseType();
		ResultType resultType = jaxbFactory.createResultType();
		TransactionResponseType transactionResponseType = getTransactionResponseType(
				processingResultType, transactionType, uniqueId, referenceId);
		resultType.getTransaction().add(transactionResponseType);
		responseType.setResult(resultType);

		return responseType;
	}
	
	private TransactionResponseType getTransactionResponseType(ProcessingResultType processingResultType,
			TransactionType transactionType, String uniqueId, String referenceId)
	{
		TransactionResponseType transactionResponseType = jaxbFactory.createTransactionResponseType();
		ProcessingType processingType = jaxbFactory.createProcessingType();
		processingType.setCode(String.format("CC.%s", transactionType.getCode()));
		processingType.setResult(processingResultType.name());
		transactionResponseType.setProcessing(processingType);
		
		IdentificationResponseType identificationResponseType = jaxbFactory.createIdentificationResponseType();
		identificationResponseType.setUniqueID(uniqueId);
		identificationResponseType.setReferenceID(referenceId);
		transactionResponseType.setIdentification(identificationResponseType);

		return transactionResponseType;
	}
	
	@Test
	public void getActiveSchedulerTransactionSimpleTest()
	{
		ResponseType responseType = jaxbFactory.createResponseType();
		ResultType resultType = jaxbFactory.createResultType();
		
		TransactionResponseType transactionResponseType = getTransactionResponseType(ProcessingResultType.ACK, TransactionType.SCHEDULE, "", "");
		
		resultType.getTransaction().add(transactionResponseType);
		responseType.setResult(resultType);
		
		List<TransactionResponseType> result = connector.getActiveSchedulerTransaction(responseType);
		assertThat(result.isEmpty(), is(false));
	}
	
	@Test
	public void getActiveSchedulerTransactionNotFoundTest()
	{
		ResponseType responseType = jaxbFactory.createResponseType();
		ResultType resultType = jaxbFactory.createResultType();
		
		TransactionResponseType transactionResponseType = getTransactionResponseType(
				ProcessingResultType.NOK, TransactionType.SCHEDULE, "", "");
		
		resultType.getTransaction().add(transactionResponseType);
		responseType.setResult(resultType);
		
		List<TransactionResponseType> result = connector.getActiveSchedulerTransaction(responseType);
		assertThat(result.isEmpty(), is(true));
	}
	
	@Test
	public void getActiveSchedulerTransactionAdvancedTest()
	{
		ResponseType responseType = jaxbFactory.createResponseType();
		ResultType resultType = jaxbFactory.createResultType();
		
		// 1 active
		TransactionResponseType initSubscription = getTransactionResponseType(
				ProcessingResultType.ACK, TransactionType.SCHEDULE, "initSubscription", "");
		TransactionResponseType failedRequest = getTransactionResponseType(
				ProcessingResultType.NOK, TransactionType.RESCHEDULE, "updatedSubscription", "initSubscription");
		TransactionResponseType updatedSubscription = getTransactionResponseType(
				ProcessingResultType.ACK, TransactionType.RESCHEDULE, "updatedSubscription", "initSubscription");
		
		resultType.getTransaction().addAll(Arrays.asList(initSubscription, failedRequest, updatedSubscription));
		responseType.setResult(resultType);
		
		assertThat(connector.getActiveSchedulerTransaction(responseType).isEmpty(), is(false));
		
		// Last active disabled
		TransactionResponseType descheduledSubscription = getTransactionResponseType(
				ProcessingResultType.ACK, TransactionType.DESCHEDULE, "descheduledSubscription", "updatedSubscription");
		
		resultType.getTransaction().addAll(Arrays.asList(initSubscription, failedRequest, updatedSubscription, descheduledSubscription));
		responseType.setResult(resultType);
		
		assertThat(connector.getActiveSchedulerTransaction(responseType).isEmpty(), is(true));
	}
	
	@Test
	public void getTrasactionTypePatternTest()
	{
		final String trasactionTypePattern = connector.getTrasactionTypePattern(
				TransactionType.SCHEDULE, TransactionType.DESCHEDULE);
		assertThat(trasactionTypePattern, is("^.*\\.(SD|DS).*$"));
	}

}
