package de.binaerebauten.gleichklang.core.model.heidelpay;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "heidelpay_registration")
public class Registration extends BaseEntity
{
	@Column(name = "unique_id")
	@NotNull
	private String uniqueId;
	
	@Column(name = "transaction_id")
	private String transactionId;
	
	@NotNull
	private boolean active;
	
	private String email;
	
	@Column(name = "first_name")
	private String firstName;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "heidelpay_date")
	private LocalDateTime heidelpayDate;
	
	public String getUniqueId()
	{
		return uniqueId;
	}
	
	public void setUniqueId(String uniqueId)
	{
		this.uniqueId = uniqueId;
	}
	
	public String getTransactionId()
	{
		return transactionId;
	}
	
	public void setTransactionId(String transactionId)
	{
		this.transactionId = transactionId;
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public void setActive(boolean active)
	{
		this.active = active;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getFirstName()
	{
		return firstName;
	}
	
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}
	
	public String getLastName()
	{
		return lastName;
	}
	
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}
	
	public LocalDateTime getHeidelpayDate()
	{
		return heidelpayDate;
	}
	
	public void setHeidelpayDate(LocalDateTime heidelpayDate)
	{
		this.heidelpayDate = heidelpayDate;
	}
}
