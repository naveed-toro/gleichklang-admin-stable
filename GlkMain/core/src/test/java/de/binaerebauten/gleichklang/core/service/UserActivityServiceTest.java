package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog.UserActivity;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.user.UserActivityLogRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class UserActivityServiceTest extends BasePersistenceTest
{
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Autowired
	private UserActivityService userActivityService;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Autowired
	private UserActivityLogRepository userActivityLogRepository;

	@After
	public void tearDown() throws Exception
	{
		defaultEntityFactory.reset();
	}
	
	@Test
	public void testCreateUserActivity()
	{
		final User user = defaultEntityFactory.persistDefaultUser("test");

		final List<UserActivity> userActivities = Arrays.stream(UserActivity.values()).filter(value -> !value.isWithCategory()).collect(Collectors.toList());

		assertThat(userActivityLogRepository.findAll().size(), equalTo(0));
		for(UserActivity userActivity : userActivities)
		{
			userActivityService.createActivity(user, userActivity);
		}
		assertThat(userActivityLogRepository.findAll().size(), equalTo(userActivities.size()));

		for(UserActivity userActivity : userActivities)
		{
			exception.expect(IllegalArgumentException.class);
			userActivityService.createActivity(user, userActivity, RecommendationCategory.PARTNERSHIP);
		}
	}

	@Test
	public void testCreateUserActivityWithCategory()
	{
		final User user = defaultEntityFactory.persistDefaultUser("test");

		final List<UserActivity> userActivities = Arrays.stream(UserActivity.values()).filter(UserActivity::isWithCategory).collect(Collectors.toList());

		assertThat(userActivityLogRepository.findAll().size(), equalTo(0));
		for(UserActivity userActivity : userActivities)
		{
			userActivityService.createActivity(user, userActivity, RecommendationCategory.PARTNERSHIP);
		}
		assertThat(userActivityLogRepository.findAll().size(), equalTo(userActivities.size()));

		for(UserActivity userActivity : userActivities)
		{
			exception.expect(IllegalArgumentException.class);
			userActivityService.createActivity(user, userActivity);
		}
	}
}