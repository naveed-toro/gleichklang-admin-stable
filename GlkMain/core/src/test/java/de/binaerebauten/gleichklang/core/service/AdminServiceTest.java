package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminRole;
import de.binaerebauten.gleichklang.core.repository.user.AdminRepository;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultStaticEntityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link AdminService}.
 */
public class AdminServiceTest extends BasePersistenceTest
{
	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Autowired
	private DefaultEntityFactory entityFactory;

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	private AdminService adminService;

	private PasswordEncoder passwordEncoder;

	@Mock
	private AuthenticationService authenticationService;

	private Admin persistentSuperAdmin;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);

		persistentSuperAdmin = entityFactory.persistDefaultAdmin(DefaultStaticEntityFactory.DEFAULT_EMAIL, AdminRole.ADMIN_MANAGEMENT);

		adminService = new AdminService(adminRepository, null, authenticationService, passwordEncoder, "salt");
	}

	@After
	public void tearDown() throws Exception
	{
		entityFactory.reset();
	}

	@Test
	public void testSave() throws ValidationException
	{
		when(authenticationService.getAuthenticatedUserId()).thenReturn(persistentSuperAdmin.getId());

		Admin savedAdmin = adminService.save(persistentSuperAdmin, false);

		THEN:
		{
			assertThat(savedAdmin, is(persistentSuperAdmin));
		}
	}

	@Test
	public void testSave_NonUniqueEmail() throws ValidationException
	{
		Admin anotherAdmin = entityFactory.persistDefaultAdmin("another@example.com");
		anotherAdmin.setEmail(persistentSuperAdmin.getEmail());

		when(authenticationService.getAuthenticatedUserId()).thenReturn(persistentSuperAdmin.getId());

		thrown.expect(ValidationException.class);
		thrown.expectMessage(anotherAdmin.getEmail());

		adminService.save(anotherAdmin, false);
	}

	@Test
	public void testSave_NonUniqueAlias() throws ValidationException
	{
		Admin anotherAdmin = entityFactory.persistDefaultAdmin("another@example.com");
		anotherAdmin.setAlias(persistentSuperAdmin.getAlias());

		when(authenticationService.getAuthenticatedUserId()).thenReturn(persistentSuperAdmin.getId());

		thrown.expect(ValidationException.class);
		thrown.expectMessage(anotherAdmin.getAlias());

		adminService.save(anotherAdmin, false);
	}

	@Test
	public void testSave_AdminRoleValidation() throws ValidationException
	{
		persistentSuperAdmin.getRoles().remove(AdminRole.ADMIN_MANAGEMENT);

		when(authenticationService.getAuthenticatedUserId()).thenReturn(persistentSuperAdmin.getId());

		thrown.expect(ValidationException.class);
		thrown.expectMessage(I18N.ADMINSERVICE_MESSAGE.msg());

		adminService.save(persistentSuperAdmin, false);
	}

	@Test
	public void testDelete() throws ValidationException
	{
		Admin anotherAdmin = entityFactory.persistDefaultAdmin("another@admin.com");

		when(authenticationService.getAuthenticatedUserId()).thenReturn(persistentSuperAdmin.getId());

		adminService.delete(anotherAdmin);
	}

	@Test
	public void testDelete_CurrentUser() throws ValidationException
	{
		when(authenticationService.getAuthenticatedUserId()).thenReturn(persistentSuperAdmin.getId());

		thrown.expect(ValidationException.class);
		thrown.expectMessage("Sie dürfen Ihren eigenen Nutzer nicht löschen!");

		adminService.delete(persistentSuperAdmin);
	}

	@Test
	public void testSaveAdminNonUniqueEmail() throws ValidationException
	{
		String email = "admin@example.com";
		String alias = "alias";

		Admin admin = DefaultStaticEntityFactory.createDefaultAdmin(email, alias + "1");
		adminService.save(admin, false);

		thrown.expect(UniqueValidationException.class);
		thrown.expectMessage(admin.getEmail());

		Admin admin2 = DefaultStaticEntityFactory.createDefaultAdmin(email, alias + "2");
		adminService.save(admin2, false);
	}
}