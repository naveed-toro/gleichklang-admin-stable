package de.binaerebauten.gleichklang.adminweb.view.component;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;
import de.binaerebauten.gleichklang.adminweb.view.I18N;
import de.binaerebauten.gleichklang.core.model.payment.Subscription;
import de.binaerebauten.gleichklang.core.model.payment.Subscription.SubscriptionState;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.view.component.ComponentFactory;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class UserQuickBar extends CustomComponent
{
	public interface UserQuickBarListener
	{
		void openUser(User user);

		void adminBlocked(User user);
		
		void deactivateAutoRenewal(User user, Subscription currentSubscription);
		
		void deleteUserData(User user);
		
		void cancelUser(User user, Subscription currentSubscription);
		
		void generateLogin(User user);
		
		String generatePassword();
		
		void savePassword(User user, String password) throws UniqueValidationException;
		
		Subscription getCurrentSubscription(User user);

		void resetUserLogin();

		void writeMessage(User user);

		void openReminder(User user);

		void openRevocationPopup(User user);

		void closePopup();
	}
	
	private final HorizontalLayout layout;
	private final Map<Component, Supplier<Boolean>> components = new HashMap<>();
	private User user = null;
	private Subscription subscription = null;
	private UserQuickBarListener listener;
	private final PasswordField passwordField;

		public final Button confirmBlock;

	public UserQuickBar()
	{
		layout =  new HorizontalLayout();
		layout.setSpacing(true);
		layout.setSizeFull();
		layout.setWidthUndefined();
		
		addButton(I18N.USERMANAGE_ACTION_SHOW.msg(), event -> listener.openUser(user));
		addButton("Verlängerung ausstellen", event -> listener.deactivateAutoRenewal(user, subscription), withAutoRenewalEnabler());
		addButton(I18N.USERMANAGE_ACTION_DELETEDATA.msg(), event -> listener.deleteUserData(user), notDeletedEnabler());
		addButton("Fristlos kündigen", event -> listener.cancelUser(user, subscription), withSubscriptionEnabler());
		confirmBlock = addButton("Confirm block", event -> listener.adminBlocked(user), userBlocked());
		addButton(I18N.USERMANAGE_ACTION_CREATELOGIN.msg(), event -> listener.generateLogin(user), notDeletedEnabler());
		passwordField = ComponentFactory.getInstance().createField(PasswordField.class, "Passwort");
		addComponent(passwordField);
		
		addButton("*", event -> passwordField.setValue(listener.generatePassword()));
		final Button savePasswordButton = addButton("S", event -> savePassword(listener, passwordField), () -> false);

		passwordField.addTextChangeListener(event -> savePasswordButton.setEnabled(!Strings.isNullOrEmpty(passwordField.getValue())));
		passwordField.addValueChangeListener(event -> savePasswordButton.setEnabled(!Strings.isNullOrEmpty(passwordField.getValue())));
		passwordField.addShortcutListener(createEnterShortcut(passwordField, savePasswordButton));
		
		setCompositionRoot(layout);
	}
	
	private ShortcutListener createEnterShortcut(PasswordField passwordField, Button savePasswordButton)
	{
		return new ShortcutListener("Default Key", KeyCode.ENTER, null)
		{
			@Override
			public void handleAction(Object sender, Object target)
			{
				if(passwordField == target)
				{
					savePasswordButton.click();
				}
			}
		};
	}
	
	private void savePassword(UserQuickBarListener listener, PasswordField passwordField)
	{
		try
		{
			listener.savePassword(user, passwordField.getValue());
			passwordField.setValue(null);
		}
		catch (UniqueValidationException e)
		{
			Notification.show("Passwort konnte nicht gesetzt werden", Type.ERROR_MESSAGE);
		}
	}
	
	public void setListener(UserQuickBarListener listener)
	{
		this.listener = listener;
		refresh();
	}
	
	public void onUserChanged(User user)
	{
		this.user = user;
		refresh();
	}
	
	private void refresh()
	{
		this.subscription = listener != null && user != null ? listener.getCurrentSubscription(user) : null;
		components.forEach((c, s) -> c.setEnabled(s.get()));
		passwordField.setValue(null);
	}
	
	private Button addButton(String caption, ClickListener listener)
	{
		return addButton(caption, listener, defaultEnabler());
	}
	
	private Button addButton(String caption, ClickListener listener, Supplier<Boolean> enabler)
	{
		final Button button = new Button(caption, listener);
		addComponent(button, enabler);
		
		return button;
	}
	
	private void addComponent(Component component)
	{
		addComponent(component, defaultEnabler());
	}
	
	private void addComponent(Component component, Supplier<Boolean> enabler)
	{
		component.setEnabled(false);
		
		components.put(component, enabler);
		layout.addComponent(component);
		layout.setComponentAlignment(component, Alignment.BOTTOM_CENTER);
	}
	
	private Supplier<Boolean> defaultEnabler()
	{
		return () -> user != null && listener != null;
	}
	
	private Supplier<Boolean> notDeletedEnabler()
	{
		return () -> defaultEnabler().get() && !user.isDataDeleted();
	}

	private Supplier<Boolean> userBlocked()
	{
		return () -> defaultEnabler().get() && user.isBlocked();
	}
	
	private Supplier<Boolean> withSubscriptionEnabler()
	{
		final EnumSet<SubscriptionState> validSubscriptionStates = EnumSet.of(SubscriptionState.ACTIVE, SubscriptionState.EXPIRING);
		return () -> defaultEnabler().get() && subscription != null && validSubscriptionStates.contains(subscription.getState());
	}
	
	private Supplier<Boolean> withAutoRenewalEnabler()
	{
		return () -> withSubscriptionEnabler().get() && subscription.isAutomaticRenewal();
	}
}
