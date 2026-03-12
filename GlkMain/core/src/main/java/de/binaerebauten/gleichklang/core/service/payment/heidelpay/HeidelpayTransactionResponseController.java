package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.config.HeidelpayControllerConfig;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ObjectFactory;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ResponseType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.payment.I18N;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.processor.HeidelpayTransactionProcessor;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static de.binaerebauten.gleichklang.core.utils.FunctionalUtils.nullSafe;

/**
 * This rest controller implements the heidelpay push endpoint to receive
 * notfications about payments.
 * <p/>
 * This is currently placed in the core module to make testing easier. This
 * basically means that it's available in both web apps. We later will moved it
 * back to the admin-web.
 */
@RestController
public class HeidelpayTransactionResponseController
{
	private static final Logger LOG = LoggerFactory.getLogger(HeidelpayTransactionResponseController.class);
	
	private final UserRepository userRepository;
	
	private final MessageService messageService;
	
	private final List<HeidelpayTransactionProcessor> heidelpayTransactionProcessors;
	
	private final HeidelpayQueryBuilder queryBuilder;

	private final boolean ignoreHostId;

	private final ObjectFactory jaxbFactory = new ObjectFactory();
	
	@Autowired
	public HeidelpayTransactionResponseController(UserRepository userRepository,
			MessageService messageService,
			List<HeidelpayTransactionProcessor> heidelpayTransactionProcessors,
			HeidelpayQueryBuilder queryBuilder, Environment environment)
	{
		this.userRepository = userRepository;
		this.messageService = messageService;
		this.heidelpayTransactionProcessors = heidelpayTransactionProcessors;
		this.queryBuilder = queryBuilder;
		this.ignoreHostId = environment.getProperty("heidelpay.ignore_host_id", boolean.class, false);
	}
	
	@Transactional
	@RequestMapping(value = "heidelpay/push", method = RequestMethod.POST,
			consumes = HeidelpayControllerConfig.TRANSACTION_MEDIA_TYPE_VALUE)
	public ResponseEntity<Void> handleResponse(@RequestBody ResponseType response)
	{
		final Optional<ProcessingResultType> result = getResult(response);
		if (result.isPresent())
		{
			final TransactionResponseType transactionResponse = response.getTransaction();
			Objects.requireNonNull(transactionResponse, "transactionResponse == null");
			try
			{
				TransactionID transactionID = getTransactionID(transactionResponse);
				if (ignoreHostId || transactionID.isFromLocalHost())
				{
					LOG.info("Retrieved heidelpay response:\n{}", queryBuilder.encodeAsXml(jaxbFactory.createResponse(response)));

					Optional<HeidelpayTransactionProcessor> heidelpayTransactionProcessor =
							heidelpayTransactionProcessors.stream().filter(p -> p.canProcess(transactionResponse)).findFirst();
					
					if (heidelpayTransactionProcessor.isPresent())
					{
						heidelpayTransactionProcessor.get().process(transactionResponse);
					}
					else
					{
						LOG.debug("Received {} transaction without processing it.", getPaymentCode(transactionResponse));
					}

					return ResponseEntity.ok().build();
				}

				LOG.debug("Ignoring transaction with id: {} since it is not from this host", transactionID);

				// response status OK is correct here, this avoids that this host gets the message again
				return ResponseEntity.ok().build();
			}
			catch (NullPointerException | IllegalArgumentException e)
			{
				LOG.error("Error in handling transaction, marking as ok while in dev: ", e);
				notifyResponseHandlingFailed(transactionResponse);
				return ResponseEntity.ok().build();
			}
			catch (Exception e)
			{
				LOG.error("Error in handling transaction: ", e);
				return ResponseEntity.ok().build();
			}
		}
		else
		{
			LOG.error("Retrieved empty heidelpay response");
			return ResponseEntity.ok().build();
		}
	}
	
	private Optional<ProcessingResultType> getResult(ResponseType response)
	{
		return nullSafe(() -> ProcessingResultType.from(response.getTransaction().getProcessing()));
	}
	
	/**
	 * Returns the transaction id of the given transaction response.
	 *
	 * @param transactionResponse the non-null transaction
	 * @return the transaction id of the given response
	 */
	private TransactionID getTransactionID(TransactionResponseType transactionResponse)
	{
		final String transactionID = transactionResponse.getIdentification().getTransactionID();
		Objects.requireNonNull(transactionID, "transactionID == null");
		
		return TransactionID.parse(transactionID);
	}
	
	private PaymentCode getPaymentCode(TransactionResponseType transactionResponse)
	{
		final Optional<PaymentCode> paymentCode =
				nullSafe(() -> PaymentCode.parse(transactionResponse.getPayment().getCode()));
		Preconditions.checkState(paymentCode.isPresent(), "payment code is not present");
		
		return paymentCode.get();
	}
	
	private void notifyResponseHandlingFailed(TransactionResponseType transactionResponse)
	{
		Optional<String> email = nullSafe(() -> transactionResponse.getCustomer().getContact().getEmail());
		if (email.isPresent())
		{
			User user = userRepository.findByEmail(email.get());
			if (Objects.nonNull(user))
			{
				Message message = messageService.createNewMessage(user);
				message.getReceiverEnvelope().setUser(null); // send message to admins
				message.setSubject(I18N.RESPONSE_HANDLING_FAILED_TITLE.msg());
				message.setBody(I18N.RESPONSE_HANDLING_FAILED_MSG.msg(
						transactionResponse.getIdentification().getUniqueID(),
						user.getAlias(), user.getEmail()));
				try
				{
					messageService.sendMessageToAdmin(message, null);
				}
				catch (ValidationException e)
				{
					LOG.error(e.getMessage(), e);
				}
			}
			else
			{
				LOG.error("Unable to notify admin, user with E-Mail {} not found.", email.get());
			}
		}
	}
	
}
