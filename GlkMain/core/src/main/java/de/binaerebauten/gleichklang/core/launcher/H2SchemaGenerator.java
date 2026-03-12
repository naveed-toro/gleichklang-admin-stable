package de.binaerebauten.gleichklang.core.launcher;

import de.binaerebauten.gleichklang.core.config.PersistenceConfig;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Driver;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class creates a H2 ddl sql script with all constraints from a
 * temporarily created mysql database that reflects the minimal database model
 * without any legacy tables.
 */
public class H2SchemaGenerator
{
	protected static final String TABLE_SEPARATOR = "$";
	private static final String JDBC_URL_PROPERTY = "db.jdbc.url";
	private static final Logger LOG = LoggerFactory.getLogger(H2SchemaGenerator.class);

	/**
	 * Starts the schema generator from the command line.
	 *
	 * @param args the first argument must specify the location of ddl script to
	 *             generate.
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception
	{
		if (args.length != 2)
		{
			LOG.info("Missing arguments: <export.properties> <output_file>");
			System.exit(-1);
		}
		final String propertiesLocation = args[0];
		final String schemaFile = args[1];
		
		final Properties properties = loadProperties(propertiesLocation);
		
		final String databaseSchema = properties.getProperty("db.database");
		
		createH2SchemaExportDb(properties, databaseSchema);
		
		exportH2Schema(properties, databaseSchema, schemaFile);
	}
	
	/**
	 * Starts the flyway migration the export database configuration and then
	 * creates the test ddl script from this database.
	 */
	private static void exportH2Schema(Properties properties, String databaseSchema, String schemaFile)
			throws IOException
	{
		String dbJdbcUrl = properties.getProperty(JDBC_URL_PROPERTY);
		String dbUser = properties.getProperty("db.user");
		String dbPassword = properties.getProperty("db.password");
		String filePath = properties.getProperty("file.path");

		Flyway flyway = PersistenceConfig.createFlyway(dbJdbcUrl, dbUser, dbPassword, filePath);
		flyway.setCleanOnValidationError(true);
		try
		{
			flyway.migrate();
		}
		catch (FlywayException e)
		{
			LOG.error("Migration failed with error", e);
			LOG.info("Info first migration attempt failed, recreating database and retry migration");

			recreateDatabaseIfNotExists(properties, databaseSchema);
			flyway.migrate();
		}

		DataSource dataSource = createDataSource(properties, JDBC_URL_PROPERTY);

		final List<String> createTableStatements = getCreateTableStatements(dataSource, databaseSchema);
		
		final String createScript = new MySqlH2Converter().convert(createTableStatements);
		Files.write(Paths.get(schemaFile), createScript.getBytes(StandardCharsets.UTF_8));

		final String dropTableScript = "SET FOREIGN_KEY_CHECKS = 0;\n\n" +
				getDropTableStatements(dataSource, databaseSchema) +
				"\nSET FOREIGN_KEY_CHECKS = 1;";
		Files.write(Paths.get(schemaFile).resolveSibling("mysql-drop-hibernate-tables.sql"),
				dropTableScript.getBytes(StandardCharsets.UTF_8));

		LOG.info("Exported database schema {} to {}", databaseSchema, schemaFile);
	}
	
	private static void createH2SchemaExportDb(Properties properties, String databaseSchema)
	{
		final DataSource dataSource = createDataSource(properties, "jdbc.url");
		
		final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		createDatabaseIfNotExists(databaseSchema, jdbcTemplate);
	}

	private static void recreateDatabaseIfNotExists(Properties properties, String databaseSchema)
	{
		final DataSource dataSource = createDataSource(properties, "jdbc.url");

		final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.execute("DROP DATABASE IF EXISTS " + databaseSchema);
		jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS " + databaseSchema);
	}

	private static void createDatabaseIfNotExists(String databaseSchema, JdbcTemplate jdbcTemplate)
	{
		jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS " + databaseSchema);
	}
	
	private static Properties loadProperties(String propertiesLocation) throws IOException
	{
		final Properties properties = new Properties();
		try (InputStream is = H2SchemaGenerator.class.getResourceAsStream(propertiesLocation))
		{
			properties.load(is);
		}
		return properties;
	}

	private static DataSource createDataSource(Properties properties, String jdbcUrlProperty)
	{
		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setUrl(properties.getProperty(jdbcUrlProperty));

		dataSource.setUsername(properties.getProperty("db.user"));
		dataSource.setPassword(properties.getProperty("db.password"));
		dataSource.setDriverClassName(Driver.class.getName());
		return dataSource;
	}

	private static Collection<String> getTableNames(DataSource dataSource, String databaseSchema)
	{
		final JdbcOperations jdbcTemplate = new JdbcTemplate(dataSource);
		
		return jdbcTemplate.queryForList("SELECT TABLE_NAME "
				+ "FROM INFORMATION_SCHEMA.TABLES "
				+ "WHERE TABLE_SCHEMA =  '" + databaseSchema + "'; ", String.class);
	}
	
	private static String getCreateTableQuery(DataSource dataSource, String table)
	{
		final JdbcOperations jdbcTemplate = new JdbcTemplate(dataSource);
		final List<Map<String, Object>> result = jdbcTemplate.queryForList("SHOW CREATE TABLE " + table + " ; ");
		return String.valueOf(result.get(0).get("Create Table")).toLowerCase() + TABLE_SEPARATOR;
	}

