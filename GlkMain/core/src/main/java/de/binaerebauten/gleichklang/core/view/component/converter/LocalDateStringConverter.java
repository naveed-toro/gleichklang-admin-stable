package de.binaerebauten.gleichklang.core.view.component.converter;

import com.vaadin.data.util.converter.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@SuppressWarnings("serial")
public class LocalDateStringConverter implements Converter<String, LocalDate>
{
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd");

	@Override
	public LocalDate convertToModel(String value, Class<? extends LocalDate> targetType, Locale locale) throws ConversionException
	{
		if (value == null) return null;
		return LocalDate.parse(value, formatter);
	}

	@Override
	public String convertToPresentation(LocalDate value, Class<? extends String> targetType, Locale locale) throws ConversionException
	{
		if (value == null) return null;
		return value.format(formatter);
	}
	
	@Override
	public Class<LocalDate> getModelType()
	{
		return LocalDate.class;
	}
	
	@Override
	public Class<String> getPresentationType()
	{
		return String.class;
	}
	
}
