package de.binaerebauten.gleichklang.core.view.component.validator;

import de.binaerebauten.gleichklang.core.model.matching.Activator;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;

import java.util.HashSet;
import java.util.Set;

public class ValidatableFieldChangeEvent
{
	private SignableUser user;
	private Set<Activator> activators = new HashSet<>();
	
	public void setActivators(Set<Activator> activators)
	{
		this.activators = activators;
	}
	
	public Set<Activator> getActivators()
	{
		return activators;
	}
	
	public SignableUser getUser()
	{
		return user;
	}
	
	public void setUser(SignableUser user)
	{
		this.user = user;
	}
}
