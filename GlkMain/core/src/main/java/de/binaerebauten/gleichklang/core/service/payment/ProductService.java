package de.binaerebauten.gleichklang.core.service.payment;

import com.vaadin.ui.Notification;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.payment.*;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.service.affiliate.AffiliatePaymentService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * This service is provides operations for {@link Product} purchased by an
 * user.
 */
@Service
public class ProductService
{
	class ProductActivator implements ProductVisitor<Subscription>
	{
		private final AbstractPayment payment;
		
		private final Subscription currentSubscription;
		
		public ProductActivator(AbstractPayment payment, Subscription currentSubscription)
		{
			this.payment = payment;
			this.currentSubscription = currentSubscription;
		}
		
		@Override
		public Subscription visit(InitialSubscriptionOffer initialSubscriptionOffer)
		{
			return activateSubscription(payment, initialSubscriptionOffer);
		}
		
		@Override
		public Subscription visit(RenewalOffer renewalOffer)
		{
			// Prolong an affiliate payment state by renewal
			affiliatePaymentService.prolong(currentSubscription, payment);
			
			return activateSubscription(payment, renewalOffer);
		}
		
		@Override
		public Subscription visit(UpgradeOffer upgradeOffer)
		{
			final Subscription subscription = activateSubscription(payment, upgradeOffer);
			if (upgradeOffer.getUpgradeType() == UpgradeType.CATEGORY_EXTENSION)
			{
				userService.updateCategories(subscription.getUser(), upgradeOffer, true);
			}
			
			return subscription;
		}
		
		@Override
		public Subscription visit(ServiceOffer serviceOffer)
		{
			Message message = messageService.createNewMessage(payment.getUser());
			message.getReceiverEnvelope().setUser(null); // send message to admins
			message.setSubject(serviceOffer.getName());
			message.setBody(serviceOffer.getName());
			try
			{
				messageService.sendMessageToAdmin(message, null);
			}
			catch (ValidationException e)
			{
				LOG.error(e.getMessage(), e);
				Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
			}
			
			return null;
		}
		
		@Override
		public Subscription visit(Chargeback chargeback)
		{
			// nothing to do
			return null;
		}
	}
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductService.class);
	private final AffiliatePaymentService affiliatePaymentService;
	private final MessageService messageService;
	private final SubscriptionService subscriptionService;
	private final UserService userService;
	
	/**
	 * This service uses constructor based dependency injection to ease
	 * testing.
	 *
	 * @param subscriptionService
	 * @param messageService
	 * @param userService
	 */
	@Autowired
	public ProductService(AffiliatePaymentService affiliatePaymentService,
			SubscriptionService subscriptionService,
			MessageService messageService,
			UserService userService)
	{
		this.affiliatePaymentService = affiliatePaymentService;
		this.messageService = messageService;
		this.subscriptionService = subscriptionService;
		this.userService = userService;
	}
	
	/**
	 * Activates the given product from the given payment.
	 *
	 * @param product the non-null product to activate
	 * @param payment the non-null payment that activates the product
	 * @return
	 */
	@Transactional
	public Optional<Subscription> activate(Product product, AbstractPayment payment)
	{
		Objects.requireNonNull(payment, "payment == null");
		Objects.requireNonNull(product, "product == null");
		
		User user = payment.getUser();
		Optional<Subscription> currentSubscription = subscriptionService.findCurrentSubscription(user);
		ProductActivator productActivator = new ProductActivator(payment, currentSubscription.orElse(null));
		Subscription subscription = product.accept(productActivator);
		
		return Optional.ofNullable(subscription);
	}
	
	/**
	 * Calculates the price for the given parameters.
	 *
	 * @param user    the non-null user
	 * @param product the non-null product
	 * @return the price for the given parameters
	 */
	@Transactional
	public MonetaryAmount getPrice(User user, Product product)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(product, "product == null");
		
		MonetaryAmount amount;
		
		if (product instanceof UpgradeOffer)
		{
			UpgradeOffer upgradeOffer = (UpgradeOffer) product;
			
			Optional<Subscription> currentSubscription = subscriptionService.findCurrentSubscription(user);
			if (currentSubscription.isPresent())
			{
				amount = upgradeOffer.getAmount(LocalDateTime.now(), currentSubscription.get());
			}
			else
			{
				throw new NullPointerException("currentSubscription == null");
			}
		}
		else
		{
			amount = product.getAmount();
		}
		
		return amount;
	}
	
	private Subscription activateSubscription(AbstractPayment payment, SubscriptionOffer subscriptionOffer)
	{
		Objects.requireNonNull(payment, "payment == null");
		Objects.requireNonNull(subscriptionOffer, "subscriptionOffer == null");
		
		User user = payment.getUser();
		Objects.requireNonNull(user, "user == null");
		
		if (MemberStatus.CANCELED.equals(user.getMemberStatus()))
		{
			userService.activateUser(user);
		}
		
		Subscription subscription = subscriptionService.createAndSaveSubscription(user, subscriptionOffer);
		
		payment.getInvoice().getItems().stream()
				.findFirst()
				.ifPresent(invoiceItem -> invoiceItem.setSubscription(subscription));
		
		// TODO diabled for GF-342
		//		mailQueueService.enqueue(user, UserMailTemplate.NO_MATCH_MESSAGE);
		
		return subscription;
	}
}
