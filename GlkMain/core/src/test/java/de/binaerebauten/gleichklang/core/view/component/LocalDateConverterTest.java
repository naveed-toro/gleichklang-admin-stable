package de.binaerebauten.gleichklang.core.view.component;

import de.binaerebauten.gleichklang.core.view.component.converter.LocalDateConverter;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertThat;

public class LocalDateConverterTest
{
	private final Date date;
	private final LocalDate localDate;
	
	public LocalDateConverterTest()
	{
		final int year = 2042;
		final int month = 10;
		final int day = 12;
		
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);
		calendar.set(year, month - 1, day, 0, 0, 0);
		date = calendar.getTime();
		
		localDate = LocalDate.of(year, month, day);
	}
	
	@Test
	public void testConvertToModel()
	{
		final LocalDateConverter converter = new LocalDateConverter();
		final LocalDate result = converter.convertToModel(date, LocalDate.class, Locale.getDefault());
		assertThat(result, CoreMatchers.equalTo(localDate));
	}
	
	@Test
	public void testConvertToPresentation()
	{
		final LocalDateConverter converter = new LocalDateConverter();
		final Date result = converter.convertToPresentation(localDate, Date.class, Locale.getDefault());
		assertThat(result, CoreMatchers.equalTo(date));
	}
}
