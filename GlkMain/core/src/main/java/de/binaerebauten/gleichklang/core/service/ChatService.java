package de.binaerebauten.gleichklang.core.service;

import de.binaerebauten.gleichklang.core.model.user.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Service
public class ChatService
{
	public interface ChatReceiver
	{
		void receiveMessage(User fromUser, User toUser, String msg);
	}

	private final Map<User, Collection<ChatReceiver>> chatMap = new HashMap<>();

	public synchronized void register(User user, ChatReceiver chatReceiver)
	{
		final Collection<ChatReceiver> chatReceivers = chatMap.getOrDefault(user, new HashSet<>());
		chatReceivers.add(chatReceiver);
		chatMap.put(user, chatReceivers);
	}

	public synchronized void unregister(User user, ChatReceiver chatReceiver)
	{
		final Collection<ChatReceiver> chatReceivers = chatMap.getOrDefault(user, new HashSet<>());
		chatReceivers.remove(chatReceiver);
		if (chatReceivers.isEmpty()) chatMap.remove(user);
	}

	public boolean isOnline(User user)
	{
		return chatMap.containsKey(user);
	}

	@Async
	public synchronized void sendMessage(User sourceUser, User targetUser, String msg)
	{
		final Collection<ChatReceiver> sourcePresenter = chatMap.getOrDefault(sourceUser, new HashSet<>());
		final Collection<ChatReceiver> targetPresenter = chatMap.getOrDefault(targetUser, new HashSet<>());

		sourcePresenter.forEach(chatPresenter -> chatPresenter.receiveMessage(sourceUser, targetUser, msg));
		targetPresenter.forEach(chatPresenter -> chatPresenter.receiveMessage(sourceUser, targetUser, msg));

	}
}
