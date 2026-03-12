package de.binaerebauten.gleichklang.core.view.component.converter;

import com.vaadin.data.util.converter.Converter;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Domi on 10.07.2017.
 */
public class PagingTextFieldConverter implements Converter<String, Integer>
{
	private int totalPages;
	
	public PagingTextFieldConverter(int totalPages)
	{
		this.totalPages = totalPages;
	}
	
	public void setTotalPages(int totalPages)
	{
		this.totalPages = totalPages;
	}
	
	@Override
	public Integer convertToModel(String value, Class<? extends Integer> targetType, Locale locale) throws ConversionException
	{
		if (value == null)
		{
			return 0;
		}
		
		final Pattern p = Pattern.compile("(\\d)+");
		final Matcher m = p.matcher(value);
		if (m.find())
		{
			return Integer.parseInt(m.group()) - 1;
		}
		
		return 0;
	}
	
	@Override
	public String convertToPresentation(Integer value, Class<? extends String> targetType, Locale locale) throws ConversionException
	{
		if (value == null || totalPages == 0)
		{
			return String.format("%d/%d", 1, 1);
		}
		return String.format("%d/%d", value + 1, totalPages);
	}
	
	@Override
	public Class<Integer> getModelType()
	{
		return Integer.class;
	}
	
	@Override
	public Class<String> getPresentationType()
	{
		return String.class;
	}
}
