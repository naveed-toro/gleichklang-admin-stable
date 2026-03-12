package de.binaerebauten.gleichklang.core.service;

import com.vaadin.server.VaadinSession;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.ActiveDeactiveSubscription;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionOffer;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionWithState;
import de.binaerebauten.gleichklang.core.model.payment.Subscription_;
import de.binaerebauten.gleichklang.core.model.user.*;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog.UserActivity;
import de.binaerebauten.gleichklang.core.repository.ActivateDeactiveSubscriptionRepository;
import de.binaerebauten.gleichklang.core.repository.SubscriptionRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.mail.MailQueueService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.payment.ExternalPaymentService;
import de.binaerebauten.gleichklang.core.service.payment.PaymentException;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides services for working with {@link de.binaerebauten.gleichklang.core.model.payment.Subscription}
 * entities.
 */
@Service
public class SubscriptionService
{
	private static final Logger LOG = LoggerFactory.getLogger(SubscriptionService.class);
	
	private final SubscriptionRepository subscriptionRepository;
	
	private final UserRepository userRepository;
	
	private final ExternalPaymentService externalPaymentService;

	private final ActivateDeactiveSubscriptionRepository activateDeactiveSubscriptionRepository;
	
	private final UserActivityService userActivityService;
	
	private final MailQueueService mailQueueService;
	private final UserMailTemplateService userMailTemplateService;
	private static final String AUTHENTICATED_USER_TYPE_ATTRIBUTE = "AUTHENTICATED_USER_TYPE";

	@Autowired
	@Lazy
	AdminService adminService;
	//private Admin currentAdmin;
	
	@Autowired
	@Lazy
	private MailSendService mailSendService;
	
	private final int expirationPeriodInDays;
	
	/**
	 * We are using constructor injection here to ease unit testing.
	 *
	 * @param subscriptionRepository
	 * @param userRepository
	 * @param externalPaymentService
	 * @param userActivityService
	 * @param mailQueueService
	 * @param expirationPeriodInDays
	 */
	@Autowired
	public SubscriptionService(SubscriptionRepository subscriptionRepository,
			UserRepository userRepository,
			ExternalPaymentService externalPaymentService,
			UserActivityService userActivityService,
			MailQueueService mailQueueService,
			UserMailTemplateService userMailTemplateService,
			@Value("${subscription_expiration_period_in_days}") int expirationPeriodInDays,ActivateDeactiveSubscriptionRepository activeDeactiveRepository)
	{
		this.subscriptionRepository = subscriptionRepository;
		this.userRepository = userRepository;
		this.externalPaymentService = externalPaymentService;
		this.userActivityService = userActivityService;
		this.mailQueueService = mailQueueService;
		this.expirationPeriodInDays = expirationPeriodInDays;
		this.userMailTemplateService = userMailTemplateService;
		this.activateDeactiveSubscriptionRepository = activeDeactiveRepository;
		//currentAdmin = AppUI.getApplicationContext().getBean(Admin.class);
	}
	
	/**
	 * Creates and saves a subscription for the given user and offer.
	 * Deactivates an already existing active subscription.
	 *
	 * @param user  the user, non null
	 * @param offer the offer for which the user wants to subscribe, non null
	 * @return the subscription
	 */
	@Transactional
	public Subscription createAndSaveSubscription(User user, SubscriptionOffer offer)
	{
		Objects.requireNonNull(user, "user == null");
		Objects.requireNonNull(offer, "offer == null");
		
		Optional<Subscription> currentSubscription = findCurrentSubscription(user);
		if (currentSubscription.isPresent())
		{
			final SubscriptionState currentSubscriptionState = currentSubscription.get().getState();
			if (currentSubscriptionState == SubscriptionState.ACTIVE || currentSubscriptionState == SubscriptionState.EXPIRING)
			{
				currentSubscription.get().setState(SubscriptionState.REPLACED);
			}
			
			currentSubscription.get().setCurrent(null);
			
			// saving and flushing the subscription immediately is required because
			// mysql immediately evaluates the unique constraints
			subscriptionRepository.saveAndFlush(currentSubscription.get());
		}
		
		// Current subscription can be null here, but at this point we don't care
		Subscription subscription = offer.createSubscription(LocalDateTime.now(), currentSubscription.orElse(null));
		subscription.setUser(user);
		LocalDateTime end = subscription.getEnd();
		subscription.setExpirationDate(end.plusDays(expirationPeriodInDays));
		subscriptionRepository.save(subscription);
		
		return subscription;
	}
	
