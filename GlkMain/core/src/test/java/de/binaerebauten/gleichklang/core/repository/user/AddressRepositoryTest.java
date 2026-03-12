package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AddressRepositoryTest extends BasePersistenceTest
{
	@Autowired
	private AddressRepository addressRepository;
	
	@Autowired
	private DefaultEntityFactory defaultEntityFactory;
	
	@Before
	public void setup()
	{
	
	}
	
	@After
	public void teardown()
	{
		defaultEntityFactory.reset();
	}

	@Test
	public void findByUserIdTest()
	{
		final User user = defaultEntityFactory.persistDefaultUser("test");
		
		assertThat(addressRepository.findByUserId(user.getId()).size(), equalTo(1));
	}
}