package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer;

import java.util.Collection;


public interface UserAdminMessageView extends NavigateView<UserAdminMessageView.AdminMessageViewListener>, ResetableView
{
	enum AdminMessageTab
	{
		INCOMING,
		OUTGOING
	}
	
	interface AdminMessageViewListener extends NavigateView.NavigateViewListener
    {
	
        void hideMessages(Collection<Message> messages);
	
        void readMessages(Collection<Message> messages);
	
        void unreadMessages(Collection<Message> messages);
	
        void onTabSelected(UserAdminMessageViewImpl.AdminMessageTab selectedTab);
	
        void writeMessage();
	
		void openMessage(Message message);
	}
	
    void changeTab(UserAdminMessageViewImpl.AdminMessageTab tab);

    UserAdminMessageViewImpl.AdminMessageTab getSelectedMessageTab();

    void setIncomingMessagesHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Message> handler);

    void setOutgoingMessagesHandler(LazyBeanItemContainer.LazyBeanFilteredItemsHandler<Message> handler);
}
