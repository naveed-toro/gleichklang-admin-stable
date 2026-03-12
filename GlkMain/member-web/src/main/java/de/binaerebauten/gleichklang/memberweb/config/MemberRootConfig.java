package de.binaerebauten.gleichklang.memberweb.config;

import de.binaerebauten.gleichklang.core.config.RootConfig;
import de.binaerebauten.gleichklang.memberweb.controller.AuthenticationController;
import de.binaerebauten.gleichklang.memberweb.service.SubscriptionOfferService;
import de.binaerebauten.gleichklang.memberweb.service.payment.heidelpay.HeidelpayHcoResponseController;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@ComponentScan(basePackageClasses = { SubscriptionOfferService.class,
		HeidelpayHcoResponseController.class, AuthenticationController.class })
@Import({ RootConfig.class, MemberSecurityConfig.class })
@EnableAsync
public class MemberRootConfig
{
}
