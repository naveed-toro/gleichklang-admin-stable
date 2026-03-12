package de.binaerebauten.gleichklang.adminweb.service;

import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminUserLogin;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.user.AdminUserLoginRepository;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdminUserLoginServiceTest
{
	@InjectMocks
	private AdminUserLoginService adminUserLoginService;
	
	@Mock
	private AdminUserLoginRepository adminUserLoginRepository;
	
	@Mock
	private PasswordEncoder passwordEncoder;
	
	@Test
	public void cleanUpTest()
	{
		adminUserLoginService.cleanUp();
		
		verify(adminUserLoginRepository, times(1)).deleteExpired(anyObject());
	}
	
	@Test
	public void resetTest()
	{
		final Admin admin = new Admin();
		adminUserLoginService.reset(admin);
		
		verify(adminUserLoginRepository).deleteByAdmin(admin);
	}
	
	@Test
	public void createPasswordTest() throws UniqueValidationException
	{
		
		final User user = new User();
		user.setId(42L);
		user.setEmail("valid@example.com");
		
		final Admin admin = new Admin();
		user.setId(42L);
		user.setEmail("valid@example.com");
		
		final String encodedPassword = "encodedPassword";
		
		when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
		
		final String password = adminUserLoginService.createPassword(admin, user);
		
		assertThat(password, not(emptyOrNullString()));
		
		final ArgumentCaptor<AdminUserLogin> adminUserLogin = ArgumentCaptor.forClass(AdminUserLogin.class);
		verify(adminUserLoginRepository).saveAndFlush(adminUserLogin.capture());
		verify(passwordEncoder).encode(anyString());
		
		assertThat(adminUserLogin.getValue().getPassword(), equalTo(encodedPassword));
	}
}
