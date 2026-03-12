package de.binaerebauten.gleichklang.core.view.component.message;

import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.user.BlockedStatus;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.utils.LocaleAware;
import de.binaerebauten.gleichklang.core.view.component.HorizontalLine;
import de.binaerebauten.gleichklang.core.view.css.CssStyle;
import de.binaerebauten.gleichklang.core.view.popup.GenericPopup;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import static de.binaerebauten.gleichklang.core.model.user.I18N.CANCELED_USER_WITH_ALIAS;
import static de.binaerebauten.gleichklang.core.model.user.I18N.DELETED_USER_WITH_ALIAS;

public class ShowMessagePopup extends GenericPopup implements LocaleAware
{
	public interface AnswerMessageCallback
	{
		void answerMessage(Message message);
	}
	
	public interface RelationshipClickListener
	{
		void openRelationshipPopup(User targetUser);
	}
	
	private final AnswerMessageCallback answerMessageCallback;
	private final RelationshipClickListener relationshipClickListener;
	private final User currentUser;
	private final DateTimeFormatter dateTimeFormatter;
	private final Button answerButton;
	private final Label relationshipCancelledLabel;
	
	public ShowMessagePopup(Message message, User currentUser, List<MessageUploadFile> messageAttachments)
	{
		this(message, currentUser, messageAttachments, null);
	}
	
	public ShowMessagePopup(Message message, User currentUser, List<MessageUploadFile> messageAttachments, AnswerMessageCallback answerMessageCallback)
	{
		this(message, currentUser, messageAttachments, answerMessageCallback, null);
	}
	
	public ShowMessagePopup(Message message, User currentUser, List<MessageUploadFile> messageAttachments, AnswerMessageCallback answerMessageCallback, RelationshipClickListener relationshipListener)
	{
		this.answerMessageCallback = answerMessageCallback;
		this.currentUser = currentUser;
		this.relationshipClickListener = relationshipListener;
		
		dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(LocaleAware.super.getLocale());
		relationshipCancelledLabel = new Label(I18N.SHOWMESSAGEPOPUP_CAPTION_RELATIONSHIPCANCELLED.msg());
		relationshipCancelledLabel.addStyleName(CssStyle.MESSAGE_RELATIONSHIP_CANCELED_MSG.getStyleName());
		// TODO : changes for displaying custom text when the sender is blocked



		answerButton = createAnswerButton(message);
		setRelationshipCancelled(false);
		
		addStyleName(CssStyle.MESSAGE_DIALOG_POPUP.getStyleName());
		addStyleName(CssStyle.MESSAGE_DIALOG_POPUP_NORMAL.getStyleName());
		
		setCaption(I18N.SHOWMESSAGEPOPUP_CAPTION_MESSAGEHEADER.msg());
		setIcon(new ThemeResource("img/mail-normal.svg"));
		
		setPopupContent(createLayout(message, messageAttachments));
		
	}
	
	private Component createLayout(Message message, List<MessageUploadFile> messageAttachments)
	{
		final VerticalLayout wrapper = new VerticalLayout();
		wrapper.setWidth(100, Unit.PERCENTAGE);
		
		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth("80%");
		layout.setStyleName(CssStyle.LIST_MESSAGES_WRAPPER.getStyleName());
		
		final VerticalLayout firstMessageWrapper = new VerticalLayout();
		firstMessageWrapper.setSizeFull();
		firstMessageWrapper.setStyleName(CssStyle.FIRST_MESSAGE.getStyleName());
		
		final Component messageHeaderLayout = createMessageHeader();
		final Component messageLayout = createMessageView(message);
		final Component messageAttachmentContainer = createMailAttachment(messageAttachments);
		final Component replyToMessages = createReplyToMessages(message);
		
		firstMessageWrapper.addComponents(messageLayout, messageAttachmentContainer);
		
		layout.addComponents(messageHeaderLayout, firstMessageWrapper, replyToMessages);
		
		wrapper.addComponent(layout);
		wrapper.setComponentAlignment(layout, Alignment.TOP_CENTER);
		
		return wrapper;
	}
	
	private Component createMessageHeader()
	{
		final HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setWidth(100, Unit.PERCENTAGE);
		
		headerLayout.addComponents(answerButton);
		headerLayout.addComponent(relationshipCancelledLabel);
		
		return headerLayout;
	}
	
