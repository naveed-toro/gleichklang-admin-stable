package de.binaerebauten.gleichklang.memberweb.presenter;

import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.ChatService;
import de.binaerebauten.gleichklang.core.service.ChatService.ChatReceiver;
import de.binaerebauten.gleichklang.memberweb.view.ChatView;
import de.binaerebauten.gleichklang.memberweb.view.ChatView.ChatViewListener;
import de.binaerebauten.gleichklang.memberweb.view.popup.ChatPopup;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatPresenter extends NavigatePresenter implements ChatViewListener, ChatReceiver
{
	private final ChatView view;
	private final RelationshipRepository relationshipRepository;
	private final User currentUser;
	private final ChatService chatService;
	private final Map<User, ChatPopup> chats = new HashMap<>();

	public ChatPresenter(ApplicationContext ctx, ChatView view)
	{
		super(view);
		this.view = view;

		chatService = ctx.getBean(ChatService.class);
		relationshipRepository = ctx.getBean(RelationshipRepository.class);
		currentUser = ctx.getBean(UserRepository.class).findOne(ctx.getBean(AuthenticationService.class).getAuthenticatedUserId());

		view.setListener(this);
	}

	@Override
	public void enter(String parameters)
	{
		chatService.register(currentUser, this);
		this.view.setReceiverList(relationshipRepository.findAllRelationshipsForUser(currentUser).stream().map(Relationship::getTargetUser).collect(Collectors.toList()));
	}

	@Override
	public void leave()
	{
		chatService.unregister(currentUser, this);
		this.view.setReceiverList(null);
		
		super.leave();
	}

	@Override
	public void startChat(User user)
	{
		final ChatPopup chatPopup = chats.getOrDefault(user, new ChatPopup(currentUser, user, this::sendMessage));
		chats.put(user, chatPopup);
		chatPopup.show();
	}

	private void sendMessage(User targetUser, String msg)
	{
		chatService.sendMessage(currentUser, targetUser, msg);
	}

	@Override
	public void receiveMessage(User fromUser, User toUser, String msg)
	{
		this.view.getUI().access(() ->
		{
			final User targetUser = fromUser.equals(currentUser) ? toUser : fromUser;
			final ChatPopup chatPopup = chats.getOrDefault(targetUser, new ChatPopup(currentUser, targetUser, this::sendMessage));
			chats.put(targetUser, chatPopup);
			chatPopup.addText(fromUser, msg);
			chatPopup.show();
		});
	}
}
