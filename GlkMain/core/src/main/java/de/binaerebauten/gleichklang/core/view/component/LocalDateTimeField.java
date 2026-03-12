package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.DateField;
import de.binaerebauten.gleichklang.core.view.component.converter.LocalDateTimeConverter;

import java.util.Date;

/**
 * Wraps a DateField in order to provide a range restriction using a binding
 * with LocalDateTime object.
 */
public class LocalDateTimeField extends CustomField<Date>
{
	private final DateField dateField;
	
	public LocalDateTimeField()
	{
		this(false);
	}
	
	public LocalDateTimeField(boolean required)
	{
		dateField = ComponentFactory.getInstance().createField(DateField.class);
		setConverter(new LocalDateTimeConverter());
		setRequired(required);
		
		dateField.addValueChangeListener(e -> this.fireValueChange(false));
	}
	
	public void setRangeStart(Date startDate)
	{
		dateField.setRangeStart(startDate);
	}
	
	public void setDateOutOfRangeMessage(String dateOutOfRangeMessage)
	{
		dateField.setDateOutOfRangeMessage(dateOutOfRangeMessage);
	}
	
	@Override
	public void addValueChangeListener(Property.ValueChangeListener listener)
	{
		dateField.addValueChangeListener(listener);
	}
	
	@Override
	protected Date getInternalValue()
	{
		return dateField.getValue();
	}
	
	@Override
	protected void setInternalValue(Date newValue)
	{
		super.setInternalValue(newValue);
		dateField.setValue(newValue);
	}
	
	@Override
	public void validate() throws InvalidValueException
	{
		super.validate();
		dateField.validate();
	}
	
	@Override
	protected Component initContent()
	{
		return dateField;
	}
	
	@Override
	public Class<? extends Date> getType()
	{
		return Date.class;
	}
	
}
