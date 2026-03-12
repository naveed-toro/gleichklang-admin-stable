package de.binaerebauten.gleichklang.core.monitoring;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManagerFactory;

/**
 * Factory to create the hibernate statistics so that they can be exposed via JMX.
 */
public class HibernateStatisticsFactoryBean implements FactoryBean<Statistics>
{
	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Override
	public Statistics getObject() throws Exception
	{
		HibernateEntityManagerFactory entityManagerFactory = (HibernateEntityManagerFactory) this.entityManagerFactory;
		SessionFactory sessionFactory = entityManagerFactory.getSessionFactory();

		Statistics statistics = sessionFactory.getStatistics();
		statistics.setStatisticsEnabled(true);

		return statistics;
	}

	@Override
	public Class<?> getObjectType()
	{
		return Statistics.class;
	}

	@Override
	public boolean isSingleton()
	{
		return true;
	}
}
