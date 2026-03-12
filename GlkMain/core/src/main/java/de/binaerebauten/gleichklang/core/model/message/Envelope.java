package de.binaerebauten.gleichklang.core.model.message;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "envelope")
public abstract class Envelope extends BaseEntity
{
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToOne
	@JoinColumn(name = "message_id")
	private Message message;
	
	@NotNull
	@Column
	private boolean hidden = false;
	
	@NotNull
	@Column
	private boolean deleted = false;
	
	public User getUser()
	{
		return user;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}
	
	public Message getMessage()
	{
		return message;
	}
	
	public void setMessage(Message message)
	{
		this.message = message;
	}
	
	public boolean isHidden()
	{
		return hidden;
	}
	
	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}
	
	public boolean isDeleted()
	{
		return deleted;
	}
	
	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
}
