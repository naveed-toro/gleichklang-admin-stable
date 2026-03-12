package de.binaerebauten.gleichklang.core.model.user;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for {@link RecommendationCategory}.
 */
public class RecommendationCategoryTest
{
	@Test
	public void testPartnership()
	{
		assertThat("Partnership must be the first recommendation category.", RecommendationCategory.PARTNERSHIP.ordinal(), is(0));
	}
}
