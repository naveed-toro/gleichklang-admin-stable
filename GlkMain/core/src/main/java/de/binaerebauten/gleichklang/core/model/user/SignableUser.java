package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseEntity;
import de.binaerebauten.gleichklang.core.security.SanitizeContent;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="user_")
public abstract class SignableUser extends BaseEntity
{
	/**
	 * Enumerates the concrete sub types of this class.
	 */
	public enum UserType
	{
		MEMBER,
		ADMIN
	}

	public static final int MIN_PASSWORD_LENGTH = 5;

	/**
	 * The unique index name for the {@link #email} attribute.
	 */
	public static final String EMAIL_UNIQUEQ_CONSTRAINT_NAME = "user_type_email_idx";

	/**
	 * The unique index name for the {@link #alias} attribute.
	 */
	public static final String ALIAS_UNIQUEQ_CONSTRAINT_NAME = "user_type_alias_idx";

	//@Email
	private String email;
	
	//@Email
	@Column(name = "new_email")
	private String newEmail;

	/***
	 * Value shows, whether email confirmed or not. Default value is FALSE. It
	 * is set to TRUE when the user confirms his email by clicking on
	 * confirmation link. If the email is not confirmed - only email
	 * confirmation can be sent, otherwise any mail can be sent
	 */
	@Column(name = "email_confirmed")
	private boolean emailConfirmed;

	// TODO add regex constraint to validate alias
	@NotEmpty
	private String alias;

	@SanitizeContent
	@Column(name = "first_name")
	private String firstName;

	@SanitizeContent
	@Column(name = "last_name")
	private String lastName;

	@NotEmpty
	private String password;

	@Column(name = "reset_password")
	private boolean resetPassword;

	public String getAdminNotes() {
		return adminNotes;
	}

	public void setAdminNotes(String adminNotes) {
		this.adminNotes = adminNotes;
	}

	@Column(name = "admin_notes")
	private String adminNotes;

	@Column(name = "isBlocked")
	private boolean isBlocked;


	public boolean isBlocked() {
		return isBlocked;
	}

	public void setBlocked(boolean blocked) {
		isBlocked = blocked;
	}

	public String getAlias()
	{
		return alias;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getNewEmail()
	{
		return newEmail;
	}
	
	public void setNewEmail(String newEmail)
	{
		this.newEmail = newEmail;
	}
	
	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
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

	public boolean isResetPassword()
	{
		return resetPassword;
	}

	public void setResetPassword(boolean resetPassword)
	{
		this.resetPassword = resetPassword;
	}

	public boolean isEmailConfirmed()
	{
		return emailConfirmed;
	}

	public void setEmailConfirmed(boolean emailConfirmed)
	{
		this.emailConfirmed = emailConfirmed;
	}
}
