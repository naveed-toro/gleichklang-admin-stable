package de.binaerebauten.gleichklang.adminweb.view.popup;

import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.adminweb.view.I18N;
import de.binaerebauten.gleichklang.adminweb.view.component.AdminWorkItemTable.Directory;
import de.binaerebauten.gleichklang.adminweb.view.component.UserQuickBar;
import de.binaerebauten.gleichklang.adminweb.view.component.UserQuickBar.UserQuickBarListener;
import de.binaerebauten.gleichklang.adminweb.view.component.UserResetBar;
import de.binaerebauten.gleichklang.core.model.message.*;
import de.binaerebauten.gleichklang.core.model.user.Admin;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.model.user.User_;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.view.component.ComponentGroup;
import de.binaerebauten.gleichklang.core.view.component.HorizontalLine;
import de.binaerebauten.gleichklang.core.view.component.LabelField;
import de.binaerebauten.gleichklang.core.view.component.Popup;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;

import java.util.List;
import java.util.Objects;

public class AdminWorkItemPopup extends Popup
{
	public interface MessageButtonClickListener
	{
		void buttonClicked(Message message);
	}

	public interface AdminWorkItemButtonClickListener
	{
		void adminWorkItemButtonClicked(AdminWorkItemPopup sender, AdminWorkItem workItem);
	}

	private final String context;

	private final ComponentGroup<Message> selectedMessageFieldGroup;
	private final ComponentContainer messageAttachmentContainer;
	private final ComponentContainer replyToMessages;

	private final Button messageButton;
	private final Button readByMeButton;
	private final Button unreadButton;
	private final Button replyAgainButton;
	private final Button doneButton;
	private final Button ignoreButton;
	private final Button editButton;
	private final UserQuickBar userQuickBar;
	private final AdminWorkItem adminWorkItem;
	private final UserResetBar userResetBar;

	private MessageButtonClickListener messageButtonClickListener = null;
	private AdminWorkItemButtonClickListener readByMeButtonClickListener = null;
	private AdminWorkItemButtonClickListener unreadButtonClickListener = null;
	private AdminWorkItemButtonClickListener replyAgainButtonClickListener = null;
	private AdminWorkItemButtonClickListener doneButtonClickListener = null;
	private AdminWorkItemButtonClickListener ignoreButtonClickListener = null;

	public AdminWorkItemPopup(AdminWorkItem adminWorkItem, Directory directory, List<MessageUploadFile> messageAttachments)
	{
		this(adminWorkItem, directory, messageAttachments, null);
	}

	public AdminWorkItemPopup(AdminWorkItem adminWorkItem, Directory directory, List<MessageUploadFile> messageAttachments, String context)
	{
		this.adminWorkItem = Objects.requireNonNull(adminWorkItem);
		this.context = context;

		selectedMessageFieldGroup = new ComponentGroup<>(Message.class);

		messageAttachmentContainer = new VerticalLayout();
		messageAttachmentContainer.setStyleName(CssStyle.MESSAGE_ATTACHMENT_WRAPPER.getStyleName());
		messageAttachments.stream().map(this::getAttachmentLink).forEach(messageAttachmentContainer::addComponent);

		replyToMessages = new VerticalLayout();
		((VerticalLayout) replyToMessages).setSpacing(true);
		replyToMessages.setStyleName(CssStyle.REPLY_MESSAGES.getStyleName());

		userQuickBar = new UserQuickBar();
		messageButton = createMessageButton();
		readByMeButton = createReadByMeButton();
		unreadButton = createUnreadButton();
		replyAgainButton = createReplyAgainButton();
		ignoreButton = createIgnoreButton();
		doneButton = createDoneButton();
		editButton = createEditButton();
		userResetBar = new UserResetBar(adminWorkItem.getMessage().getSubject());
        userResetBar.setAbuseUserMessageBody(adminWorkItem.getMessage().getSubject());

		setStyleName(CssStyle.ADMIN_MESSAGE_POPUP.getStyleName());

		setContent(createMailView(directory));

		onWorkItemSelect(adminWorkItem);
	}

	private Component createMailView(Directory directory)
	{
		final VerticalLayout layout = new VerticalLayout();
		layout.setStyleName(CssStyle.ADMIN_LIST_MESSAGES_WRAPPER.getStyleName());

		layout.addComponent(userQuickBar);
		Label label = new Label();
		layout.addComponent(label);
		layout.addComponent(userResetBar);
		layout.addComponent(new Label());

		final Component mailPanel = createMailContentView(selectedMessageFieldGroup);

		layout.addComponents(mailPanel, messageAttachmentContainer, replyToMessages);

		final HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);

