package de.binaerebauten.gleichklang.core.model.message;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "admin_work_item")
public class AdminWorkItem extends BaseEntity
{
	/**
	 * Created by rgoerner on 26.04.16.
	 */
	public enum AdminWorkItemStatus
	{
		NEW,
		OPEN,
		ANSWERED,
		IGNORED,
		DONE
		
	}
	
	@ManyToOne
	@JoinColumn(name = "admin_id")
	private Admin admin;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "message_id")

	private Message message;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "work_item_status", nullable = false)
	private AdminWorkItemStatus workItemStatus = AdminWorkItemStatus.NEW;
	
	public Admin getAdmin()
	{
		return admin;
	}
	
	public void setAdmin(Admin admin)
	{
		this.admin = admin;
	}
	
	public Message getMessage()
	{
		return message;
	}
	
	public void setMessage(Message message)
	{
		this.message = message;
	}
	
	public AdminWorkItemStatus getWorkItemStatus()
	{
		return workItemStatus;
	}
	
	public void setWorkItemStatus(AdminWorkItemStatus workItemStatus)
	{
		this.workItemStatus = workItemStatus;
	}
}