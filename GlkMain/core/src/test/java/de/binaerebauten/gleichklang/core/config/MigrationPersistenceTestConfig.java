package de.binaerebauten.gleichklang.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Configures a mysql database for running migration tests.
 */
@Configuration
@Import(PropertySourcesConfiguration.class)
@PropertySource("classpath:/migration_test.properties")
public class MigrationPersistenceTestConfig extends PersistenceConfig
{
}
