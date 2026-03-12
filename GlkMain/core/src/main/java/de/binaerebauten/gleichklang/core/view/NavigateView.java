package de.binaerebauten.gleichklang.core.view;

import com.vaadin.navigator.View;

import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.view.NavigateView.NavigateViewListener;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;

public interface NavigateView<T extends NavigateViewListener> extends View, DefaultView<T>
{
	interface NavigateViewListener extends DefaultViewListener
	{
		void enter(String parameters);
		
		void enter(ParametersHolder parameterMap);
		
		void enter();
		
		void leave();
		
		void navigateTo(NavigationEnum... navigationEnums);
	}
	
	void leave();
	
	void enter(String parameters);
	
	void enter(ParametersHolder parameterMap);
	
	default void onDeviceChanged(Device device)
	{
		initView(device);
	}
	
	void initView(Device device);
	
	default void initDesktopView()
	{}
	
	default void initMobileView()
	{
		initDesktopView();
	}
	
	default void initTabletView()
	{
		initMobileView();
	}
}
