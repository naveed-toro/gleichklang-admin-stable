package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.DateField;
import de.binaerebauten.gleichklang.core.utils.LocaleAware;
import de.binaerebauten.gleichklang.core.view.component.converter.LocalDateConverter;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Component for creation of birthdate field selection with corresponding
 * validation and minimum, maximum possible values
 */
public class BirthDateField extends CustomField<Date> implements LocaleAware, QuickRegistrable
{
	public static final int AGE_OF_MAJORITY = 18;
	public static final int MAXIMAL_POSSIBLE_AGE = 130;
	
	private final DateField birthDateField;
	
	public BirthDateField()
	{
		this(false);
	}
	
	public BirthDateField(boolean required)
	{
		setValidationVisible(false);
		
		birthDateField = ComponentFactory.getInstance().createField(DateField.class);
		setConverter(new LocalDateConverter());
		setRequired(required);
		
		final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, LocaleAware.super.getLocale());
		final String pattern = dateFormat instanceof SimpleDateFormat ? ((SimpleDateFormat) dateFormat).toPattern() : "dd.MM.YYYY";
		
		birthDateField.setDateFormat(pattern);
		
		final String patternLabel = getPatternLabel(dateFormat);
		final String caption = I18N.USER_CAPTION_BIRTHDATE.msg(patternLabel);
		setCaption(caption);
		
		final Calendar endCal = Calendar.getInstance();
		endCal.add(Calendar.YEAR, -AGE_OF_MAJORITY);
		final Date rangeEnd = endCal.getTime();
		
		final Calendar startCal = Calendar.getInstance();
		startCal.add(Calendar.YEAR, -MAXIMAL_POSSIBLE_AGE);
		final Date rangeStart = startCal.getTime();
		
		birthDateField.setRangeStart(rangeStart);
		birthDateField.setRangeEnd(rangeEnd);
		birthDateField.setDateOutOfRangeMessage(I18N.USER_VALIDATION_DATE_OUT_OF_RANGE.msg());
		birthDateField.setParseErrorMessage(I18N.USER_BIRTHDATE_PARSE_ERROR.msg());
		birthDateField.addValueChangeListener(e ->
		{
			this.fireValueChange(false);
			if (birthDateField.getValue() == null)
				birthDateField.addStyleName(CssStyle.EMPTY_ANSWER.getStyleName());
			else
				birthDateField.addStyleName(CssStyle.ANSWERED.getStyleName());
		});
	}
	
	/**
	 * Example date to show the pattern format
	 *
	 * @param dateFormat
	 * @return
	 */
	private String getPatternLabel(DateFormat dateFormat)
	{
		final Calendar cal = Calendar.getInstance();
		cal.set(1970, Calendar.JANUARY, 30);
		return dateFormat.format(cal.getTime());
	}
	
	@Override
	protected Date getInternalValue()
	{
		return birthDateField.getValue();
	}
	
	@Override
	protected void setInternalValue(Date newValue)
	{
		super.setInternalValue(newValue);
		birthDateField.setValue(newValue);
	}
	
	@Override
	public void validate() throws InvalidValueException
	{
		super.validate();
		birthDateField.validate();
	}
	
	@Override
	public Class<? extends Date> getType()
	{
		return Date.class;
	}
	
	@Override
	protected Component initContent()
	{
		return this.birthDateField;
	}
	
	@Override
	public void quickRegister(String value)
	{
		setValue(new Date(1));
	}
}
