package de.binaerebauten.gleichklang.adminweb.service.payment.heidelpay;

import com.google.common.base.Preconditions;
import de.binaerebauten.gleichklang.adminweb.service.payment.BackofficePaymentService;
import de.binaerebauten.gleichklang.adminweb.service.payment.ExternalPaymentAdminService;
import de.binaerebauten.gleichklang.core.model.heidelpay.Registration;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.CustomerType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.NameType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.ProcessingType;
import de.binaerebauten.gleichklang.core.model.heidelpay.query.TransactionResponseType;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRegistrationRepository;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRepository;
import de.binaerebauten.gleichklang.core.repository.UserPaymentSettingsRepository;
import de.binaerebauten.gleichklang.core.repository.heidelpay.RegistrationRepository;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.heidelpay.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static de.binaerebauten.gleichklang.core.utils.FunctionalUtils.nullSafe;

/**
 * Heidelpay specific implementation of the {@link BackofficePaymentService}
 * which provides services for the backoffice user.
 */
@Service
public class HeidelpayBackofficePaymentService implements BackofficePaymentService
{
	private interface HeidelpayFunction<T>
	{
		T get() throws PaymentException;
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(HeidelpayBackofficePaymentService.class);
	
	@Autowired
	private HeidelpayQueryApiConnector heidelpayQueryApiConnector;
	
	@Autowired
	private ExternalPaymentRepository externalPaymentRepository;
	
	@Autowired
	private HeidelpayTransactionService heidelpayTransactionService;
	
	@Autowired
	private ExternalPaymentAdminService externalPaymentAdminService;
	
	/*
	 * @Autowired private UserPaymentSettingsRepository
	 * userPaymentSettingsRepository;
	 */
	
	@Autowired
	private RegistrationRepository registrationRepository;
	
	/*
	 * @Autowired private ExternalPaymentRegistrationRepository
	 * externalPaymentRegistrationRepository;
	 */
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	@Value("${payment.external_payment_max_synchronizations}")
	private int maxSynchronizations;
	
	@Override
	@Deprecated
	public void synchronizePendingExternalPayments()
	{
		Pageable pageable = new PageRequest(0, 25);
		while (pageable != null)
		{
			Page<ExternalPayment> pendingPayments = externalPaymentRepository
					.findByStateAndSynchronizationCountLessThan(PaymentState.PENDING, maxSynchronizations, pageable);
			for (ExternalPayment externalPayment : pendingPayments)
			{
				try
				{
					synchronizePaymentState(externalPayment);
				}
				catch (PaymentException e)
				{
					LOG.error("Error in synchronization of pending payment with id: {}. {}",
							externalPayment.getId(), e.getMessage());
				}
			}
			
			pageable = pendingPayments.nextPageable();
		}
	}
	
	@Deprecated
	private void synchronizePaymentState(ExternalPayment externalPayment) throws PaymentException
	{
		TransactionResponseType transaction =
				heidelpayQueryApiConnector.getTransaction(externalPayment, TransactionID.Type.PAYMENT);
		PaymentState paymentState = toPaymentState(externalPayment, transaction);
		externalPaymentAdminService.synchronizeExternalPayment(externalPayment, paymentState);
	}
	
	@Deprecated
	private PaymentState toPaymentState(ExternalPayment externalPayment,
			TransactionResponseType transaction)
	{
		ProcessingType processingElement = transaction.getProcessing();
		heidelpayTransactionService.log(externalPayment, processingElement);
		return ProcessingResultType.to(processingElement);
	}
	
