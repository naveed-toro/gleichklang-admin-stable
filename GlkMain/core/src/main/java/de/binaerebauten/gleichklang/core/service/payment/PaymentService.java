package de.binaerebauten.gleichklang.core.service.payment;

import com.google.common.base.Preconditions;
import com.vaadin.ui.Notification;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.repository.UserPaymentSettingsRepository;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Provides common operations for payments.
 */
@Service
public class PaymentService
{
	private final BankAccountService bankAccountService;
	
	private final PaymentRepository paymentRepository;
	private final ExternalPaymentService externalPaymentService;
	
	private final MailSendService mailSendService;
	private final UserMailTemplateService userMailTemplateService;
	
	private final UserPaymentSettingsRepository userPaymentSettingsRepository;
	
	private final TransactionTemplate transactionTemplate;
	private final SubscriptionService subscriptionService;
	private final ProductRepository productRepository;
	
	/**
	 * This service uses constructor dependency injection to ease testing.
	 *
	 * @param bankAccountService
	 * @param paymentRepository
	 * @param externalPaymentService
	 * @param mailSendService
	 * @param userMailTemplateService
	 * @param userPaymentSettingsRepository
	 * @param transactionTemplate
	 */
	@Autowired
	public PaymentService(BankAccountService bankAccountService, PaymentRepository paymentRepository,
			ExternalPaymentService externalPaymentService, MailSendService mailSendService, UserMailTemplateService userMailTemplateService,
			UserPaymentSettingsRepository userPaymentSettingsRepository,
			TransactionTemplate transactionTemplate, SubscriptionService subscriptionService,ProductRepository productRepository)
	{
		this.bankAccountService = bankAccountService;
		this.paymentRepository = paymentRepository;
		this.externalPaymentService = externalPaymentService;
		this.mailSendService = mailSendService;
		this.userMailTemplateService = userMailTemplateService;
		this.userPaymentSettingsRepository = userPaymentSettingsRepository;
		this.transactionTemplate = transactionTemplate;
		this.subscriptionService = subscriptionService;
		this.productRepository = productRepository;
	}
	
	/**
	 * Creates a new new payment for the given invoice, method and amount.
	 *
	 * @param invoice the invoice
	 * @param method  the payment method
	 * @param amount  the amount
	 * @return a new and already saved payment
	 * @throws IllegalStateException when a current unpaid payment exists
	 */
	public AbstractPayment createAndSavePayment(Invoice invoice, PaymentMethod method, MonetaryAmount amount, boolean replaceCurrent)
	{
		Objects.requireNonNull(amount, "amount == null");
		
		return createAndSavePayment(invoice, method, amount, replaceCurrent, amount.isFreeOfCharge());
	}
	
	/**
	 * Creates a new new payment for the given invoice, method and amount.
	 *
	 * @param invoice the invoice
	 * @param method  the payment method
	 * @param amount  the amount
	 * @return a new and already saved payment
	 * @throws IllegalStateException when a current unpaid payment exists
	 */
	public AbstractPayment createAndSavePayment(Invoice invoice, PaymentMethod method, MonetaryAmount amount, boolean replaceCurrent, boolean isPaid)
	{
		Objects.requireNonNull(method, "method == null");
		Objects.requireNonNull(invoice, "invoice == null");
		Objects.requireNonNull(amount, "amount == null");
		
		// the transaction is split into two transaction-steps
		
		// 1. step: save payment
		AbstractPayment payment = transactionTemplate.execute(t -> createPayment(invoice, method, amount, replaceCurrent));
		
		// 2. step: set external reference id in another transaction, this is done in a separate transaction
		//          so that we can use the id
		AbstractPayment paymentWithExternalReferenceId = transactionTemplate.execute(t -> setExternalReferenceId(payment));
		
		// 3. step: send notification E-Mails
		if (payment instanceof Prepayment && !isPaid && !amount.isFreeOfCharge())
		{
			sendNotificationEmail((Prepayment) payment);
		}
		
		return paymentWithExternalReferenceId;
	}
	
