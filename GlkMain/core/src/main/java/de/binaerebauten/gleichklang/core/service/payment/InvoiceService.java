package de.binaerebauten.gleichklang.core.service.payment;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.AdminRole;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.ChargebackRepository;
import de.binaerebauten.gleichklang.core.repository.ExternalPaymentRegistrationRepository;
import de.binaerebauten.gleichklang.core.repository.InvoiceRepository;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.repository.user.AdminRepository;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.MailTemplateInstance;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate.*;

/**
 * Provides services for working with {@link de.binaerebauten.gleichklang.core.model.payment.Invoice} entities.
 */
@Service
public class InvoiceService
{
	private final static Logger LOG = LoggerFactory.getLogger(InvoiceService.class);
	
	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	private InvoiceRepository invoiceRepository;
	
	@Autowired
	private PaymentRepository paymentRepository;
	
	@Autowired
	private PaymentService paymentService;
	
	@Autowired
	private ExternalPaymentRegistrationRepository externalPaymentRegistrationRepository;
	
	@Autowired
	private ExternalPaymentService externalPaymentService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private ChargebackRepository chargebackRepository;
	
	@Autowired
	private MailSendService mailSendService;
	
	@Autowired
	private MailQueueService mailQueueService;
	
	@Autowired
	private UserMailTemplateService userMailTemplateService;
	
	/**
	 * Creates and saves a new invoice for the given user, product and payment method.
	 *
	 * @param user    the user
	 * @param product the product
	 * @param method  the payment method
	 * @return the invoice specified by the parameters
	 */
	@Transactional
	public Invoice createAndSaveInvoice(User user, Product product, PaymentMethod method)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(product, "product == null");
		
