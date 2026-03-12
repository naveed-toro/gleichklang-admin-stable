package de.binaerebauten.gleichklang.core.view.component.converter;

import com.vaadin.data.util.converter.Converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("serial")
public class LocalDateTimeConverter implements Converter<Date, LocalDateTime>
{
	@Override
	public LocalDateTime convertToModel(Date value, Class<? extends LocalDateTime> targetType, Locale locale) throws ConversionException
	{
		if (value == null) return null;
		return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
	}

	@Override
	public Date convertToPresentation(LocalDateTime value, Class<? extends Date> targetType, Locale locale) throws ConversionException
	{
		if (value == null) return null;
		return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	@Override
	public Class<LocalDateTime> getModelType()
	{
		return LocalDateTime.class;
	}
	
	@Override
	public Class<Date> getPresentationType()
	{
		return Date.class;
	}
	
}
