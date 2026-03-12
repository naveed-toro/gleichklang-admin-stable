package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

/**
 * An easy to use horizontal line via label.
 * 
 * @author fhessel
 *
 */
@SuppressWarnings("serial")
public class HorizontalLine extends CustomComponent
{
	public HorizontalLine()
	{
		addStyleName("question-group-seperator");
		final Label label = new Label("<hr/>", ContentMode.HTML);
		setCompositionRoot(label);
	}
}
