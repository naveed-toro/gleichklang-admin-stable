package de.binaerebauten.gleichklang.core.utils;

import de.binaerebauten.gleichklang.core.utils.ParametersHolder.ParameterKey;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link AppUrlBuilder}
 */
public class AppUrlBuilderTest
{
	private final static String BASE_URL = "http://localhost:8080/";

	private AppUrlBuilder appUrlBuilder;

	@Before
	public void setup()
	{
		appUrlBuilder = new AppUrlBuilder(URI.create(BASE_URL));
	}

	//@Test
	public void testToApi() throws UnsupportedEncodingException
	{
		assertThat(appUrlBuilder.toApi("validate").toString(),
				equalTo(BASE_URL + "api/link?token=validate"));
	}

	@Test
	public void testToVaadinFragment()
	{
		assertThat(appUrlBuilder.toVaadinFragment("View").toString(),
				equalTo(BASE_URL + "#View"));
	}
	
	@Test
	public void testToAppPath()
	{
		assertThat(appUrlBuilder.toAppPath().toString(), equalTo(BASE_URL));
		
		final String path = "path";
		final ParameterKey parameter1 = ParameterKey.EMAIL;
		final ParameterKey parameter2 = ParameterKey.MESSAGE;
		final String value1 = "value1";
		final ParametersHolder parametersHolder1 = new ParametersHolder();
		parametersHolder1.addParameter(parameter1, value1);
		final ParametersHolder parametersHolder2 = new ParametersHolder();
		parametersHolder2.addParameter(parameter2);
		
		assertThat(appUrlBuilder.toAppPath(path).toString(), equalTo(BASE_URL + path));
		assertThat(appUrlBuilder.toAppPath(parametersHolder1).toString(), equalTo(BASE_URL + "?email=value1"));
		assertThat(appUrlBuilder.toAppPath(parametersHolder2).toString(), equalTo(BASE_URL + "?message"));
		assertThat(appUrlBuilder.toAppPath(path, parametersHolder1).toString(), equalTo(BASE_URL + path + "?email=value1"));
	}
}
