package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.*;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRegistrationRepository;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBElement;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This service provides access to the heidelpay XML Query API.
 */
@Service
public class HeidelpayQueryApiConnector
{
	private static final Logger LOG = LoggerFactory.getLogger(HeidelpayQueryApiConnector.class);
	
	private final URI queryUrl;
	
	private final RestTemplate restTemplate;
	
	private final ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;
	
	private final HeidelpayQueryBuilder queryBuilder;
	
	private final HeidelpayTransactionService heidelpayTransactionService;
	
	/**
	 * This class uses constructor injection instead of field injection so that mocking is easier.
	 *
	 * @param queryUrl
	 * @param restTemplate
	 * @param externalPaymentRegistrationRepository
	 * @param queryBuilder
	 * @param heidelpayTransactionService
	 */
	@Autowired
	public HeidelpayQueryApiConnector(@Value("${heidelpay.query.url}") URI queryUrl,
			RestTemplate restTemplate, ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository,
			HeidelpayQueryBuilder queryBuilder, HeidelpayTransactionService heidelpayTransactionService)
	{
		this.queryUrl = queryUrl;
		this.restTemplate = restTemplate;
		this.externalPaymentRegistrationRepository = externalPaymentRegistrationRepository;
		this.queryBuilder = queryBuilder;
		this.heidelpayTransactionService = heidelpayTransactionService;
	}
	
	/**
	 * Query for transactions matching the given parameters.
	 *
	 * @param from the non-null from date
	 * @param to   the non-null to date
	 * @param type the non null payment type
	 * @return the transaction matching the given parameters
	 * @throws PaymentException
	 */
	public List<TransactionResponseType> queryForTransactionTypes(LocalDate from, LocalDate to, TransactionType type, ProcessingResultType resultType)
			throws PaymentException
	{
		JAXBElement<RequestType> requestQuery = queryBuilder.createQueryRequest(from, to, type, resultType);
		
		ResponseEntity<ResponseType> responseEntity = performQuery(requestQuery);
		
		ResponseType response = processResponse(responseEntity);
		
		return response.getResult().getTransaction();
	}
	
	/**
	 * Sends a payment request with the given parameters to heidelpay.
	 *
	 * @param externalPayment             the non-null external payment
	 * @param externalPaymentRegistration the non-null external payment registration
	 * @throws PaymentException
	 */
	@Transactional
	public void requestPayment(ExternalPayment externalPayment, ExternalPaymentRegistration externalPaymentRegistration)
			throws PaymentException
	{
		JAXBElement<RequestType> queryRequest = queryBuilder.createQueryRequest(externalPayment,
				TransactionID.Type.PAYMENT, ProcessingResultType.ACK);
		ResponseEntity<ResponseType> queryResponseEntity = performQuery(queryRequest);
		ResponseType queryResonse = processResponse(queryResponseEntity);
		
		boolean paymentRequestExists = transactionExists(queryResonse);
		if (!paymentRequestExists)
		{
			JAXBElement<RequestType> paymentRequest = queryBuilder.createPaymentRequest(externalPayment, externalPaymentRegistration);
			LOG.info("Inside RequestPayment======="+queryBuilder.encodeAsXml(paymentRequest));
			
			ResponseEntity<ResponseType> responseEntity = performQuery(paymentRequest);
			
			processResponse(responseEntity);
			heidelpayTransactionService.log(externalPayment, responseEntity.getBody().getTransaction().getProcessing());
		}
		else
		{
			LOG.info("Did not resend already send payment request for payment {}", externalPayment.getId());
		}
	}
	
	/**
	 * Sends a refund request with the given parameters.
	 *
	 * @param paymentToRefund
	 * @param refundPayment
	 * @param externalPaymentRegistration
	 * @throws PaymentException
	 */
	public void requestRefund(ExternalPayment paymentToRefund, ExternalPayment refundPayment, ExternalPaymentRegistration externalPaymentRegistration)
			throws PaymentException
	{
		JAXBElement<RequestType> queryRequest = queryBuilder.createQueryRequest(refundPayment, TransactionID.Type.PAYMENT, ProcessingResultType.ACK);
		ResponseEntity<ResponseType> queryResponseEntity = performQuery(queryRequest);
		ResponseType queryResponse = processResponse(queryResponseEntity);
		
		final boolean paymentRequestExists = transactionExists(queryResponse);
		final String referenceId = paymentToRefund.getExternalId();
		if (!paymentRequestExists && !Strings.isNullOrEmpty(referenceId))
		{
			queryRequest = queryBuilder.createQueryRequest(referenceId);
			queryResponseEntity = performQuery(queryRequest);
			queryResponse = processResponse(queryResponseEntity);
			if (!transactionExists(queryResponse))
			{
				throw new PaymentException("Payment to refund doesn't exist!", queryResponse.getResult().getResponse());
			}
			
			JAXBElement<RequestType> paymentRequest = queryBuilder.createRefundRequest(referenceId, refundPayment, externalPaymentRegistration);
			LOG.debug(queryBuilder.encodeAsXml(paymentRequest));
			
			ResponseEntity<ResponseType> responseEntity = performQuery(paymentRequest);
			
			processResponse(responseEntity);
			heidelpayTransactionService.log(refundPayment, responseEntity.getBody().getTransaction().getProcessing());
		}
		else
		{
			LOG.info("Did not resend already sent payment request for payment {}", refundPayment.getId());
		}
	}
	
