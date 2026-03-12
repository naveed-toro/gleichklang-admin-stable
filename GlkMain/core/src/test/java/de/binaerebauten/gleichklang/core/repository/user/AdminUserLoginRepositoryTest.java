package de.binaerebauten.gleichklang.core.repository.user;

import de.binaerebauten.gleichklang.core.model.user.AdminUserLogin;
import de.binaerebauten.gleichklang.core.repository.AbstractRepositoryTest;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class AdminUserLoginRepositoryTest extends AbstractRepositoryTest<AdminUserLogin>
{
	@Autowired
	private AdminUserLoginRepository adminUserLoginRepository;

	@Autowired
	private DefaultEntityFactory entityFactory;
	
	private AdminUserLogin adminUserLogin;
	
	public AdminUserLoginRepositoryTest()
	{
	}

	@Override
	protected Collection<AdminUserLogin> getPersistedEntities()
	{
		AdminUserLogin adminUserLogin = new AdminUserLogin();
		adminUserLogin.setAdmin(entityFactory.persistDefaultAdmin("admin@example.com"));
		adminUserLogin.setUser(entityFactory.persistDefaultUser("user"));
		adminUserLogin.setPassword("test");
		
		this.adminUserLogin = adminUserLoginRepository.save(adminUserLogin);
		
		return Collections.singletonList(this.adminUserLogin);
	}
	
	@Override
	public void teardown()
	{
		adminUserLoginRepository.deleteAll();
		super.teardown();
	}
	
	@Override
	protected JpaRepository<AdminUserLogin, Long> getRepository()
	{
		return adminUserLoginRepository;
	}
	
	//@Test
	public void deleteExpiredTest()
	{
		final int minutesToExpired = 1;
		
		adminUserLoginRepository.deleteExpired(LocalDateTime.now().minus(minutesToExpired, ChronoUnit.MINUTES));
		assertThat(adminUserLoginRepository.count(), equalTo(1L));
		
		adminUserLogin.setCreateDate(LocalDateTime.now().minus(minutesToExpired * 30, ChronoUnit.SECONDS));
		adminUserLoginRepository.save(adminUserLogin);
		
		adminUserLoginRepository.deleteExpired(LocalDateTime.now().minus(minutesToExpired, ChronoUnit.MINUTES));
		assertThat(adminUserLoginRepository.count(), equalTo(1L));
		
		adminUserLogin.setCreateDate(LocalDateTime.now().minus(minutesToExpired, ChronoUnit.MINUTES));
		adminUserLoginRepository.save(adminUserLogin);
		
		adminUserLoginRepository.deleteExpired(LocalDateTime.now().minus(minutesToExpired, ChronoUnit.MINUTES));
		assertThat(adminUserLoginRepository.count(), equalTo(0L));
	}
	
	@Test
	public void deleteByAdminTest()
	{
		assertThat(adminUserLoginRepository.count(), equalTo(1L));
		
		adminUserLoginRepository.deleteByAdmin(adminUserLogin.getAdmin());
		
		assertThat(adminUserLoginRepository.count(), equalTo(0L));
	}
	
	@Test
	public void findByUserTest()
	{
		final AdminUserLogin expectedUserLogin = adminUserLogin;
		final AdminUserLogin actualUserLogin = adminUserLoginRepository.findByUser(expectedUserLogin.getUser());
		
		assertThat(actualUserLogin, equalTo(expectedUserLogin));
	}
}
