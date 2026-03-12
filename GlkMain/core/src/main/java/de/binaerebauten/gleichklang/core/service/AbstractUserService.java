package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.user.I18N;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.repository.SignableUserRepository;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.transaction.Transactional;

public abstract class AbstractUserService<T extends SignableUser>
{
	private final PasswordEncoder passwordEncoder;
	private final SignableUserRepository<T> userRepository;
	
	protected String salt;
	
	public AbstractUserService(PasswordEncoder passwordEncoder, @Value("${security.salt}") String salt, SignableUserRepository<T> userRepository)
	{
		this.passwordEncoder = passwordEncoder;
		this.salt = salt;
		this.userRepository = userRepository;
	}
	
	public abstract T getCurrentUser();
	
	@Transactional
	public void changePassword(String oldPassword, String newPassword) throws ValidationException
	{
		final T user = getCurrentUser();
		if (!passwordEncoder.matches(oldPassword, user.getPassword()))
		{
			throw new ValidationException(de.binaerebauten.gleichklang.core.view.I18N.USER_VALIDATION_INCORRECTPASSWORD.msg());
		}
		
		changePassword(user, newPassword);
	}
	
	@Transactional
	public void changePassword(String newPassword) throws ValidationException
	{
		final T user = getCurrentUser();
		changePassword(user, newPassword);
	}
	
	private void changePassword(T user, String newPassword)
	{
		if (passwordEncoder.matches(newPassword, user.getPassword())) return;
		
		user.setPassword(encodePassword(newPassword));
		user.setResetPassword(false);
		userRepository.save(user);
	}
	
	public abstract void sendResetPasswordMail(SignableUser user);
	
	public abstract T findByEmail(String email);
	
	/**
	 * Converts the given exception with the optional user to a {@link
	 * UniqueValidationException}.
	 *
	 * @param e            the non-null exception to c
	 * @param signableUser
	 * @return
	 */
	protected UniqueValidationException convert(DataIntegrityViolationException e, SignableUser signableUser)
	{
		String message = e.getMessage().toLowerCase();
		if (message.contains(SignableUser.ALIAS_UNIQUEQ_CONSTRAINT_NAME) && signableUser != null)
		{
			return new UniqueValidationException(I18N.SIGNABLEUSER_NOTIFICATION_NONUNIQUEALIAS.msg(signableUser.getAlias()));
		}
		else if (message.contains(SignableUser.EMAIL_UNIQUEQ_CONSTRAINT_NAME) && signableUser != null)
		{
			return new UniqueValidationException(I18N.SIGNABLEUSER_NOTIFICATION_NONUNIQUEEMAIL.msg(signableUser.getEmail()));
		}
		else
		{
			return new UniqueValidationException(e);
		}
	}
	
	protected String encodePassword(String password)
	{
		return passwordEncoder.encode(password);
	}
}
