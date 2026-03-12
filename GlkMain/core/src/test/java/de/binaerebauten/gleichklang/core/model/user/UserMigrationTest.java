package de.binaerebauten.gleichklang.core.model.user;

import de.binaerebauten.gleichklang.core.model.BaseMigrationTest;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

@Ignore("tests disabled until migration test runs faster")
public class UserMigrationTest extends BaseMigrationTest
{
	@Override
	public List<Class<?>> getEntityClasses()
	{
		return Arrays.asList(User.class, UserSettings.class);
	}
}
