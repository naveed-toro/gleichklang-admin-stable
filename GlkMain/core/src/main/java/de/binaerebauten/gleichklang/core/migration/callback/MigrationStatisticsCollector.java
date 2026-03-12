package de.binaerebauten.gleichklang.core.migration.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.List;
import java.util.Map;

public class MigrationStatisticsCollector
{
	private final String databaseName;

	MigrationStatisticsCollector(String databaseName){
		this.databaseName = databaseName;
	}


	public void beforeMigrate(JdbcTemplate jdbcTemplate, String regexp){
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS stat_history (TABLE_NAME  VARCHAR(255), " + " TABLE_ROWS INT(11), change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);");
		printTableStat(jdbcTemplate, regexp);
	}

	public void afterMigrate(JdbcTemplate jdbcTemplate, String regexp){
		printTableStat(jdbcTemplate, regexp);
		printExecutionTime(jdbcTemplate);
		jdbcTemplate.execute(String.format(
				"INSERT INTO stat_history (TABLE_NAME, TABLE_ROWS) "
						+ "SELECT TABLE_NAME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME NOT REGEXP '^comp' "
						+ "ORDER BY table_rows DESC", databaseName));
		printSlowestMigrationsStat(jdbcTemplate);
	}


	private static final Logger LOG = LoggerFactory.getLogger(MigrationStatisticsCollector.class);

	private void printTableStat(JdbcTemplate jdbcTemplate, String regexp)
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("\nPrinting statistics for %s %s\n", databaseName, regexp));
		SqlRowSet result = jdbcTemplate.queryForRowSet(
				String.format("SELECT TABLE_NAME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES "
								+ "WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME %s "
								+ "ORDER BY table_rows DESC",
						databaseName, regexp));

		while (result.next()) {
			String str = String.format("%-50s%-50s\n", result.getString(1), result.getString(2));
			stringBuilder.append(str);
		}

		LOG.info(stringBuilder.toString());
	}

	private void printExecutionTime(JdbcTemplate jdbcTemplate){
		List<Integer> executionTime = jdbcTemplate.queryForList("SELECT SUM(execution_time) FROM schema_version",
				Integer.class);
		LOG.info("Migration execution time {} ms", executionTime.get(0));
	}
	
	private void printSlowestMigrationsStat(JdbcTemplate jdbcTemplate){
		List<Map<String, Object>> statResults = jdbcTemplate.queryForList("SELECT CONCAT_WS(' ', version, description) as migration_name, execution_time FROM schema_version ORDER BY execution_time DESC LIMIT 10");
		int idx = 1;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\nPrinting slowest migrations stat: \n");
		for(Map<String, Object> statResult : statResults){
			String migrationName = (String) statResult.get("migration_name");
			Integer executionTime = (Integer) statResult.get("execution_time");
			String line = String.format("%-10s%-50s%-50s\n", idx, migrationName, executionTime + " ms");
			stringBuilder.append(line);
			idx++;
		}
		LOG.info(stringBuilder.toString());
	}
	

}
