package de.binaerebauten.gleichklang.core.view;

import com.vaadin.ui.CustomComponent;
import de.binaerebauten.gleichklang.core.view.DefaultView.DefaultViewListener;

import java.util.function.Consumer;

@SuppressWarnings("serial")
public abstract class AbstractDefaultView<T extends DefaultViewListener> extends CustomComponent implements DefaultView<T>
{
	private T listener = null;
	
	@Override
	public void setListener(T listener)
	{
		this.listener = listener;
	}
	
	protected void fireEvent(Consumer<? super T> eventAction)
	{
		if(listener != null) eventAction.accept(listener);
	}
	
	public T getListener()
	{
		return listener;
	}
}
