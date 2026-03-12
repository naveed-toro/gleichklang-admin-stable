package de.binaerebauten.gleichklang.core.migration;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.Set;

public class V01_00_02__ChangeCollation implements SpringJdbcMigration
{
	private static final Logger LOG = LoggerFactory.getLogger(V01_00_02__ChangeCollation.class);

	private final String[] TABLES_FOR_CHARSET_CHANGE = new String[]
			{
					"compmessage",
					"compadjektive",
					"compaussehen",
					"compext_adjektive",
					"compext_freundschaft",
					"compext_gesellschaft",
					"compext_hobby_e",
					"compext_kritische_lebensereignisse",
					"compext_partnerschaft",
					"compext_persoenlichkeit",
					"compext_stoerbar",
					"compext_stoerbarkeit",
					"compfreund",
					"compfreundschaft",
					"compftext",
					"compgesellschaft",
					"comphobby_e",
					"comphobby_v",
					"compkritische_lebensereignisse",
					"comppartner",
					"comppartnerschaft",
					"comppersoenlichkeit",
					"compperson",
					"compffoto",
					"comppfoto",
					"compptext",
					"comppayment_offer",
					"comppayment_transaction",
					"compstoerbar",
					"compvorschlag" // this is the biggest table, but our migrations run slow when this table isn't converted
			};

	private final static String CHAR_SET = "utf8";

	private final static String COLLATION = "utf8_unicode_ci";

	private Joiner joiner = Joiner.on(" ");

	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		changeDatabaseDefault(jdbcTemplate);

		convertTables(jdbcTemplate);
	}

	/**
	 * Changes the database default charset and collation
	 */
	private void changeDatabaseDefault(JdbcTemplate jdbcTemplate)
			throws SQLException
	{
		final String alterDatabase = Joiner.on(" ")
				.join("ALTER DATABASE", "DEFAULT CHARACTER SET", CHAR_SET, "COLLATE", COLLATION, ";");

		jdbcTemplate.execute(alterDatabase);
		LOG.info("Changed database charset to '{}' and collation to '{}'",
				CHAR_SET, COLLATION);
	}

	private void convertTables(JdbcTemplate jdbcTemplate)
	{
		final Set<String> existingTables = Sets.newHashSet(TABLES_FOR_CHARSET_CHANGE);
		existingTables.retainAll(jdbcTemplate.queryForList("SHOW TABLES", String.class));

		existingTables.forEach(table -> convertTable(jdbcTemplate, table));
	}

	private void convertTable(JdbcTemplate jdbcTemplate, String table)
	{
		String convertTable = joiner.
				join("ALTER TABLE", table, "CONVERT TO CHARACTER SET", CHAR_SET, "COLLATE", COLLATION, ";");
		jdbcTemplate.execute(convertTable);

		LOG.info("Convert table '{}' charset to '{}' and collation to '{}'", table, CHAR_SET, COLLATION);
	}
}
