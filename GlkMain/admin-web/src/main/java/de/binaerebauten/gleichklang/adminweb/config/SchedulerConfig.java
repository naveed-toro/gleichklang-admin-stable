package de.binaerebauten.gleichklang.adminweb.config;

import static de.binaerebauten.gleichklang.core.config.RootConfig.SCAMMING_EXECUTOR;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import de.binaerebauten.gleichklang.adminweb.service.matching.GenerateSuggestionService;
import de.binaerebauten.gleichklang.adminweb.service.matching.MatchingService;
import de.binaerebauten.gleichklang.core.service.mail.MailReceiveService;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer
{
	private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

	@Autowired
	private Environment environment;

	@Autowired
	private MatchingService matchingService;

	@Autowired
	private MailReceiveService mailReceiveService;

	@Autowired
	private GenerateSuggestionService generateSuggestionService;

	/*
	 * @Autowired private SubscriptionRenewalService subscriptionRenewalService;
	 */

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar)
	{
		final Boolean autoMatchingEnabled = environment.getProperty("matching.auto.enabled", Boolean.class, false);

		log.error("------------------->configureTasks<---------------------------------");

		taskRegistrar.setScheduler(taskExecutor());

		if (autoMatchingEnabled)
		{
			configureAutoMatching(taskRegistrar);
		}

		configureEmailDownload(taskRegistrar);
	}

	private void configureAutoMatching(ScheduledTaskRegistrar taskRegistrar)
	{
		taskRegistrar.addFixedDelayTask(new IntervalTask(() ->
		{
			try
			{
				matchingService.startMatching().get();
			}
			catch (InterruptedException e)
			{
				log.error("matching interrupted", e);
			}
			catch (ExecutionException e)
			{
				log.error("matching execution exception", e);
			}
			generateSuggestionService.generateSuggestion();
		}, 60000, 3600000));
	}


	private void configureEmailDownload(ScheduledTaskRegistrar taskRegistrar)
	{
		
		taskRegistrar.addFixedDelayTask(new IntervalTask(() ->
		{
			try
			{
				log.info("------------------->start admin email fetch<---------------------------------");
				mailReceiveService.createUpdateAdminEmailCache();
				log.info("------------------->end admin email fetch<---------------------------------");
			}
			catch (Exception e)
			{
				log.error("mailReceiveService interrupted", e);
			}

		}, 120000, 60000));
	}

	@Bean(destroyMethod = "destroy")
	public TaskScheduler taskExecutor()
	{
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(30);
		taskScheduler.setErrorHandler(t -> log.error("Exception in @Scheduled task. ", t));
        taskScheduler.setThreadNamePrefix("gleichklang-sch-");
        taskScheduler.initialize();
        return taskScheduler;
	}

	@Bean(destroyMethod = "shutdown")
	@Qualifier(SCAMMING_EXECUTOR)
	public ExecutorService scammingExecutor()
	{
		return Executors.newSingleThreadExecutor();
	}

}