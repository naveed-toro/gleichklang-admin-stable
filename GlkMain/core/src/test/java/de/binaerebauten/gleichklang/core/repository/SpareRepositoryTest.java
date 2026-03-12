package de.binaerebauten.gleichklang.core.repository;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SpareRepositoryTest extends BasePersistenceTest
{
	private static final String USER_ALIAS = "test";
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	private User user;

	@Before
	public void setup()
	{
		user = defaultEntityFactory.persistDefaultUser(USER_ALIAS);
	}

	@After
	public void tearDown()
	{
		defaultEntityFactory.reset();
	}

	@Test
	public void testFindAllWithIdsOnly()
	{
		final Collection<Long> userIds = userRepository.findAllWithIdsOnly();
		
		assertThat(userIds.size(), equalTo(1));
		assertThat(userIds.iterator().next(), equalTo(user.getId()));
	}
	
	@Test
	public void testFindAllWithIdsOnlyWithSpec()
	{
		Collection<Long> userIds;
		
		final Specification<User> hitSpec = (root, query, cb) -> cb.equal(root.get(User_.alias), USER_ALIAS);
		final Specification<User> failSpec = (root, query, cb) -> cb.equal(root.get(User_.alias), USER_ALIAS + "fail");
		
		userIds = userRepository.findAllWithIdsOnly(hitSpec);
		assertThat(userIds.size(), equalTo(1));
		assertThat(userIds.iterator().next(), equalTo(user.getId()));
		
		userIds = userRepository.findAllWithIdsOnly(failSpec);
		assertThat(userIds.size(), equalTo(0));
	}
}
