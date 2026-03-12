package de.binaerebauten.gleichklang.core.view;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.view.NavigateView.NavigateViewListener;

@SuppressWarnings("serial")
public abstract class AbstractNavigateView<T extends NavigateViewListener> extends AbstractDefaultView<T> implements NavigateView<T>
{
	@SuppressWarnings("unchecked")
	@Override
	public void enter(ViewChangeEvent event)
	{
		if (event.getOldView() != null && event.getOldView() instanceof NavigateView<?>)
		{
			final NavigateView<?> oldView = (AbstractNavigateView<?>) event.getOldView();
			oldView.leave();
		}

		enter(event.getParameters());
	}
	
	@Override
	public void leave()
	{
		fireEvent(NavigateViewListener::leave);
	}
	
	@Override
	public void enter(String parameters)
	{
		fireEvent(listener -> listener.enter(parameters));
	}
	
	@Override
	public void enter(ParametersHolder parameterMap)
	{
		fireEvent(listener -> listener.enter(parameterMap));
	}
	
	@Override
	public void initView(Device device)
	{
		switch(device)
		{
			case MOBILE:
				initMobileView();
				break;
			case TABLET:
				initTabletView();
				break;
			case DESKTOP:
				initDesktopView();
				break;
		}
	}
}