		if (directory == Directory.INCOMING)
			buttons.addComponents(readByMeButton, unreadButton, messageButton, replyAgainButton, ignoreButton, doneButton);
		else if (directory == Directory.DRAFT)
			buttons.addComponents(editButton);

		layout.addComponent(buttons);
		layout.setComponentAlignment(buttons, Alignment.TOP_CENTER);
		layout.setComponentAlignment(userQuickBar, Alignment.TOP_CENTER);

		return layout;
	}

	private Component createMailContentView(ComponentGroup<Message> selectedMessageFieldGroup)
	{
		final Panel mailContentPanel = new Panel();

		final VerticalLayout messageLayout = new VerticalLayout();
		messageLayout.setMargin(true);
		messageLayout.setStyleName(CssStyle.FIRST_MESSAGE.getStyleName());
		messageLayout.setSizeFull();

		final HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setSpacing(true);
		headerLayout.setSizeFull();

		final VerticalLayout bodyLayout = new VerticalLayout();
		bodyLayout.setSpacing(true);

		//final LabelField senderLabel = selectedMessageFieldGroup.buildAndBind(LabelField.class, Message_.senderEnvelope, SenderEnvelope_.user, User_.email);
		User user=this.adminWorkItem.getMessage().getSenderEnvelope().getUser();
		if(user != null)
		{

			if (user.getMemberStatus() != null && user.getMemberStatus().equals(MemberStatus.DELETED)){
				headerLayout.addComponent(new Label(user.getAlias()+" "+I18N.ADMINMESSAGEVIEW_MESSAGE_DELETE.msg()));
			}
			else {
				headerLayout.addComponent(new Label(user.getAlias()+" - "+user.getEmail()));
			}
		}

		final LabelField subjectLabel = selectedMessageFieldGroup.buildAndBind(LabelField.class, Message_.subject);
		final LabelField bodyLabel = selectedMessageFieldGroup.buildAndBind(LabelField.class, Message_.body);
		bodyLabel.setContentMode(ContentMode.HTML);

		bodyLayout.addComponents(subjectLabel, bodyLabel);

		messageLayout.addComponents(headerLayout, new HorizontalLine(), bodyLayout);
		mailContentPanel.setContent(messageLayout);
		return mailContentPanel;
	}

	private void onWorkItemSelect(AdminWorkItem workItem)
	{
		replyToMessages.removeAllComponents();

		if (workItem.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.ANSWERED))
		{
			readByMeButton.setEnabled(true);
			unreadButton.setEnabled(false);
			messageButton.setEnabled(false);
			replyAgainButton.setEnabled(true);
			ignoreButton.setEnabled(true);
			doneButton.setEnabled(true);
		}
		else if (workItem.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.DONE)
				|| workItem.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.IGNORED))
		{
			readByMeButton.setEnabled(true);
			unreadButton.setEnabled(false);
			messageButton.setEnabled(false);
			replyAgainButton.setEnabled(true);
			ignoreButton.setEnabled(false);
			doneButton.setEnabled(false);
		}
		else if (workItem.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.NEW))
		{
			readByMeButton.setEnabled(true);
			unreadButton.setEnabled(false);
			messageButton.setEnabled(false);
			replyAgainButton.setEnabled(false);
			ignoreButton.setEnabled(false);
			doneButton.setEnabled(false);
		}
		else if (workItem.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.OPEN))
		{
			readByMeButton.setEnabled(true);
			unreadButton.setEnabled(true);
			messageButton.setEnabled(true);
			replyAgainButton.setEnabled(false);
			ignoreButton.setEnabled(true);
			doneButton.setEnabled(true);
		}

		selectedMessageFieldGroup.setItemDataSource(workItem.getMessage());
		for (Message replyToMessage = workItem.getMessage().getReplyToMessage(); replyToMessage != null; replyToMessage = replyToMessage.getReplyToMessage())
		{
			final ComponentGroup<Message> replyToMessageFieldGroup = new ComponentGroup<>(Message.class);
			replyToMessageFieldGroup.setItemDataSource(replyToMessage);
			replyToMessages.addComponent(createMailContentView(replyToMessageFieldGroup));
		}

		final User sender = workItem.getMessage().getSenderEnvelope().getUser();
		final User receiver = workItem.getMessage().getReceiverEnvelope().getUser();
		userQuickBar.onUserChanged(sender != null ? sender : receiver);
	}

	private Button createMessageButton()
	{
		final Button messageButton = new Button(I18N.ADMINMESSAGEVIEW_MESSAGE_ANSWER.msg());
		messageButton.setVisible(false);
		messageButton.addClickListener(event ->
		{

			messageButtonClickListener.buttonClicked(selectedMessageFieldGroup.getItemDataSource().getBean());
		});

		return messageButton;
	}

	private Button createReadByMeButton()
	{
		final Button readByMeButton = new Button(I18N.ADMINMESSAGEVIEW_MESSAGE_READ.msg());
		readByMeButton.setVisible(false);
		readByMeButton.setEnabled(false);

		readByMeButton.addClickListener(event ->
		{
			if (readByMeButtonClickListener != null)
				readByMeButtonClickListener.adminWorkItemButtonClicked(this, adminWorkItem);
		});

		return readByMeButton;
	}

	private Button createUnreadButton()
	{
		final Button unreadButton = new Button(I18N.ADMINMESSAGEVIEW_MESSAGE_UNREAD.msg());
		unreadButton.setVisible(false);
		unreadButton.setEnabled(false);

		unreadButton.addClickListener(event ->
		{
			if (unreadButtonClickListener != null)
				unreadButtonClickListener.adminWorkItemButtonClicked(this, adminWorkItem);
		});

		return unreadButton;
	}

	private Button createReplyAgainButton()
	{
		final Button replyAgainButton = new Button(I18N.ADMINMESSAGEVIEW_MESSAGE_ANSWERAGAIN.msg());
		replyAgainButton.setVisible(false);
		replyAgainButton.setEnabled(false);

		replyAgainButton.addClickListener(event ->
		{
			if (replyAgainButtonClickListener != null)
				replyAgainButtonClickListener.adminWorkItemButtonClicked(this, adminWorkItem);
		});

		return replyAgainButton;
	}

	private Button createDoneButton()
	{
		final Button doneButton = new Button(I18N.ADMINMESSAGEVIEW_MESSAGE_DONE.msg());
		doneButton.setVisible(false);
		doneButton.setEnabled(false);

		doneButton.addClickListener(event ->
		{
			if (doneButtonClickListener != null)
				doneButtonClickListener.adminWorkItemButtonClicked(this, adminWorkItem);
		});

		return doneButton;
	}

	private Button createIgnoreButton()
	{
		final Button ignoreButton = new Button(I18N.ADMINMESSAGEVIEW_MESSAGE_IGNORE.msg());
		ignoreButton.setVisible(false);
		ignoreButton.setEnabled(false);

		ignoreButton.addClickListener(event ->
		{
			if (ignoreButtonClickListener != null)
				ignoreButtonClickListener.adminWorkItemButtonClicked(this, adminWorkItem);
		});

		return ignoreButton;
	}

	private Button createEditButton()
	{
		final Button editButton = new Button();
		editButton.setCaption("bearbeiten");

		editButton.addClickListener(event ->
		{
			if (messageButtonClickListener != null)
				messageButtonClickListener.buttonClicked(adminWorkItem.getMessage());
		});

		return editButton;
	}

	private Link getAttachmentLink(MessageUploadFile messageUploadFile)
	{
		if (messageUploadFile.getPath() == null) return new Link();

		final Link link = new Link(messageUploadFile.getFilename(), new FileResource(messageUploadFile.toFile()));
		link.setTargetName("_blank");
		link.setIcon(FontAwesome.PAPERCLIP);
		return link;
	}

	public void setReadByMeButtonClickListener(AdminWorkItemButtonClickListener messageButtonClickListener)
	{
		this.readByMeButtonClickListener = messageButtonClickListener;
		readByMeButton.setVisible(readByMeButtonClickListener != null);
	}

	public void setUnreadButtonClickListener(AdminWorkItemButtonClickListener messageButtonClickListener)
	{
		this.unreadButtonClickListener = messageButtonClickListener;
		unreadButton.setVisible(messageButtonClickListener != null);
	}

	public void setReplyAgainButtonClickListener(AdminWorkItemButtonClickListener messageButtonClickListener)
	{
		this.replyAgainButtonClickListener = messageButtonClickListener;
		replyAgainButton.setVisible(messageButtonClickListener != null);
	}

	public void setIgnoreButtonClickListener(AdminWorkItemButtonClickListener messageButtonClickListener)
	{
		this.ignoreButtonClickListener = messageButtonClickListener;
		ignoreButton.setVisible(messageButtonClickListener != null);
	}

	public void setUserQuickBarListener(UserQuickBarListener userQuickBarListener)
	{
		userQuickBar.setListener(userQuickBarListener);
	}

	public void setDoneButtonClickListener(AdminWorkItemButtonClickListener messageButtonClickListener)
	{
		this.doneButtonClickListener = messageButtonClickListener;
		doneButton.setVisible(messageButtonClickListener != null);
	}

	public void setMessageButtonClickListener(MessageButtonClickListener messageButtonClickListener)
	{
		this.messageButtonClickListener = messageButtonClickListener;
		messageButton.setVisible(messageButtonClickListener != null);
	}

	public void validateWorkItemStatus(Admin admin, AdminWorkItem item)
	{
		if (item.getAdmin() == admin)
		{
			if (item.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.ANSWERED))
			{
				readByMeButton.setEnabled(true);
				unreadButton.setEnabled(true);
				messageButton.setEnabled(false);
				replyAgainButton.setEnabled(true);
				ignoreButton.setEnabled(true);
				doneButton.setEnabled(true);
			}
			else if (item.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.DONE)
					|| item.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.IGNORED))
			{
				readByMeButton.setEnabled(true);
				unreadButton.setEnabled(true);
				messageButton.setEnabled(false);
				replyAgainButton.setEnabled(true);
				ignoreButton.setEnabled(false);
				doneButton.setEnabled(false);
			}
			else if (item.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.NEW))
			{
				readByMeButton.setEnabled(true);
				unreadButton.setEnabled(false);
				messageButton.setEnabled(false);
				replyAgainButton.setEnabled(false);
				ignoreButton.setEnabled(false);
				doneButton.setEnabled(false);
			}
			else if (item.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.OPEN))
			{
				readByMeButton.setEnabled(true);
				unreadButton.setEnabled(true);
				messageButton.setEnabled(true);
				replyAgainButton.setEnabled(false);
				ignoreButton.setEnabled(true);
				doneButton.setEnabled(true);
			}
		}
		else if (item.getAdmin() != null && item.getAdmin() != admin)
		{
			if (item.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.ANSWERED))
			{
				readByMeButton.setEnabled(true);
				unreadButton.setEnabled(false);
				messageButton.setEnabled(false);
				replyAgainButton.setEnabled(true);
				ignoreButton.setEnabled(true);
				doneButton.setEnabled(true);
			}
			else if (item.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.DONE)
					|| item.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.IGNORED))
			{
				readByMeButton.setEnabled(true);
				unreadButton.setEnabled(false);
				messageButton.setEnabled(false);
				replyAgainButton.setEnabled(true);
				ignoreButton.setEnabled(false);
				doneButton.setEnabled(false);
			}
			else if (item.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.NEW))
			{
				readByMeButton.setEnabled(true);
				unreadButton.setEnabled(false);
				messageButton.setEnabled(false);
				replyAgainButton.setEnabled(false);
				ignoreButton.setEnabled(false);
				doneButton.setEnabled(false);
			}
			else if (item.getWorkItemStatus().equals(AdminWorkItem.AdminWorkItemStatus.OPEN))
			{
				readByMeButton.setEnabled(true);
				unreadButton.setEnabled(true);
				messageButton.setEnabled(true);
				replyAgainButton.setEnabled(false);
				ignoreButton.setEnabled(true);
				doneButton.setEnabled(true);
			}
		}
	}

	@Override
	public String getUniqueName()
	{
		return context != null ? context + super.getUniqueName() : super.getUniqueName();
	}

	public void setUserResetBarListener(UserResetBar.UserResetBarListener userResetBarListener)
	{
		userResetBar.setListener(userResetBarListener);
	}

	public void setUserUnblockListener(UserResetBar.UserUnblockListner userUnblockListner)
	{
		userResetBar.setListener(userUnblockListner);
	}

	public void setAbuseUserListener(UserResetBar.AbuserLoginListener abuseUserListener)
	{
		userResetBar.setListener(abuseUserListener);
	}

	public void setReminderLoginListener(UserResetBar.ReminderLoginListener reminderLoginListener)
	{
		userResetBar.setListener(reminderLoginListener);
	}

	@Override
	public void close() {

		this.setVisible(false);
	}
}
