package de.binaerebauten.gleichklang.adminweb.service.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.user.CancelReason;
import de.binaerebauten.gleichklang.core.repository.I18NRepository;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.repository.SubscriptionRepository;
import de.binaerebauten.gleichklang.core.service.UserService;

@Service
public class SubscriptionExpiringService {
	private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiringService.class);

	private static final int PAGE_SIZE = 10;

	private final SubscriptionRepository subscriptionRepository;

	private final TransactionTemplate transactionTemplate;

	private final SubscriptionRenewalService subscriptionRenewalService;

	private final MailSendService mailSendService;

	private final UserMailTemplateService userMailTemplateService;

	private final I18NRepository i18NRepository;

	@Autowired
	UserService userService;

	@Autowired
	public SubscriptionExpiringService(SubscriptionRepository subscriptionRepository,
									   SubscriptionRenewalService subscriptionRenewalService,
									   TransactionTemplate transactionTemplate, UserService userService, MailSendService mailSendService, UserMailTemplateService
									   userMailTemplateService, I18NRepository i18NRepository) {
		this.subscriptionRepository = subscriptionRepository;
		this.subscriptionRenewalService = subscriptionRenewalService;
		this.transactionTemplate = transactionTemplate;
		this.userService = userService;
		this.mailSendService = mailSendService;
		this.userMailTemplateService = userMailTemplateService;
		this.i18NRepository = i18NRepository;
	}


	/**
	 * This method periodically updates the expired active subscriptions.
	 */
	@Scheduled(cron = "0 30 2 * * *", zone = "Europe/Berlin")//every day at 2:30 am
	public void processExpiringActiveSubscriptions() {
		final Pageable pageable = new PageRequest(0, PAGE_SIZE);
		final LocalDateTime today = LocalDateTime.now();
		boolean hasContent;
		try {
			do {

				hasContent = transactionTemplate.execute(status ->
				{
					final Page<Subscription> page = subscriptionRepository.findSubscriptionStateTransitionToExpiring(today, pageable);
					page.forEach(this::process);

					return page.hasContent();
				});
			} while (hasContent);
		}
		catch (Throwable e)
		{
			log.error("processExpiringActiveSubscriptions: ", e);
		}
	}

	private void process(Subscription subscription) {
		try {
			transactionTemplate.execute((status) ->
			{
				log.info("With in process==" + subscription.getUser().getEmail() + "   " + subscription.isAutomaticRenewal());
				if (subscription.isAutomaticRenewal() && subscriptionRenewalService.isUserPrepayment(subscription.getUser())) {
					subscription.setState(SubscriptionState.EXPIRING);
					subscriptionRepository.save(subscription);
					subscriptionRenewalService.tryToRenew(subscription);
				}

				else if(subscription.isAutomaticRenewal() && !subscriptionRenewalService.isUserPrepayment(subscription.getUser())){
					subscription.setState(SubscriptionState.EXPIRED);
					subscriptionRepository.save(subscription);
					subscriptionRenewalService.tryToRenew(subscription);
				}

				else {
					sendMail(subscription);
					subscription.setState(SubscriptionState.EXPIRED);
					userService.updateUser(subscription.getUser());
					subscriptionRepository.save(subscription);
				}
				return null;
			});
		}catch (Exception ex)
		{
			log.error("process(Subscription): ", ex);
		}
	}

	private void sendMail(Subscription subscription) {
		String successOfMediation = i18NRepository.successOfMediation(subscription.getUser().getId());
		//String satisfactionWithHarmony = i18NRepository.satisfactionWithHarmony(subscription.getUser().getId());
		List<Object> satisfactionWithHarmonyList = i18NRepository.satisfactionWithHarmony(subscription.getUser().getId());
		Set<CancelReason> cancelReason = subscription.getUser().getCancelReasons();
		if((satisfactionWithHarmonyList.size()>0?(satisfactionWithHarmonyList.contains("Zufrieden")|| satisfactionWithHarmonyList.contains("Satisfied")):false ||
				cancelReason.contains(CancelReason.SUCCESS_THROW_GK) ||
				successOfMediation != null ? successOfMediation.equalsIgnoreCase("Ich habe partnerschaft gefunden.") ||
				successOfMediation.equalsIgnoreCase("ich habe freundschaft gefunden.") ||
				successOfMediation.equalsIgnoreCase("ich habe partnerschaft und freundschaft gefunden.") : false) && cancelReason!=null && !cancelReason.contains(CancelReason.UNHAPPY_WITH_SERVICE))
		{
			    mailSendService.sendEmail(subscription.getUser(), userMailTemplateService.createMailTemplateInstanceForAdminMessage(UserMailTemplate.SUBSCRIPTION_END_AUTORENEWAL_OFF_USER_SATISFIED, subscription.getUser()));
		}
		else{
			    mailSendService.sendEmail(subscription.getUser(), userMailTemplateService.createMailTemplateInstanceForAdminMessage(UserMailTemplate.SUBSCRIPTION_END_AUTORENEWAL_OFF_USER_UNSATISFIED, subscription.getUser()));

		}
	}
}
