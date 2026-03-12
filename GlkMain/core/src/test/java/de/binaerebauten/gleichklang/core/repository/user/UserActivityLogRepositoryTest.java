package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.UserActivityLog;
import de.binaerebauten.gleichklang.core.model.user.UserActivityLog.UserActivity;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

public class UserActivityLogRepositoryTest extends AbstractRepositoryTest<UserActivityLog>
{
	@Autowired
	private UserActivityLogRepository userActivityLogRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;

	public UserActivityLogRepositoryTest()
	{
	}

	@Override
	protected Collection<UserActivityLog> getPersistedEntities()
	{
		return Collections.singletonList(entityFactory.persistDefaultUserActivityLog(entityFactory.persistDefaultUser("test"), UserActivity.NEW_MATCH));
	}

	@Override
	protected JpaRepository<UserActivityLog, Long> getRepository()
	{
		return userActivityLogRepository;
	}
}
