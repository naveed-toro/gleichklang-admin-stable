package de.binaerebauten.gleichklang.core.utils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class StringUtilsTest
{
	@Test
	public void removeInvalidFilePathSignsTest()
	{
		final String test = "abc?*<>.,\\+:def=/\";[]|^²³ghi";
		final String replaced = StringUtils.removeInvalidFilePathSigns(test);
		
		assertThat(replaced, equalTo("abcdefghi"));
	}
}