	/**
	 * Finds all subscriptions of given user.
	 *
	 * @param user the user
	 * @return the list of subscriptions
	 */
	@Transactional
	public List<Subscription> findAllSubscriptions(User user)
	{
		return subscriptionRepository.findAllByUser(user);
	}
	
	/**
	 * Finds the current user subscription.
	 *
	 * @param user the user
	 * @return the active subscription or null
	 */
	@Transactional
	public Optional<Subscription> findCurrentSubscription(User user)
	{
		return subscriptionRepository.findCurrentSubscription(user);
	}
	
	/**
	 * Finds the current user subscription.
	 *
	 * @param userId the user
	 * @return the active subscription or null
	 */
	@Transactional
	public Optional<Subscription> findCurrentSubscription(Long userId)
	{
		return subscriptionRepository.findCurrentSubscription(userId);
	}
	
	@Transactional
	public Optional<SubscriptionOffer> findCurrentSubscriptionOffer(User user)
	{
		Optional<Subscription> currentSubscription = findCurrentSubscription(user);
		return Optional.ofNullable(currentSubscription.map(Subscription::getOffer).orElse(null));
	}
	
	@Transactional
	public Set<RecommendationCategory> getCurrentSubscriptionOfferCategories(User user)
	{
		Objects.requireNonNull(user, "user == null");
		
		return subscriptionRepository.findCurrentSubscriptionOfferCategories(user);
	}
	
	@Transactional
	public Set<RecommendationCategory> getLastSubscriptionOfferCategories(Long userId)
	{
		return subscriptionRepository.findLastSubscriptionOfferCategories(userId).stream()
				.map(RecommendationCategory::valueOf)
				.collect(Collectors.toSet());
	}
	
	/**
	 * Saves the given subscription and updates the state of the subscription
	 * with regards to the given date date.
	 *
	 * @param subscription the non-null subscription
	 */
	@CheckedTransactional
	public void saveAndUpdateStatus(Subscription subscription)
	{
		Objects.requireNonNull(subscription, "subscription == null");
		
		if (Boolean.TRUE.equals(subscription.getCurrent()))
		{
			final User user = subscription.getUser();
			
			if (subscription.isAutomaticRenewalChanged())
			{
				if (subscription.isAutomaticRenewal())
				{
					mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(UserMailTemplate.RENEWAL_CHOSEN_REMINDER, subscription));
					mailQueueService.dequeue(user, UserMailTemplate.RENEWAL_DISABLED_REMINDER);
					userActivityService.createActivity(user, UserActivity.RENEWAL_REACTIVATED);
				}
				else
				{
					mailSendService.sendEmail(user, userMailTemplateService.createMailTemplateInstance(UserMailTemplate.RENEWAL_DISABLED, subscription));
					mailQueueService.enqueue(user, UserMailTemplate.RENEWAL_DISABLED_REMINDER, subscription.getEnd().minusDays(7));
					userActivityService.createActivity(user, UserActivity.RENEWAL_CANCELLED);
				}
			}
		}
		
