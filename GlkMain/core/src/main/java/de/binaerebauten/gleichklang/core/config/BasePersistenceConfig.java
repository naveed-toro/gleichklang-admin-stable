package de.binaerebauten.gleichklang.core.config;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.repository.BaseRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class, basePackageClasses = BaseRepositoryImpl.class)
public abstract class BasePersistenceConfig
{
	@Bean
	public abstract DataSource dataSource();
	
	protected abstract Properties getHibernateProperties();
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory()
	{
		final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		
		final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan(BaseEntity.class.getPackage().getName());
		factory.setDataSource(dataSource());
		factory.setJpaProperties(getHibernateProperties());
		afterPropertiesSet(factory);

		return factory;
	}

	protected void afterPropertiesSet(LocalContainerEntityManagerFactoryBean factory)
	{
		factory.afterPropertiesSet();
	}
	
	@Bean
	public PlatformTransactionManager transactionManager()
	{
		final JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setDataSource(dataSource());
		return txManager;
	}

	@Bean
	public TransactionTemplate transactionTemplate()
	{
		return new TransactionTemplate(transactionManager());
	}
}
