package de.binaerebauten.gleichklang.core.view.component;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;

public abstract class Popup extends Window
{
	public Popup()
	{
		this("");
	}

	public Popup(String caption)
	{
		super(caption);
		setModal(true);
		setResizable(false);
		center();
	}

	public void show()
	{
		if (!this.isAttached())
			UI.getCurrent().addWindow(this);
	}

	public void onDeviceChanged(ClientInformation.Device device)
	{

	}
	
	public String getUniqueName()
	{
		return this.getClass().getName();
	}
}