	@Override
	public void synchronizeRefunds()
	{
		final int maxInterval = 35;
		
		final LocalDate endDate = LocalDate.of(2017, 10, 11);
		LocalDate startDate = LocalDate.now();
		
		final AtomicInteger existingRefunds = new AtomicInteger(0);
		final AtomicInteger paymentNotFound = new AtomicInteger(0);
		final AtomicInteger paymentNotPaid = new AtomicInteger(0);
		final AtomicInteger additionalRefunds = new AtomicInteger(0);
		
		final long duration = ChronoUnit.DAYS.between(startDate, endDate);
		
		while (startDate != null && startDate.isAfter(endDate))
		{
			final long leftDuration = duration - ChronoUnit.DAYS.between(startDate, endDate);
			final float percentage = (float) leftDuration / (float) duration;
			
			LOG.info("synchronized progress {}%", percentage * 100f);
			
			final LocalDate currentStartDate = startDate;
			
			startDate = transactionTemplate.execute(transactionStatus ->
			{
				try
				{
					final List<TransactionResponseType> result = callHeidelpay(() -> heidelpayQueryApiConnector.queryForTransactionTypes(currentStartDate.minus(maxInterval - 1, ChronoUnit.DAYS), currentStartDate, TransactionType.REFUND, ProcessingResultType.ACK));
					
					for (TransactionResponseType entry : result)
					{
						final String externalId = entry.getIdentification().getUniqueID();
						final String referenceId = entry.getIdentification().getReferenceID();
						
						// Check whether we got this transaction notification earlier
						final Optional<ExternalPayment> existingPayment = externalPaymentRepository.findByExternalId(externalId);
						existingPayment.ifPresent(p -> existingRefunds.incrementAndGet());
						
						if (!existingPayment.isPresent())
						{
							final Optional<ExternalPayment> optionalPayment = heidelpayTransactionService.getPayment(referenceId);
							
							if (!optionalPayment.isPresent())
							{
								paymentNotFound.incrementAndGet();
							}
							else
							{
								final ExternalPayment payment = optionalPayment.get();
								
								if (PaymentState.PAID.equals(payment.getState()))
								{
									additionalRefunds.incrementAndGet();
								}
								else
								{
									paymentNotPaid.incrementAndGet();
								}
							}
						}
					}
					return currentStartDate.minus(maxInterval, ChronoUnit.DAYS);
				}
				catch (PaymentException ex)
				{
					LOG.error("can't synchronized", ex);
					return null;
				}
			});
		}
		
		LOG.info("existingRefunds {}", existingRefunds.get());
		LOG.info("paymentNotFound {}", paymentNotFound.get());
		LOG.info("paymentNotPaid {}", paymentNotPaid.get());
		LOG.info("additionalRefunds {}", additionalRefunds.get());
	}
	
