package de.binaerebauten.gleichklang.adminweb.presenter.handler;

import com.vaadin.ui.Window.CloseListener;
import de.binaerebauten.gleichklang.adminweb.view.component.AdminWorkItemTable.Directory;
import de.binaerebauten.gleichklang.adminweb.view.component.UserQuickBar.UserQuickBarListener;
import de.binaerebauten.gleichklang.adminweb.view.component.UserResetBar;
import de.binaerebauten.gleichklang.adminweb.view.popup.AdminWorkItemPopup;
import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.presenter.PopupOpener;
import de.binaerebauten.gleichklang.core.repository.user.AdminRepository;
import de.binaerebauten.gleichklang.core.security.AuthenticationService;
import de.binaerebauten.gleichklang.core.service.MessageService;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.message.NewMessagePopup;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AdminMessageHandler
{
	private final Admin currentUser;
	private  UserQuickBarListener userQuickBarListener;
	private  UserResetBar.UserResetBarListener userResetBarListener;
	private  UserResetBar.UserUnblockListner userUnblockListner;
	private UserResetBar.AbuserLoginListener abuserLoginListener;
	private UserResetBar.ReminderLoginListener reminderLoginListener;
	private final MessageService messageService;
	private final ApplicationContext ctx;
	private final PopupOpener popupOpener;

	public AdminMessageHandler(ApplicationContext ctx, PopupOpener popupOpener)
	{
		this(ctx, null, popupOpener);
	}

		public AdminMessageHandler(ApplicationContext ctx, UserQuickBarListener userQuickBarListener, PopupOpener popupOpener)
	{
		this.ctx=ctx;
		this.popupOpener = Objects.requireNonNull(popupOpener);
		this.userQuickBarListener = userQuickBarListener;
		this.userResetBarListener = ( UserResetBar.UserResetBarListener) userQuickBarListener;
		this.userUnblockListner = (UserResetBar.UserUnblockListner) userQuickBarListener;
		this.abuserLoginListener = (UserResetBar.AbuserLoginListener) userQuickBarListener;
		this.reminderLoginListener = (UserResetBar.ReminderLoginListener) userQuickBarListener;
		messageService = ctx.getBean(MessageService.class);

		final AuthenticationService authenticationService = ctx.getBean(AuthenticationService.class);

		currentUser = ctx.getBean(AdminRepository.class).findOne(authenticationService.getAuthenticatedUserId());
	}

	public void openAdminWorkItem(AdminWorkItem adminWorkItem, Directory directory, CloseListener closeListener)
	{
		final AdminWorkItemPopup popup = new AdminWorkItemPopup(adminWorkItem, directory, messageService.getMessageAttachments(adminWorkItem.getMessage()), this.toString());

		if(userQuickBarListener==null)
		{
			this.userQuickBarListener=new UserControlHandler(this.ctx,this.popupOpener);
			this.userResetBarListener = ( UserResetBar.UserResetBarListener) this.userQuickBarListener;
			this.userUnblockListner = (UserResetBar.UserUnblockListner) this.userQuickBarListener;
			this.abuserLoginListener = (UserResetBar.AbuserLoginListener) this.userQuickBarListener;
			this.reminderLoginListener = (UserResetBar.ReminderLoginListener) this.userQuickBarListener;

		}
		popup.setUserQuickBarListener(userQuickBarListener);
		popup.setUserResetBarListener(userResetBarListener);
		popup.setUserUnblockListener(userUnblockListner);
		popup.setAbuseUserListener(abuserLoginListener);
		popup.setReminderLoginListener(reminderLoginListener);
		switch(directory)
		{
			case INCOMING:
				popup.setMessageButtonClickListener(this::answerMessage);
				popup.setReadByMeButtonClickListener(this::readAdminMessage);
				popup.setUnreadButtonClickListener(this::unreadAdminMessage);
				popup.setReplyAgainButtonClickListener(this::answerAgain);
				popup.setIgnoreButtonClickListener(this::ignoreAdminMessage);
				popup.setDoneButtonClickListener(this::doneAdminMessage);
				break;
			case OUTGOING:
				break;
			case DRAFT:
				popup.setMessageButtonClickListener(this::editMessage);
				break;
			case Reminder:

		}

		if(closeListener != null) popup.addCloseListener(closeListener);

		popupOpener.tryOpenPopup(popup);
	}

	
	private void sendMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		if(message.getSubject().contains("(Mobile")){
			message.setSubject(message.getSubject().split("\\(Mobile")[0]);
		}
		else if(message.getSubject().contains("(Desktop")){
			message.setSubject(message.getSubject().split("\\(Desktop")[0]);
		}
		messageService.sendMessageFromAdmin(message, messageUploadFiles, currentUser);
	}
	
	private void saveMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		messageService.saveAdminMessage(message, messageUploadFiles, currentUser);
	}
	
	private void editMessage(Message message)
	{
		final NewMessagePopup popup = new NewMessagePopup(message, messageService.getMessageAttachments(message));
		
		popup.setNewUploadCallback(this::newUpload);
		popup.setSendCallback(this::sendMessage);
		popup.setSaveCallback(this::saveMessage);
		
		if(message.getReplyToMessage()!=null){
			popup.setReceiver(message.getReplyToMessage().getSenderEnvelope().getUser());
		}
		else{
			popup.setReceiver(message.getReceiverEnvelope().getUser());
		}
		
		popupOpener.tryOpenPopup(popup);
	}
	
	private MessageUploadFile newUpload(Message message, Collection<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		return messageService.createMessageAttachment(message, messageUploadFiles.size());
	}
	
	private void answerMessage(Message message)
	{
		final NewMessagePopup popup = new NewMessagePopup(messageService.createAdminAnswerMessage(message,currentUser));
		popup.setNewUploadCallback(this::newUpload);
		popup.setSendCallback(this::sendMessage);
		popup.setSaveCallback(this::saveMessage);
		
		popup.setReceiver(message.getSenderEnvelope().getUser());
		popup.setOldMessage(message.getBody());
		
		popupOpener.tryOpenPopup(popup);
	}
	
	private void answerAgain(AdminWorkItemPopup sender, AdminWorkItem item)
	{
		final Message message = item.getMessage();
		final NewMessagePopup popup = new NewMessagePopup(messageService.createAdminAnswerMessage(message,currentUser));
		popup.setNewUploadCallback(this::newUpload);
		popup.setSendCallback(this::sendMessage);
		popup.setSaveCallback(this::saveMessage);
		
		popup.setReceiver(message.getSenderEnvelope().getUser());
		popup.setOldMessage(message.getBody());
		
		popupOpener.tryOpenPopup(popup);
	}
	
	private void readAdminMessage(AdminWorkItemPopup sender, AdminWorkItem workItem)
	{
		messageService.readAdminMessage(workItem, currentUser);
		sender.validateWorkItemStatus(currentUser, workItem);
	}
	
	private void unreadAdminMessage(AdminWorkItemPopup sender, AdminWorkItem workItem)
	{
		sender.close();
		messageService.unreadAdminMessage(workItem);
		sender.validateWorkItemStatus(currentUser, workItem);
	}
	
	private void ignoreAdminMessage(AdminWorkItemPopup sender, AdminWorkItem workItem)
	{
		sender.close();
		messageService.ignoreAdminMessage(workItem);
		sender.validateWorkItemStatus(currentUser, workItem);
	}
	
	private void doneAdminMessage(AdminWorkItemPopup sender, AdminWorkItem workItem)
	{
		sender.close();
		messageService.doneAdminMessage(workItem);
		sender.validateWorkItemStatus(currentUser, workItem);
	}
}
