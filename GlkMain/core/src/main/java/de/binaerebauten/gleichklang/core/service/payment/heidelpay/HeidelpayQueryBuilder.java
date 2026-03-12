package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.*;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides a builder for the heidelpay query xml api.
 * Is separated in an own class so that unit tests can
 * reuse it.
 */
@Component
public class HeidelpayQueryBuilder
{
	private static final QName REQUEST_QNAME = new QName(null, "Request");

	private final ObjectFactory jaxbFactory = new ObjectFactory();

	@Value("${heidelpay.transaction.channel}")
	private String transactionChannel;

	@Value("${heidelpay.security.sender}")
	private String securitySender;

	@Value("${heidelpay.transaction.mode}")
	private String transactionMode;

	@Value("${heidelpay.user.login}")
	private String userLogin;

	@Value("${heidelpay.user.pwd}")
	private String userPwd;

	@Value("${heidelpay.frontend.response_url}")
	private String frontendResponseUrl;


	/**
	 * Creates a query request to retrieve the heidelpay transaction for the payment type.
	 *
	 * @param from the from date
	 * @param to   the to date
	 * @param type the payment type
	 * @return the request which contains the heidelpay transaction
	 */
	public JAXBElement<RequestType> createQueryRequest(LocalDate from, LocalDate to, TransactionType type, ProcessingResultType resultType)
	{
		Objects.requireNonNull(from, "from == null");
		Objects.requireNonNull(to, "to == null");
		Objects.requireNonNull(type, "type == null");

		final JAXBElement<RequestType> request = createRequest();

		final QueryType query = createQuery();
		query.setPeriod(createPeriod(from, to));
		query.setTypes(createTransactionTypes(type));

		if (Objects.nonNull(resultType))
		{
			query.setProcessingResult(resultType.name());
		}

		request.getValue().setQuery(query);

		return request;
	}

	public JAXBElement<RequestType> createLinkedTransactionsQuery(String uniqueId, TransactionType type, ProcessingResultType resultType)
	{
		Objects.requireNonNull(uniqueId, "uniqueId == null");
		Objects.requireNonNull(type, "type == null");

		final JAXBElement<RequestType> request = createRequest();

		final QueryType query = createQuery();
		query.setType("LINKED_TRANSACTIONS");
		query.setTypes(createTransactionTypes(type));

		if (Objects.nonNull(resultType))
		{
			query.setProcessingResult(resultType.name());
		}

		request.getValue().setQuery(query);

		final IdentificationRequestQueryType identificationRequestQuery = createIdentificationRequestQuery(uniqueId);
		query.setIdentification(identificationRequestQuery);

		return request;
	}

	public JAXBElement<RequestType> createQueryRequest(String uniqueId)
	{
		final JAXBElement<RequestType> request = createRequest();

		final QueryType query = createQuery();
		request.getValue().setQuery(query);

		final IdentificationRequestQueryType identificationRequestQuery = createIdentificationRequestQuery(uniqueId);
		query.setIdentification(identificationRequestQuery);

		return request;
	}

	/**
	 * Creates a query request to retrieve the heidelpay transaction for the given payment.
	 *
	 * @param externalPayment the payment
	 * @param type            the transaction type
	 * @param resultType 	  the result type (ACK/NOK)
	 *
	 * @return the request which contains the heidelpay transaction query
	 */
	public JAXBElement<RequestType> createQueryRequest(ExternalPayment externalPayment,
													   TransactionID.Type type, ProcessingResultType resultType)
	{
		Objects.requireNonNull(externalPayment, "externalPayment == null");
		Objects.requireNonNull(type, "type == null");

		TransactionID transactionID = TransactionID.toTransactionID(externalPayment, type);

		return createQueryRequest(transactionID, resultType);
	}

	public JAXBElement<RequestType> createPaymentRequest(ExternalPayment externalPayment,
														 ExternalPaymentRegistration externalPaymentRegistration)
	{
		Objects.requireNonNull(externalPayment, "externalPayment == null");
		Objects.requireNonNull(externalPaymentRegistration, "externalPaymentRegistration == null");

		TransactionRequestType transactionRequest = createTransactionRequest(externalPayment.getMethod());

		AccountRequestType accountRequest = createAccountRequest(externalPaymentRegistration);
		transactionRequest.setAccount(accountRequest);

		PaymentRequestType paymentRequest = createPaymentRequest(externalPayment.getAmount(),
				externalPayment.getMethod(), TransactionType.DEBIT);
		transactionRequest.setPayment(paymentRequest);

		TransactionID transactionID = TransactionID.toTransactionID(externalPayment, TransactionID.Type.PAYMENT);
		final IdentificationRequestType identificationRequest = createIdentificationRequest(transactionID);
		transactionRequest.setIdentification(identificationRequest);

		JAXBElement<RequestType> request = createRequest();

		RequestType requestType = request.getValue();
		requestType.setTransaction(transactionRequest);

		return request;
	}

