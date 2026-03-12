package de.binaerebauten.gleichklang.core.migration;

import com.google.common.base.Joiner;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Fixes user pseudonyms which are duplicated after changing the table charset
 * to utf8 and collation to utf8_unicode.
 */
public class V01_00_01__Fix_NonUnique_Pseudonyms implements SpringJdbcMigration
{
	/**
	 * Helper class to store an user with his pseudonym.
	 * <p>
	 * Implements {@link Comparable} so that users are ordered like mysql
	 * utf8_unicode_ci colation.
	 */
	private static class User implements Comparable<User>
	{
		private final String no;
		private final String pseudonym;
		private final String collationKey;

		public User(ResultSet rs) throws SQLException
		{
			no = rs.getString(NO_COLUMN);
			pseudonym = rs.getString(PSEUDONYM_COLUMN);

			collationKey = createCollationKey(pseudonym);

			Objects.requireNonNull(no, "no == null");
			Objects.requireNonNull(pseudonym, "pseudonym == null");
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final User user = (User) o;

			return no.equals(user.no);

		}

		@Override
		public int hashCode()
		{
			return no.hashCode();
		}

		@Override
		public int compareTo(User o)
		{
			return collationKey.compareTo(o.collationKey);
		}
	}

	/* not used yet */
	private static final Collator DIN_5007_1_COLLATOR;
	private static final Logger LOG = LoggerFactory.getLogger(V01_00_01__Fix_NonUnique_Pseudonyms.class);

	/**
	 * New table to stored data fixed during migration.
	 */
	private static final String MIGRATION_FIXED_DATA_TABLE = "migration_fixed_data";
	private static final String COMPUSER_TABLE = "compuser";
	private static final String NO_COLUMN = "no";
	private static final String PSEUDONYM_COLUMN = "pseudonym";
	private static final String COMPUSER_PSEUDONYM_INDEX = "compuser_pseudonym";
	private static final String CHAR_SET = "utf8";
	private static final String COLLATION = "utf8_unicode_ci";

	static
	{
		DIN_5007_1_COLLATOR = Collator.getInstance(Locale.ROOT);
		DIN_5007_1_COLLATOR.setStrength(Collator.PRIMARY);
	}

	private final Joiner spacer = Joiner.on(" ");

	private static String createCollationKey(String value)
	{
		return value.toLowerCase(Locale.GERMAN).replace('ä', 'a').replace('ö', 'o').replace('ü', 'u').replace('²', '2').replace('³', '3').replace("ß", "ss");
	}

	@Override
	public void migrate(JdbcTemplate jdbcTemplate) throws Exception
	{
		createFixedDataTable(jdbcTemplate);
		dropIndex(jdbcTemplate);
		convertTable(jdbcTemplate);

		final List<User> usersSortedByPseudonym = findUsersSortedByPseudonym(jdbcTemplate);
		final Map<User, String> usersNewPseudonym = generateNewPseudonyms(usersSortedByPseudonym);

		usersNewPseudonym.entrySet().forEach(e -> fixUser(jdbcTemplate, e.getKey(), e.getValue()));

		createIndex(jdbcTemplate);
	}

	/**
	 * Creates the {@link V01_00_01__Fix_NonUnique_Pseudonyms#MIGRATION_FIXED_DATA_TABLE}
	 * table.
	 */
	private void createFixedDataTable(JdbcTemplate jdbcTemplate)
	{
		final StringJoiner columnDefinitions = new StringJoiner(",", "(", ")")
				.add("id          BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL")
				.add("legacy_id   VARCHAR(255) NOT NULL")
				.add("change_date DATETIME")
				.add("create_date DATETIME")
				.add("table_name  VARCHAR(255) NOT NULL")
				.add("column_name VARCHAR(255) NOT NULL")
				.add("old_value   VARCHAR(255) NOT NULL")
				.add("new_value   VARCHAR(255) NOT NULL");

		final String createTable = spacer.join("CREATE TABLE IF NOT EXISTS", MIGRATION_FIXED_DATA_TABLE, columnDefinitions, ";");
		jdbcTemplate.execute(createTable);

		LOG.info("Created table '{}'", MIGRATION_FIXED_DATA_TABLE);
	}

	/**
	 * Drops the {@link V01_00_01__Fix_NonUnique_Pseudonyms#COMPUSER_PSEUDONYM_INDEX}
	 * index.
	 */
	private void dropIndex(JdbcTemplate jdbcTemplate)
	{
		final String dropIndex = spacer.join("DROP INDEX", COMPUSER_PSEUDONYM_INDEX, "ON", COMPUSER_TABLE, ";");
		jdbcTemplate.execute(dropIndex);

		LOG.info("Dropped index '{}' on '{}'", COMPUSER_PSEUDONYM_INDEX, COMPUSER_TABLE);
	}