		subscriptionRepository.save(subscription);
		activateDeactiveSubscription(subscription);
	}

	public void activateDeactiveSubscription(Subscription subscription){
		adminService = AppUI.getApplicationContext().getBean(AdminService.class);


		Admin admin = adminService.getCurrentUser();

		ActiveDeactiveSubscription activeDeactiveSubscription = new ActiveDeactiveSubscription();
		activeDeactiveSubscription.setSubscription(subscription);
		activeDeactiveSubscription.setCreateDate(LocalDateTime.now());


		if(getAuthenticatedUserType().equals(SignableUser.UserType.ADMIN) && admin!=null){

			activeDeactiveSubscription.setUserType("Admin");
			activeDeactiveSubscription.setUser(userRepository.findById(admin.getId()));
			activeDeactiveSubscription.setAlias(admin.getAlias());
			if(subscription.isAutomaticRenewal()){
				activeDeactiveSubscription.setType("Activated");
			}
			else {
				activeDeactiveSubscription.setType("Deactivated");
			}
		}
		else if(getAuthenticatedUserType().equals(SignableUser.UserType.MEMBER)){
			activeDeactiveSubscription.setType("Activated");
			activeDeactiveSubscription.setUserType("Member");
			activeDeactiveSubscription.setUser(subscription.getUser());
			activeDeactiveSubscription.setAlias(subscription.getUser().getAlias());
		}
		activateDeactiveSubscriptionRepository.save(activeDeactiveSubscription);
	}
	/**
	 * This method returns the current subscription of the given user and
	 * calculates the subscription state for the given date.
	 *
	 * @param user the non-null user
	 * @param date the non-null date to calculate the state
	 * @return the current subscription with the calculated state
	 */
	@Deprecated
	public SubscriptionWithState getCurrentSubscriptionWithState(User user, LocalDateTime date)
	{
		Objects.requireNonNull(user, "user == null");
		
		final Optional<Subscription> currentSubscription = subscriptionRepository.findCurrentSubscription(user);
		
		return new SubscriptionWithState(currentSubscription.orElse(null));
	}
	
	/**
	 * Cancels the given subscription. User gets the status cancelled as well.
	 *
	 * @param subscription the non-null subscription
	 * @throws PaymentException
	 */
	@CheckedTransactional
	public void cancelSubscription(Subscription subscription)
	{
		Objects.requireNonNull(subscription, "subscription == null");
		
		if (!SubscriptionState.EXPIRED.equals(subscription.getState()) && !SubscriptionState.CANCELED.equals(subscription.getState()))
		{
			final LocalDateTime today = LocalDateTime.now();
			
			if (today.isBefore(subscription.getExpirationDate()))
			{
				subscription.setExpirationDate(today);
				subscription.setEnd(today);
				subscription.setState(SubscriptionState.CANCELED);
			}
			else
			{
				subscription.setState(SubscriptionState.EXPIRED);
			}
			subscription.setCurrent(null);
			subscriptionRepository.save(subscription);
		}
		
		cancelUser(subscription.getUser());
	}
	
	public void cancelUser(User user)
	{
		Objects.requireNonNull(user, "user == null");

		if (!MemberStatus.DELETED.equals(user.getMemberStatus()) && !MemberStatus.CANCELED.equals(user.getMemberStatus()) && !MemberStatus.ADMIN_DELETED.equals(user.getMemberStatus()) && !MemberStatus.ADMIN_CANCELED.equals(user.getMemberStatus()))
		{
			try {
				if (getAuthenticatedUserType().equals(SignableUser.UserType.ADMIN))
					user.setMemberStatus(MemberStatus.ADMIN_CANCELED);
				else user.setMemberStatus(MemberStatus.CANCELED);
			}catch (NullPointerException e){ user.setMemberStatus(MemberStatus.CANCELED);}
			userRepository.save(user);
			mailQueueService.dequeue(user, UserMailTemplate.RENEWAL_DISABLED_REMINDER);
		}
	}
	
	public long countSubscriptions(User user)
	{
		return subscriptionRepository.count((root, query, cb) -> cb.equal(root.get(Subscription_.user), user));
	}
	
	public boolean subscriptionsExists(User user)
	{
		return countSubscriptions(user) > 0;
	}
	
	public LazyBeanFilteredItemsHandler<Subscription> createSubscriptionHandler(User user)
	{
		final Specifications<Subscription> specs = Specifications.where((root, query, cb) -> cb.equal(root.get(Subscription_.user), user));
		return (specification, pageable) -> subscriptionRepository.findAll(specs.and(specification), pageable);
	}

	/**
	 * Returns the authenticated user UserType.
	 *
	 * @return the authenticated user User type in the current session or empty
	 */

	public SignableUser.UserType getAuthenticatedUserType()
	{
		Object authenticatedUserType = VaadinSession.getCurrent().getAttribute(AUTHENTICATED_USER_TYPE_ATTRIBUTE);
		return authenticatedUserType != null ? (SignableUser.UserType) authenticatedUserType : null;
	}

	@CheckedTransactional
	public void deactivateAutoRenewal(Subscription subscription){
		subscriptionRepository.save(subscription);
	}
}
