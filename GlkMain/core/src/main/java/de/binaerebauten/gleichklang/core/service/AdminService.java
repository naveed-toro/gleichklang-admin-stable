package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminRole;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.repository.user.AdminRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.MailTemplateInstance;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;
import de.binaerebauten.gleichklang.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate.ADMIN_PASSWORD_RESET;

@Service
public class AdminService extends AbstractUserService<Admin>
{
	private final AdminRepository adminRepository;
	
	private final UserMailTemplateService userMailTemplateService;
	private final AuthenticationService authenticationService;
	
	@Autowired
	private MailSendService mailSendService;
	
	/**
	 * This class uses constructor dependency injection to ease testing.
	 *
	 * @param adminRepository
	 * @param userMailTemplateService
	 * @param authenticationService
	 */
	@Autowired
	public AdminService(AdminRepository adminRepository,
                        UserMailTemplateService userMailTemplateService,
                        AuthenticationService authenticationService,
                        PasswordEncoder passwordEncoder,
                        @Value("${security.salt}") String salt)
	{
		super(passwordEncoder, salt, adminRepository);
		this.adminRepository = adminRepository;
		this.userMailTemplateService = userMailTemplateService;
		this.authenticationService = authenticationService;
	}
	
	@CheckedTransactional
	public Admin save(Admin admin, boolean resetPassword) throws ValidationException, MailException
	{
		if (getCurrentUser() != null && getCurrentUser().equals(admin) && !admin.getRoles().contains(AdminRole.ADMIN_MANAGEMENT))
		{
			final String message = I18N.ADMINSERVICE_MESSAGE.msg();
			throw new ValidationException(message);
		}
		
		String tmpPassword = null;
		if (resetPassword)
		{
			tmpPassword = SecurityUtils.generateTempPassword(UUID.randomUUID().toString(), salt);
			admin.setPassword(encodePassword(tmpPassword));
			admin.setResetPassword(true);
		}
		try
		{
			final Admin savedAdmin = adminRepository.save(admin);
			
			if (resetPassword) // split sending after successful save so that user is saved
			{
				mailSendService.sendEmail(admin, userMailTemplateService.createAdminMailTemplateInstance(ADMIN_PASSWORD_RESET, admin, tmpPassword));
			}
			
			return savedAdmin;
		}
		catch (DataIntegrityViolationException e)
		{
			throw convert(e, admin);
		}
	}
	
	/**
	 * Deletes the gven admin.
	 *
	 * @param admin the non-null admin to delete
	 * @throws ValidationException when trying to delete the current user
	 */
	public void delete(Admin admin) throws ValidationException
	{
		Objects.requireNonNull(admin, "admin == null");

		if (getCurrentUser().equals(admin))
		{
			throw new ValidationException("Sie dürfen Ihren eigenen Nutzer nicht löschen!");
		}
		adminRepository.delete(admin);
	}
	
	@Override
	public Admin getCurrentUser()
	{
		Long authenticatedUserId = authenticationService.getAuthenticatedUserId();
		return authenticatedUserId != null ? adminRepository.findOne(authenticatedUserId) : null;
	}
	
	public Admin createInitialAdmin()
	{
		Admin testAdmin = new Admin();
		testAdmin.setEmail("superadmin@gleichklang.de");
		testAdmin.setPassword(encodePassword("=11$,Se;"));
		testAdmin.setFirstName("Super");
		testAdmin.setLastName("Admin");
		testAdmin.setAlias("gk_superadmin");
		testAdmin.setRoles(new HashSet<>(Arrays.asList(AdminRole.values())));
		
		if (adminRepository.findByEmail(testAdmin.getEmail()) == null
				&& adminRepository.findByAlias(testAdmin.getAlias()) == null)
		{
			testAdmin = adminRepository.save(testAdmin);
		}

		return testAdmin;
	}
	
	@Override
	public void sendResetPasswordMail(SignableUser user)
	{
		final List<Admin> superAdmins = adminRepository.findByRoles(AdminRole.ADMIN_MANAGEMENT);
		final Admin recipient = (Admin) user;
		
		for (Admin superAdmin : superAdmins)
		{
			if (!superAdmin.equals(recipient))
			{
				final MailTemplateInstance mailTemplateInstance = userMailTemplateService.createAdminPasswordResetRequest(superAdmin, recipient);
				mailSendService.sendEmail(superAdmin, mailTemplateInstance);
			}
		}
		
	}
	
	@Override
	public Admin findByEmail(String email)
	{
		return adminRepository.findByEmail(email);
	}
}
