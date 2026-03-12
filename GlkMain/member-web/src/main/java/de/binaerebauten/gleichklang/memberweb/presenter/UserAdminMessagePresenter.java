package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.server.Sizeable;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.message.NewMessagePopup;
import de.binaerebauten.gleichklang.core.view.component.message.ShowMessagePopup;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.view.UserAdminMessageView;
import de.binaerebauten.gleichklang.memberweb.view.UserAdminMessageViewImpl;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UserAdminMessagePresenter extends NavigatePresenter implements UserAdminMessageView.AdminMessageViewListener
{
	private final MessageService messageService;
	private final UserAdminMessageView view;
	private final User currentUser;
	
	public UserAdminMessagePresenter(ApplicationContext ctx, UserAdminMessageView view)
	{
		super(view);
		this.view = view;
		
		final AuthenticationService authenticationService = ctx.getBean(AuthenticationService.class);
		final UserRepository userRepository = ctx.getBean(UserRepository.class);
		messageService = ctx.getBean(MessageService.class);
		
		currentUser = userRepository.findOne(authenticationService.getAuthenticatedUserId());
		
		view.setListener(this);
	}
	
	@Override
	public void enter(String parameters)
	{
		refreshView();
		this.view.reset();
	}
	
	@Override
	public void leave()
	{
		this.view.setIncomingMessagesHandler(null);
		this.view.setOutgoingMessagesHandler(null);
		
		super.leave();
	}
	
	private void sendMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		messageService.sendMessageToAdmin(message, messageUploadFiles);
		refreshView();
	}
	
	@Override
	public void onTabSelected(UserAdminMessageViewImpl.AdminMessageTab selectedTab)
	{
		switch (selectedTab)
		{
			case INCOMING:
				view.setIncomingMessagesHandler(messageService.createUserAdminMessageIncomingMessagesHandler(currentUser));
				break;
			case OUTGOING:
				view.setOutgoingMessagesHandler(messageService.createUserAdminMessageOutgoingMessagesHandler(currentUser));
				break;
		}
	}
	
	@Override
	public void openMessage(Message message)
	{
		final ShowMessagePopup popup;
		
		switch (messageService.getMessageDirectory(message, currentUser))
		{
			case INCOMING:
				popup = new ShowMessagePopup(message, currentUser, messageService.getMessageAttachments(message), this::answerMessage);
				popup.addCloseListener(event ->
				{
					messageService.readMessages(Collections.singleton(message));
					refreshView();
				});
				popup.setBoxSize(GenericPopup.BoxSize.WIDE);
				popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
				tryOpenPopup(popup);
				break;
			case OUTGOING:
				popup = new ShowMessagePopup(message, currentUser, messageService.getMessageAttachments(message));
				popup.setBoxSize(GenericPopup.BoxSize.WIDE);
				popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
				tryOpenPopup(popup);
				break;
		}
	}
	
	private void answerMessage(Message message)
	{
		final NewMessagePopup popup = new NewMessagePopup(true, messageService.createAnswerMessage(message));
		popup.setNewUploadCallback(this::newUpload);
		popup.setSendCallback(this::sendMessage);
		popup.setOldMessage(message.getBody());
		popup.setBoxSize(GenericPopup.BoxSize.WIDE);
		popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
		tryOpenPopup(popup);
	}
	
	@Override
	public void writeMessage()
	{
		final NewMessagePopup popup = new NewMessagePopup(true, messageService.createNewMessage(currentUser));
		popup.setNewUploadCallback(this::newUpload);
		popup.setSendCallback(this::sendMessage);
		popup.setBoxSize(GenericPopup.BoxSize.WIDE);
		popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
		tryOpenPopup(popup);
	}
	
	@Override
	public void hideMessages(Collection<Message> messages)
	{
		messageService.hideMessages(currentUser, messages);
		refreshView();
	}
	
	private void refreshView()
	{
		onTabSelected(view.getSelectedMessageTab());
	}
	
	@Override
	public void readMessages(Collection<Message> messages)
	{
		messageService.readMessages(messages);
		refreshView();
	}
	
	@Override
	public void unreadMessages(Collection<Message> messages)
	{
		messageService.unreadMessages(messages);
		refreshView();
	}
	
	private MessageUploadFile newUpload(Message message, Collection<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		return messageService.createMessageAttachment(message, messageUploadFiles.size());
	}
}