		final MonetaryAmount amount = productService.getPrice(user, product);
		return createAndSaveInvoice(user, product, method, amount, amount.isFreeOfCharge());
	}
	
	/**
	 * Creates and saves a new invoice for the given user, product and payment method.
	 *
	 * @param user    the user
	 * @param product the product
	 * @param method  the payment method
	 * @return the invoice specified by the parameters
	 */
	@Transactional
	public Invoice createAndSaveInvoice(User user, Product product, PaymentMethod method, MonetaryAmount amount, boolean isPaid)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(product, "product == null");
		Objects.requireNonNull(method, "method == null");
		
		final Invoice invoice = new Invoice();
		invoice.setUser(user);
		
		final InvoiceItem invoiceItem = new InvoiceItem();
		invoiceItem.setProduct(product);
		invoiceItem.setAmount(amount);
		invoiceItem.setInvoice(invoice);
		invoice.getItems().add(invoiceItem);
		
		invoiceRepository.save(invoice);
		
		final boolean replaceCurrent;
		
		switch (product.getProductType())
		{
			case RENEWAL_OFFER:
			case INITIAL_SUBSCRIPTION_OFFER:
			case UPGRADE_OFFER:
				replaceCurrent = true;
				break;
			default:
				replaceCurrent = false;
		}
		
		final AbstractPayment payment = paymentService.createAndSavePayment(invoice, method, amount, replaceCurrent, isPaid);
		if (isPaid || amount.isFreeOfCharge())
		{
			paymentReceived(payment,null);
		}
		
		return invoice;
	}
	
	/**
	 * Changes the state of the given failedPayment to failed and saves the change.
	 *
	 * @param failedPayment the non-null failed failedPayment
	 */
	@Transactional
	public void paymentFailed(AbstractPayment failedPayment)
	{
		Objects.requireNonNull(failedPayment, "failedPayment == null");
		
		failedPayment.setState(PaymentState.FAILED);
		invoiceRepository.save(failedPayment.getInvoice());
	}
	
	/**
	 * Sets the given payment to the {@link PaymentState#PAID} and creates a new subscription that starts now and
	 * ends at the given end date.
	 *
	 * @param payment the received payment
	 * @throws org.springframework.orm.ObjectOptimisticLockingFailureException
	 */
	public void paymentReceived(AbstractPayment payment,Boolean isSuccess)
	{
		LOG.info("**** Payment State **** "+payment.getState().name());
		if (PaymentState.PENDING.equals(payment.getState()))
		{
			LOG.info("**** Payment State - PENDING**** ");

			if(isSuccess !=null && isSuccess.booleanValue()) {
				payment.setState(PaymentState.PAID);
			}
			else
			{
				payment.setState(PaymentState.PAID);
			}
			if (Objects.isNull(payment.getPaymentToRefund()))
			{
				LOG.info("**** Payment State Refund**** ");

				Invoice invoice = payment.getInvoice();

				Preconditions.checkArgument(invoice.getItems().size() > 0, "invoice.items <= 0");
				InvoiceItem invoiceItem = Iterables.getFirst(invoice.getItems(), null); // null will never be returned

				Product product = invoiceItem.getProduct();
				if(isSuccess!=null && isSuccess.booleanValue()) {
					Optional<Subscription> activeSubscription = productService.activate(product, payment);
					activeSubscription.ifPresent(invoiceItem::setSubscription);
				}
				else
				{
					Optional<Subscription> activeSubscription = productService.activate(product, payment);
					activeSubscription.ifPresent(invoiceItem::setSubscription);
				}
			}
			else
			{
				LOG.info("**** Payment State else **** ");
				payment.getPaymentToRefund().setRefunded(true);
			}

			invoiceRepository.save(payment.getInvoice());

			// Sending E-Mail should not break the whole transaction
			try
			{
				sendConfirmationEmail(payment);
			}
			catch (MailException e)
			{
				LOG.error("Unable to send an E-Mail.", e.getMessage());
			}
		}
		else if (PaymentState.PAID.equals(payment.getState()))
		{
			LOG.info("**** Payment State - PAID **** ");

			// TODO Task 1 changes

			//TODO : changes to to be able to edit comments
			// invoiceRepository.save(payment.getInvoice());

			//payment.getId()

			paymentRepository.updatePaymentComments(payment,payment.getComment());


			LOG.warn("Received payment {} is already paid, skipping.", payment.getId());
		}
		else
		{
			LOG.info("**** Payment State  **** ");
			LOG.error("Received payment {} is in state {}, sending a message to admin.", payment.getId(), payment.getState());

			MailTemplateInstance mailTemplateInstance =
					userMailTemplateService.createMailTemplateInstance(ADMIN_UNKNOWN_EXTERNAL_PAYMENT, payment);
			mailSendService.sendSystemEmail(mailTemplateInstance);
			adminRepository.findByRoles(AdminRole.ADMIN_MANAGEMENT)
					.forEach(superAdmin -> mailSendService.sendEmail(superAdmin, mailTemplateInstance));
		}
	}
	
	/**
	 * Send a confirmation E-Mail after a payment got status paid.
	 *
	 * @param payment
	 * @throws MailException
	 */
	private void sendConfirmationEmail(AbstractPayment payment) throws MailException
	{
		if (payment instanceof Prepayment)
		{
			sendConfirmationEmail((Prepayment) payment);
		}
		else
		{
			sendConfirmationEmail((ExternalPayment) payment);
		}
	}
	
	/**
	 * Send a confirmation E-Mail after a prepayment got status paid.
	 *
	 * @param prepayment
	 * @throws MailException
	 */
	private void sendConfirmationEmail(Prepayment prepayment) throws MailException
	{
		Set<InvoiceItem> invoiceItems = prepayment.getInvoice().getItems();
		
		MailTemplateInstance mailTemplate;
		if (prepayment.getAmount().isRefund())
		{
			mailTemplate = userMailTemplateService.createMailTemplateInstance(PREPAYMENT_REFUND_NOTFICATION, prepayment);
		}
		else if (invoiceItems.stream().anyMatch(i -> i.getProduct() instanceof Chargeback))
		{
			mailTemplate = userMailTemplateService.createMailTemplateInstance(PP_PAID_CB_NOTIF, prepayment);
		}
		else if (invoiceItems.stream().anyMatch(i -> i.getProduct() instanceof RenewalOffer))
		{
			mailTemplate = userMailTemplateService.createMailTemplateInstance(RENEWAL_NOTIF_DDCC, prepayment);
		}
		else if (invoiceItems.stream().anyMatch(i -> i.getProduct() instanceof UpgradeOffer &&
				((UpgradeOffer) i.getProduct()).getUpgradeType() == UpgradeType.DONATION))
		{
			mailTemplate = userMailTemplateService.createMailTemplateInstance(PP_PAID_DONATION_NOTIF, prepayment);
		}
		else if (invoiceItems.stream().anyMatch(i -> i.getProduct() instanceof ServiceOffer))
		{
			mailTemplate = userMailTemplateService.createMailTemplateInstance(PP_PAID_OPTIMIZATION_NOTIF, prepayment);
		}
		else
		{
			mailTemplate = userMailTemplateService.createMailTemplateInstance(PREPAYMENT_PAID_NOTFICATION, prepayment);
		}
		
		mailSendService.sendEmail(prepayment.getUser(), mailTemplate);
	}
	
	/**
	 * Send a confirmation E-Mail after a prepayment got status paid.
	 *
	 * @param externalPayment
	 * @throws MailException
	 */
	private void sendConfirmationEmail(ExternalPayment externalPayment) throws MailException
	{
		if (externalPayment.isRefund())
		{
			mailSendService.sendEmail(externalPayment.getUser(), userMailTemplateService.createMailTemplateInstance(PREPAYMENT_REFUND_NOTFICATION, externalPayment));
			return;
		}
		
		Invoice invoice = externalPayment.getInvoice();
		Optional<InvoiceItem> invoiceItemOptional = invoice.getItems().stream().findFirst();
		if (invoiceItemOptional.isPresent())
		{
			InvoiceItem invoiceItem = invoiceItemOptional.get();
			Product product = invoiceItem.getProduct();
			
			/*
			 * Welcome E-Mails.
			 * Existing payments mean that user already got these E-Mails.
			 */
			if (product instanceof InitialSubscriptionOffer)
			{
				mailSendService.sendEmail(invoice.getUser(),
						userMailTemplateService.createMailTemplateInstance(ADMITTANCE_1, externalPayment));
				mailSendService.sendEmail(invoice.getUser(),
						userMailTemplateService.createMailTemplateInstance(ADMITTANCE_2, externalPayment));
			}
			else if (product instanceof RenewalOffer)
			{
				mailSendService.sendEmail(invoice.getUser(),
						userMailTemplateService.createMailTemplateInstance(RENEWAL_NOTIF_DDCC, externalPayment));
			}
			else if (product instanceof UpgradeOffer)
			{
				UpgradeOffer upgradeOffer = (UpgradeOffer) product;
				if (upgradeOffer.getUpgradeType() == UpgradeType.DONATION)
				{
					mailSendService.sendEmail(invoice.getUser(),
							userMailTemplateService.createMailTemplateInstance(DONATION, externalPayment));
				}
				else if (upgradeOffer.getUpgradeType() == UpgradeType.CATEGORY_EXTENSION)
				{
					mailSendService.sendEmail(invoice.getUser(),
							userMailTemplateService.createMailTemplateInstance(EXTENSION_DDCC, externalPayment));
				}
			}
			else if (product instanceof ServiceOffer)
			{
				mailSendService.sendEmail(invoice.getUser(),
						userMailTemplateService.createMailTemplateInstance(OPTIMIZATION_DDCC, externalPayment));
			}
		}
	}
	
	/**
	 * This method is called when the payment couldn't be charged because the
	 * payment wasn't possible. This method then cancels the external payment and creates
	 * a new prepayment as an alternative payment for the same invoice.
	 *
	 * @param prepayment    the prepayment to cancel and create a new external payment for
	 * @param paymentMethod the new payment method
	 * @return new prepayment connected to the same invoice as the given external payment
	 */
	@Transactional
	public ExternalPayment cancelPrepaymentAndCreateExternalPayment(Prepayment prepayment, PaymentMethod paymentMethod)
	{
		Objects.requireNonNull(prepayment, "prepayment == null");
		
		prepayment.setState(PaymentState.CANCELED);
		paymentRepository.saveAndFlush(prepayment);
		
		ExternalPayment externalPayment = (ExternalPayment)
				paymentService.createAndSavePayment(prepayment.getInvoice(), paymentMethod, prepayment.getAmount(), prepayment.getCurrent() != null);
		
		return externalPayment;
	}
	
	/**
	 * This method is called when the payment couldn't be charged because the
	 * payment wasn't possible. This method then cancels the external payment and creates
	 * a new prepayment as an alternative payment for the same invoice.
	 *
	 * @param externalPayment the external payment to cancel and create a new prepayment for
	 * @param reason          of the chargeback
	 * @return new prepayment connected to the same invoice as the given external payment
	 */
	@Transactional
	public Prepayment cancelExternalPaymentAndCreatePrepayment(ExternalPayment externalPayment, ChargebackReason reason)
	{
		Objects.requireNonNull(externalPayment, "externalPayment == null");
		
		externalPayment.setState(PaymentState.CANCELED);
		externalPayment.getInvoice().getItems().stream().findFirst().ifPresent(i -> i.setChargebackReason(reason));
		
		paymentRepository.saveAndFlush(externalPayment);
		
		Prepayment prepayment = (Prepayment)
				paymentService.createAndSavePayment(externalPayment.getInvoice(), PaymentMethod.PREPAYMENT, externalPayment.getAmount(), externalPayment.getCurrent() != null);
		
		return prepayment;
	}
	
	/**
	 * This method is called when a member has revoked an external payment. In this case
	 * the member has to pay the chargeback costs associated with the chosen payment method
	 * via the {@link Chargeback#forMethod} with the returned prepayment.
	 *
	 * @param externalPayment the external payment to cancel
	 * @return the chargeback prepayment
	 */
	@Transactional
	public Prepayment cancelExternalPaymentAndChargeback(ExternalPayment externalPayment)
	{
		Objects.requireNonNull(externalPayment, "externalPayment == null");
		
		externalPayment.setState(PaymentState.CANCELED);
		
		PaymentMethod method = externalPayment.getMethod();
		Chargeback chargeback = chargebackRepository.findByForMethod(method);
		
		InvoiceItem invoiceItem = new InvoiceItem();
		invoiceItem.setProduct(chargeback);
		invoiceItem.setAmount(chargeback.getAmount());
		invoiceItem.setInvoice(externalPayment.getInvoice());
		externalPayment.getInvoice().getItems().add(invoiceItem);
		
		paymentRepository.saveAndFlush(externalPayment);
		
		Prepayment prepayment = (Prepayment) paymentService.createAndSavePayment(externalPayment.getInvoice(), PaymentMethod.PREPAYMENT, chargeback.getAmount(), false);
		
		return prepayment;
	}
	
	/**
	 * Creates a refund payment for the given parameters and triggers the actual refund payment
	 * for an external payment.
	 *
	 * @param paymentToRefund the non-null payment to refund
	 * @param refundAmount    the negative monetary amount to refund
	 * @return the newly created refund payment
	 * @throws PaymentException
	 */
	@Transactional
	public AbstractPayment refundPayment(AbstractPayment paymentToRefund, MonetaryAmount refundAmount, boolean usePrepayment)
			throws PaymentException
	{
		Objects.requireNonNull(paymentToRefund, "paymentToRefund == null");
		Objects.requireNonNull(refundAmount, "refundAmount");
		Preconditions.checkArgument(refundAmount.getAmount().signum() == -1, "Refund amount must be negative!");
		
		User user = paymentToRefund.getUser();
		ExternalPaymentRegistration registration = externalPaymentRegistrationRepository.findByUser(user);
		PaymentMethod paymentMethod = Objects.nonNull(registration) && !usePrepayment ? paymentToRefund.getMethod() : PaymentMethod.PREPAYMENT;
		AbstractPayment refundPayment = paymentService.createAndSavePayment(paymentToRefund.getInvoice(), paymentMethod, refundAmount, false);
		refundPayment.setPaymentToRefund(paymentToRefund);
		
		if (refundPayment instanceof ExternalPayment)
		{
			ExternalPayment externalPaymentToRefund = (ExternalPayment) paymentToRefund;
			ExternalPayment externalRefundPayment = (ExternalPayment) refundPayment;
			
			externalPaymentService.requestRefund(externalPaymentToRefund, externalRefundPayment);
		}
		invoiceRepository.save(paymentToRefund.getInvoice());
		
		return refundPayment;
	}
	
	public LazyBeanFilteredItemsHandler<Invoice> createInvoiceHandler(User user)
	{
		final Specifications<Invoice> specs = Specifications.where((root, query, cb) -> cb.equal(root.get(Invoice_.user), user));
		return (specification, pageable) -> invoiceRepository.findAll(specs.and(specification), pageable);
	}
}
