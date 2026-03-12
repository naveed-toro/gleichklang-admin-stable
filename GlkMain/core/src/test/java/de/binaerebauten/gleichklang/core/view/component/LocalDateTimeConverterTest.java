package de.binaerebauten.gleichklang.core.view.component;

import de.binaerebauten.gleichklang.core.view.component.converter.LocalDateTimeConverter;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertThat;

public class LocalDateTimeConverterTest
{
	private final Date date;
	private final LocalDateTime localDateTime;

	public LocalDateTimeConverterTest()
	{
		final int year = 2042;
		final int month = 10;
		final int day = 12;
		final int hour = 4;
		final int minute = 42;
		
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);
		calendar.set(year, month - 1, day, hour, minute, 0);
		date = calendar.getTime();
		
		localDateTime = LocalDateTime.of(year, month, day, hour, minute);
	}
	
	@Test
	public void testConvertToModel()
	{
		final LocalDateTimeConverter converter = new LocalDateTimeConverter();
		final LocalDateTime result = converter.convertToModel(date, LocalDateTime.class, Locale.getDefault());
		assertThat(result, CoreMatchers.equalTo(localDateTime));
	}
	
	@Test
	public void testConvertToPresentation()
	{
		final LocalDateTimeConverter converter = new LocalDateTimeConverter();
		final Date result = converter.convertToPresentation(localDateTime, Date.class, Locale.getDefault());
		assertThat(result, CoreMatchers.equalTo(date));
	}
}
