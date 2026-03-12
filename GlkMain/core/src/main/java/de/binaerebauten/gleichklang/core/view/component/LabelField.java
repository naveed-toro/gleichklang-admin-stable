package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;

/**
 * The data binding is not possible for a normal label. This LabelField makes it
 * possible to bind a attribute to a read only field.
 * 
 * @author fhessel
 *
 */
@SuppressWarnings("serial")
public class LabelField extends CustomField<String>
{
	private final Label label = new Label();
	
	public LabelField()
	{}
	
	public LabelField(String caption)
	{
		setCaption(caption);
	}

	public LabelField(String caption, String value)
	{
		setCaption(caption);
		setValue(value);
	}
	
	@Override
	protected Component initContent()
	{
		return label;
	}
	
	@Override
	protected void setInternalValue(String newValue)
	{
		super.setInternalValue(newValue);
		label.setValue(newValue);
	}
	
	@Override
	public Class<? extends String> getType()
	{
		return String.class;
	}
	
	/**
	 * Sets the content mode of the Label.
	 * 
	 * @param contentMode
	 *            the New content mode of the label.
	 * 
	 * @see ContentMode
	 */
	public void setContentMode(ContentMode contentMode)
	{
		label.setContentMode(contentMode);
	}
}
