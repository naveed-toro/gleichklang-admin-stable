package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.RegistrationState;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.UserRegistrationState;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;


public class UserRegistrationStateRepositoryTest extends AbstractRepositoryTest<UserRegistrationState>
{
	private static final String EMAIL = "email@example.com";

	@Autowired
	private UserRegistrationStateRepository userRegistrationStateRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	@Autowired
	private UserRepository userRepository;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static final Logger LOG = LoggerFactory.getLogger(UserRegistrationStateRepositoryTest.class);
	private User user;

	@Override
	protected Collection<UserRegistrationState> getPersistedEntities()
	{
		assertThat(userRepository.findByEmail(EMAIL), nullValue());

		user = defaultEntityFactory.persistDefaultUser(EMAIL, "alias12");
		UserRegistrationState registrationState = new UserRegistrationState();
		registrationState.setUser(user);
		registrationState.setRegistrationState(RegistrationState.CATEGORY);
		registrationState.setRecommendationCategory(RecommendationCategory.FRIENDSHIP);
		return Collections.singletonList(userRegistrationStateRepository.save(registrationState));
	}

	@Test
	public void testFindByUser() throws Exception
	{
		UserRegistrationState registrationState = userRegistrationStateRepository.findByUser(user);
		assertThat(registrationState.getRegistrationState(), equalTo(RegistrationState.CATEGORY));
	}
	
	@Test
	public void testDeleteByUser()
	{
		assertThat(userRegistrationStateRepository.count(), equalTo(1L));
		userRegistrationStateRepository.deleteByUser(user);
		assertThat(userRegistrationStateRepository.count(), equalTo(0L));
	}

	@Test
	public void testRegistrationStateUniqueness(){

		UserRegistrationState registrationState = new UserRegistrationState();
		registrationState.setRegistrationState(RegistrationState.MEMBERADDRESS);
		registrationState.setUser(user);

		thrown.expect(DataIntegrityViolationException.class);

		userRegistrationStateRepository.save(registrationState);
	}

	@Override
	protected JpaRepository<UserRegistrationState, Long> getRepository()
	{
		return userRegistrationStateRepository;
	}
}