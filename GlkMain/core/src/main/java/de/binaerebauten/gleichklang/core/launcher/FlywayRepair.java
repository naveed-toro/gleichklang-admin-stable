package de.binaerebauten.gleichklang.core.launcher;

import de.binaerebauten.gleichklang.core.config.PersistenceConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FlywayRepair
{
	private static final String APPLICATION_PROPERTIES = "/application.properties";
//	private static final String APPLICATION_PROPERTIES = "/application-qa.properties";

	private static final String JDBC_URL_PROPERTY = "db.jdbc.url";
	private static final String DB_USER = "db.user";
	private static final String DB_PASSWORD = "db.password";
	private static final String FILE_PATH ="file.path";

	public static void main(String[] args) throws IOException
	{
		final Properties properties = loadProperties();

		String dbJdbcUrl = properties.getProperty(JDBC_URL_PROPERTY);
		String dbUser = properties.getProperty(DB_USER);
		String dbPassword = properties.getProperty(DB_PASSWORD);
		String filePath = properties.getProperty(FILE_PATH);

		PersistenceConfig.repairFlyway(dbJdbcUrl, dbUser, dbPassword, filePath);
	}

	private static Properties loadProperties() throws IOException
	{
		final Properties properties = new Properties();
		try (InputStream is = FlywayRepair.class.getResourceAsStream(APPLICATION_PROPERTIES))
		{
			properties.load(is);
		}
		return properties;
	}
}
