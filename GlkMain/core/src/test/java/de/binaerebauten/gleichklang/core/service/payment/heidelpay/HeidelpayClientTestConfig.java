package de.binaerebauten.gleichklang.core.service.payment.heidelpay;

import de.binaerebauten.gleichklang.core.config.HeidelpayClientConfig;
import de.binaerebauten.gleichklang.core.config.PropertySourcesConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({ PropertySourcesConfiguration.class, HeidelpayClientConfig.class })
@PropertySource("classpath:/heidelpay_test.properties")
public class HeidelpayClientTestConfig
{
}
