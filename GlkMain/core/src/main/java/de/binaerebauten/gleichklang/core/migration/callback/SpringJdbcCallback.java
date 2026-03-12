package de.binaerebauten.gleichklang.core.migration.callback;

import org.springframework.jdbc.core.JdbcTemplate;

public interface SpringJdbcCallback
{
	void execute(JdbcTemplate jdbcTemplate, String databaseName) throws Exception;
}
