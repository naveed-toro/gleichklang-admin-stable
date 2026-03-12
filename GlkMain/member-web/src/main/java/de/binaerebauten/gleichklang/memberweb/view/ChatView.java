package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.memberweb.view.ChatView.ChatViewListener;

import java.util.List;

public interface ChatView extends NavigateView<ChatViewListener>
{
	interface ChatViewListener extends NavigateView.NavigateViewListener
	{
		void startChat(User user);
	}

	void setReceiverList(List<User> userList);
}
