package de.binaerebauten.gleichklang.core.security;

import de.binaerebauten.gleichklang.core.model.user.AdminUserLogin;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;

import java.util.Collections;
import java.util.Objects;

@SuppressWarnings("serial")
public class AuthenticatedUser extends org.springframework.security.core.userdetails.User
{
	private final Long id;
	private final AuthenticatedUser adminUserLoginUser;
	
	public AuthenticatedUser(SignableUser user)
	{
		this(user, null);
	}
	
	public AuthenticatedUser(SignableUser user, AdminUserLogin adminUserLogin)
	{
		super(user.getEmail(), user.getPassword(), Collections.emptyList());
		
		this.adminUserLoginUser = adminUserLogin != null ? new AuthenticatedUser(adminUserLogin) : null;
		this.id = Objects.requireNonNull(user.getId(), "id == null");
	}
	
	private AuthenticatedUser(AdminUserLogin adminUserLogin)
	{
		super(adminUserLogin.getUser().getEmail(), adminUserLogin.getPassword(), Collections.emptyList());
		
		this.adminUserLoginUser = null;
		this.id = null;
	}
	
	public Long getId()
	{
		return id;
	}
	
	public AuthenticatedUser getAdminUserLoginUser()
	{
		return adminUserLoginUser;
	}
}
