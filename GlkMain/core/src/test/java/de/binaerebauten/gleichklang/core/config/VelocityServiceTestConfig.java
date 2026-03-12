package de.binaerebauten.gleichklang.core.config;


import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by Domi on 03.02.2017.
 */
@Configuration
@Import({VelocityConfig.class})
@ComponentScan(basePackageClasses = { TemplateEngineService.class })
public class VelocityServiceTestConfig {

}
