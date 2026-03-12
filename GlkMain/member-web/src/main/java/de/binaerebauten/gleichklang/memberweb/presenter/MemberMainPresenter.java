package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.UI;
import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.presenter.ManualNavigatePresenter;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.security.AuthenticationService.RedirectGoal;
import de.binaerebauten.gleichklang.core.service.ClientInformationService;
import de.binaerebauten.gleichklang.core.service.UserService;
import de.binaerebauten.gleichklang.core.utils.ParametersHolder;
import de.binaerebauten.gleichklang.core.view.component.MenuItem;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.ChangePasswordPopup;
import de.binaerebauten.gleichklang.memberweb.navigation.DefaultNavigatorFactory;
import de.binaerebauten.gleichklang.memberweb.navigation.RegistrationNavigatorFactory;
import de.binaerebauten.gleichklang.memberweb.service.LanguageService;
import de.binaerebauten.gleichklang.memberweb.view.MemberMainView;
import de.binaerebauten.gleichklang.memberweb.view.popup.MissingConfirmationPopup;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

public class MemberMainPresenter extends ManualNavigatePresenter implements MemberMainView.MemberMainViewListener, ViewChangeListener
{
	private final MemberMainView view;
	private final DefaultNavigator navigator;
	
	private final AuthenticationService authenticationService;
	private final LanguageService languageService;
	
	public MemberMainPresenter(ApplicationContext ctx, MemberMainView mainView)
	{
		super(mainView);
		
		this.view = mainView;
		
		
		final UserService userService = ctx.getBean(UserService.class);
		final User currentUser = userService.getCurrentUser();
		
		this.authenticationService = ctx.getBean(AuthenticationService.class);
		this.languageService = ctx.getBean(LanguageService.class);
		
		this.navigator = createNavigator(ctx, isRegistration(currentUser), mainView.getViewPort());
		initNavigator(mainView);
		
		ctx.getBean(ClientInformationService.class).updateClientInformation(currentUser, navigator.getDevice());
		
		mainView.setListener(this);
		mainView.setCurrentUser(currentUser);
		
		if (isRegistration(currentUser))
		{
			mainView.addStyleName(CssStyle.REGISTRATION_MAIN_VIEW.getStyleName());
		}
		
		if(userService.needsEmailConfirmation(currentUser))
		{
			final MissingConfirmationPopup popup = new MissingConfirmationPopup(ctx,userService::sendChangeMail, currentUser , this.navigator);
			tryOpenPopup(popup);
		}
		
		if (currentUser.isResetPassword())
		{
			final ChangePasswordPopup changePasswordPopup = new ChangePasswordPopup(userService::changePassword);
			changePasswordPopup.show();
		}
	}
	
	private boolean isRegistration(User currentUser)
	{
		//return !MemberStatus.REGISTERED.equals(currentUser.getMemberStatus());
		return MemberStatus.REGISTRATION.equals(currentUser.getMemberStatus());
	}
	
	private DefaultNavigator createNavigator(ApplicationContext ctx, boolean isRegistration, ComponentContainer container)
	{
		if (isRegistration)
		{
			final RegistrationNavigatorFactory registrationNavigatorFactory = new RegistrationNavigatorFactory(ctx, () -> Page.getCurrent().reload());
			return registrationNavigatorFactory.buildNavigator(container);
		}
		else
		{
			final DefaultNavigatorFactory defaultNavigatorFactory = new DefaultNavigatorFactory(ctx);
			return defaultNavigatorFactory.buildNavigator(container);
		}
	}
	
	private void initNavigator(MemberMainView mainView)
	{
		Collection<MenuItem<String>> items = this.navigator.getParentItems();
		if (items.isEmpty())
		{
			items = this.navigator.getMenuItems();
			items.forEach(mainView::addMenuItem);
		}
		else
		{
			items.forEach(mainView::addParentMenuItem);
		}
		
		// adds view change listener to navigator
		// to highlight top level menu for active view
		this.navigator.addViewChangeListener(this);
	}
	
	@Override
	public void changeView(MenuItem<String> menuItem)
	{
		if (menuItem != null && !menuItem.isDisabled())
		{
			navigator.navigateTo(menuItem.getContent());
			UI.getCurrent().setScrollTop(0);
			UI.getCurrent().requestRepaint();
		}
	}
	
	@Override
	public void enter(ParametersHolder parameterMap)
	{
	
	}
	
	@Override
	public void logout()
	{
		authenticationService.logout(RedirectGoal.LANDING);
	}
	
	@Override
	public boolean beforeViewChange(ViewChangeEvent event)
	{
		return true;
	}
	
	@Override
	public void afterViewChange(ViewChangeEvent event)
	{
		final String viewName = event.getViewName();
		final MenuItem menuItem = navigator.getMenuByKey(viewName);
		
		if (menuItem != null)
		{
			view.setSelectedMenuItem(menuItem);
		}
	}
	
	@Override
	public void changeLanguage(Language language)
	{
		if(languageService.changeLanguage(language))
		{
			UI.getCurrent().getPage().reload();
		}
	}
}

