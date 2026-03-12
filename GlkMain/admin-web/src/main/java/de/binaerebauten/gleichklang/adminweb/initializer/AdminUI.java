package de.binaerebauten.gleichklang.adminweb.initializer;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.CssLayout;
import de.binaerebauten.gleichklang.adminweb.navigation.ManualNavigatorFactory;
import de.binaerebauten.gleichklang.adminweb.navigation.ManualNavigatorFactory.ManualNavigationItem;
import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.SignableUser;
import de.binaerebauten.gleichklang.core.navigation.ManualNavigator;
import de.binaerebauten.gleichklang.core.service.AdminService;

@SuppressWarnings("serial")
public class AdminUI extends AppUI
{
	@Override
	protected String getTitle()
	{
		return "Gleichklang Admin";
	}
	
	@Override
	protected void init(VaadinRequest request)
	{
		super.init(request);
		rootContext.getBean(AdminService.class).createInitialAdmin();
	}
	
	@Override
	protected void initView(VaadinRequest request)
	{
		final Admin currentUser = getCurrentUser();
		
		final CssLayout mainContainer = new CssLayout();
		mainContainer.setWidth(100, Unit.PERCENTAGE);
		setContent(mainContainer);
		
		final ManualNavigatorFactory manualNavigatorFactory = new ManualNavigatorFactory(rootContext);
		final ManualNavigator manualNavigator = manualNavigatorFactory.buildNavigator(mainContainer);
		
		if (currentUser == null)
		{
			manualNavigator.navigateTo(ManualNavigationItem.LOGIN);
		}
		else
		{
			manualNavigator.navigateTo(ManualNavigationItem.MAIN);
		}
	}
	
	private Admin getCurrentUser()
	{
		return rootContext.getBean(AdminService.class).getCurrentUser();
	}
	
	@Override
	protected SignableUser.UserType getUserType()
	{
		return SignableUser.UserType.ADMIN;
	}
}
