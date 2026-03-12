package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "email_link_parameter")
public class EmailLinkParameter extends BaseEntity
{
	public enum ParameterType
	{
		EMAIL,
		PASSWORD,
		MAIL_TEMPLATE
	}
	
	@ManyToOne
	@NotNull
	@JoinColumn(name = "email_link_id")
	private EmailLink emailLink;
	
	@NotNull
	@Column(name = "parameter_type")
	@Enumerated(EnumType.STRING)
	private ParameterType parameterType;
	
	@NotNull
	@Column(name = "parameter_value")
	private String value;
	
	public EmailLinkParameter()
	{
	}
	
	public EmailLinkParameter(ParameterType parameterType, String value)
	{
		this.parameterType = parameterType;
		this.value = value;
	}
	
	public EmailLink getEmailLink()
	{
		return emailLink;
	}
	
	public void setEmailLink(EmailLink emailLink)
	{
		this.emailLink = emailLink;
	}
	
	public ParameterType getParameterType()
	{
		return parameterType;
	}
	
	public void setParameterType(ParameterType parameterType)
	{
		this.parameterType = parameterType;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
}
