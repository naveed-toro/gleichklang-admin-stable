package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.BaseVersionedEntity;
import de.binaerebauten.gleichklang.core.model.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * This entity represents the external payment registration of an user.
 */
@Entity
@Table(name = "external_payment_registration")
public class ExternalPaymentRegistration extends BaseVersionedEntity
{
	@NotNull
	@Column(name = "last_used_date")
	private LocalDateTime lastUsedDate;

	@Column(name = "registration_id")
	private String registrationId;

	@NotNull
	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	/**
	 * TODO MK update:
	 * This stores the external reference id of the {@link ExternalPayment}
	 * that was used to register the user.
	 */
	@NotNull
	@Column(name = "external_reference_id")
	private String externalReferenceId;

	public LocalDateTime getLastUsedDate()
	{
		return lastUsedDate;
	}

	public void setLastUsedDate(LocalDateTime lastUsedDate)
	{
		this.lastUsedDate = lastUsedDate;
	}

	public String getRegistrationId()
	{
		return registrationId;
	}

	public void setRegistrationId(String registrationId)
	{
		this.registrationId = registrationId;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public String getExternalReferenceId()
	{
		return externalReferenceId;
	}

	public void setExternalReferenceId(String externalReferenceId)
	{
		this.externalReferenceId = externalReferenceId;
	}
}
