package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ProcessingType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ReturnType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRegistrationRepository;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.repository.HeidelpayTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

import static de.binaerebauten.gleichklang.core.utils.FunctionalUtils.nullSafe;

/**
 * Provides operation for handling the mapping of heidelpay transaction ids to
 * the internal payment model.
 */
@Service
public class HeidelpayTransactionService
{
	private final HeidelpayTransactionRepository heidelpayTransactionRepository;
	
	private final ExternalPaymentRepository externalPaymentRepository;
	
	private final ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;
	
	/**
	 * This service uses constructor based dependency injection to ease testing.
	 *
	 * @param heidelpayTransactionRepository
	 * @param externalPaymentRepository
	 * @param externalPaymentRegistrationRepository
	 */
	@Autowired
	public HeidelpayTransactionService(HeidelpayTransactionRepository heidelpayTransactionRepository,
			ExternalPaymentRepository externalPaymentRepository,
			ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository)
	{
		this.heidelpayTransactionRepository = heidelpayTransactionRepository;
		this.externalPaymentRepository = externalPaymentRepository;
		this.externalPaymentRegistrationRepository = externalPaymentRegistrationRepository;
	}
	
	@Transactional
	public HeidelpayTransaction log(ExternalPayment externalPayment, ProcessingType processingElement)
	{
		Objects.requireNonNull(processingElement, "processingElement == null");
		Objects.requireNonNull(processingElement.getReturn(), "processingElement.return == null");
		
		ReturnType returnType = processingElement.getReturn();
		ProcessingResultType result = ProcessingResultType.from(processingElement);
		
		return log(externalPayment, result, returnType.getCode(), returnType.getValue());
	}
	
	@Transactional
	public HeidelpayTransaction log(ExternalPayment externalPayment,
			ProcessingResultType result, String returnCode, String returnMessage)
	{
		Objects.requireNonNull(externalPayment, "externalPayment == null");
		Objects.requireNonNull(result, "result == null");
		Objects.requireNonNull(returnCode, "returnCode == null");
		Objects.requireNonNull(returnMessage, "returnMessage == null");
		
		HeidelpayTransaction heidelpayTransaction = new HeidelpayTransaction();
		
		heidelpayTransaction.setPayment(externalPayment);
		
		heidelpayTransaction.setResult(result);
		heidelpayTransaction.setReturnCode(returnCode);
		heidelpayTransaction.setReturnMessage(returnMessage);
		
		heidelpayTransactionRepository.save(heidelpayTransaction);
		
		return heidelpayTransaction;
	}
	
	/**
	 * Returns the payment code of the given transaction response.
	 *
	 * @param transactionResponse the non-null transaction response
	 * @return the payment code of the given response
	 */
	public PaymentCode getPaymentCode(TransactionResponseType transactionResponse)
	{
		final Optional<PaymentCode> paymentCode =
				nullSafe(() -> PaymentCode.parse(transactionResponse.getPayment().getCode()));
		Preconditions.checkState(paymentCode.isPresent(), "payment code is not present");
		
		return paymentCode.get();
	}
	
	/**
	 * Returns the payment method of the given transaction response.
	 *
	 * @param transactionResponse the non-null transaction response
	 * @return the payment method of the given response
	 */
	public PaymentMethod getPaymentMethod(TransactionResponseType transactionResponse)
	{
		PaymentCode paymentCode = getPaymentCode(transactionResponse);
		
		return paymentCode.paymentMethod;
	}
	
	/**
	 * Returns the transaction id of the given transaction response.
	 *
	 * @param transactionResponse the non-null transaction
	 * @return the transaction id of the given response
	 */
	public TransactionID getTransactionID(TransactionResponseType transactionResponse)
	{
		return TransactionID.parse(transactionResponse.getIdentification().getTransactionID());
	}
	
	public Optional<ExternalPayment> getPayment(String externalId)
	{
		Objects.requireNonNull(externalId, "externalId == null");
		
		return externalPaymentRepository.findByExternalId(externalId);
	}
	
	/**
	 * Returns the payment associated with the given transaction id.
	 *
	 * @param transactionId non-null transaction id with type
	 *                      {@link de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID.Type#PAYMENT}
	 * @return the payment
	 */
	@Transactional
	public ExternalPayment getPayment(TransactionID transactionId)
	{
		Objects.requireNonNull(transactionId, "transactionId == null");
		
		ExternalPayment payment = externalPaymentRepository.findByExternalReferenceId(transactionId.getId());
		Objects.requireNonNull(payment, "payment doesn't exist: " + transactionId.getId());
		
		return payment;
	}
	
	/**
	 * Returns the external payment registration associated with the given transaction id.
	 *
	 * @param transactionId non-null transaction id with type
	 *                      {@link de.binaerebauten.gleichklang.core.service.payment.heidelpay.TransactionID.Type#CHANGE_REGISTRATION}
	 * @return the external payment registration
	 */
	@Transactional
	public ExternalPaymentRegistration getExternalPaymentRegistration(TransactionID transactionId)
	{
		Objects.requireNonNull(transactionId, "transactionId == null");
		Preconditions.checkArgument(transactionId.getType() == TransactionID.Type.CHANGE_REGISTRATION, "Invalid transaction id type:" + transactionId.getType());
		
		ExternalPaymentRegistration externalPaymentRegistration = externalPaymentRegistrationRepository.findByExternalReferenceId(transactionId.getId());
		Objects.requireNonNull(externalPaymentRegistration, "externalPaymentRegistration == null");
		
		return externalPaymentRegistration;
	}
	
	public LocalDateTime getTransactionDateTime(TransactionResponseType transactionResponseType)
	{
		if (transactionResponseType == null || transactionResponseType.getProcessing() == null || transactionResponseType.getProcessing().getTimestamp() == null)
			return null;
		
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		try
		{
			return LocalDateTime.parse(transactionResponseType.getProcessing().getTimestamp(), formatter);
		}
		catch (DateTimeParseException ex)
		{
			return null;
		}
	}
}