	private Component createMessageView(Message message)
	{


		final VerticalLayout messageLayout = new VerticalLayout();
		messageLayout.setWidth(100, Unit.PERCENTAGE);
		
		final HorizontalLayout messageHeaderLayout = new HorizontalLayout();
		messageHeaderLayout.setWidth(100, Unit.PERCENTAGE);
		
		final VerticalLayout messageInfoWrapper = new VerticalLayout();
		final VerticalLayout bodyLayout = new VerticalLayout();
		bodyLayout.setSizeFull();
		bodyLayout.setSpacing(false);
		
		final Label dateLabel = new Label(dateTimeFormatter.format(message.getSendDate()));

		// TODO : Changes for message dislay of blocked user

		String subject =  message.getSubject();

		String body = message.getBody();
//		if(message.getSenderEnvelope() != null && message.getSenderEnvelope().getUser() != null && message.getSenderEnvelope().getUser().isBlocked())
//		{
//			if(!currentUser.isBlocked()){
//			subject = I18N.IN_REVIEW_BY_TEAM.msg();
//			body = I18N.MESSAGE_CURRENTLY_BLOCKED.msg();
//			}
//		}

		//final Label subjectLabel = new Label(message.getSubject(), ContentMode.HTML);

		final Label subjectLabel = new Label(subject, ContentMode.HTML);
		final Button userNameLinkButton = new Button();

		userNameLinkButton.addStyleName(CssStyle.USERNAME_LABEL.getStyleName());
		userNameLinkButton.setHtmlContentAllowed(true);
		userNameLinkButton.addStyleName(CssStyle.TEXT_BUTTON.getStyleName());
		subjectLabel.addStyleName("subject");
		dateLabel.setStyleName(CssStyle.DATE_LABEL.getStyleName());
		
		final User senderUser = message.getSenderEnvelope().getUser();
		if (currentUser.equals(senderUser))
		{
			bodyLayout.addStyleName(CssStyle.MESSAGE_SENT_BODY.getStyleName());
			messageHeaderLayout.addStyleName(CssStyle.MESSAGE_SENT_HEADER.getStyleName());
			userNameLinkButton.setCaption(currentUser.getAlias());
			userNameLinkButton.setEnabled(false);
		}
		else
		{
			bodyLayout.setStyleName(CssStyle.MESSAGE_RECEIVED_BODY.getStyleName());
			messageHeaderLayout.addStyleName(CssStyle.MESSAGE_RECEIVED_HEADER.getStyleName());
			userNameLinkButton.setCaption(getSenderLabel(senderUser));
			userNameLinkButton.setEnabled(false);
			if (relationshipClickListener != null)
			{
				userNameLinkButton.addClickListener(event -> relationshipClickListener.openRelationshipPopup(senderUser));
				if(senderUser.getBlockedStatus()!=BlockedStatus.ADMIN_BLOCKED) {
					userNameLinkButton.setEnabled(true);
				}
			}
			
		}
		
		messageInfoWrapper.addComponents(userNameLinkButton, subjectLabel, dateLabel);
		messageHeaderLayout.addComponents(messageInfoWrapper);

		final Label bodyLabel = new Label(body);

		//final Label bodyLabel = new Label(message.getBody());
		bodyLabel.setContentMode(ContentMode.HTML);
		bodyLayout.addComponents(new HorizontalLine(), bodyLabel);
		bodyLayout.setWidth(100, Unit.PERCENTAGE);
		
		messageLayout.addComponents(messageHeaderLayout, bodyLayout);
		
		return messageLayout;
	}
	
	private String getSenderLabel(User user)
	{
		if (user == null) return I18N.SHOWMESSAGEPOPUP_CAPTION_TEAMGK.msg();
		
		final String alias = user.getAlias();
		
		if (user.isDataDeleted()) return DELETED_USER_WITH_ALIAS.msg(alias);
		if (user.isCanceled()) return CANCELED_USER_WITH_ALIAS.msg(alias);
		
		return alias;
	}
	
	private Component createMailAttachment(List<MessageUploadFile> messageAttachments)
	{
		final VerticalLayout messageAttachmentContainer = new VerticalLayout();
		messageAttachmentContainer.setStyleName(CssStyle.MESSAGE_ATTACHMENT_WRAPPER.getStyleName());
		
		messageAttachments.stream().map(this::getAttachmentLink).forEach(messageAttachmentContainer::addComponent);
		
		return messageAttachmentContainer;
	}
	
	private Component createReplyToMessages(Message message)
	{
		final VerticalLayout replyToMessages = new VerticalLayout();
		replyToMessages.setStyleName(CssStyle.REPLY_MESSAGES.getStyleName());
		
		for (Message replyToMessage = message.getReplyToMessage(); replyToMessage != null; replyToMessage = replyToMessage.getReplyToMessage())
		{
			replyToMessages.addComponent(createMessageView(replyToMessage));
		}
		
		return replyToMessages;
	}
	
	private Button createAnswerButton(Message message)
	{
		final Button button = new Button(I18N.MESSAGEPOPUP_ACTION_REPLY.msg(), FontAwesome.MAIL_REPLY);
		button.addClickListener(event -> answerMessageCallback.answerMessage(message));
		
		return button;
	}
	
	private Link getAttachmentLink(MessageUploadFile messageUploadFile)
	{
		if (messageUploadFile.getPath() == null) return new Link();
		
		final Link link = new Link(messageUploadFile.getFilename(), new FileResource(messageUploadFile.toFile()));
		link.setIcon(FontAwesome.PAPERCLIP);
		link.setTargetName("_blank");
		return link;
	}
	
	public void setRelationshipCancelled(boolean relationshipCancelled)
	{
		relationshipCancelledLabel.setVisible(relationshipCancelled);
		answerButton.setVisible(answerMessageCallback != null && !relationshipCancelled);
	}
}
