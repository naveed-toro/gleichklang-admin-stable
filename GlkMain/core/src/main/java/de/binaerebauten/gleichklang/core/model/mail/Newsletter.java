package de.binaerebauten.gleichklang.core.model.mail;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import org.hibernate.validator.constraints.Email;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "newsletter")
public class Newsletter extends BaseEntity
{
	@Email
	@NotNull
	private String email;
	
	@Column(name = "confirmation_ip")
	private String confirmationIp;
	
	@Column(name = "confirmation_date")
	private LocalDateTime confirmationDate;
	
	public String getEmail()
	{
		return email;
	}
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getConfirmationIp()
	{
		return confirmationIp;
	}
	
	public void setConfirmationIp(String confirmationIp)
	{
		this.confirmationIp = confirmationIp;
	}
	
	public LocalDateTime getConfirmationDate()
	{
		return confirmationDate;
	}
	
	public void setConfirmationDate(LocalDateTime confirmationDate)
	{
		this.confirmationDate = confirmationDate;
	}
}