	public JAXBElement<RequestType> createRefundRequest(String referenceId, ExternalPayment refundPayment, ExternalPaymentRegistration externalPaymentRegistration)
	{
		Objects.requireNonNull(referenceId, "referenceId == null");
		Objects.requireNonNull(externalPaymentRegistration, "externalPaymentRegistration == null");

		TransactionRequestType transactionRequest = createTransactionRequest(null);

		AccountRequestType accountRequest = createAccountRequest(externalPaymentRegistration);
		transactionRequest.setAccount(accountRequest);

		PaymentRequestType paymentRequest = createPaymentRequest(refundPayment.getAmount(), refundPayment.getMethod(), TransactionType.REFUND);
		transactionRequest.setPayment(paymentRequest);

		TransactionID transactionID = TransactionID.toTransactionID(refundPayment, TransactionID.Type.PAYMENT);
		final IdentificationRequestType identificationRequest = createIdentificationRequest(transactionID);
		identificationRequest.setReferenceID(referenceId);
		transactionRequest.setIdentification(identificationRequest);

		JAXBElement<RequestType> request = createRequest();

		RequestType requestType = request.getValue();
		requestType.setTransaction(transactionRequest);

		return request;
	}

	public JAXBElement<RequestType> createQueryRequest(TransactionID transactionID, ProcessingResultType resultType)
	{
		Objects.requireNonNull(transactionID, "transactionID == null");

		final JAXBElement<RequestType> request = createRequest();

		final QueryType query = createQuery();
		request.getValue().setQuery(query);

		final IdentificationRequestQueryType identificationRequestQueryType = createIdentificationRequestQuery(transactionID);
		query.setIdentification(identificationRequestQueryType);

		if (Objects.nonNull(resultType))
		{
			query.setProcessingResult(resultType.name());
		}

		return request;
	}

	public JAXBElement<RequestType> createRegisterRequest(User completeUser,
														  UserPaymentSettings userPaymentSettings, TransactionID transactionId, Account account)
	{
		Objects.requireNonNull(userPaymentSettings, "userPaymentSettings == null");

		JAXBElement<RequestType> request = createRequest();

		TransactionRequestType transactionRequest = createTransactionRequest(null);

		IdentificationRequestType identification = jaxbFactory.createIdentificationRequestType();
		identification.setTransactionID(transactionId.toString());
		transactionRequest.setIdentification(identification);

		PaymentRequestType paymentType = createPaymentRequestType(userPaymentSettings.getPaymentMethod(), TransactionType.REGISTRATION);
		transactionRequest.setPayment(paymentType);

		AccountRequestType accountType = jaxbFactory.createAccountRequestType();
		accountType.setHolder(account.getHolder());
		if (account instanceof CreditCardAccount)
		{
			CreditCardAccount creditCard = (CreditCardAccount) account;
			accountType.setNumber(creditCard.getNumber());
			accountType.setVerification(creditCard.getVerification());

			ExpiryType expiryType = jaxbFactory.createExpiryType();
			expiryType.setMonth(String.valueOf(creditCard.getExpirationDate().getMonthValue()));
			expiryType.setYear(String.valueOf(creditCard.getExpirationDate().getYear()));
			accountType.setExpiry(expiryType);

			accountType.setBrand(creditCard.getBrand());
		}
		else if (account instanceof DirectDebitAccount)
		{
			DirectDebitAccount directDebitAccount = (DirectDebitAccount) account;
			accountType.setBank(directDebitAccount.getBank());
			accountType.setBIC(directDebitAccount.getBank());
			accountType.setNumber(directDebitAccount.getNumber());
			accountType.setBankName(directDebitAccount.getBankName());
			accountType.setCountry(directDebitAccount.getCountry());
		}
		transactionRequest.setAccount(accountType);

		CustomerType customerType = createCustomerType(completeUser);
		transactionRequest.setCustomer(customerType);

		request.getValue().setTransaction(transactionRequest);

		return request;
	}