	/**
	 * Converts the given table charset to {@link V01_00_01__Fix_NonUnique_Pseudonyms#CHAR_SET}
	 * and collation to {@link V01_00_01__Fix_NonUnique_Pseudonyms#COLLATION}.
	 */
	private void convertTable(JdbcTemplate jdbcTemplate)
	{
		final String convertTable = spacer.join("ALTER TABLE", COMPUSER_TABLE, "CONVERT TO CHARACTER SET", CHAR_SET, "COLLATE", COLLATION, ";");
		jdbcTemplate.execute(convertTable);

		LOG.info("Convert table '{}' charset to '{}' and collation to '{}'", V01_00_01__Fix_NonUnique_Pseudonyms.COMPUSER_TABLE, CHAR_SET, COLLATION);
	}

	private List<User> findUsersSortedByPseudonym(JdbcTemplate jdbcTemplate)
	{
		final String columnDef = Joiner.on(", ").join(NO_COLUMN, PSEUDONYM_COLUMN);
		final String findUsers = spacer.join("SELECT", columnDef, "FROM", COMPUSER_TABLE, "WHERE type = 'teilnehmer' OR type = 'exteilnehmer' ORDER BY", PSEUDONYM_COLUMN, ";");

		return jdbcTemplate.query(findUsers, (rs, rowNum) -> new User(rs));
	}

	/**
	 * Iterates over the given sorted set of users and creates new pseudonyms
	 * for users with duplicate pseudonyms.
	 *
	 * @param usersSortedByPseudonym all users sorted with mysql utf8_unicode_ci
	 *                               collation
	 * @return non-null map that contains a new pseudonym for all users with
	 * duplicate pseudonyms
	 */
	private Map<User, String> generateNewPseudonyms(List<User> usersSortedByPseudonym)
	{
		final Map<User, String> usersNewPseudonym = new HashMap<>();
		final Set<String> uniquePseudonyms = usersSortedByPseudonym.stream().map(u -> u.collationKey).collect(Collectors.toSet());

		User previous = null;

		for (User user : usersSortedByPseudonym)
		{
			if (previous != null && previous.compareTo(user) == 0)
			{
				final String newPseudonym = generateNewPseudonym(user, uniquePseudonyms);
				usersNewPseudonym.put(user, newPseudonym);
			}
			previous = user;
		}
		return usersNewPseudonym;
	}

	private String generateNewPseudonym(User user, Set<String> uniquePseudonyms)
	{
		int index = 0;

		while (true)
		{
			final String newPseudonym = user.pseudonym + "" + index;
			final String collationKey = createCollationKey(newPseudonym);

			if (!uniquePseudonyms.contains(collationKey)) return newPseudonym;
			index++;
		}
	}

	/**
	 * Changes the given users pseudonym and stores the old and new pseudonym
	 * value in {@link V01_00_01__Fix_NonUnique_Pseudonyms#MIGRATION_FIXED_DATA_TABLE}.
	 */
	private void fixUser(JdbcTemplate jdbcTemplate, User user, String newPseudonym)
	{
		updatePseudonym(jdbcTemplate, user, newPseudonym);
		insertIntoFixedData(jdbcTemplate, user, newPseudonym);
	}

	private void updatePseudonym(JdbcTemplate jdbcTemplate, User user, String newPseudonym)
	{
		final String updatePseudonym = spacer.join(
				"UPDATE",
				COMPUSER_TABLE,
				"SET",
				PSEUDONYM_COLUMN + " = '" + newPseudonym + "'",
				"WHERE",
				NO_COLUMN + "= '" + user.no + "'",
				";");
		jdbcTemplate.update(updatePseudonym);

		LOG.info("Updated user pseudonym from '{}' to '{}'", user.pseudonym, newPseudonym);
	}

	private void insertIntoFixedData(JdbcTemplate jdbcTemplate, User user, String newPseudonym)
	{
		final String setValues = new StringJoiner(",\n")
				.add("legacy_id = '" + user.no + "'")
				.add("create_date = CURDATE()")
				.add("table_name = '" + COMPUSER_TABLE + "'")
				.add("column_name = '" + PSEUDONYM_COLUMN + "'")
				.add("old_value = '" + user.pseudonym + "'")
				.add("new_value = '" + newPseudonym + "'")
				.toString();

		final String insertFixedData = spacer.join("INSERT", MIGRATION_FIXED_DATA_TABLE, "\nSET", setValues, ";");
		jdbcTemplate.execute(insertFixedData);

		LOG.info("Inserted user pseudonym old '{}', new '{}' into table '{}'", user.pseudonym, newPseudonym, MIGRATION_FIXED_DATA_TABLE);
	}

	/**
	 * Recreates the {@link V01_00_01__Fix_NonUnique_Pseudonyms#COMPUSER_PSEUDONYM_INDEX}
	 * index.
	 */
	private void createIndex(JdbcTemplate jdbcTemplate)
	{
		final String createIndex = spacer.join("CREATE", "UNIQUE", "INDEX", COMPUSER_PSEUDONYM_INDEX, "ON", COMPUSER_TABLE, "(", PSEUDONYM_COLUMN, ");");
		jdbcTemplate.execute(createIndex);

		LOG.info("Created index '{}' on table '{}'", COMPUSER_PSEUDONYM_INDEX, COMPUSER_TABLE);
	}
}
