package de.binaerebauten.gleichklang.core.view;

import de.binaerebauten.gleichklang.core.model.user.SignableUser;

public class PasswordStrengthValidator
{
	/**
	 * Checks the strength of the password
	 * It checks whether password contains
	 * 1. Upper case letters
	 * 2. Lower case letters
	 * 3. Numbers
	 * 4. Special characters
	 * <p>
	 * For every match it gets 1 point
	 * <p>
	 * 1 point - very weak
	 * 2 points - weak
	 * 3 points - strong
	 * 4 points - very strong
	 *
	 * @param password
	 * @return
	 */
	public static int validatePasswordStrength(String password)
	{
		int strength = 0;
		if (password == null || password.length() < SignableUser.MIN_PASSWORD_LENGTH)
		{
			return strength;
		}
		if (password.matches(".*[A-Z].*"))
		{
			strength++;
		}
		if (password.matches(".*[a-z].*"))
		{
			strength++;
		}
		if (password.matches(".*[0-9].*") || password.matches(".*[^a-zA-Z0-9].*"))
		{
			if (strength > 0)
			{
				strength++;
			}
			strength++;
		}
		if (strength > 3 && password.length() < 8)
		{
			strength--;
		}
		return strength;
	}
}
