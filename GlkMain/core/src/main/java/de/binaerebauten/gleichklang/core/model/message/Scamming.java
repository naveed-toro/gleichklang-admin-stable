package de.binaerebauten.gleichklang.core.model.message;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "scamming")
public class Scamming extends BaseEntity
{
	@ManyToOne
	@NotNull
	@JoinColumn(name = "user_id")
	private User user;
	
	public User getUser()
	{
		return user;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}
}