	private JAXBElement<RequestType> createRegisterRequestOfType(UserPaymentSettings userPaymentSettings,
																 ExternalPaymentRegistration externalPaymentRegistration,
																 TransactionID transactionId, TransactionType transactionType)
	{
		Objects.requireNonNull(userPaymentSettings, "userPaymentSettings == null");
		Objects.requireNonNull(externalPaymentRegistration, "externalPaymentRegistration == null");

		Preconditions.checkArgument(userPaymentSettings.getUser().equals(externalPaymentRegistration.getUser()),
				"!userPaymentSettings.getUser().equals(externalPaymentRegistration.getUser())");

		JAXBElement<RequestType> request = createRequest();

		TransactionRequestType transactionRequest = createTransactionRequest(null);

		IdentificationRequestType identification = jaxbFactory.createIdentificationRequestType();
		identification.setTransactionID(transactionId.toString());
		transactionRequest.setIdentification(identification);

		AccountRequestType accountRequest = createAccountRequest(externalPaymentRegistration);
		transactionRequest.setAccount(accountRequest);

		PaymentRequestType paymentRequest = createPaymentRequestType(userPaymentSettings.getPaymentMethod(), transactionType);

		transactionRequest.setPayment(paymentRequest);

		request.getValue().setTransaction(transactionRequest);

		return request;
	}

	public JAXBElement<RequestType> createReregisterRequest(UserPaymentSettings userPaymentSettings,
															ExternalPaymentRegistration externalPaymentRegistration, TransactionID transactionId)
	{
		return createRegisterRequestOfType(userPaymentSettings, externalPaymentRegistration,
				transactionId, TransactionType.REREGISTRATION);
	}

	public JAXBElement<RequestType> createDeregisterRequest(UserPaymentSettings userPaymentSettings,
															ExternalPaymentRegistration externalPaymentRegistration, TransactionID transactionId)
	{
		return createRegisterRequestOfType(userPaymentSettings, externalPaymentRegistration,
				transactionId, TransactionType.DEREGISTRATION);
	}

	private TransactionRequestType createTransactionRequest(PaymentMethod paymentMethod)
	{
		TransactionRequestType transactionRequest = jaxbFactory.createTransactionRequestType();

		transactionRequest.setUser(createUser());
		transactionRequest.setChannel(transactionChannel);

		transactionRequest.setResponse("SYNC");

		if(paymentMethod!=null) {
			if (paymentMethod.equals(PaymentMethod.CREDIT_CARD)) {
				transactionRequest.setResponse("ASYNC");
			}
		}
		transactionRequest.setMode(transactionMode);

		transactionRequest.setFrontend(createFrontend());

		return transactionRequest;
	}

	private PaymentRequestType createPaymentRequest(MonetaryAmount monetaryAmount, PaymentMethod paymentMethod, TransactionType transactionType)
	{
		PaymentRequestType paymentRequest = createPaymentRequestType(paymentMethod, transactionType);

		PresentationType presentation = getPresentation(monetaryAmount);
		paymentRequest.setPresentation(presentation);

		return paymentRequest;
	}

	private PaymentRequestType createPaymentRequestType(PaymentMethod paymentMethod, TransactionType transactionType)
	{
		PaymentRequestType paymentRequest = jaxbFactory.createPaymentRequestType();
		paymentRequest.setCode(PaymentCode.of(paymentMethod, transactionType));

		return paymentRequest;
	}

	/**
	 * We need a complete user here, because the addresses are used.
	 *
	 * @param completeUser
	 * @return
	 */
	private CustomerType createCustomerType(User completeUser)
	{
		CustomerType customerType = jaxbFactory.createCustomerType();

		NameType nameType = jaxbFactory.createNameType();
		nameType.setFamily(completeUser.getLastName());
		nameType.setGiven(completeUser.getFirstName());
		customerType.setName(nameType);

		Optional<Address> billingAddress = completeUser.getAddresses().stream()
				.filter(Address::isPayment)
				.findFirst();
		if (billingAddress.isPresent())
		{
			Address address = billingAddress.get();
			AddressType addressType = jaxbFactory.createAddressType();
			addressType.setStreet(address.getStreetWithNumber());
			addressType.setZip(address.getZip() != null ? address.getZip().getZip() : null);
			addressType.setCity(address.getCity());
			addressType.setState(address.getRegion() != null ? address.getRegion().getName() : null);
			addressType.setCountry(address.getCountry() != null ? address.getCountry().getCountryCode() : null);
			customerType.setAddress(addressType);
		}
		else
		{
			throw new IllegalArgumentException("No billing address found for the user " + completeUser.getEmail());
		}

		ContactType contactType = jaxbFactory.createContactType();
		contactType.setEmail(completeUser.getEmail());
		contactType.setIp(completeUser.getRegisterIp());
		customerType.setContact(contactType);

		return customerType;
	}

