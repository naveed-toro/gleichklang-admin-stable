package de.binaerebauten.gleichklang.core.config;

import com.mysql.jdbc.Driver;
import de.binaerebauten.gleichklang.core.migration.V00_00__Example;
import de.binaerebauten.gleichklang.core.migration.callback.SpringJdbcFlywayCallback;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class PersistenceConfig extends BasePersistenceConfig
{
	private static final String DATABASE_PLACEHOLDER = "database";
	public static final String PROPERTY_FILE_PATH = "file.path";
	
	private static final Logger LOG = LoggerFactory.getLogger(PersistenceConfig.class);
	
	@Value("${db.jdbc.url}")
	private String dbJdbcUrl;
	
	@Value("${db.user}")
	private String dbUser;
	
	@Value("${db.password}")
	private String dbPassword;

	@Value("${db.connection.pool.max.active}")
	private int connectionPoolMaxActive;

	@Value("${db.connection.pool.max.idle}")
	private int connectionPoolMaxIdle;

	@Value("${db.connection.pool.remove.abandoned.timeout}")
	private int connectionPoolRemoveAbandonedTimeout;

	@Value("${db.auto.flyway.repair}")
	private boolean autoFlywayRepair;

	@Value("${file.path}")
	private String filePath;
	
	public static void performMigration(Flyway flyway)


	{

		final int migrations = flyway.migrate();

		if (migrations > 0)
		{
			LOG.info("Completed DB migration to version {}", flyway.info().current().getVersion());
		}
		else
		{
			LOG.info("your database is already up to date");
		}
	}
	
	public static void repairFlyway(String dbJdbcUrl, String dbUser, String dbPassword, String filePath)
	{
		final Flyway flyway = createFlyway(dbJdbcUrl, dbUser, dbPassword, filePath);
		flyway.repair();
	}
	
	public static Flyway createFlyway(String dbJdbcUrl, String dbUser, String dbPassword, String filePath)
	{
		final String databaseName = dbJdbcUrl.substring(dbJdbcUrl.lastIndexOf("/") + 1);
		final Flyway flyway = new Flyway();
		flyway.setBaselineOnMigrate(true);
		flyway.setPlaceholderReplacement(false);
		flyway.setPlaceholderPrefix("$ghghghgh(((8");
		// let flyway manage it's own datasource and using a db connection pool doesn't make sense
		flyway.setDataSource(dbJdbcUrl, dbUser, dbPassword);

		flyway.setLocations(V00_00__Example.class.getPackage().getName(), "db/migration");
		flyway.setCallbacks(new SpringJdbcFlywayCallback(databaseName));
		flyway.getPlaceholders().put(DATABASE_PLACEHOLDER, databaseName);
		flyway.getPlaceholders().put(PROPERTY_FILE_PATH, filePath);

		final MigrationInfo current = flyway.info().current();
		if (current == null)
		{
			LOG.info("No existing schema_version table found, starting migration from scratch");
		}
		else
		{
			LOG.info("Current DB version is {}", current.getVersion());
		}
		return flyway;
	}

	@Override
	@Bean
	public DataSource dataSource()
	{
		final org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();

		ds.setDriverClassName(Driver.class.getName());
		ds.setUrl(dbJdbcUrl);
		ds.setUsername(dbUser);
		ds.setPassword(dbPassword);

		ds.setValidationQuery("SELECT 1");
		ds.setRemoveAbandoned(true);
		ds.setRemoveAbandonedTimeout(connectionPoolRemoveAbandonedTimeout);
		ds.setTestOnBorrow(true);
		ds.setMaxActive(connectionPoolMaxActive);
		ds.setMaxIdle(connectionPoolMaxIdle);

		return ds;
	}

//	@Bean
//	public Flyway flyway()
//	{
//		final Flyway flyway = createFlyway(dbJdbcUrl, dbUser, dbPassword, filePath);
////       if (autoFlywayRepair) flyway.repair();
////        performMigration(flyway);
//		return flyway;
//	}
	
	@Override
	protected Properties getHibernateProperties()
	{
		final Properties properties = new Properties();
		
		properties.put("hibernate.dialect", GleichklangMySQLDialect.class.getName());
		properties.put("hibernate.show_sql", false);
		properties.put("hibernate.format_sql", false);
		properties.put("hibernate.hbm2ddl.auto", "none");
		properties.put("hibernate.jdbc.batch_size", 20);
		properties.put("hibernate.default_batch_fetch_size", 16);
		// Application start parameter
		if (Boolean.getBoolean("showSql"))
		{
			properties.put("hibernate.show_sql", true);
			properties.put("hibernate.use_sql_comments", true);
		}
		
		return properties;
	}
	
	@Override
	@Bean
//	@DependsOn("flyway")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory()
	{
		return super.entityManagerFactory();
	}
}
