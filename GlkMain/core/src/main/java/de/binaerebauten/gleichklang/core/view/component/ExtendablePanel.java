package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * This is an extendable settings panel. It has a checkbox which activates
 * the extension and the content, which is shown when the check box is activated
 * Created by michael on 14/07/15.
 */
public class ExtendablePanel extends VerticalLayout
{
	private final CheckBox checkbox;
	private final Component content;
	private final boolean showOnEnabled;
	
	public ExtendablePanel(String checkBoxCaption, Component content, boolean showOnEnabled)
	{
		this.content = content;
		this.checkbox = (CheckBox) ComponentFactory.getInstance().createFieldByType(boolean.class, checkBoxCaption);
		this.showOnEnabled = showOnEnabled;
		
		this.checkbox.addValueChangeListener(valueChangeEvent -> refreshVisibility());
		this.addComponent(checkbox);
		
		checkbox.setValue(!showOnEnabled);
	}
	
	private void refreshVisibility()
	{
		final boolean visible = showOnEnabled ? checkbox.getValue() : !checkbox.getValue();
		
		if (visible)
		{
			this.addComponent(this.content);
		}
		else
		{
			this.removeComponent(this.content);
		}
	}
	
	public void addValueChangeListener(ValueChangeListener listener)
	{
		checkbox.addValueChangeListener(listener);
	}
	
	public void setChecked(boolean checked)
	{
		checkbox.setValue(checked);
	}
}
