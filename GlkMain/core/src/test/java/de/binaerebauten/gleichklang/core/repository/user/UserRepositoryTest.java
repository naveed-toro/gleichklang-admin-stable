package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.locatable.Country;
import de.binaerebauten.gleichklang.core.model.user.Address;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class UserRepositoryTest extends AbstractRepositoryTest<User>
{
	public static final String UNUSED_EMAIL = "another@example.com";
	public static final String UNUSED_ALIAS = "AnotherUser";
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	private User persistentUser;

	@Override
	protected Collection<User> getPersistedEntities()
	{
		persistentUser = defaultEntityFactory.persistDefaultUser(DefaultStaticEntityFactory.DEFAULT_EMAIL, DefaultStaticEntityFactory.DEFAULT_ALIAS);
		return Collections.singletonList(persistentUser);
	}

	@Override
	protected JpaRepository<User, Long> getRepository()
	{
		return userRepository;
	}

	@Test
	public void testGetOne()
	{
		final User user = userRepository.getOne(persistentUser.getId());
		assertThat(user, notNullValue());
	}
	
	@Test
	public void testGetByEmail()
	{
		final User user = userRepository.findByEmail(persistentUser.getEmail());
		assertThat(user, notNullValue());
	}
	
	@Test
	public void testGetByAlias() throws Exception
	{
		User user = userRepository.findByAlias(persistentUser.getAlias());
		assertThat(user, notNullValue());

		user = userRepository.findByAlias("not existing alias");
		assertThat(user, nullValue());
	}

	@Test
	public void testSaveUser() throws Exception
	{
		defaultEntityFactory.persistDefaultUser(UNUSED_EMAIL, UNUSED_ALIAS);
		final User user = userRepository.findByEmail(UNUSED_EMAIL);
		assertThat(user, notNullValue());
	}

	@Test
	public void testSaveDuplicateUser() throws Exception
	{
		defaultEntityFactory.persistDefaultUser(UNUSED_EMAIL, UNUSED_ALIAS);

		thrown.expect(DataIntegrityViolationException.class);
		defaultEntityFactory.persistDefaultUser(UNUSED_EMAIL, UNUSED_ALIAS);
	}

	@Test
	public void testUserWithMultipleAddresses() throws Exception
	{
		User user = defaultEntityFactory.persistDefaultUser("user_with_addresses");
		Country defaultCountry = defaultEntityFactory.persistDefaultCountry();
		int sortOrder = user.getAddresses().size();

		Address expectedPrimaryAddress = user.getAddresses().get(0);
		expectedPrimaryAddress.setPayment(true);
		user.addAddress(expectedPrimaryAddress);
		sortOrder++;

		for (int i = 1; i < 4; i++)
		{
			Address defaultAddress = DefaultStaticEntityFactory.createDefaultAddress(defaultEntityFactory.persistDefaultZip(defaultCountry));
			user.addAddress(defaultAddress);
			sortOrder++;
		}

		user = userRepository.save(user);

		assertThat(user.getAddresses().size(), equalTo(sortOrder));
		assertThat(user.getPaymentAddress(), equalTo(expectedPrimaryAddress));
	}
	
	@Test
	public void testDeleteUser()
	{
		final User user = defaultEntityFactory.persistDefaultUser("deleteUser");
		
		final User preDeleteUser = userRepository.findOne(user.getId());
		assertThat(preDeleteUser, equalTo(user));
		assertThat(preDeleteUser.getEmail(), notNullValue());
		assertThat(preDeleteUser.getPassword(), notNullValue());
		assertThat(preDeleteUser.getFirstName(), notNullValue());
		assertThat(preDeleteUser.getLastName(), notNullValue());
		assertThat(preDeleteUser.getBirthDate(), notNullValue());
		assertThat(preDeleteUser.getMemberStatus(), not(equalTo(MemberStatus.CANCELED)));
		
		userRepository.deleteUser(MemberStatus.DELETED, user);
		
		final User postDeleteUser = userRepository.findOne(user.getId());
		assertThat(postDeleteUser, equalTo(user));
		assertThat(postDeleteUser.getEmail(), nullValue());
		assertThat(postDeleteUser.getPassword(), nullValue());
		assertThat(postDeleteUser.getFirstName(), nullValue());
		assertThat(postDeleteUser.getLastName(), nullValue());
		assertThat(postDeleteUser.getBirthDate(), nullValue());
		assertThat(postDeleteUser.getMemberStatus(), equalTo(MemberStatus.DELETED));
	}
	
	@Test
	public void findUsersWithUserNewsActivatedTest()
	{
		final User hitUser = defaultEntityFactory.persistDefaultUser("hit");
		final User notCanceledUser = defaultEntityFactory.persistDefaultUser("notCanceled");
		final User notActivatedNewsUser = defaultEntityFactory.persistDefaultUser("notActivatedNews");
		
		final List<String> emails = new ArrayList<>();
		emails.add(hitUser.getEmail());
		emails.add(notCanceledUser.getEmail());
		emails.add(notActivatedNewsUser.getEmail());
		
		hitUser.getUserSettings().setDisableNewsNotifications(false);
		hitUser.setMemberStatus(MemberStatus.CANCELED);
		userRepository.save(hitUser);
		
		notCanceledUser.getUserSettings().setDisableNewsNotifications(false);
		notCanceledUser.setMemberStatus(MemberStatus.REGISTERED);
		userRepository.save(notCanceledUser);
		
		notActivatedNewsUser.getUserSettings().setDisableNewsNotifications(true);
		notActivatedNewsUser.setMemberStatus(MemberStatus.CANCELED);
		userRepository.save(notActivatedNewsUser);
		
		final List<User> users = userRepository.findUsersWithUserNewsActivated(emails, MemberStatus.CANCELED);
		assertThat(users.size(), equalTo(1));
		assertThat(users.iterator().next(), equalTo(hitUser));
	}
}
