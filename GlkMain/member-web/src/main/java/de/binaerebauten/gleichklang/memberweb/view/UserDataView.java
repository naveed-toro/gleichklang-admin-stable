package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.validator.UniqueValidationException;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.SubNavigateView;
import de.binaerebauten.gleichklang.core.view.component.NavigationComponent.SubNavigationListener;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.view.UserDataView.UserDataTab;
import de.binaerebauten.gleichklang.memberweb.view.UserDataView.UserDataViewListener;
import de.binaerebauten.gleichklang.memberweb.view.component.PersonalDataComponent.PersonalDataHandler;

import java.io.IOException;
import java.sql.SQLException;

public interface UserDataView extends SubNavigateView<UserDataTab, UserDataViewListener>
{
	enum UserDataTab implements DefaultEnumI18N, NavigationEnum
	{
		USERDATA("userdata"),
		PASSWORD("password"),
		EMAIL("email"),
		MESSAGES("messages"),
		CANCELLATION("cancellation");

		private final String path;

		UserDataTab(String path)
		{
			this.path = path;
		}

		@Override
		public String toString()
		{
			return msg();
		}
		
		@Override
		public String getPath()
		{
			return path;
		}
	}
	
	interface UserDataViewListener extends NavigateView.NavigateViewListener, SubNavigationListener
	{


        User saveUser(User user);

        void showMessages();
		void issueExtention();

		void savePassword(String oldPassword, String newPassword) throws ValidationException;

		void sendChangeMail(User user, String newMail) throws UniqueValidationException;

		/**
		 * Called when the user presses the cancel subscription button.
		 *
		 * @param user
		 * @param deleteUser
		 * @param mailsAfterCancel
		 */
		void cancelSubscription(User user, boolean deleteUser, boolean mailsAfterCancel);
	}
	void setUser(User user);

	void initPasswordTab();
	
	/**
	 * Sets the visiblity of the cancel subscription tab.
	 *
	 * @param visible
	 */
	void setCancelSubscriptionVisible(boolean visible);
	
	void initPersonalDataTab(PersonalDataHandler personalDataHandler);
	
	UserDataTab getSelectedUserDataTab();
}
