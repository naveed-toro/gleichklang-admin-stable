package de.binaerebauten.gleichklang.adminweb.service.payment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.transaction.Transactional;

import de.binaerebauten.gleichklang.core.service.payment.unzer.UnzerTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import de.binaerebauten.gleichklang.adminweb.service.I18N;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.payment.AbstractPayment;
import de.binaerebauten.gleichklang.core.model.payment.ExternalPayment;
import de.binaerebauten.gleichklang.core.model.payment.Invoice;
import de.binaerebauten.gleichklang.core.model.payment.PaymentMethod;
import de.binaerebauten.gleichklang.core.model.payment.PaymentState;
import de.binaerebauten.gleichklang.core.model.payment.RenewalOffer;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.repository.UserPaymentSettingsRepository;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;

/**
 * This service is responsible for managing the renewal of inactive subscriptions.
 */
@Service
public class SubscriptionRenewalService {
	private static final Logger LOG = LoggerFactory.getLogger(SubscriptionRenewalService.class);

	private final PaymentRepository paymentRepository;

	private final UserPaymentSettingsRepository userPaymentSettingsRepository;

	private final InvoiceService invoiceService;

	private final MessageService messageService;

	private final ExternalPaymentService externalPaymentService;


	@Autowired
	private UnzerTransactionService unzerTransactionService;

	@Autowired
	public SubscriptionRenewalService(PaymentRepository paymentRepository,
									  UserPaymentSettingsRepository userPaymentSettingsRepository,
									  InvoiceService invoiceService, MessageService messageService,
									  ExternalPaymentService externalPaymentService) {
		this.paymentRepository = paymentRepository;
		this.userPaymentSettingsRepository = userPaymentSettingsRepository;
		this.invoiceService = invoiceService;
		this.messageService = messageService;
		this.externalPaymentService = externalPaymentService;
	}

	@Transactional
	public void tryToRenew(Subscription subscription) {
		final User user = subscription.getUser();

		try {
			final Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
			if (user.getMemberStatus() == MemberStatus.REGISTERED &&
					paymentSettings.isPresent() &&
					subscription.isAutomaticRenewal()) {
				final UserPaymentSettings userPaymentSettings = paymentSettings.get();

				final RenewalOffer autoRenewalOffer = subscription.getOffer().getAutoRenewalOffer();
				if (Objects.nonNull(autoRenewalOffer)) {
					final Optional<AbstractPayment> currentPayment = paymentRepository.findCurrentPayment(user);
					final Predicate<PaymentState> isPendingPayment = s -> s == PaymentState.PENDING || s == PaymentState.FAILED;

					if (currentPayment.map(AbstractPayment::getState).filter(isPendingPayment).isPresent()) {
						notifySubscriptionRenewalFailed(user, autoRenewalOffer);
						return;
					}

					final PaymentMethod paymentMethod = externalPaymentService.usesExternalPayment(user) ? userPaymentSettings.getPaymentMethod() : PaymentMethod.PREPAYMENT;
					final Invoice invoice = invoiceService.createAndSaveInvoice(user, autoRenewalOffer, paymentMethod);
					final Optional<AbstractPayment> paymentOptional = invoice.getNewestPayment();

					if (paymentOptional.isPresent()) {
						final AbstractPayment payment = paymentOptional.get();
						if (payment instanceof ExternalPayment) {
							final ExternalPayment externalPayment = (ExternalPayment) payment;
							//externalPaymentService.requestPayment(externalPayment);

							///////////////////////////
							try
							{
								Map<String,Object> successMap=new HashMap<>();
								Map<String, String> chargeAuthorizeCardResponse = unzerTransactionService.processProlongationOrRenewPayment(successMap,externalPayment,user);
							} catch (Exception e) {
								LOG.error("Error saving renew payment", e.getMessage());
							}


							/////////////////////////
							// TODO catch exception and than paymentFailed and create prepayment
						}
					} else {
						LOG.error("No payment for new invoice {}, so that the user {} can't renewed", invoice.getId(), user.getId());
					}
				} else {
					LOG.error("Couldn't create invoice for subscription renewal of subscription: {} of user {}", subscription.getId(), subscription.getUser().getId());
				}
			}
		} catch (Exception e) {
			LOG.error("Error in subscription renewal:", e);
		}
	}

	private void notifySubscriptionRenewalFailed(User user, RenewalOffer autoRenewalOffer) {
		final Message message = messageService.createNewMessage(user);
		message.setMessageType(MessageType.PENDING_PAYMENT);
		message.setSubject(I18N.SUBSCRIPTION_RENEWAL_FAILED_TITLE.msg());
		message.setBody(I18N.SUBSCRIPTION_RENEWAL_FAILED_MSG.msg(user.getAlias(), user.getEmail(), autoRenewalOffer.getName()));
		try {
			messageService.sendMessageToAdmin(message, null);
		} catch (ValidationException e) {
			LOG.error(e.getMessage(), e);
		}
	}

//	public boolean isUserPrepayment(User user) {
//			try{
//			final Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
//			if (paymentSettings.isPresent()) {
//				final UserPaymentSettings userPaymentSettings = paymentSettings.get();
//				if (PaymentMethod.PREPAYMENT == userPaymentSettings.getPaymentMethod()) {
//					return true;
//				} else {
//					return false;
//				}
//			} else {
//				return false;
//			}
//		}
//		catch(Exception ex){
//			ex.printStackTrace();
//			return false;
//			}
//	   }
//	}

     public boolean isUserPrepayment(User user){

     try {
	 if (externalPaymentService.usesExternalPayment(user)) {
	 	return false;
	 } else {
		return true;
	 }
     }
     catch (Exception ex){
	 LOG.error("isUserPrepayment", ex);
		 return  false;
    }
    }
}
