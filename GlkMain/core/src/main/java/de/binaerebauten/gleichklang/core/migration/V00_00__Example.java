package de.binaerebauten.gleichklang.core.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

public class V00_00__Example implements SpringJdbcMigration
{

	@Override public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{

	}
}
