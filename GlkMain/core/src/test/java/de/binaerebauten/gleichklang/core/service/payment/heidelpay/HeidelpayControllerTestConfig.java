package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import de.binaerebauten.gleichklang.core.config.PersistenceTestConfig;
import de.binaerebauten.gleichklang.core.config.RootTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("heidelpay")
@Import({ HeidelpayClientTestConfig.class, RootTestConfig.class, PersistenceTestConfig.class })
public class HeidelpayControllerTestConfig
{
}
