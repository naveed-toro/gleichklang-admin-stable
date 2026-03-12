package de.binaerebauten.gleichklang.core.model.payment;

import de.binaerebauten.gleichklang.core.model.BaseMigrationTest;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

@Ignore("tests disabled until migration test runs faster")
public class ProductMigrationTest extends BaseMigrationTest
{
	@Override
	protected List<Class<?>> getEntityClasses()
	{
		return Arrays.asList(Product.class);
	}
}
