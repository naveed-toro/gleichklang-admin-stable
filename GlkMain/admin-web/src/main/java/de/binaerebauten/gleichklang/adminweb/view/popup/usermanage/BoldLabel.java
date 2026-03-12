package de.binaerebauten.gleichklang.adminweb.view.popup.usermanage;

import com.google.common.base.Strings;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class BoldLabel extends Label
{
	public BoldLabel()
	{
		this(null);
	}
	
	public BoldLabel(String content)
	{
		setContentMode(ContentMode.HTML);
		setValue(content);
	}
	
	@Override
	public void setContentMode(ContentMode contentMode)
	{
		super.setContentMode(ContentMode.HTML);
	}
	
	@Override
	public void setValue(String newStringValue)
	{
		super.setValue(createValue(newStringValue));
	}
	
	private String createValue(String text)
	{
		if(Strings.isNullOrEmpty(text)) return text;
		return "<B>" + text + "</B>";
	}
}
