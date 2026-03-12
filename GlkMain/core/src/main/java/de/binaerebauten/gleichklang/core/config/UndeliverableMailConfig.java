package de.binaerebauten.gleichklang.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.mail.search.FromStringTerm;

/**
 * This configuration loads the regex patterns from a spring xml file
 * and thus allows the modification of the patterns without
 * changing the java source code.
 *
 * Additionally this configuration provides an imap search term to query
 * the inbox for mails that the mailer daemon send {@link #mailerDaemonPattern()}.
 */
@Configuration
@ImportResource("classpath:undeliverableMailReasons.xml")
public class UndeliverableMailConfig
{
	@Bean
	public FromStringTerm mailerDaemonPattern()
	{
		return new FromStringTerm("MAILER-DAEMON@");
	}
}
