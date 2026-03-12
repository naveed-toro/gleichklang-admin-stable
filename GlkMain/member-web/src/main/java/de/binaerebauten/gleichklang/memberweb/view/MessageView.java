package de.binaerebauten.gleichklang.memberweb.view;

import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.view.NavigateView;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.memberweb.view.MessageView.MessageViewListener;

import java.util.Collection;

public interface MessageView extends NavigateView<MessageViewListener>, ResetableView
{
	enum MessageTab
	{
		INCOMING,
		OUTGOING,
		DRAFT,
		DELETED
	}
	
	interface MessageViewListener extends NavigateView.NavigateViewListener
	{
		void hideMessages(Collection<Message> messages);

		void restoreMessages(Collection<Message> messages);
		
		void deleteMessages(Collection<Message> messages);
		
		void readMessages(Collection<Message> messages);
		
		void unreadMessages(Collection<Message> messages);
		
		void onTabSelected(MessageTab selectedTab);
		
		void openMessage(Message message);
		
		void writeMessage();
	}
	
	void changeTab(MessageTab tab);
	
	MessageTab getSelectedMessageTab();

	void goToIncomingMessages();
	
	void setIncomingMessagesHandler(LazyBeanFilteredItemsHandler<Message> handler);
	
	void setOutgoingMessagesHandler(LazyBeanFilteredItemsHandler<Message> handler);
	
	void setDraftMessagesHandler(LazyBeanFilteredItemsHandler<Message> handler);
	
	void setDeletedMessagesHandler(LazyBeanFilteredItemsHandler<Message> handler);
	
	void setActiveRecommendationCategories(Collection<RecommendationCategory> categories,
			String category);
}
