package de.binaerebauten.gleichklang.core.presenter;

import com.google.common.base.Strings;
import de.binaerebauten.gleichklang.core.view.SubNavigateView;
import de.binaerebauten.gleichklang.core.view.component.NavigationComponent.SubNavigationListener;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;

import java.util.EnumSet;

public abstract class SubNavigatePresenter<T extends Enum<T> & NavigationEnum> extends NavigatePresenter implements SubNavigationListener
{
	private final Class<T> navigationEnumClass;
	private final NavigationEnum baseNavigationEnum;
	private final SubNavigateView<T, ?> view;
	
	private T currentNavigationEnum;
	private String currentParameters;
	
	public SubNavigatePresenter(SubNavigateView<T, ?> view, Class<T> navigationEnumClass, NavigationEnum baseNavigationEnum)
	{
		super(view);
		this.view = view;
		this.navigationEnumClass = navigationEnumClass;
		this.baseNavigationEnum = baseNavigationEnum;
	}
	
	@Override
	public void enter(String parameters)
	{
		final EnumSet<T> enumNavigationSet = EnumSet.allOf(navigationEnumClass);
		currentNavigationEnum = enumNavigationSet.stream().sorted().findFirst().orElse(null);
		currentParameters = parameters;
		
		if(!Strings.isNullOrEmpty(parameters))
		{
			for(T e : enumNavigationSet)
			{
				final String path = e.getPath();
				if(parameters.equals(path) || parameters.startsWith(path + "/"))
				{
					currentParameters = "";
					if (parameters.length() > path.length() + 1)
					{
						currentParameters = parameters.substring(path.length() + 1);
					}
					currentNavigationEnum = e;
				}
			}
		}
		
		refresh();
	}
	
	public abstract void enter(T navigationEnum, String parameters);
	
	@Override
	public void navigateToSubView(NavigationEnum navigationEnum)
	{
		navigateTo(baseNavigationEnum, navigationEnum);
	}
	
	protected void refresh()
	{
		enter(currentNavigationEnum, currentParameters);
		view.selectSubNavigation(currentNavigationEnum);
	}
}
