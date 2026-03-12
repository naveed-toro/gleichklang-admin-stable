package de.binaerebauten.gleichklang.adminweb.navigation;

import com.vaadin.ui.ComponentContainer;
import de.binaerebauten.gleichklang.adminweb.presenter.*;
import de.binaerebauten.gleichklang.adminweb.view.*;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.AdminRole;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.navigation.AbstractNavigator.ViewItem;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.service.AdminService;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.view.component.MenuItem;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

public class DefaultNavigatorFactory
{
	public enum AdminMenuItem implements DefaultEnumI18N, NavigationEnum
	{
		ADMIN_MANAGEMENT(AdminRole.ADMIN_MANAGEMENT),
		ADMIN_MESSAGES(AdminRole.ADMIN_MESSAGES),
		QUESTIONNAIRE(AdminRole.QUESTIONNAIRE),
		MATCHING(AdminRole.MATCHING),
		STATISTICS(AdminRole.STATISTICS),
		PRODUCTS(AdminRole.PRODUCTS),
		SUBSCRIPTIONS(AdminRole.SUBSCRIPTIONS),
		HEIDELPAY(AdminRole.HEIDELPAY),
		BANK_ACCOUNTS(AdminRole.BANK_ACCOUNTS),
		INVOICE(AdminRole.INVOICE),
		FILTER(AdminRole.FILTER),
		USER_CONTROL(AdminRole.USER_CONTROL),
		TRANSLATION(AdminRole.TRANSLATION),
		NEWS(AdminRole.NEWS),
		SERIALIZATION(AdminRole.SERIALIZATION),
		SYSTEM_CONFIGURATION(AdminRole.SYSTEM_CONFIGURATION),
		SCAMMING(AdminRole.SECURITY);
		
		private final AdminRole adminRole;
		
		AdminMenuItem(AdminRole adminRole)
		{
			this.adminRole = adminRole;
		}
		
		public AdminRole getAdminRole()
		{
			return adminRole;
		}
		
		@Override
		public String toString()
		{
			return msg();
		}
		
		@Override
		public String getPath()
		{
			return name();
		}
	}
	
	private class AdminViewItem implements ViewItem
	{
		private final AdminMenuItem adminMenuItem;
		
		public AdminViewItem(AdminMenuItem adminMenuItem)
		{
			this.adminMenuItem = adminMenuItem;
		}
		
		@Override
		public String getKey()
		{
			return adminMenuItem.getPath();
		}
		
		@Override
		public String getCaption()
		{
			return adminMenuItem.toString();
		}
		
		@Override
		public NavigatePresenter getPresenter(Device device)
		{
			return DefaultNavigatorFactory.this.getPresenter(adminMenuItem);
		}
	}
	
	private final ApplicationContext ctx;
	private AdminViewItem startMenuItem = null;
	
	public DefaultNavigatorFactory(ApplicationContext ctx)
	{
		Objects.requireNonNull(ctx);
		
		this.ctx = ctx;
	}
	
	public DefaultNavigator buildNavigator(ComponentContainer container)
	{
		final DefaultNavigator navigator = new DefaultNavigator(container);
		createMenu(navigator);
		
		return navigator;
	}
	
	private void createMenu(DefaultNavigator navigator)
	{
		final Admin currentAdmin = ctx.getBean(AdminService.class).getCurrentUser();
		
		for (AdminMenuItem adminMenuItem : AdminMenuItem.values())
		{
			createAdminMenuItem(currentAdmin, adminMenuItem, navigator);
		}
	}
	
	private MenuItem createAdminMenuItem(Admin currentAdmin, AdminMenuItem adminMenuItem, DefaultNavigator navigator)
	{
		if (currentAdmin.getRoles().contains(adminMenuItem.getAdminRole()))
		{
			final AdminViewItem viewItem = new AdminViewItem(adminMenuItem);
			if (startMenuItem == null)
			{
				startMenuItem = viewItem;
				navigator.setStartViewItem(startMenuItem);
			}
			return navigator.createParentMenuItem(viewItem);
		}
		return null;
	}
	
	private NavigatePresenter getPresenter(AdminMenuItem menuItem)
	{
		switch (menuItem)
		{
			case ADMIN_MANAGEMENT:
				return new AdminManagementPresenter(ctx, new AdminManagementViewImpl());
			
			case QUESTIONNAIRE:
				return new QuestionnaireAdminPresenter(ctx, new QuestionnaireAdminViewImpl());
			
			case MATCHING:
				return new MatchingPresenter(ctx, new MatchingViewImpl());
			
			case STATISTICS:
				return new StatisticsPresenter(ctx, new StatisticsViewImpl());
				
			case PRODUCTS:
				return new ProductAdminPresenter(ctx, new ProductAdminViewImpl());
			
			case SUBSCRIPTIONS:
				return new SubscriptionAdminPresenter(ctx, new SubscriptionAdminViewImpl());
				
			case HEIDELPAY:
				return new HeidelpayPresenter(ctx, new HeidelpayViewImpl());
			
			case BANK_ACCOUNTS:
				return new BankAccountPresenter(ctx, new BankAccountViewImpl());
			
			case INVOICE:
				return new InvoiceAdminPresenter(ctx, new InvoiceAdminViewImpl());
			
			case FILTER:
				return new FilterPresenter(ctx, new FilterViewImpl());
			
			case USER_CONTROL:
				return new UserManagePresenter(ctx, new UserManageViewImpl());
			
			case TRANSLATION:
				return new TranslationPresenter(ctx, new TranslationViewImpl());
			
			case NEWS:
				return new NewsPresenter(ctx, new NewsViewImpl());
			
			case ADMIN_MESSAGES:
				return new AdminMessagePresenter(ctx, new AdminMessageViewImpl());
			case SYSTEM_CONFIGURATION: {

				//          TODO : changes for emails mapping
				return new SystemConfigurationPresenter(ctx, new SystemConfigurationViewImpl());
			}
			case SERIALIZATION:
				return new SerializationPresenter(ctx, new SerializationViewImpl());
			
			case SCAMMING:
				return new ScammingPresenter(ctx, new ScammingViewImpl());
		}
		
		return null;
	}
}
