package de.binaerebauten.gleichklang.core.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by michael on 12/06/15.
 */
public class V01_35__Insert_ChoiceAnswers implements SpringJdbcMigration
{
	private static final Logger LOG = LoggerFactory.getLogger(V01_35__Insert_ChoiceAnswers.class);

	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		LOG.info("Skip V01_35__Insert_ChoiceAnswers");
	}


}
