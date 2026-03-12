package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.user.CompleteUserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.PaymentService;
import de.binaerebauten.gleichklang.memberweb.service.payment.ExternalPaymentFormService;
import de.binaerebauten.gleichklang.memberweb.view.SubscriptionDetailsView;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Presenter for the newest subscription of an user {@link Subscription}.
 */
public class SubscriptionDetailsPresenter extends SubscriptionTabPresenter
		implements SubscriptionDetailsView.SubscriptionViewListener
{
	public static final String SUCCESS_PARAM = "?success";
	public static final String ERROR_PARAM = "?error=";
	
	private static final Logger LOG = LoggerFactory.getLogger(SubscriptionDetailsPresenter.class);
	
	private final SubscriptionDetailsView subscriptionDetailsView;
	
	private final AuthenticationService authenticationService;
	
	private final CompleteUserRepository userRepository;
	
	private final InvoiceService invoiceService;
	private final PaymentService paymentService;
	private final ExternalPaymentService externalPaymentService;
	private final ExternalPaymentFormService externalPaymentFormService;
	private final SubscriptionService subscriptionService;
	
	public SubscriptionDetailsPresenter(ApplicationContext ctx, SubscriptionDetailsView subscriptionDetailsView)
	{
		super(subscriptionDetailsView);
		
		this.subscriptionDetailsView = subscriptionDetailsView;
		
		this.userRepository = ctx.getBean(CompleteUserRepository.class);
		
		this.authenticationService = ctx.getBean(AuthenticationService.class);
		
		this.invoiceService = ctx.getBean(InvoiceService.class);
		this.paymentService = ctx.getBean(PaymentService.class);
		this.externalPaymentService = ctx.getBean(ExternalPaymentService.class);
		this.externalPaymentFormService = ctx.getBean(ExternalPaymentFormService.class);
		this.subscriptionService = ctx.getBean(SubscriptionService.class);
		
		subscriptionDetailsView.setListener(this);
	}
	
	@Override
	public void changePaymentData(PaymentMethod paymentMethod)
	{
		User user = getUser();
		
		try
		{
			URI paymentDataUpdateFormUrl = externalPaymentFormService.getPaymentDataUpdateFormUrl(user, paymentMethod);
			
			subscriptionDetailsView.showPaymentFormView(paymentDataUpdateFormUrl);
		}
		catch (PaymentException e)
		{
			LOG.error("Error while contacting external payment system", e);
			Notification.show(I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_EXTERNALPAYMENTERRORTITLE.msg(),
					I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_EXTERNALPAYMENTSETTINGSERROR.msg(), Notification.Type.ERROR_MESSAGE);
		}
	}
	
	@Override
	public void changePaymentMethod(PaymentMethod paymentMethod)
	{
		User user = getUser();
		Optional<AbstractPayment> currentPayment = paymentService.findCurrentPayment(user);
		try
		{
			if (paymentMethod == PaymentMethod.PREPAYMENT)
			{
				// Cancel the current pending payment if necessary
				if (currentPayment.isPresent())
				{
					AbstractPayment payment = currentPayment.get();
					if (payment instanceof ExternalPayment && payment.getState() == PaymentState.PENDING)
					{
						ExternalPayment externalPayment = (ExternalPayment) payment;
						invoiceService.cancelExternalPaymentAndCreatePrepayment(externalPayment, null);
					}
				}
				
				UserPaymentSettings userPaymentSettings = paymentService.updateUserPaymentSettings(user, paymentMethod);
				subscriptionDetailsView.setUserPaymentSettings(userPaymentSettings);
				
				Notification.show(
						I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_PAYMENT_METHOD_CHANGED.msg(),
						Type.TRAY_NOTIFICATION);
			}
			else
			{
				// Cancel the current pending payment if necessary
				if (currentPayment.isPresent())
				{
					AbstractPayment payment = currentPayment.get();
					if (payment instanceof Prepayment && payment.getState() == PaymentState.PENDING)
					{
						Prepayment prepayment = (Prepayment) payment;
						invoiceService.cancelPrepaymentAndCreateExternalPayment(prepayment, paymentMethod);
					}
				}
				
				URI paymentDataUpdateFormUrl = externalPaymentFormService.getPaymentMethodChangeFormUrl(user, paymentMethod);
				subscriptionDetailsView.showPaymentFormView(paymentDataUpdateFormUrl);
			}
		}
		catch (PaymentException e)
		{
			LOG.error("Error while contacting external payment system", e);
			Notification.show(I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_EXTERNALPAYMENTERRORTITLE.msg(),
					I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_EXTERNALPAYMENTSETTINGSERROR.msg(), Notification.Type.ERROR_MESSAGE);
		}
	}
	
	@Override
	public void enter(String parameters)
	{
		Set<PaymentMethod> availablePaymentMethods = Arrays.stream(PaymentMethod.values())
				.collect(Collectors.toCollection(LinkedHashSet::new));
		subscriptionDetailsView.setAvailablePaymentMethods(availablePaymentMethods);
		
		User user = getUser();
		updateSubscription(user);
		
		boolean registered = externalPaymentService.isRegistered(user);
		subscriptionDetailsView.setUserIsRegistered(registered);
		
		UserPaymentSettings userPaymentSettings = paymentService.createOrGetUserPaymentSettings(user);
		subscriptionDetailsView.setUserPaymentSettings(userPaymentSettings);
		
		// Show prepayment reminder or the HCO page (for the incomplete payment method change)
		Optional<AbstractPayment> currentPayment = paymentService.findCurrentPayment(user);
		if (currentPayment.isPresent())
		{
			AbstractPayment payment = currentPayment.get();
			if (payment.getState() == PaymentState.PENDING)
			{
				if (payment instanceof Prepayment)
				{
					Prepayment prepayment = (Prepayment) payment;
					showPaymentInfo(subscriptionDetailsView, prepayment);
				}
				else if (payment instanceof ExternalPayment)
				{
					if (!userPaymentSettings.usesExternalPayment())
					{
						ExternalPayment externalPayment = (ExternalPayment) payment;
						try
						{
							URI paymentDataUpdateFormUrl = externalPaymentFormService.getPaymentMethodChangeFormUrl(user, externalPayment.getMethod());
							subscriptionDetailsView.showPaymentFormView(paymentDataUpdateFormUrl);
						}
						catch (PaymentException e)
						{
							LOG.error(e.getMessage());
						}
					}
					else
					{
						subscriptionDetailsView.setAvailablePaymentMethods(Collections.emptySet());
					}
				}
			}
		}
		
		// If the URL fragment contains some response message
		showHCOResponse();
	}
	
	private void showHCOResponse()
	{
		String fragment = UI.getCurrent().getPage().getLocation().getFragment();
		if (fragment.contains(SUCCESS_PARAM))
		{
			Notification.show(
					I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_PAYMENT_METHOD_CHANGED.msg(),
					Type.TRAY_NOTIFICATION);
		}
		else if (fragment.contains(ERROR_PARAM))
		{
			String errorMsg = StringUtils.substringAfterLast(fragment, ERROR_PARAM);
			Notification.show(errorMsg,
					I18N.SUBSCRIPTIONPRESENTER_PAYMENT_PURCHASED_ERROR.msg(),
					Type.ERROR_MESSAGE);
		}
	}
	
	private User getUser()
	{
		final Long authenticatedUserId = authenticationService.getAuthenticatedUserId();
		return userRepository.findById(authenticatedUserId);
	}
	
	private void updateSubscription(User user)
	{
		final Subscription subscription = subscriptionService.findCurrentSubscription(user).orElse(null);
		subscriptionDetailsView.setSubscription(subscription, getAdditionalSubscriptionInfo(user));
	}
	
	@Override
	public void activateAutoRenewal(Subscription subscription)
	{
		final User user = subscription.getUser();
		subscription.setAutomaticRenewal(true);
		
		subscriptionService.saveAndUpdateStatus(subscription);
		
		updateSubscription(user);
	}
	
	private String getAdditionalSubscriptionInfo(User user)
	{
		String infoText = null;
		final Optional<AbstractPayment> payment = paymentService.findCurrentPayment(user);
		if (payment.isPresent())
		{
			final List<Product> products = payment.get().getInvoice().getItems().stream().map(InvoiceItem::getProduct).collect(Collectors.toList());
			
			if (PaymentState.PENDING.equals(payment.get().getState()))
			{
				if (products.stream().anyMatch(p -> p instanceof InitialSubscriptionOffer || p instanceof UpgradeOffer || p instanceof RenewalOffer))
				{
					switch (payment.get().getMethod())
					{
						case PREPAYMENT:
							infoText = I18N.HOMEPRESENTER_CAPTION_PAYMENTAWAITING.msg();
							break;
						default:
							infoText = I18N.HOMEPRESENTER_CAPTION_PAYMENTPROCESSING.msg();
					}
				}
			}
		}
		return infoText;
	}
}
