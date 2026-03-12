package de.binaerebauten.gleichklang.core.migration.callback;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;

public class SpringJdbcFlywayCallback implements FlywayCallback
{
	private static final Logger LOG = LoggerFactory.getLogger(SpringJdbcFlywayCallback.class);
	
	private final String databaseName;
	private final MigrationStatisticsCollector statisticsCollector;

	public SpringJdbcFlywayCallback(String databaseName)
	{
		this.databaseName = databaseName;
		this.statisticsCollector = new MigrationStatisticsCollector(databaseName);

	}
	
	private void migrate(Connection connection, SpringJdbcCallback callback)
	{
		try
		{
			callback.execute(new JdbcTemplate(new SingleConnectionDataSource(connection, true)), databaseName);
		}
		catch (final Exception e)
		{
			throw new FlywayException("Callback failed !", e);
		}
	}
	
	@Override
	public void beforeClean(Connection connection)
	{
		LOG.debug("beforeClean");
	}
	
	@Override
	public void afterClean(Connection connection)
	{
		LOG.debug("afterClean");
	}
	
	@Override
	public void beforeMigrate(Connection connection)
	{
		LOG.info("beforeMigrate");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
		statisticsCollector.beforeMigrate(jdbcTemplate, "REGEXP '^comp'" );
	}

	@Override
	public void afterMigrate(Connection connection)
	{
		LOG.info("afterMigrate");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));

		statisticsCollector.afterMigrate(jdbcTemplate, "NOT REGEXP '^comp'");
	}
	
	@Override
	public void beforeEachMigrate(Connection connection, MigrationInfo info)
	{
		LOG.debug("beforeEachMigrate: {} {}", info.getVersion(), info.getDescription());
	}
	
	@Override
	public void afterEachMigrate(Connection connection, MigrationInfo info)
	{
		LOG.debug("afterEachMigrate: {} {}. It took {} ms", info.getVersion(), info.getDescription(), info.getExecutionTime());
	}
	
	@Override
	public void beforeValidate(Connection connection)
	{
		LOG.debug("beforeValidate");
	}
	
	@Override
	public void afterValidate(Connection connection)
	{
		LOG.debug("afterValidate");
	}
	
	@Override
	public void beforeBaseline(Connection connection)
	{
		LOG.debug("beforeBaseline");
	}
	
	@Override
	public void afterBaseline(Connection connection)
	{
		LOG.debug("afterBaseline");
	}
	
	@Override
	public void beforeRepair(Connection connection)
	{
		LOG.debug("beforeRepair");
	}
	
	@Override
	public void afterRepair(Connection connection)
	{
		LOG.debug("afterRepair");
	}
	
	@Override
	public void beforeInfo(Connection connection)
	{
		LOG.debug("beforeInfo");
	}
	
	@Override
	public void afterInfo(Connection connection)
	{
		LOG.debug("afterInfo");
	}
	
}
