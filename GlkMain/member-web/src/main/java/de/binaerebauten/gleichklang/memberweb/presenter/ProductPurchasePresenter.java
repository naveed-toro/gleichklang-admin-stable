package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.ui.Notification;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.payment.Product.ProductType;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserPaymentSettings;
import de.binaerebauten.gleichklang.core.repository.PaymentRepository;
import de.binaerebauten.gleichklang.core.repository.ProductRepository;
import de.binaerebauten.gleichklang.core.repository.UserPaymentSettingsRepository;
import de.binaerebauten.gleichklang.core.repository.user.CompleteUserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.InvoiceService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.service.payment.ProductService;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.filter.ProductValidFilter;
import de.binaerebauten.gleichklang.memberweb.view.ProductPurchaseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Presenter for the {@link de.binaerebauten.gleichklang.memberweb.view.ProductPurchaseView}.
 * Shows either product purchase options or the payment information if the current
 * payment is a prepayment in the status 'pending'.
 */
public class ProductPurchasePresenter<T extends Product> extends SubscriptionTabPresenter
		implements ProductPurchaseView.ProductPurchaseViewListener
{
	private static final Logger LOG = LoggerFactory.getLogger(ProductPurchasePresenter.class);
	
	private final ProductPurchaseView productPurchaseView;
	
	private final BiFunction<LocalDate, SubscriptionOffer, ProductValidFilter<T>> productFilterCreator;
	
	private final AuthenticationService authenticationService;
	
	private final ProductRepository productRepository;
	private final PaymentRepository paymentRepository;
	private final UserPaymentSettingsRepository userPaymentSettingsRepository;
	private final CompleteUserRepository userRepository;
	
	private final InvoiceService invoiceService;
	private final SubscriptionService subscriptionService;
	private final ProductService productService;
	private final ExternalPaymentService externalPaymentService;
	
	private final ProductType productType;
	
	public ProductPurchasePresenter(ApplicationContext ctx, ProductPurchaseView productPurchaseView, BiFunction<LocalDate, SubscriptionOffer, ProductValidFilter<T>> productFilterCreator, Class<T> productClass)
	{
		super(productPurchaseView);
		authenticationService = ctx.getBean(AuthenticationService.class);
		
		productRepository = ctx.getBean(ProductRepository.class);
		paymentRepository = ctx.getBean(PaymentRepository.class);
		userPaymentSettingsRepository = ctx.getBean(UserPaymentSettingsRepository.class);
		userRepository = ctx.getBean(CompleteUserRepository.class);
		
		invoiceService = ctx.getBean(InvoiceService.class);
		productService = ctx.getBean(ProductService.class);
		subscriptionService = ctx.getBean(SubscriptionService.class);
		externalPaymentService = ctx.getBean(ExternalPaymentService.class);
		
		this.productPurchaseView = productPurchaseView;
		this.productFilterCreator = productFilterCreator;
		this.productType = ProductType.of(productClass);
		productPurchaseView.setListener(this);
	}
	
	@Override
	public void purchase(Product product)
	{
		User user = getUser();
		
		MonetaryAmount price = productService.getPrice(user, product);
		
		final String msg = I18N.PRODUCTPURCHASEPRESENTER_CAPTION_CONFIRMPURCHASE.msg(product.getName(), price);
		MessageBox.show(msg, MessageBox.MessageBoxButtons.YES_NO, r -> doPurchase(r, product));
	}
	
	@Override
	public void enter(String parameters)
	{
		final User user = getUser();
		
		if(ProductType.UPGRADE_OFFER.equals(productType))
		{
			final LocalDateTime lockDate = LocalDateTime.now().plus(1, ChronoUnit.WEEKS);
			if(subscriptionService.findCurrentSubscription(user).map(Subscription::getEnd).filter(lockDate::isAfter).isPresent())
			{
				showWaitForRenewal();
				return;
			}
		}
		
		final Set<AbstractPayment> pendingPayments = paymentRepository.findPendingPayments(user);
		if (!pendingPayments.isEmpty())
		{
			showPaymentInfo(productPurchaseView, pendingPayments);
		}
		else
		{
			showAvailableProducts(user);
		}
	}
	
	private void showWaitForRenewal()
	{
		productPurchaseView.setPaymentInfo(I18N.PRODUCTPURCHASEPRESENTER_CAPTION_WAITFORRENEWAL.msg());
	}
	
	/**
	 * Shows available products for users with external payment method.
	 *
	 * @param user
	 */
	private void showAvailableProducts(User user)
	{
		boolean registered = externalPaymentService.isRegistered(user);
		Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
		if (paymentSettings.isPresent() && (registered || !paymentSettings.get().usesExternalPayment()))
		{
			Optional<SubscriptionOffer> activeSubscriptionOffer = subscriptionService.findCurrentSubscriptionOffer(user);
			final ProductValidFilter<T> filter = productFilterCreator.apply(LocalDate.now(), activeSubscriptionOffer.orElse(null));
			List<T> products = productRepository.findAll(filter);
			productPurchaseView.setProducts(products);
		}
		else
		{
			showPendingRegistration(productPurchaseView);
		}
	}
	
	private void doPurchase(MessageBox.DialogResult dialogResult, Product product)
	{
		if (dialogResult == MessageBox.DialogResult.YES)
		{
			User user = getUser();
			
			Optional<UserPaymentSettings> paymentSettings = userPaymentSettingsRepository.findByUser(user);
			if (paymentSettings.isPresent())
			{
				PaymentMethod paymentMethod = externalPaymentService.usesExternalPayment(user) ? paymentSettings.get().getPaymentMethod() : PaymentMethod.PREPAYMENT;
				try
				{
					Invoice invoice = invoiceService.createAndSaveInvoice(user, product, paymentMethod);
					
					Optional<AbstractPayment> paymentOptional = invoice.getPayments().stream().findFirst();
					if (paymentOptional.isPresent())
					{
						AbstractPayment payment = paymentOptional.get();
						if (payment.getState() == PaymentState.PENDING)
						{
							if (payment instanceof Prepayment)
							{
								Prepayment prepayment = (Prepayment) payment;
								showPurchased(productPurchaseView, prepayment);
							}
							else if (payment instanceof ExternalPayment)
							{
								ExternalPayment externalPayment = (ExternalPayment) payment;
								externalPaymentService.requestPayment(externalPayment);
								showPurchased(productPurchaseView, externalPayment);
							}
						}
						else
						{
							LOG.info("No pending payment found, skipping");
							
							// For the case where amount is 0
							showPurchased(productPurchaseView, payment);
						}
					}
				}
				catch (IllegalStateException e)
				{
					LOG.error("Error saving new payment", e);
					Notification.show(I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_PENDING_PAYMENT_TITLE.msg(),
							I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_PENDING_PAYMENT_ERROR.msg(),
							Notification.Type.ERROR_MESSAGE);
				}
				catch (PaymentException e)
				{
					LOG.error("Error while contacting external payment system", e);
					Notification.show(I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_EXTERNALPAYMENTERRORTITLE.msg(),
							I18N.SUBSCRIPTIONPRESENTER_NOTIFICATION_EXTERNALPAYMENTERROR.msg(),
							Notification.Type.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private User getUser()
	{
		Long authenticatedUserId = authenticationService.getAuthenticatedUserId();
		return userRepository.findById(authenticatedUserId);
	}
	
}
