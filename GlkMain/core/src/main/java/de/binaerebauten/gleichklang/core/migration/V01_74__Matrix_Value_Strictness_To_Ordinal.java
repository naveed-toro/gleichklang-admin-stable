package de.binaerebauten.gleichklang.core.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class V01_74__Matrix_Value_Strictness_To_Ordinal implements SpringJdbcMigration
{

	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		final SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SHOW FIELDS FROM matching_matrix_value where Field ='strictness';");
		if (rowSet.next())
		{
			final String strictnessType = rowSet.getString("Type");

			if (strictnessType.startsWith("int")) return;

			jdbcTemplate.execute("CALL ADD_COLUMN('matching_matrix_value', 'strictness_new', 'INT');");

			jdbcTemplate.execute("UPDATE matching_matrix_value SET strictness_new = 0 WHERE strictness = '_1';");
			jdbcTemplate.execute("UPDATE matching_matrix_value SET strictness_new = 1 WHERE strictness = '_2';");
			jdbcTemplate.execute("UPDATE matching_matrix_value SET strictness_new = 2 WHERE strictness = '_3';");
			jdbcTemplate.execute("UPDATE matching_matrix_value SET strictness_new = 3 WHERE strictness = '_4';");
			jdbcTemplate.execute("UPDATE matching_matrix_value SET strictness_new = 4 WHERE strictness = 'EXCLUSION';");
			jdbcTemplate.execute("UPDATE matching_matrix_value SET strictness_new = 4 WHERE strictness_new IS NULL;");

			jdbcTemplate.execute("CALL DROP_COLUMN ('matching_matrix_value', 'strictness');");
		}

		jdbcTemplate.execute("ALTER TABLE matching_matrix_value CHANGE strictness_new strictness INT NOT NULL");
	}
}
