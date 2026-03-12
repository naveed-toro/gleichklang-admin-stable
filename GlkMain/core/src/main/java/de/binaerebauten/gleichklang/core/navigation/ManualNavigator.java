package de.binaerebauten.gleichklang.core.navigation;

import com.vaadin.ui.ComponentContainer;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;

public class ManualNavigator extends AbstractNavigator
{
	private final ComponentContainer container;
	
	private NavigateView<?> currentView;
	
	public ManualNavigator(ComponentContainer container)
	{
		super();
		
		this.container = container;
	}
	
	public void navigateTo(NavigationEnum navigationEnum, ParametersHolder parameterMap)
	{
		if(navigationEnum != null)
		{
			navigateTo(navigationEnum.getPath(), parameterMap);
		}
	}
	
	public void navigateTo(NavigationEnum navigationEnum)
	{
		if(navigationEnum != null)
		{
			navigateTo(navigationEnum.getPath(), null);
		}
	}
	
	@Override
	protected void navigateTo(String viewKey)
	{
		navigateTo(viewKey, null);
	}
	
	private void navigateTo(String viewKey, ParametersHolder parameterMap)
	{
		final String viewName = getViewName(viewKey);
		final NavigateView<?> newView = getView(viewName);
		
		if(currentView != null)
		{
			currentView.leave();
		}
		
		container.removeAllComponents();
		currentView = newView;
		
		if(newView != null)
		{
			container.addComponent(newView);
			newView.enter(parameterMap);
		}
	}
}
