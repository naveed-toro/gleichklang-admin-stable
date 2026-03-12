package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminRole;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AdminRepositoryTest extends AbstractRepositoryTest<Admin>
{
	public static final String UNUSED_EMAIL = "admin@example.com";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private DefaultEntityFactory defaultEntityFactory;

	private ArrayList<Admin> admins;

	@Override
	protected Collection<Admin> getPersistedEntities()
	{
		admins = new ArrayList<>();
		admins.add(defaultEntityFactory.persistDefaultAdmin("a@a.aa", AdminRole.ADMIN_MANAGEMENT, AdminRole
				.BANK_ACCOUNTS));
		admins.add(defaultEntityFactory.persistDefaultAdmin("b@b.bb", AdminRole.ADMIN_MANAGEMENT));
		admins.add(defaultEntityFactory.persistDefaultAdmin("c@c.cc", AdminRole.BANK_ACCOUNTS));
		admins.add(defaultEntityFactory.persistDefaultAdmin("d@d.dd", AdminRole.ADMIN_MANAGEMENT, AdminRole.INVOICE,
				AdminRole.MATCHING));

		return admins;
	}

	@Override
	protected JpaRepository<Admin, Long> getRepository()
	{
		return adminRepository;
	}

	@Test
	public void findByRoles() throws Exception
	{
		List<Admin> foundAdmins = adminRepository.findByRoles(AdminRole
				.ADMIN_MANAGEMENT);
		assertThat(foundAdmins.size(), equalTo(3));
	}

	@Test
	public void testSaveAdmin() throws Exception
	{
		defaultEntityFactory.persistDefaultAdmin(UNUSED_EMAIL);
		final Admin admin = adminRepository.findByEmail(UNUSED_EMAIL);
		Assert.assertThat(admin, notNullValue());
	}

	@Test
	public void testSaveDuplicateAdmin() throws Exception
	{
		defaultEntityFactory.persistDefaultAdmin(UNUSED_EMAIL);

		thrown.expect(DataIntegrityViolationException.class);
		defaultEntityFactory.persistDefaultAdmin(UNUSED_EMAIL);
	}
}