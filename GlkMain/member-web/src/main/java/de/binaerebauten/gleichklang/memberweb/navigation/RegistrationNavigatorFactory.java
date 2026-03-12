package de.binaerebauten.gleichklang.memberweb.navigation;

import com.vaadin.ui.ComponentContainer;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation.Device;
import de.binaerebauten.gleichklang.core.navigation.AbstractNavigator.DefaultViewItem;
import de.binaerebauten.gleichklang.core.navigation.DefaultNavigator;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.memberweb.initializer.MemberUI;
import de.binaerebauten.gleichklang.memberweb.presenter.RegistrationPresenter;
import de.binaerebauten.gleichklang.memberweb.presenter.UserAdminMessagePresenter;
import de.binaerebauten.gleichklang.memberweb.view.*;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

public class RegistrationNavigatorFactory
{
	public interface RegistrationSuccessHandler
	{
		void onRegistrationSuccess();
	}
	
	private class RegisterViewItem extends DefaultViewItem
	{
		public RegisterViewItem()
		{
			super("REGISTRATION", I18N.MEMBERNAVIGATOR_MENUITEM_REGISTRATION.msg());
		}
		
		@Override
		public NavigatePresenter getPresenter(Device device)
		{
			final RegistrationViewImpl registrationView = new RegistrationViewImpl(MemberUI.isEnableDebugFeatures());
			final SubscriptionOfferPaymentViewImpl subscriptionOfferView = new SubscriptionOfferPaymentViewImpl();
			final ExternalPaymentFormView externalPaymentFormView = new ExternalPaymentFormViewImpl();
			final PersonalDataView personalDataView = new PersonalDataViewImpl();
			final PreConfigNotificationsView preConfigNotificationsView = new PreConfigNotificationsViewImpl();
			
			final RegistrationPresenter registrationPresenter = new RegistrationPresenter(ctx, registrationView, subscriptionOfferView, externalPaymentFormView, personalDataView, preConfigNotificationsView);
			registrationPresenter.addOnFinishClickListener(event -> registrationSuccessHandler.onRegistrationSuccess());
			
			return registrationPresenter;
		}
	}
	
	private class AdminMessageViewItem extends DefaultViewItem
	{
		public AdminMessageViewItem()
		{
			super("MESSAGE", I18N.MEMBERMENUITEM_ENUM_MESSAGETOGLEICHKLANG.msg());
		}
		
		@Override
		public NavigatePresenter getPresenter(Device device)
		{
			return new UserAdminMessagePresenter(ctx, new UserAdminMessageViewImpl(device));
		}
	}
	
	private final ApplicationContext ctx;
	private final RegistrationSuccessHandler registrationSuccessHandler;
	
	public RegistrationNavigatorFactory(ApplicationContext ctx, RegistrationSuccessHandler registrationSuccessHandler)
	{
		Objects.requireNonNull(ctx);
		Objects.requireNonNull(registrationSuccessHandler);
		
		this.ctx = ctx;
		this.registrationSuccessHandler = registrationSuccessHandler;
	}
	
	public DefaultNavigator buildNavigator(ComponentContainer container)
	{
		final DefaultNavigator navigator = new DefaultNavigator(container);
		navigator.setStartViewItem(new RegisterViewItem());

		// Message
		navigator.createParentMenuItem(new AdminMessageViewItem()).setVisible(false);
		navigator.createParentMenuItem(new RegisterViewItem());

		return navigator;
	}
}
