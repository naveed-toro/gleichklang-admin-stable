package de.binaerebauten.gleichklang.adminweb.service.payment;

import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.repository.SubscriptionRepository;
import de.binaerebauten.gleichklang.core.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

/**
 * This service is responsible for managing the renewal of inactive
 * subscriptions.
 */
@Service
public class SubscriptionExpiredService
{
	private static final Logger LOG = LoggerFactory.getLogger(SubscriptionExpiredService.class);
	
	private static final int PAGE_SIZE = 10;
	
	private final SubscriptionRepository subscriptionRepository;
	
	private final TransactionTemplate transactionTemplate;
	
	private final SubscriptionService subscriptionService;
	
	@Autowired
	public SubscriptionExpiredService(SubscriptionRepository subscriptionRepository, SubscriptionService subscriptionService, TransactionTemplate transactionTemplate)
	{
		this.subscriptionRepository = subscriptionRepository;
		this.subscriptionService = subscriptionService;
		this.transactionTemplate = transactionTemplate;
	}
	
	/**
	 * This method periodically updates the expired active subscriptions.
	 */
	@Scheduled(cron = "0 40 2 * * *", zone = "Europe/Berlin")//every day at 2:40 am
	public void processExpiringActiveSubscriptions()
	{
		final Pageable pageable = new PageRequest(0, PAGE_SIZE);
		final LocalDateTime today = LocalDateTime.now();
		boolean hasContent;
		try
		{
			do
			{
				hasContent = transactionTemplate.execute(status ->
				{
					final Page<Subscription> page = subscriptionRepository.findSubscriptionStateTransitionToExpired(today, pageable);
					page.forEach(this::process);
					
					return page.hasContent();
				});
			} while (hasContent);
		}
		catch (Throwable e)
		{
			LOG.error("SubscriptionExpiredService.processExpiringActiveSubscriptions", e);
		}
	}
	
	private void process(Subscription subscription)
	{
		transactionTemplate.execute((status) ->
		{
			subscriptionService.cancelSubscription(subscription);
			return null;
		});
	}
}