	/**
	 * Retrieves the transaction response for the given payment from heidelpay.
	 *
	 * @param payment the payment
	 * @param type    the transaction type
	 * @return the non null response type element for the given payment
	 * @throws PaymentException
	 */
	public TransactionResponseType getTransaction(ExternalPayment payment, TransactionID.Type type) throws PaymentException
	{
		final TransactionID transactionID = TransactionID.toTransactionID(payment, type);
		
		final JAXBElement<RequestType> requestQuery = queryBuilder.createQueryRequest(transactionID, null);
		
		final ResponseEntity<ResponseType> responseEntity = performQuery(requestQuery);
		
		return processTransactionResponse(responseEntity);
	}
	
	/**
	 * Returns the transaction for the given external payment registration.
	 *
	 * @param externalPaymentRegistration the non-null external payment registration
	 * @return the transaction associated with the given external payment registration
	 * @throws PaymentException
	 */
	public TransactionResponseType getRegistrationTransaction(ExternalPaymentRegistration externalPaymentRegistration)
			throws PaymentException
	{
		JAXBElement<RequestType> requestQuery = queryBuilder.createQueryRequest(externalPaymentRegistration.getRegistrationId());
		LOG.debug(queryBuilder.encodeAsXml(requestQuery));
		
		ResponseEntity<ResponseType> responseEntity = performQuery(requestQuery);
		
		TransactionResponseType response = processTransactionResponse(responseEntity);
		
		return response;
	}
	
	/**
	 * Returns the linked transactions for the given uniqueId
	 *
	 * @param uniqueId the non-null uniqueId
	 * @return the transaction associated with the given uniqueId
	 * @throws PaymentException
	 */
	public List<TransactionResponseType> getLinkedTransactions(String uniqueId, TransactionType type, ProcessingResultType resultType)
			throws PaymentException
	{
		JAXBElement<RequestType> requestQuery = queryBuilder.createLinkedTransactionsQuery(uniqueId, type, resultType);
		
		ResponseEntity<ResponseType> responseEntity = performQuery(requestQuery);
		
		ResponseType response = processResponse(responseEntity);
		
		return response.getResult().getTransaction();
	}
	
	/**
	 * Registers user with given data.
	 *
	 * @param userPaymentSettings
	 * @throws PaymentException
	 */
	public ExternalPaymentRegistration register(User completeUser,
			UserPaymentSettings userPaymentSettings,
			ExternalPayment externalPayment, Account account)
			throws PaymentException
	{
		TransactionID transactionId = TransactionID.toTransactionID(externalPayment, Type.REGISTRATION);
		JAXBElement<RequestType> requestQuery = queryBuilder.createRegisterRequest(completeUser, userPaymentSettings, transactionId, account);
		LOG.info("Inside register====="+queryBuilder.encodeAsXml(requestQuery));
		
		ResponseEntity<ResponseType> responseEntity = performQuery(requestQuery);
		TransactionResponseType response = processResponse(responseEntity).getTransaction();
		
		ProcessingType processing = response.getProcessing();
		if (Objects.nonNull(processing) && Objects.equals("NOK", processing.getResult()))
		{
			throw new PaymentException("Registration error: " +
					response.getProcessing().getReason().getValue(),
					response.getProcessing().getCode());
		}
		
		// Heidelpay sends the registration id as a unique id in its response
		String registrationId = response.getIdentification() != null ? response.getIdentification().getUniqueID() : null;
		ExternalPaymentRegistration registration = externalPaymentRegistrationRepository.findByUser(completeUser);
		if (Objects.nonNull(registration))
		{
			externalPaymentRegistrationRepository.delete(registration);
		}
		registration = new ExternalPaymentRegistration();
		registration.setUser(completeUser);
		registration.setRegistrationId(registrationId);
		registration.setExternalReferenceId(transactionId.getId());
		registration.setLastUsedDate(LocalDateTime.now());
		externalPaymentRegistrationRepository.save(registration);
		
		return registration;
	}
	
