package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileData;
import de.binaerebauten.gleichklang.core.view.component.UserProfile.UserProfileListener;

public interface ProfileView extends NavigateView<ProfileView.MemberProfileViewListener>
{
	interface MemberProfileViewListener extends NavigateView.NavigateViewListener
	{
		void editStatus(User user);

		void editAvatar(User user);

		void editFreeText(User user);
	}

	void setUserData(UserProfileData userProfileData, UserProfileListener listener);
}