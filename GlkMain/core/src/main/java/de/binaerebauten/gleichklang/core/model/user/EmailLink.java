package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "email_link")
public class EmailLink extends BaseEntity
{
	public enum EmailLinkContext
	{
		UNSUBSCRIBE,
		RESET_PASSWORD,
		VALIDATE
	}
	
	@ManyToOne
	@NotNull
	private SignableUser user;
	
	@NotEmpty
	@Column(name = "unique_token")
	private String uniqueToken;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private EmailLinkContext context;
	
	@OneToMany(mappedBy = "emailLink", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<EmailLinkParameter> emailLinkParameters = new HashSet<>();
	
	@Transient
	private URI uri;
	
	public SignableUser getUser()
	{
		return user;
	}
	
	public void setUser(SignableUser user)
	{
		this.user = user;
	}
	
	public String getUniqueToken()
	{
		return uniqueToken;
	}
	
	public void setUniqueToken(String uniqueToken)
	{
		this.uniqueToken = uniqueToken;
	}
	
	public EmailLinkContext getContext()
	{
		return context;
	}
	
	public void setContext(EmailLinkContext context)
	{
		this.context = context;
	}
	
	public Set<EmailLinkParameter> getEmailLinkParameters()
	{
		return emailLinkParameters;
	}
	
	public void setEmailLinkParameters(Set<EmailLinkParameter> emailLinkParameters)
	{
		this.emailLinkParameters = emailLinkParameters;
	}
	
	public void addEmailLinkParameter(EmailLinkParameter emailLinkParameter)
	{
		this.emailLinkParameters.add(emailLinkParameter);
		emailLinkParameter.setEmailLink(this);
	}
	
	public URI getUri()
	{
		return uri;
	}
	
	public void setUri(URI uri)
	{
		this.uri = uri;
	}
}