	/**
	 * Creates or updates the user payment settings for the given user with the
	 * given payment method.
	 *
	 * @param user          the non-null user
	 * @param paymentMethod the non-null payment method
	 * @return the user payment settings of the given user updated with the
	 * given payment method
	 */
	@Transactional
	public UserPaymentSettings createOrUpdateUserPaymentSettings(User user, PaymentMethod paymentMethod)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(paymentMethod, "paymentMethod == null");
		
		Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
		UserPaymentSettings userPaymentSettings;
		if (paymentSettings.isPresent())
		{
			userPaymentSettings = paymentSettings.get();
		}
		else
		{
			userPaymentSettings = new UserPaymentSettings();
			userPaymentSettings.setUser(user);
		}
		
		userPaymentSettings.setPaymentMethod(paymentMethod);
		userPaymentSettingsRepository.save(userPaymentSettings);
		
		return userPaymentSettings;
	}
	
	/**
	 * Creates a default user payment settings if none exist for the given user.
	 * Or returns the existing user payment settings for the given user.
	 *
	 * @param user the non-null user
	 * @return the user payment settings
	 */
	@CheckedTransactional
	public UserPaymentSettings createOrGetUserPaymentSettings(User user)
	{
		Objects.requireNonNull(user, "user == null");
		
		Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
		UserPaymentSettings userPaymentSettings;
		if (paymentSettings.isPresent())
		{
			userPaymentSettings = paymentSettings.get();
		}
		else
		{
			userPaymentSettings = new UserPaymentSettings();
			userPaymentSettings.setUser(user);
		}
		
		userPaymentSettingsRepository.save(userPaymentSettings);
		
		return userPaymentSettings;
	}
	
	/**
	 * Update an existing user payment settings {@link UserPaymentSettings} for
	 * the given user with the given payment method. Deregister the user in the
	 * external payment system when changing from {@link
	 * PaymentMethod#PREPAYMENT} to another payment method.
	 *
	 * @param user                 the non-null user
	 * @param paymentMethod        the non-null user
	 * @return the updated user payment settings
	 * @throws PaymentException
	 */
	@CheckedTransactional
	public UserPaymentSettings updateUserPaymentSettings(User user, PaymentMethod paymentMethod) throws PaymentException
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(paymentMethod, "paymentMethod == null");
		
		Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
		UserPaymentSettings userPaymentSettings;
		if (paymentSettings.isPresent())
		{
			userPaymentSettings = paymentSettings.get();
		}
		else
		{
			userPaymentSettings = new UserPaymentSettings();
			userPaymentSettings.setUser(user);
		}
		
		// User changes his payment method from external to prepayment
		if (userPaymentSettings.usesExternalPayment() && PaymentMethod.PREPAYMENT.equals(paymentMethod))
		{
			// and deregister the user
			externalPaymentService.deregister(user);
		}
		
		userPaymentSettings.setPaymentMethod(paymentMethod);
		userPaymentSettingsRepository.save(userPaymentSettings);
		
		return userPaymentSettings;
	}
	
	private AbstractPayment setExternalReferenceId(AbstractPayment payment)
	{
		String externalReferenceId = payment.createExternalReferenceId();
		payment.setExternalReferenceId(externalReferenceId);
		
		paymentRepository.save(payment);
		
		return payment;
	}
	
	
	private AbstractPayment createPayment(Invoice invoice, PaymentMethod method, MonetaryAmount amount, boolean replaceCurrent)
	{
		final User user = invoice.getUser();
		final AbstractPayment newPayment;
		if (method.isExternal())
		{
			newPayment = new ExternalPayment(method);
		}
		else
		{
			final BankAccount bankAccount = bankAccountService.findPrepaymentBankAccount(user);
			newPayment = new Prepayment(bankAccount);
		}
		
		newPayment.setCreateDate(LocalDateTime.now());
		newPayment.setUser(user);
		newPayment.setAmount(amount);
		newPayment.setExternalReferenceId("1"); // temporarily set to 1, will be fixed in further transaction in the createAndSavePayment
		newPayment.setInvoice(invoice);
		
		if (replaceCurrent)
		{
			newPayment.setCurrent(true);
			paymentRepository.findCurrentPayment(user).ifPresent(p ->
			{
				Preconditions.checkState(p.getState() != PaymentState.PENDING, "current payment is pending");
				
				p.setCurrent(null);
				
				// saving and flushing the payment immediately is required because mysql immediately
				// evaluates the unique constraints
				paymentRepository.saveAndFlush(p);
			});
		}
		
		invoice.getPayments().add(newPayment);
		
		return paymentRepository.save(newPayment);
	}
	
	private void sendNotificationEmail(Prepayment payment)
	{
		Invoice invoice = payment.getInvoice();
		Optional<InvoiceItem> invoiceItemOptional = invoice.getItems().stream().findFirst();
		if (invoiceItemOptional.isPresent())
		{
			InvoiceItem invoiceItem = invoiceItemOptional.get();
			Product product = invoiceItem.getProduct();
			
			if (!payment.isRefund())
			{
				/*
				 * Welcome E-Mails.
				 * Existing payments mean that user already got these E-Mails.
				 */
				if (product instanceof InitialSubscriptionOffer)
				{
					Subscription subscription = invoiceItem.getSubscription();
					List<AbstractPayment> payments = paymentRepository.findBySubscription(subscription);
					if (payments.isEmpty())
					{
						mailSendService.sendEmail(invoice.getUser(),
								userMailTemplateService.createMailTemplateInstance(UserMailTemplate.ADMITTANCE_2, payment));
					}
				}
				else if (product instanceof UpgradeOffer)
				{
					UpgradeOffer upgradeOffer = (UpgradeOffer) product;
					if (upgradeOffer.getUpgradeType() == UpgradeType.DONATION)
					{
						mailSendService.sendEmail(payment.getUser(),
								userMailTemplateService.createMailTemplateInstance(UserMailTemplate.DONATION, payment));
					}
					else if (upgradeOffer.getUpgradeType() == UpgradeType.CATEGORY_EXTENSION)
					{
						mailSendService.sendEmail(invoice.getUser(),
								userMailTemplateService.createMailTemplateInstance(UserMailTemplate.EXTENSION_PP, payment));
					}
				}
			}
			else // refund
			{
				mailSendService.sendEmail(payment.getUser(),
						userMailTemplateService.createMailTemplateInstance(UserMailTemplate.PREPAYMENT_REFUND_REQUEST, payment));
			}
		}
	}
	
	public Optional<AbstractPayment> findCurrentPayment(User user)
	{

		return paymentRepository.findCurrentPayment(user);
	}
	
	public boolean existsPendingPayments(User user)
	{
		return paymentRepository.existsPendingPayments(user);
	}
	public List<AbstractPayment> findAllPayments(User user)
	{
		return paymentRepository.findByUser(user);
	}



    public boolean isEligibleForMoneyBack(User user, Date date){
        Optional<AbstractPayment> abstractPayment  = paymentRepository.findCurrentPayment(user);
        Subscription subscription =  subscriptionService.findCurrentSubscription(user).orElse(null);
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        if(abstractPayment.isPresent()){
            AbstractPayment abstractPayment1 = abstractPayment.get();
            boolean bool = (abstractPayment.get().getCreateDate().plusDays(14).isAfter(ldt) || abstractPayment.get().getChangeDate().plusDays(14).isAfter(ldt)) && !subscription.isAutomaticRenewal();
            return bool;
        }
        return false;
    }

	@Transactional
	public void updateRevocation(BigDecimal bigDecimal, AbstractPayment abstractPayment,Date date){
		paymentRepository.updateRevocation(bigDecimal, date, abstractPayment);

	}

	public boolean currentPayment(User user){
		Optional<AbstractPayment> abstractPayment = paymentRepository.findCurrentPayment(user);
		if(abstractPayment.isPresent()){
			if(abstractPayment.get().getRevocationAmount()!=null)
				return true;
		}
		return false;
	}

	public Optional<AbstractPayment> currentAbsctractPayment(User user) {
		return paymentRepository.findCurrentPayment(user);
	}
}