	private PresentationType getPresentation(MonetaryAmount monetaryAmount)
	{
		PresentationType presentation = jaxbFactory.createPresentationType();

		presentation.setAmount(monetaryAmount.getAmount().abs().toString());
		presentation.setCurrency(monetaryAmount.getCurrency().name());
		return presentation;
	}

	/**
	 * Encode the given element as xml string.
	 *
	 * @param jaxbElement the element to encode as xml
	 *
	 * @return the xml string
	 *
	 * @throws PaymentException
	 */
	public String encodeAsXml(JAXBElement<?> jaxbElement)
			throws PaymentException
	{
		try (StringWriter writer = new StringWriter())
		{
			final JAXBContext context = JAXBContext.newInstance(jaxbElement.getDeclaredType());

			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(jaxbElement, writer);

			return writer.toString();
		}
		catch (IOException | JAXBException e)
		{
			throw new PaymentException("Error in encoding xml:", e);
		}
	}

	private JAXBElement<RequestType> createRequest()
	{
		RequestType request = jaxbFactory.createRequestType();

		request.setVersion("1.1");
		request.setHeader(createHeader());

		return new JAXBElement<>(REQUEST_QNAME, RequestType.class, null, request);
	}

	private HeaderType createHeader()
	{
		HeaderType header = jaxbFactory.createHeaderType();

		SecurityType security = jaxbFactory.createSecurityType();
		security.setSender(securitySender);

		header.setSecurity(security);

		return header;
	}

	private QueryType createQuery()
	{
		QueryType query = jaxbFactory.createQueryType();

		query.setMode(transactionMode);
		query.setLevel("CHANNEL");
		query.setEntity(transactionChannel);

		query.setUser(createUser());

		return query;
	}

	private UserType createUser()
	{
		UserType user = jaxbFactory.createUserType();

		user.setLogin(userLogin);
		user.setPwd(userPwd);

		return user;
	}

	private FrontendType createFrontend()
	{
		FrontendType frontend = jaxbFactory.createFrontendType();

		frontend.setResponseUrl(frontendResponseUrl);

		return frontend;
	}

	private PeriodType createPeriod(LocalDate from, LocalDate to)
	{
		PeriodType period = jaxbFactory.createPeriodType();

		period.setFrom(from.format(DateTimeFormatter.ISO_DATE));
		period.setTo(to.format(DateTimeFormatter.ISO_DATE));

		return period;
	}

	private TransactionTypesType createTransactionTypes(TransactionType paymentType)
	{
		TransactionTypesType types = jaxbFactory.createTransactionTypesType();

		TransactionTypeType type = jaxbFactory.createTransactionTypeType();
		type.setCode(paymentType.getCode());

		types.getType().add(type);

		return types;
	}

	private AccountRequestType createAccountRequest(ExternalPaymentRegistration externalPaymentRegistration)
	{
		AccountRequestType accountRequestType = jaxbFactory.createAccountRequestType();
		accountRequestType.setRegistration(externalPaymentRegistration.getRegistrationId());

		return accountRequestType;
	}

	private IdentificationRequestQueryType createIdentificationRequestQuery(String uniqueId)
	{
		final IdentificationRequestQueryType identificationRequestQueryType = jaxbFactory.createIdentificationRequestQueryType();
		identificationRequestQueryType.setUniqueID(uniqueId);

		return identificationRequestQueryType;
	}

	private IdentificationRequestQueryType createIdentificationRequestQuery(TransactionID transactionID)
	{
		IdentificationRequestQueryType identificationRequestQueryType =
				jaxbFactory.createIdentificationRequestQueryType();

		identificationRequestQueryType.setTransactionID(transactionID.toString());

		return identificationRequestQueryType;
	}

	private IdentificationRequestType createIdentificationRequest(TransactionID transactionID)
	{
		IdentificationRequestType identificationRequest = jaxbFactory.createIdentificationRequestType();

		identificationRequest.setTransactionID(transactionID.toString());

		return identificationRequest;
	}

	private IdentificationRequestType createIdentificationRequest(String transactionId, String uniqueId)
	{
		final IdentificationRequestType identificationRequest = jaxbFactory.createIdentificationRequestType();

		identificationRequest.setTransactionID(transactionId);
		identificationRequest.setReferenceID(uniqueId);

		return identificationRequest;
	}
}