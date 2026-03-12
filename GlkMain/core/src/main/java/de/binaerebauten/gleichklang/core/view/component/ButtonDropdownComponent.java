package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.ArrayList;
import java.util.List;

import static de.binaerebauten.gleichklang.core.view.component.I18N.USERPROFILE_DROPDOWN_PROMPT;

public class ButtonDropdownComponent extends CustomComponent
{
	public interface RefreshListener
	{
		void onRefreshEvent();
	}
	
	private final Device device;
	
	private final AbstractOrderedLayout layout;
	private final ComboBox comboBox;
	private final List<RefreshListener> refreshListeners = new ArrayList<>();
	
	public ButtonDropdownComponent(Device device)
	{
		this.device = device;
		
		layout = new HorizontalLayout();
		layout.setHeightUndefined();

		if (device == null || device == Device.DESKTOP)
		{
			comboBox = null;
		}
		else
		{
			comboBox = ComponentFactory.getInstance().createField(ComboBox.class);
			comboBox.setTextInputAllowed(false);
			comboBox.setInputPrompt(USERPROFILE_DROPDOWN_PROMPT.msg());
			comboBox.addValueChangeListener(event ->
			{
				final Button button = (Button) comboBox.getValue();
				button.click();
				
				//needed to set selected value of dropdown to "null"
				refreshListeners.forEach(RefreshListener::onRefreshEvent);
			});
			
			layout.setWidth(100, Unit.PERCENTAGE);
			layout.addComponent(comboBox);
		}
		
		setCompositionRoot(layout);
	}
	
	public void setStyleName(CssStyle desktopStyle, CssStyle otherStyle)
	{
		setStyleName(desktopStyle.getStyleName(), otherStyle.getStyleName());
	}
	
	public void setStyleName(String desktopStyle, String otherStyle)
	{
		if (device == null || device == Device.DESKTOP)
		{
			layout.setStyleName(desktopStyle);
		}
		else
		{
			layout.setStyleName(otherStyle);
		}
	}
	
	public void addItem(String caption, ClickListener clickListener)
	{
		final Button button = new Button(caption, clickListener);
		button.setStyleName(CssStyle.TEXT_BUTTON.getStyleName());
		
		if (comboBox == null)
		{
			layout.addComponent(button);
			layout.setComponentAlignment(button, Alignment.MIDDLE_CENTER);
		}
		else
		{
			comboBox.addItem(button);
			comboBox.setItemCaption(button, caption);
		}
	}
	
	public void addRefreshListener(RefreshListener refreshListener)
	{
		if(refreshListener == null) return;
		
		refreshListeners.add(refreshListener);
	}
}
