package de.binaerebauten.gleichklang.core.view;

import com.vaadin.ui.Component;

import de.binaerebauten.gleichklang.core.view.DefaultView.DefaultViewListener;

public interface DefaultView<T extends DefaultViewListener> extends Component
{
	interface DefaultViewListener
	{}
	
	void setListener(T listener);
}
