package de.binaerebauten.gleichklang.core.model;

import de.binaerebauten.gleichklang.core.config.MigrationPersistenceTestConfig;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests that the status of the migrations is success.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MigrationPersistenceTestConfig.class })
public class CheckMigrationTest
{
	/**
	 * Max duration of all migrations (in hours)
	 */
	private static final int MAX_DURATION = 30;
	
	@Autowired
	private Flyway flyway;

	@Test
	public void testMigrationInfoState()
	{
		List<MigrationInfo> migrationInfos = Arrays.asList(flyway.info().all());

		for (MigrationInfo migrationInfo : migrationInfos)
		{
			assertThat(migrationInfo.getState(),
					anyOf(equalTo(MigrationState.SUCCESS), equalTo(MigrationState.BELOW_BASELINE)));
		}
		long totalExecutionTimeInMs = migrationInfos.stream()
				.filter(m -> m.getExecutionTime() != null)
				.mapToInt(MigrationInfo::getExecutionTime)
				.sum() / 1000;

		// Commented out because of hardware differences between test server and production,
		// which implies a bigger time to run all migrations on the test server.
		// assertThat(totalExecutionTimeInMs, lessThan(Duration.ofHours(MAX_DURATION).getSeconds()));
	}
}
