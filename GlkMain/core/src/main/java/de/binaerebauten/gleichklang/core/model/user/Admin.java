package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.DeletableEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Admin extends SignableUser implements DeletableEntity<Long>
{
	@ElementCollection(targetClass = AdminRole.class, fetch = FetchType.EAGER)
	@Column
	@CollectionTable(name = "admin_roles", joinColumns = @JoinColumn(name = "admin_id"))
	@Enumerated(EnumType.STRING)
	private Set<AdminRole> roles = new HashSet<>();

	private boolean deleted = false;


	public Set<AdminRole> getRoles()
	{
		return roles;
	}

	public void setRoles(Set<AdminRole> roles)
	{
		this.roles = roles;
	}

	@Override
	public boolean isDeleted()
	{
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
}
