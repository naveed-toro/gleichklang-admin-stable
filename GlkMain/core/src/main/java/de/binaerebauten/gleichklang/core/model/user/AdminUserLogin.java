package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "admin_user_login")
public class AdminUserLogin extends BaseEntity
{
	@ManyToOne
	@NotNull
	private User user;
	
	@ManyToOne
	@NotNull
	private Admin admin;
	
	@NotNull
	private String password;
	
	public User getUser()
	{
		return user;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}
	
	public Admin getAdmin()
	{
		return admin;
	}
	
	public void setAdmin(Admin admin)
	{
		this.admin = admin;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
}
