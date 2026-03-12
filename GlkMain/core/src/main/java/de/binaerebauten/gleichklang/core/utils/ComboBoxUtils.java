package de.binaerebauten.gleichklang.core.utils;

import com.google.common.base.Strings;
import com.vaadin.server.Resource;
import com.vaadin.ui.ComboBox;

import java.util.Objects;

public class ComboBoxUtils
{
	public static final Object emptyEntry = new Object();
	
	private ComboBoxUtils()
	{
		
	}
	
	public static void addEmptyEntry(ComboBox comboBox, String caption)
	{
		Objects.requireNonNull(comboBox);
		
		comboBox.addItem(emptyEntry);
		comboBox.setItemCaption(emptyEntry, Strings.nullToEmpty(caption));
	}
	
	public static void addEntry(ComboBox comboBox, Object item, String caption, Resource icon)
	{
		Objects.requireNonNull(comboBox);
		Objects.requireNonNull(item);
		
		comboBox.addItem(item);
		if(caption != null) comboBox.setItemCaption(item, caption);
		if(icon != null) comboBox.setItemIcon(item, icon);
	}
	
	public static void addEntry(ComboBox comboBox, Object item)
	{
		addEntry(comboBox, item, null, null);
	}
	
	public static void addEntry(ComboBox comboBox, Object item, String caption)
	{
		addEntry(comboBox, item, caption, null);
	}
	
	public static void addEntry(ComboBox comboBox, Object item, Resource icon)
	{
		addEntry(comboBox, item, null, icon);
	}
	
	public static boolean isEmptySelected(ComboBox comboBox)
	{
		Objects.requireNonNull(comboBox);
		
		return Objects.equals(emptyEntry, comboBox.getValue());
	}
	
	public static <T> T getValue(ComboBox comboBox, Class<T> classType)
	{
		Objects.requireNonNull(comboBox);
		
		final Object value = comboBox.getValue();
		return classType.isInstance(value) ? (T) value : null;
	}
}
