package de.binaerebauten.gleichklang.core.config;

import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Konfiguriert Spring Data Jpa für Tests mit H2 als In-Memory Datenbank.
 * 
 * @author matthiaskoster
 */
@Configuration
@ComponentScan(basePackageClasses = { UserService.class, UserRepository.class, DefaultEntityFactory.class })
public class PersistenceTestConfig extends BasePersistenceConfig
{
	@Override
	public DataSource dataSource()
	{
		final EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder()
				.setType(EmbeddedDatabaseType.H2)
				.addScript("classpath:h2-schema.sql")
						// this ensures that the cached application context creates a new db after each shutdown
						// this is mainly required to be able to use springs @DirtiesContext annotation
				.generateUniqueName(true);

		return builder.build();
	}
	
	@Override
	protected Properties getHibernateProperties()
	{
		final Properties properties = new Properties();
		properties.put("hibernate.dialect", GleichklangH2Dialect.class.getName());
		properties.put("hibernate.hbm2ddl.auto", "update");
		if(Boolean.getBoolean("showSql")){
			properties.put("hibernate.show_sql", true);
		}

		return properties;
	}
}
