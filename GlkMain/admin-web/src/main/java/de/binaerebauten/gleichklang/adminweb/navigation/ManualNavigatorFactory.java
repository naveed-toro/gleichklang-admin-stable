package de.binaerebauten.gleichklang.adminweb.navigation;

import com.vaadin.ui.ComponentContainer;
import de.binaerebauten.gleichklang.adminweb.presenter.AdminForgotPasswordPresenter;
import de.binaerebauten.gleichklang.adminweb.presenter.AdminLoginPresenter;
import de.binaerebauten.gleichklang.adminweb.view.AdminLoginViewImpl;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.navigation.AbstractNavigator.ViewItem;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.navigation.ManualNavigator;
import de.binaerebauten.gleichklang.adminweb.presenter.AdminMainPresenter;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.view.ForgotPasswordViewImpl;
import de.binaerebauten.gleichklang.adminweb.view.AdminMainView;
import de.binaerebauten.gleichklang.adminweb.view.AdminMainViewImpl;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Objects;

public class ManualNavigatorFactory
{
	public enum ManualNavigationItem implements NavigationEnum
	{
		LOGIN,
		FORGET_PASSWORD,
		MAIN;
		
		@Override
		public String getPath()
		{
			return name();
		}
	}
	
	private class ManualViewItem implements ViewItem
	{
		private final ManualNavigationItem manualNavigationItem;
		
		public ManualViewItem(ManualNavigationItem manualNavigationItem)
		{
			this.manualNavigationItem = manualNavigationItem;
		}
		
		@Override
		public String getKey()
		{
			return manualNavigationItem.getPath();
		}
		
		@Override
		public String getCaption()
		{
			return manualNavigationItem.toString();
		}
		
		@Override
		public NavigatePresenter getPresenter(Device device)
		{
			return ManualNavigatorFactory.this.getPresenter(manualNavigationItem, device);
		}
	}
	
	private final ApplicationContext ctx;
	
	private ManualNavigator navigator;
	
	public ManualNavigatorFactory(ApplicationContext ctx)
	{
		Objects.requireNonNull(ctx);
		
		this.ctx = ctx;
	}
	
	public ManualNavigator buildNavigator(ComponentContainer container)
	{
		final ManualNavigator navigator = new ManualNavigator(container);
		
		Arrays.stream(ManualNavigationItem.values()).map(ManualViewItem::new).forEach(navigator::addViewItem);
		
		this.navigator = navigator;
		
		return navigator;
	}
	
	private NavigatePresenter getPresenter(ManualNavigationItem manualNavigationItem, Device device)
	{
		switch (manualNavigationItem)
		{
			case LOGIN:
				return new AdminLoginPresenter(ctx, new AdminLoginViewImpl(), navigator);
			case FORGET_PASSWORD:
				return new AdminForgotPasswordPresenter(ctx, new ForgotPasswordViewImpl(), navigator);
			case MAIN:
				final AdminMainView adminMainView = new AdminMainViewImpl();
				final DefaultNavigatorFactory defaultNavigatorFactory = new DefaultNavigatorFactory(ctx);
				final DefaultNavigator defaultNavigator = defaultNavigatorFactory.buildNavigator(adminMainView.getViewPort());
				return new AdminMainPresenter(ctx, adminMainView, defaultNavigator);
		}
		
		return null;
	}
}
