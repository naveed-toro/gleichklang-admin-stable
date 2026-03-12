package de.binaerebauten.gleichklang.core.config;

import org.springframework.core.env.Environment;

/**
 * Enumerates the different spring profiles that we use.
 */
public enum SpringProfile
{
	QA("qa"),
	BUGFIX("bugfix"),
	DEV("dev"),
	STAGING("staging"),
	PROD("prod");

	private final String name;

	SpringProfile(String name)
	{
		this.name = name;
	}

	/**
	 * Returns true if this profile is activated as spring profile.
	 *
	 * @return true iff. this profile is active
	 */
	public boolean isActive()
	{
		String activeProfiles = System.getProperty("spring.profiles.active");

		return activeProfiles != null && activeProfiles.contains(name);
	}

	/**
	 * Returns true if this profile is active for the given environment.
	 *
	 * @param environment the non-null environment
	 *
	 * @return true iff. this profile is active
	 */
	public boolean isActive(Environment environment)
	{
		return environment.acceptsProfiles(name);
	}
}
