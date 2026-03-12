package de.binaerebauten.gleichklang.adminweb.view;

import de.binaerebauten.gleichklang.adminweb.presenter.CsvSeparator;
import de.binaerebauten.gleichklang.adminweb.view.UserManageView.UserManageTab;
import de.binaerebauten.gleichklang.adminweb.view.UserManageView.UserManageViewListener;
import de.binaerebauten.gleichklang.adminweb.view.component.UserQuickBar.UserQuickBarListener;
import de.binaerebauten.gleichklang.core.model.mail.Newsletter;
import de.binaerebauten.gleichklang.core.model.mail.UndeliverableMail;
import de.binaerebauten.gleichklang.core.model.payment.SubscriptionWithState;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.file.AbstractUpload.UploadResult;
import de.binaerebauten.gleichklang.core.service.file.InMemoryUpload;
import de.binaerebauten.gleichklang.core.utils.DefaultEnumI18N;
import de.binaerebauten.gleichklang.core.utils.filter.FilterSpecificationBuilder;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.SubNavigateView;
import de.binaerebauten.gleichklang.core.view.component.FilterControlComponent.FilterControlHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.NavigationComponent.SubNavigationListener;
import de.binaerebauten.gleichklang.core.view.component.NavigationEnum;
import org.springframework.data.jpa.domain.Specification;
import java.io.InputStream;


public interface UserManageView extends SubNavigateView<UserManageTab, UserManageViewListener>
{
	enum UserManageTab implements DefaultEnumI18N, NavigationEnum
	{
		USER_CONTROL,
		BLACKLIST,
		NEWSLETTER,
		BLOCKED,
		USER_STATISTICS
		;

		@Override
		public String getPath()
		{
			return name();
		}

		@Override
		public String toString()
		{
			return msg();
		}
	}

	interface UserManageViewListener extends NavigateView.NavigateViewListener, SubNavigationListener
	{

		void resetLogins();

		byte[] export(Specification<User> specification);

		boolean isOnline(User user);

		boolean isBlacklisted(User user);

		boolean isBlocked(User user);

		void removeFromBlacklist(UndeliverableMail undeliverableMail);

		void removeBlacklist(User user);

		void addToBlacklist(User user);

		void addToUnblockList(User user);

		void adminBlocked(User user);

		void ShowUserProfile(User user);

		void ShowBlackListedUserProfile(UndeliverableMail undeliverableMail);

		void onBlackListUpload(InMemoryUpload uploadFile, UploadResult uploadResult, CsvSeparator value);

		void onNewsletterUpload(InMemoryUpload uploadFile, UploadResult uploadResult);

		void deleteFromNewsletter(Newsletter newsletter);

		InputStream exportNewsletter();

		void addToBlockList(User item);

		void writeMessage(User item);

		void openReminderPopup(User item);

		void showPin(User user);

		boolean audioForFriendship(User user);

		boolean audioForPartnership(User user);

		SubscriptionWithState getCurrentSubscription(User user);

		String lastLoginDate(User user);

		//void removeFromBlacklist(Object item);
	}

	void setUserHandler(LazyBeanFilteredItemsHandler<User> handler);

	void setBlockedUserHandler(LazyBeanFilteredItemsHandler<User> handler);

	void setUndeliverableMailHandler(LazyBeanFilteredItemsHandler<UndeliverableMail> handler);

	void setNewsletterHandler(LazyBeanFilteredItemsHandler<Newsletter> handler);

	void setBlockedUserFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder);

	void setUserFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder);

	void setUndeliverableMailFilterHandlerAndBuilder(FilterControlHandler filterControlHandler, FilterSpecificationBuilder filterSpecificationBuilder);

	void setUserQuickBarListener(UserQuickBarListener userQuickBarListener);

	void refreshBlockedUsers();
}
