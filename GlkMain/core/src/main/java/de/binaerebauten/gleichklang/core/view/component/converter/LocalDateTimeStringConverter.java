package de.binaerebauten.gleichklang.core.view.component.converter;

import com.vaadin.data.util.converter.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@SuppressWarnings("serial")
public class LocalDateTimeStringConverter implements Converter<String, LocalDateTime>
{
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm");

	@Override
	public LocalDateTime convertToModel(String value, Class<? extends LocalDateTime> targetType, Locale locale) throws ConversionException
	{
		if (value == null) return null;
		return LocalDateTime.parse(value, formatter);
	}

	@Override
	public String convertToPresentation(LocalDateTime value, Class<? extends String> targetType, Locale locale) throws ConversionException
	{
		if (value == null) return null;
		return value.format(formatter);
	}
	
	@Override
	public Class<LocalDateTime> getModelType()
	{
		return LocalDateTime.class;
	}
	
	@Override
	public Class<String> getPresentationType()
	{
		return String.class;
	}
	
}
