package de.binaerebauten.gleichklang.adminweb.config;

import de.binaerebauten.gleichklang.adminweb.service.NewsService;
import de.binaerebauten.gleichklang.adminweb.service.mail.UndeliverableMailReceiveService;
import de.binaerebauten.gleichklang.adminweb.service.payment.PrepaymentReminderService;
import de.binaerebauten.gleichklang.adminweb.service.payment.SocialTariffMailReceiverService;
import de.binaerebauten.gleichklang.core.service.mail.UndeliverableMailService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackageClasses = NewsService.class,
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
				classes = { UndeliverableMailService.class, SocialTariffMailReceiverService.class,
						UndeliverableMailReceiveService.class, PrepaymentReminderService.class }) )
public class AdminTestConfig
{
}
