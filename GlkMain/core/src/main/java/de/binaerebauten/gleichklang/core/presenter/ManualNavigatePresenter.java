package de.binaerebauten.gleichklang.core.presenter;

import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;

public abstract class ManualNavigatePresenter extends NavigatePresenter
{
	public ManualNavigatePresenter(NavigateView<?> view)
	{
		super(view);
	}
	
	@Override
	public final void enter(String parameters)
	{
	}
	
	@Override
	public abstract void enter(ParametersHolder parameterMap);
	
	@Override
	public void navigateTo(NavigationEnum... navigationEnums)
	{
		throw new UnsupportedOperationException();
	}
}
