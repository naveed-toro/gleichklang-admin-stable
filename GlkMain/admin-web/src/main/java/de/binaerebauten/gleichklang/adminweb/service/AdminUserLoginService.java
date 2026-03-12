package de.binaerebauten.gleichklang.adminweb.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminUserLogin;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.user.AdminUserLoginRepository;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.utils.CheckedTransactional;

@Service
public class AdminUserLoginService
{
	private static final Logger LOG = LoggerFactory.getLogger(AdminUserLoginService.class);
	
	private static final int MINUTES_TO_EXPIRED = 1;
	
	private final AdminUserLoginRepository adminUserLoginRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Autowired
	public AdminUserLoginService(AdminUserLoginRepository adminUserLoginRepository, PasswordEncoder passwordEncoder)
	{
		Objects.requireNonNull(adminUserLoginRepository);
		Objects.requireNonNull(passwordEncoder);
		
		this.adminUserLoginRepository = adminUserLoginRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Scheduled(fixedRate = 60000)
	@Transactional
	public void cleanUp()
	{
		LOG.info("cleanUp started");
		try
		{
			adminUserLoginRepository.deleteExpired(LocalDateTime.now().minus(MINUTES_TO_EXPIRED, ChronoUnit.MINUTES));
		}
		catch (Throwable e)
		{
			LOG.error("AdminUserLoginService.cleanUp", e);
		}
		LOG.info("cleanUp started");
	}
	
	public void reset(Admin admin)
	{
		Objects.requireNonNull(admin);
		
		adminUserLoginRepository.deleteByAdmin(admin);
	}
	
	@CheckedTransactional
	public String createPassword(Admin admin, User user) throws UniqueValidationException
	{
		final String password = UUID.randomUUID().toString();
		
		final AdminUserLogin adminUserLogin = new AdminUserLogin();
		adminUserLogin.setAdmin(admin);
		adminUserLogin.setUser(user);
		adminUserLogin.setPassword(passwordEncoder.encode(password));
		
		try
		{
			adminUserLoginRepository.saveAndFlush(adminUserLogin);
		}
		catch (final DataIntegrityViolationException e)
		{
			throw new UniqueValidationException("Nur ein Login pro Nutzer und pro Admin möglich!");
			// TODO i18n wenn feature soweit i.O. (GK-838)
		}
		
		return password;
	}
}
