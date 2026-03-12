package de.binaerebauten.gleichklang.core;

import de.binaerebauten.gleichklang.core.utils.DefaultI18N;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.util.*;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

/**
 * Abstract base test class for I18N tests.
 * Checks that a message exists for each value of an I18N enum class.
 */
public abstract class BaseI18NTest<E extends DefaultI18N>
{
	public abstract Class<E> getI18NClass();

	/**
	 * The error collector is used so that all missing resource exceptions are collected for a test run.
	 * This avoids rerunning the test after fixing a missing resource exception.
	 */
	@Rule
	public ErrorCollector errorCollector = new ErrorCollector();

	private Set<String> enumValues = new HashSet<>();

	@Before
	public void setup()
	{
		E[] enumConstants = getI18NClass().getEnumConstants();

		for (E enumConstant : enumConstants)
		{
			enumValues.add(enumConstant.getKey());
		}
	}

	//@Test
	public void testMsgExists()
	{
		for (E enumValue : getI18NClass().getEnumConstants())
		{
			try
			{
				assertNotNull("Expected that a msg is available for: " + enumValue, enumValue.msg());
			}
			catch (MissingResourceException m)
			{
				errorCollector.addError(m);
			}
		}
	}

	//@Test
	public void testUnusedResourceBundleKeys()
	{
		ResourceBundle resourceBundle = getI18NClass().getEnumConstants()[0].getBundle();
		Collection<String> bundleKeys = Collections.list(resourceBundle.getKeys());

		for (String bundleKey : bundleKeys)
		{
			errorCollector.checkThat(
					String.format("Resource bundle '%s' contains unused key '%s'",
							resourceBundle.getBaseBundleName(), bundleKey),
					enumValues, hasItem(bundleKey));
		}
	}
}