	private static String getDropTableStatements(final DataSource dataSource, String databaseSchema)
	{
		Collection<String> tableNames = getTableNames(dataSource, databaseSchema);

		// list of intermeditae tables make the generated table drop sql script more reliable when our migration tests fail
		List<String> intermediateTablesToDrop = Arrays.asList("offer", "offer_search_domain", "offer_payment_option", "payment_event", "user_roles");
		tableNames.addAll(intermediateTablesToDrop);
		List<String> excludedTables = Arrays.asList("query_performance");
		tableNames.removeAll(excludedTables);

		String dropTableStatements = tableNames.stream()
				.sorted()
				.filter(table -> !table.startsWith("comp"))
				.map(table -> String.format("DROP TABLE IF EXISTS %s;\n", table))
				.collect(Collectors.joining());
		return dropTableStatements;
	}


	private static List<String> getCreateTableStatements(final DataSource dataSource, String databaseSchema)
	{
		return getTableNames(dataSource, databaseSchema).stream()
				.sorted()
				.filter(table -> !table.startsWith("comp"))
				.map(table -> getCreateTableQuery(dataSource, table))
				.collect(Collectors.toList());
	}

	private static String normaliseLine(String line)
	{
		line = line.trim().toLowerCase();
		if (line.endsWith(","))
		{
			StringBuilder stringBuilder = new StringBuilder(line);
			int lastIndexOfComa = line.lastIndexOf(",");
			stringBuilder.deleteCharAt(lastIndexOfComa);
			line = stringBuilder.toString();
		}
		return line;
	}

	/**
	 * Created by michael on 27/04/15.
	 */
	public static class MySqlH2Converter
	{
		private final Map<String, Set<String>> constraintStatementsMap = new HashMap<>();

		public String convert(Iterable<String> scripts)
		{
			final StringBuilder stringBuilder = new StringBuilder();

			for (final String script : scripts)
			{
				stringBuilder.append(convertScript(script));
			}
			stringBuilder.append(getConstraintsStatementsScript());
			return stringBuilder.toString();
		}

		private StringBuilder convertScript(String script)
		{
			StringBuilder stringBuilder = new StringBuilder();

			for (String statement : script.split(TABLE_SEPARATOR))
			{
				if (!statement.replaceAll("\n", "").trim().isEmpty())
				{
					stringBuilder.append(convertStatement(statement)).append("\n");
				}
			}
			return stringBuilder;
		}

		private StringBuilder convertStatement(String statement)
		{
			final StringJoiner stringJoiner = new StringJoiner(",\n");
			StringBuilder stringBuilder = new StringBuilder();

			String tableName = "";

			for (String line : statement.split("\n"))
			{
				line = normaliseLine(line);
				if (line.startsWith("create table"))
				{
					tableName = getTableName(line);
					LOG.info("Table found: " + tableName);
					stringBuilder.append(line).append("\n");
					continue;
				}
				else if (line.startsWith(") engine=") || line.startsWith("key "))
				{
					continue;
				}

				else if (line.startsWith("constraint"))
				{
					line = line.replaceAll(",", "");
					addConstraintStatement(tableName, line); // collate
					// utf8_unicode_ci
					continue;
				}
				else
				{
					line = changeBitRepresentation(line);
					line = changeTextRepresentation(line);
					line = changeDatetimeRepresentation(line);
					line = removeCollate(line);
				}
				stringJoiner.add(line);
			}
			stringBuilder.append(stringJoiner);
			stringBuilder = appendSuffix(stringBuilder, statement);
			return stringBuilder;
		}



		private StringBuilder appendSuffix(StringBuilder stringBuilder, String statement)
		{
			if (!statement.endsWith(");"))
			{
				stringBuilder.append(");\n");
			}
			return stringBuilder;
		}

		private Set<String> addConstraintStatement(String tableName, String line)
		{
			Set<String> constraintStatementLines = constraintStatementsMap.get(tableName);
			if (constraintStatementLines == null)
			{
				constraintStatementLines = new HashSet<>();
			}
			constraintStatementLines.add(line);
			return constraintStatementsMap.put(tableName, constraintStatementLines);
		}

		private String getConstraintsStatementsScript()
		{
			final List<String> constaintsStatements = new ArrayList<>();

			for (String tableName : constraintStatementsMap.keySet())
			{
				for (String statement : constraintStatementsMap.get(tableName))
				{
					statement = statement.replaceAll(",", "");
					final String constraintsStatement = "alter table " + tableName + " add " + statement + ";\n";
					constaintsStatements.add(constraintsStatement);
				}
			}

			return constaintsStatements.stream().sorted().collect(Collectors.joining());
		}

		private String changeBitRepresentation(String line)
		{
			return line.replaceAll("b'0'", "0").replaceAll("b'1'", "1");
		}

		private String changeDatetimeRepresentation(String line)
		{
			return line.replace(
					"timestamp not null default current_timestamp on update current_timestamp",
					"datetime default null");
		}

		private String removeCollate(String line)
		{

			final String collateRegex = " collate [\\w]*";
			return line.replaceFirst(collateRegex, "");
		}

		private String getTableName(String line)
		{
			final String regex = "(?i)(CREATE TABLE )(.+?)\\(";
			final Pattern pattern = Pattern.compile(regex);
			final Matcher matcher = pattern.matcher(line);
			if (matcher.find())
			{
				return matcher.group(2).trim().replaceAll("`", "");
			}
			return null;
		}

		private String changeTextRepresentation(String line)
		{
			return line.replace(" text", " clob");
		}
	}


}
