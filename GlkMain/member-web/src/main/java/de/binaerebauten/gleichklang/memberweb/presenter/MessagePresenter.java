package de.binaerebauten.gleichklang.memberweb.presenter;

import com.vaadin.server.Sizeable;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.ClientInformation;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.presenter.NavigatePresenter;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.MessageBox;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.DialogResult;
import de.binaerebauten.gleichklang.core.view.component.MessageBox.MessageBoxButtons;
import de.binaerebauten.gleichklang.core.view.component.message.NewMessagePopup;
import de.binaerebauten.gleichklang.core.view.component.message.ShowMessagePopup;
import de.binaerebauten.gleichklang.core.view.component.message.ShowMessagePopup.AnswerMessageCallback;
import de.binaerebauten.gleichklang.core.view.component.message.ShowMessagePopup.RelationshipClickListener;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;
import de.binaerebauten.gleichklang.memberweb.presenter.handler.RelationshipHandler;
import de.binaerebauten.gleichklang.memberweb.view.MessageView;
import de.binaerebauten.gleichklang.memberweb.view.MessageView.MessageTab;
import de.binaerebauten.gleichklang.memberweb.view.MessageView.MessageViewListener;
import de.binaerebauten.gleichklang.memberweb.view.popup.RelationshipPopup;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MessagePresenter extends NavigatePresenter implements MessageViewListener
{
	private final MessageService messageService;
	private final MessageView view;
	private final User currentUser;
	private final RelationshipHandler relationshipHandler;
	private final RelationshipRepository relationshipRepository;
	private final ClientInformation.Device device;
	private ShowMessagePopup showMessagePopup;

	public MessagePresenter(ApplicationContext ctx, MessageView view, ClientInformation.Device device)
	{
		super(view);
		this.view = view;

		final AuthenticationService authenticationService = ctx.getBean(AuthenticationService.class);
		final UserRepository userRepository = ctx.getBean(UserRepository.class);

		messageService = ctx.getBean(MessageService.class);
		relationshipRepository = ctx.getBean(RelationshipRepository.class);

		currentUser = userRepository.findOne(authenticationService.getAuthenticatedUserId());

		this.device = device;

		view.setListener(this);

		relationshipHandler = new RelationshipHandler(ctx, this);
	}

	@Override
	public void enter(String category)
	{
		refreshView();
		this.view.setActiveRecommendationCategories(currentUser.getOrderedCategories(), category);
		this.view.reset();
	}

	@Override
	public void leave()
	{
		this.view.setDeletedMessagesHandler(null);
		this.view.setDraftMessagesHandler(null);
		this.view.setDeletedMessagesHandler(null);
		this.view.setIncomingMessagesHandler(null);

		super.leave();
	}

	private void sendMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		messageService.sendMessage(message, messageUploadFiles);
		currentUser.setBlocked(message.getSenderEnvelope().getUser().isBlocked());
		refreshView();
	}

	private void saveMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		messageService.saveMessage(message, messageUploadFiles);


		refreshView();
	}

	@Override
	public void onTabSelected(MessageTab selectedTab)
	{
		switch (selectedTab)
		{
			case DELETED:
				view.setDeletedMessagesHandler(messageService.createHiddenMessagesHandler(currentUser));
				break;
			case DRAFT:
				view.setDraftMessagesHandler(messageService.createDraftMessagesHandler(currentUser));
				break;
			case INCOMING:
				view.setIncomingMessagesHandler(messageService.createIncomingMessagesHandler(currentUser));
				break;
			case OUTGOING:
				view.setOutgoingMessagesHandler(messageService.createOutgoingMessagesHandler(currentUser));
				break;
		}
	}

	@Override
	public void openMessage(Message message)
	{
		switch (messageService.getMessageDirectory(message, currentUser))
		{
			case INCOMING:
				openIncomingMessage(message);
				break;
			case OUTGOING:
				openOutgoingMessage(message);
				break;
			case DRAFT:
				openDraftMessage(message);
				break;
			case HIDDEN:
				openDeletedMessage(message);
				break;
		}
	}

	private void openIncomingMessage(Message message)
	{
		final boolean relationshipsCancelled = messageService.isRelationshipsCancelled(message);
		final AnswerMessageCallback answerMessageCallback = relationshipsCancelled ? null : this::answerMessage;
		final RelationshipClickListener openRelationshipPopup = relationshipsCancelled ? null : this::openRelationshipPopup;
		showMessagePopup = new ShowMessagePopup(message, currentUser, messageService.getMessageAttachments(message), answerMessageCallback, openRelationshipPopup);
		showMessagePopup.setRelationshipCancelled(relationshipsCancelled);
		messageService.readMessages(Collections.singleton(message));
		refreshView();

		showMessagePopup.setBoxSize(GenericPopup.BoxSize.WIDE);
		showMessagePopup.setHeight(80, Sizeable.Unit.PERCENTAGE);
		tryOpenPopup(showMessagePopup);
	}

	private void openOutgoingMessage(Message message)
	{
		final RelationshipClickListener openRelationshipPopup = messageService.isRelationshipsCancelled(message) ? null : this::openRelationshipPopup;
		final ShowMessagePopup popup = new ShowMessagePopup(message, currentUser, messageService.getMessageAttachments(message), null, openRelationshipPopup);
		popup.setBoxSize(GenericPopup.BoxSize.WIDE);
		popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
		tryOpenPopup(popup);
	}

	private void openDraftMessage(Message message)
	{
		if (!messageService.isRelationshipsCancelled(message))
		{
			final NewMessagePopup popup = new NewMessagePopup(message, messageService.getMessageAttachments(message));
			popup.setNewUploadCallback(this::newUpload);
			popup.setSendCallback(this::sendMessage);
			popup.setSaveCallback(this::saveMessage);
			popup.setReceiver(message.getReceiverEnvelope().getUser());

			popup.setBoxSize(GenericPopup.BoxSize.WIDE);
			popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
			tryOpenPopup(popup);
		}
		else
		{
			MessageBox.show(I18N.MESSAGEPRESENTER_NOTIFICATION_RELATIONREMOVED.msg(), MessageBoxButtons.YES_NO, dialogResult ->
			{
				if (dialogResult.equals(DialogResult.YES))
					hideMessages(Collections.singleton(message));
			});
		}
	}

	private void openDeletedMessage(Message message)
	{
		if (message.isSent())
		{
			final RelationshipClickListener openRelationshipPopup = messageService.isRelationshipsCancelled(message) ? null : this::openRelationshipPopup;
			final ShowMessagePopup popup = new ShowMessagePopup(message, currentUser, messageService.getMessageAttachments(message), null, openRelationshipPopup);
			popup.setBoxSize(GenericPopup.BoxSize.WIDE);
			popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
			tryOpenPopup(popup);
		}
		else
		{
			MessageBox.show(I18N.MESSAGEPRESENTER_NOTIFICATION_OPENDELETEDDRAFT.msg(), MessageBoxButtons.YES_NO, dialogResult ->
			{
				if (dialogResult.equals(DialogResult.YES))
					restoreMessages(Collections.singleton(message));
			});
		}
	}

	@Override
	public void writeMessage()
	{
		final NewMessagePopup popup = new NewMessagePopup(messageService.createNewMessage(currentUser));
		popup.setNewUploadCallback(this::newUpload);
		popup.setSendCallback(this::sendMessage);
		popup.setSaveCallback(this::saveMessage);
		popup.setReceiverList(messageService.getReceiverList(currentUser));

		popup.setBoxSize(GenericPopup.BoxSize.WIDE);
		popup.setHeight(80, Sizeable.Unit.PERCENTAGE);
		tryOpenPopup(popup);
	}

	private void answerMessage(Message message)
	{
		final NewMessagePopup popup = new NewMessagePopup(messageService.createAnswerMessage(message));
		popup.setNewUploadCallback(this::newUpload);
		popup.setSendCallback(this::sendAnswer);
		popup.setSaveCallback(this::saveMessage);
		popup.setReceiver(message.getSenderEnvelope().getUser());
		popup.setOldMessage(message.getBody());
		popup.setBoxSize(GenericPopup.BoxSize.WIDE);
		popup.setHeight(80, Sizeable.Unit.PERCENTAGE);

		tryOpenPopup(popup);
	}

	private void sendAnswer( Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		sendMessage(message,messageUploadFiles);

		if(showMessagePopup !=null)
			showMessagePopup.close();
		refreshView();
	}

	private void openRelationshipPopup(User targetUser)
	{
		final Relationship relationship = relationshipRepository.findRelationshipBySourceUserAndTargetUser(currentUser, targetUser);

		if (relationship != null)
		{
			final RelationshipPopup popup = relationshipHandler.createRelationshipPopup(relationship, null, null, device);
			tryOpenPopup(popup);
		}
	}

	@Override
	public void restoreMessages(Collection<Message> messages)
	{
		messageService.restoreMessages(currentUser, messages);
		refreshView();
	}

	@Override
	public void hideMessages(Collection<Message> messages)
	{
		messageService.hideMessages(currentUser, messages);
		refreshView();
	}

	@Override
	public void deleteMessages(Collection<Message> messages)
	{
		messageService.deleteMessages(currentUser, messages);
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