	@Override
	public void synchronizeChargebacks()
	{
		final int maxInterval = 35;
		
		final LocalDate endDate = LocalDate.of(2017, 10, 11);
		LocalDate startDate = LocalDate.now();
		
		final AtomicInteger existingChargebacks = new AtomicInteger(0);
		final AtomicInteger paymentNotFound = new AtomicInteger(0);
		final AtomicInteger paymentNotPaid = new AtomicInteger(0);
		final EnumMap<TransactionReturnCode, AtomicInteger> additionalChargebacks = new EnumMap<>(TransactionReturnCode.class);
		Arrays.stream(TransactionReturnCode.values()).forEach(trc -> additionalChargebacks.put(trc, new AtomicInteger(0)));
		
		final long duration = ChronoUnit.DAYS.between(startDate, endDate);
		
		while (startDate != null && startDate.isAfter(endDate))
		{
			final long leftDuration = duration - ChronoUnit.DAYS.between(startDate, endDate);
			final float percentage = (float) leftDuration / (float) duration;
			
			LOG.info("synchronized progress {}%", percentage * 100f);
			
			final LocalDate currentStartDate = startDate;
			
			startDate = transactionTemplate.execute(transactionStatus ->
			{
				try
				{
					final List<TransactionResponseType> result = callHeidelpay(() -> heidelpayQueryApiConnector.queryForTransactionTypes(currentStartDate.minus(maxInterval - 1, ChronoUnit.DAYS), currentStartDate, TransactionType.CHARGEBACK, ProcessingResultType.ACK));
					
					for (TransactionResponseType entry : result)
					{
						final String externalId = entry.getIdentification().getUniqueID();
						final String referenceId = entry.getIdentification().getReferenceID();
						
						// Check whether we got this transaction notification earlier
						final Optional<ExternalPayment> existingPayment = externalPaymentRepository.findByExternalId(externalId);
						existingPayment.ifPresent(p -> existingChargebacks.incrementAndGet());
						
						if (!existingPayment.isPresent())
						{
							final Optional<ExternalPayment> optionalPayment = heidelpayTransactionService.getPayment(referenceId);
							
							if (!optionalPayment.isPresent())
							{
								paymentNotFound.incrementAndGet();
							}
							else
							{
								final ExternalPayment payment = optionalPayment.get();
								
								if (PaymentState.PAID.equals(payment.getState()))
								{
									final Optional<TransactionReturnCode> transactionReturnCodeOptional = nullSafe(() -> TransactionReturnCode.fromCode(entry.getProcessing().getReturn().getCode()));
									Preconditions.checkArgument(transactionReturnCodeOptional.isPresent(), "transactionReturnCode is not present");
									
									final TransactionReturnCode transactionReturnCode = transactionReturnCodeOptional.get();
									
									additionalChargebacks.get(transactionReturnCode).incrementAndGet();
								}
								else
								{
									paymentNotPaid.incrementAndGet();
								}
							}
						}
					}
					return currentStartDate.minus(maxInterval, ChronoUnit.DAYS);
				}
				catch (PaymentException ex)
				{
					LOG.error("can't synchronized", ex);
					return null;
				}
			});
		}
		
		LOG.info("existingChargebacks {}", existingChargebacks.get());
		LOG.info("paymentNotFound {}", paymentNotFound.get());
		LOG.info("paymentNotPaid {}", paymentNotPaid.get());
		
		additionalChargebacks.forEach((transactionReturnCode, value) -> LOG.info("additionChargebacks {} {}", transactionReturnCode, value));
	}
	
	
	@Override
	public void synchronizeRegistrations()
	{
		final int maxInterval = 10; //max is 35
		registrationRepository.deleteAll();
		
		final LocalDate endDate = LocalDate.of(2005, 1, 1);
		LocalDate startDate = LocalDate.now();
		
		final long duration = ChronoUnit.DAYS.between(startDate, endDate);
		
		while (startDate != null && startDate.isAfter(endDate))
		{
			final long leftDuration = duration - ChronoUnit.DAYS.between(startDate, endDate);
			final float percentage = (float) leftDuration / (float) duration;
			
			LOG.info("synchronized progress {}%", percentage * 100f);
			
			final LocalDate currentStartDate = startDate;
			
			startDate = transactionTemplate.execute(transactionStatus ->
			{
				try
				{
					final List<TransactionResponseType> result = callHeidelpay(() -> heidelpayQueryApiConnector.queryForTransactionTypes(currentStartDate.minus(maxInterval - 1, ChronoUnit.DAYS), currentStartDate, TransactionType.REGISTRATION, null));
					
					for (TransactionResponseType entry : result)
					{
						boolean validRegistration = ProcessingResultType.from(entry.getProcessing()) == ProcessingResultType.ACK;
						if (validRegistration)
						{
							final List<TransactionResponseType> linkedTransactions = callHeidelpay(() -> heidelpayQueryApiConnector.getLinkedTransactions(entry.getIdentification().getUniqueID(), TransactionType.DEREGISTRATION, ProcessingResultType.ACK));
							for (TransactionResponseType transaction : linkedTransactions)
							{
								if (TransactionType.DEREGISTRATION.equals(heidelpayTransactionService.getPaymentCode(transaction).transactionType))
								{
									validRegistration = false;
									break;
								}
							}
						}
						
						final CustomerType customer = entry.getCustomer();
						final NameType name = customer == null ? null : customer.getName();
						
						final Registration registration = new Registration();
						registration.setUniqueId(entry.getIdentification().getUniqueID());
						registration.setTransactionId(entry.getIdentification().getTransactionID());
						if (name != null)
						{
							registration.setLastName(name.getFamily());
							registration.setFirstName(name.getGiven());
						}
						if (customer != null)
						{
							registration.setEmail(customer.getContact().getEmail());
						}
						registration.setActive(validRegistration);
						registration.setHeidelpayDate(heidelpayTransactionService.getTransactionDateTime(entry));
						registrationRepository.save(registration);
					}
					return currentStartDate.minus(maxInterval, ChronoUnit.DAYS);
				}
				catch (PaymentException ex)
				{
					LOG.error("can't synchronized", ex);
					return null;
				}
			});
		}
	}
	
	private <T> T callHeidelpay(HeidelpayFunction<T> heidelpayFunction) throws PaymentException
	{
		Exception exception = null;
		
		for (int i = 0; i < 10; i++)
		{
			try
			{
				return heidelpayFunction.get();
			}
			catch (Exception ex)
			{
				LOG.error("heidelpay failed {} - retry", i + 1);
				exception = ex;
			}
		}
		
		throw new PaymentException("To many retries", exception);
	}
}