	/**
	 * Updates data of the given external payment registration.
	 *
	 * @param userPaymentSettings
	 * @param externalPaymentRegistration
	 * @throws PaymentException
	 */
	public void reregister(UserPaymentSettings userPaymentSettings, ExternalPaymentRegistration externalPaymentRegistration)
			throws PaymentException
	{
		TransactionID transactionId = TransactionID.toTransactionID(externalPaymentRegistration);
		JAXBElement<RequestType> requestQuery = queryBuilder.createReregisterRequest(userPaymentSettings, externalPaymentRegistration, transactionId);
		LOG.info("Inside reregister====="+queryBuilder.encodeAsXml(requestQuery));
		ResponseEntity<ResponseType> responseEntity = performQuery(requestQuery);
		
		processResponse(responseEntity);
	}
	
	/**
	 * Deregisters the given external payment registration.
	 *
	 * @param userPaymentSettings         the non-null user payment settings of an user
	 * @param externalPaymentRegistration the non-null external payment registration of an user
	 * @throws PaymentException
	 */
	public void deregister(UserPaymentSettings userPaymentSettings, ExternalPaymentRegistration externalPaymentRegistration)
			throws PaymentException
	{
		TransactionID transactionId = TransactionID.toTransactionID(externalPaymentRegistration);
		JAXBElement<RequestType> requestQuery = queryBuilder.createDeregisterRequest(userPaymentSettings, externalPaymentRegistration, transactionId);
		
		ResponseEntity<ResponseType> responseEntity = performQuery(requestQuery);
		
		processResponse(responseEntity);
	}
	
	/**
	 * Checks whether any successful transaction exists.
	 *
	 * @param queryResponse
	 * @return
	 */
	@VisibleForTesting
	boolean transactionExists(ResponseType queryResponse)
	{
		Optional<TransactionResponseType> transaction = queryResponse.getResult().getTransaction().stream()
				.filter(t -> ProcessingResultType.from(t.getProcessing()) == ProcessingResultType.ACK)
				.findAny();
		return transaction.isPresent();
	}
	
	/**
	 * Checks successful transactions for active schedulers and returns all of them.
	 * Any successful scheduler transaction can be descheduled due to next rescheduling or descheduling.
	 * Both reschedule or deschedule transactions contain a reference to the created scheduler.
	 *
	 * @param queryResponse
	 * @return an active scheduler transaction
	 */
	@VisibleForTesting
	List<TransactionResponseType> getActiveSchedulerTransaction(ResponseType queryResponse)
	{
		List<TransactionResponseType> successfulTransactions = queryResponse.getResult().getTransaction().stream()
				.filter(t -> ProcessingResultType.from(t.getProcessing()) == ProcessingResultType.ACK)
				.collect(Collectors.toList());
		
		String deschedulingTypes = getTrasactionTypePattern(TransactionType.RESCHEDULE, TransactionType.DESCHEDULE);
		List<String> inactiveSchedulers = successfulTransactions.stream()
				.filter(t -> t.getProcessing().getCode().matches(deschedulingTypes))
				.map(t -> t.getIdentification().getReferenceID())
				.collect(Collectors.toList());
		
		String transactionTypes = getTrasactionTypePattern(TransactionType.SCHEDULE, TransactionType.RESCHEDULE);
		return successfulTransactions.stream()
				.filter(t -> t.getProcessing().getCode().matches(transactionTypes) &&
						!inactiveSchedulers.contains(t.getIdentification().getUniqueID()))
				.collect(Collectors.toList());
	}
	
	@VisibleForTesting
	String getTrasactionTypePattern(TransactionType... types)
	{
		String transactionTypes = Stream.of(types)
				.map(TransactionType::getCode)
				.collect(Collectors.joining("|"));
		return String.format("^.*\\.(%s).*$", transactionTypes);
	}
	
	private ResponseEntity<ResponseType> performQuery(JAXBElement<RequestType> queryRequest)
			throws PaymentException
	{
		String queryAsXml = queryBuilder.encodeAsXml(queryRequest);
		MultiValueMap<String, String> postParams = new LinkedMultiValueMap<>();
		postParams.add("load", queryAsXml);
		
		return restTemplate.postForEntity(queryUrl, postParams, ResponseType.class);
	}
	
	private TransactionResponseType processTransactionResponse(ResponseEntity<ResponseType> responseEntity)
			throws PaymentException
	{
		ResponseType response = processResponse(responseEntity);
		List<TransactionResponseType> transactions = response.getResult().getTransaction();
		return transactions.stream()
				.findFirst()
				.orElseThrow(() -> new PaymentException("Unknown transaction.", HttpStatus.OK.value()));
	}
	
	private ResponseType processResponse(ResponseEntity<ResponseType> responseEntity)
			throws PaymentException
	{
		HttpStatus statusCode = responseEntity.getStatusCode();
		if (statusCode.equals(HttpStatus.OK))
		{
			ResponseType response = responseEntity.getBody();
			if (response.getError() != null)
			{
				ReturnType return_ = response.getError().getReturn();
				throw new PaymentException(return_.getValue(), return_.getCode());
			}
			return response;
		}
		else
		{
			throw new PaymentException(statusCode.getReasonPhrase(), statusCode.value());
		}
	}
	
}
