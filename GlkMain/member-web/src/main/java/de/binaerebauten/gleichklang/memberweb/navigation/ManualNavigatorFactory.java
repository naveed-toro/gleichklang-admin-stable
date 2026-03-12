package de.binaerebauten.gleichklang.memberweb.navigation;

import com.vaadin.ui.ComponentContainer;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.navigation.AbstractNavigator.ViewItem;
import de.binaerebauten.gleichklang.core.navigation.ManualNavigator;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.view.ForgotPasswordViewImpl;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import de.binaerebauten.gleichklang.memberweb.presenter.MemberForgotPasswordPresenter;
import de.binaerebauten.gleichklang.memberweb.presenter.MemberLoginPresenter;
import de.binaerebauten.gleichklang.memberweb.presenter.MemberMainPresenter;
import de.binaerebauten.gleichklang.memberweb.presenter.PreregistrationPresenter;
import de.binaerebauten.gleichklang.memberweb.view.*;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Objects;

public class ManualNavigatorFactory
{
	public enum ManualNavigationItem implements NavigationEnum
	{
		LOGIN,
		FORGOT_PASSWORD,
		MAIN,
		REGISTER;
		
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
		final boolean languageSelectionActivated = ctx.getEnvironment().getProperty("language.selection.activated", Boolean.class, false);
		
		switch (manualNavigationItem)
		{
			case LOGIN:
				return new MemberLoginPresenter(ctx, new MemberLoginViewImpl(languageSelectionActivated), navigator);
			case FORGOT_PASSWORD:
				return new MemberForgotPasswordPresenter(ctx, new ForgotPasswordViewImpl(), navigator);
			case MAIN:
				return new MemberMainPresenter(ctx, new MemberMainViewImpl(languageSelectionActivated, device));
			case REGISTER:
				final String landingRoot = ctx.getBean(Environment.class).getProperty("external.links.root", "");
				final PreregistrationView preregistrationView = new PreRegistrationViewImpl(landingRoot, languageSelectionActivated);
				return new PreregistrationPresenter(ctx, preregistrationView, navigator);
		}
		
		return null;
	}
}
