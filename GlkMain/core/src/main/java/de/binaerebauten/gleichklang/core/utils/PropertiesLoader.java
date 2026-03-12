package de.binaerebauten.gleichklang.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

/**
 * Created by michael on 05/05/15.
 */
public class PropertiesLoader
{
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesLoader.class);
	public static Properties getProperties(String resourceName)
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();

		try (InputStream inputStream = loader.getResourceAsStream(resourceName))
		{
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			properties.load(reader);
		}
		catch (IOException e)
		{
			LOG.error("Properties {} konnten nicht geladen werden", resourceName, e);
		}
		return properties;
	}
}
