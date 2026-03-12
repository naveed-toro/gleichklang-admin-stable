package de.binaerebauten.gleichklang.core;

import de.binaerebauten.gleichklang.core.config.PropertySourcesConfiguration;
import de.binaerebauten.gleichklang.core.monitoring.BuildInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * This integration test performs some basic sanity checks.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BaseSanityCheckIT.Config.class)
public abstract class BaseSanityCheckIT
{
	@Configuration
	@Import(PropertySourcesConfiguration.class)
	@PropertySource("classpath:/build.properties")
	public static class Config
	{
		@Bean
		public static RestOperations restOperations()
		{
			return new RestTemplate();
		}
	}

	@Value("${build.time}")
	private String buildTime;

	@Value("${build.version}")
	private String buildVersion;

	private String baseUrl;

	private static String BUILD_INFO_ENDPOINT = "/api/build";

	@Autowired
	private RestOperations restOperations;

	@PostConstruct
	public void init()
	{
		baseUrl = System.getProperty("test.base.url", "http://localhost:8080/Gleichklang");
	}

	@Test
	public void testIsAlive()
	{
		String buildInfoUrl = baseUrl + BUILD_INFO_ENDPOINT;
		ResponseEntity<BuildInfo> buildInfoResponse = restOperations.getForEntity(buildInfoUrl, BuildInfo.class);

		assertThat(
				String.format("Expected that build info endpoint %s is accessible", buildInfoUrl),
				buildInfoResponse.getStatusCode(), equalTo(HttpStatus.OK));

		BuildInfo buildInfo = buildInfoResponse.getBody();

		assertThat(buildInfo, notNullValue());
		assertThat(buildInfo.getBuildTime(), equalTo(buildTime));
		assertThat(buildInfo.getBuildVersion(), equalTo(buildVersion));
	}
}
