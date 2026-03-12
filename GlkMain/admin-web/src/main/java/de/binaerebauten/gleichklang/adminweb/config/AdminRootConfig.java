package de.binaerebauten.gleichklang.adminweb.config;

import de.binaerebauten.gleichklang.adminweb.service.NewsService;
import de.binaerebauten.gleichklang.adminweb.service.payment.heidelpay.HeidelpayBackofficePaymentService;
import de.binaerebauten.gleichklang.core.config.RootConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Import({ RootConfig.class, AdminSecurityConfig.class, SchedulerConfig.class })
@ComponentScan(basePackageClasses = { NewsService.class, HeidelpayBackofficePaymentService.class })
@EnableAsync
@EnableScheduling
public class AdminRootConfig
{

}
