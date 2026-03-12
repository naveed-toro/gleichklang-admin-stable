package de.binaerebauten.gleichklang.core.view.converter;

import com.vaadin.data.util.converter.Converter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings("serial")
public class SetConverter<T> implements Converter<Object, Set<T>>
{
	private final Set<T> MODEL_TYPE_INSTANCE = new HashSet<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<T> convertToModel(Object value, Class<? extends Set<T>> targetType, Locale locale) throws ConversionException
	{
		return value == null ? new HashSet<>(0) : Collections.singleton((T) value);
	}
	
	@Override
	public T convertToPresentation(Set<T> choiceValues, Class<? extends Object> value, Locale locale) throws ConversionException
	{
		return value != null && !choiceValues.isEmpty() ? choiceValues.iterator().next() : null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<Set<T>> getModelType()
	{
		return (Class<Set<T>>) MODEL_TYPE_INSTANCE.getClass();
	}
	
	@Override
	public Class<Object> getPresentationType()
	{
		return Object.class;
	}
}
