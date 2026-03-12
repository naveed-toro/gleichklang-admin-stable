package de.binaerebauten.gleichklang.core.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class V01_78__Create_Relationship implements SpringJdbcMigration
{

	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		final SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SHOW FIELDS FROM match_ where Field ='strictness';");
		if (rowSet.next())
		{
			final String strictnessType = rowSet.getString("Type");

			if (strictnessType.startsWith("int")) return;

			jdbcTemplate.execute("CALL ADD_COLUMN('match_', 'strictness_new', 'INT');");

			jdbcTemplate.execute("UPDATE match_ SET strictness_new = 0 WHERE strictness = '_1';");
			jdbcTemplate.execute("UPDATE match_ SET strictness_new = 1 WHERE strictness = '_2';");
			jdbcTemplate.execute("UPDATE match_ SET strictness_new = 2 WHERE strictness = '_3';");
			jdbcTemplate.execute("UPDATE match_ SET strictness_new = 3 WHERE strictness = '_4';");
			jdbcTemplate.execute("UPDATE match_ SET strictness_new = 4 WHERE strictness = 'EXCLUSION';");
			jdbcTemplate.execute("UPDATE match_ SET strictness_new = 4 WHERE strictness_new IS NULL;");

			jdbcTemplate.execute("CALL DROP_COLUMN ('match_', 'strictness');");
		}

		jdbcTemplate.execute("ALTER TABLE match_ CHANGE strictness_new strictness INT NOT NULL");

		jdbcTemplate.execute(createRelationship());
	}

	private String createRelationship()
	{
		String createRelationship = "";

		createRelationship += "CREATE TABLE IF NOT EXISTS relationship ";
		createRelationship += "( ";
		createRelationship += "id                       BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, ";
		createRelationship += "legacy_id                VARCHAR(255), ";
		createRelationship += "change_date              DATETIME, ";
		createRelationship += "create_date              DATETIME, ";
		createRelationship += "source_user_id           BIGINT NOT NULL, ";
		createRelationship += "target_user_id           BIGINT NOT NULL, ";
		createRelationship += "affiliation              VARCHAR(255) NOT NULL, ";
		createRelationship += "category                 VARCHAR(255) NOT NULL, ";
		createRelationship += "FOREIGN KEY (source_user_id)   REFERENCES user_ (id), ";
		createRelationship += "FOREIGN KEY (target_user_id)   REFERENCES user_ (id) ";
		createRelationship += ");";

		return createRelationship;
	}
}
