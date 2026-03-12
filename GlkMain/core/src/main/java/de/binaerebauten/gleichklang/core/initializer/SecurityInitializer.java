package de.binaerebauten.gleichklang.core.initializer;

import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

@Order(value = 2)
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer
{
}
