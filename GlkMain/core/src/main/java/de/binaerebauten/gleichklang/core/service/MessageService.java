package de.binaerebauten.gleichklang.core.service;

import static de.binaerebauten.gleichklang.core.model.message.MessageMailTemplate.ADMIN_ANSWER;
import static de.binaerebauten.gleichklang.core.model.message.MessageMailTemplate.SIGNATURE;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import de.binaerebauten.gleichklang.core.initializer.AppUI;
import de.binaerebauten.gleichklang.core.model.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

import de.binaerebauten.gleichklang.core.model.I18NEntity.Language;
import de.binaerebauten.gleichklang.core.model.mail.UserMailTemplate;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem;
import de.binaerebauten.gleichklang.core.model.message.AdminWorkItem_;
import de.binaerebauten.gleichklang.core.model.message.Envelope;
import de.binaerebauten.gleichklang.core.model.message.Envelope_;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.message.MessageAttachment;
import de.binaerebauten.gleichklang.core.model.message.Message_;
import de.binaerebauten.gleichklang.core.model.message.ReceiverEnvelope;
import de.binaerebauten.gleichklang.core.model.message.ReceiverEnvelope_;
import de.binaerebauten.gleichklang.core.model.message.SenderEnvelope;
import de.binaerebauten.gleichklang.core.model.message.SenderEnvelope_;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.repository.message.AdminWorkItemRepository;
import de.binaerebauten.gleichklang.core.repository.message.MessageAttachmentRepository;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.file.AbstractUploadFile;
import de.binaerebauten.gleichklang.core.service.file.FileService;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.MailTemplateInstance;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import de.binaerebauten.gleichklang.core.service.validator.ValidationException;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanFilteredItemsHandler;
import de.binaerebauten.gleichklang.core.view.component.LazyBeanItemContainer.LazyBeanItemsHandler;

/**
 * Internal message service.
 * <p>
 * TODO MessageFactory auslagern und für alle MessageTypes eine eigene Methode
 * erstellen
 */
@Service
public class MessageService
{
	private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);
	
	public enum MessageDirectory
	{
		INCOMING,
		OUTGOING,
		DRAFT,
		HIDDEN,
		DELETED_OR_UNKNOWN //Unknown state or not visible for user
	}

	public static class AdminWorkItemsIncomingSpecification implements Specification<AdminWorkItem>
	{
		private final User user;

		public AdminWorkItemsIncomingSpecification()
		{
			this(null);
		}

		public AdminWorkItemsIncomingSpecification(User user)
		{
			this.user = user;
		}

		@Override
		public Predicate toPredicate(Root<AdminWorkItem> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{

			final Join<AdminWorkItem, Message> messageJoin = root.join(AdminWorkItem_.message);
			final Predicate userPredicate = cb.equal(messageJoin.join(Message_.senderEnvelope).get(SenderEnvelope_.user), user);
			final Predicate predicate = cb.and
					(
							cb.equal(messageJoin.get(Message_.deleted), false),
							cb.equal(messageJoin.get(Message_.sent), true),
							cb.isNull(messageJoin.join(Message_.receiverEnvelope).get(ReceiverEnvelope_.user))
					);
			return user != null ? cb.and(predicate, userPredicate) : predicate;
		}
	}

	public static class AdminWorkItemsOutgoingMessageSpecification implements Specification<AdminWorkItem>
	{
		private final User user;

		public AdminWorkItemsOutgoingMessageSpecification()
		{
			this(null);
		}

		public AdminWorkItemsOutgoingMessageSpecification(User user)
		{
			this.user = user;
		}

		@Override
		public Predicate toPredicate(Root<AdminWorkItem> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{

			final Join<AdminWorkItem, Message> messageJoin = root.join(AdminWorkItem_.message);
			final Predicate userPredicate = cb.equal(messageJoin.join(Message_.receiverEnvelope).get(ReceiverEnvelope_.user), user);
			final Predicate predicate = cb.and
					(
							cb.equal(messageJoin.get(Message_.sent), true),
							cb.equal(messageJoin.get(Message_.deleted), false),
							messageJoin.join(Message_.senderEnvelope).get(SenderEnvelope_.user).isNull()
					);
			return user != null ? cb.and(predicate, userPredicate) : predicate;
		}
	}

	public static class AdminWorkItemsDraftMessageSpecification implements Specification<AdminWorkItem>
	{
		private final User user;

		public AdminWorkItemsDraftMessageSpecification()
		{
			this(null);
		}

		public AdminWorkItemsDraftMessageSpecification(User user)
		{
			this.user = user;
		}

		@Override
		public Predicate toPredicate(Root<AdminWorkItem> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{

			final Join<AdminWorkItem, Message> messageJoin = root.join(AdminWorkItem_.message);
			final Predicate userPredicate = cb.equal(messageJoin.join(Message_.receiverEnvelope).get(ReceiverEnvelope_.user), user);
			final Predicate predicate = cb.and
					(
							cb.equal(messageJoin.get(Message_.deleted), false),
							cb.equal(messageJoin.get(Message_.sent), false),
							messageJoin.join(Message_.senderEnvelope).get(SenderEnvelope_.user).isNull()
					);
			return user != null ? cb.and(predicate, userPredicate) : predicate;
		}
	}

	public static class IncomingSpecification implements Specification<Message>
	{
		private final User currentUser;
		private final boolean fromAdmin;
		private final boolean withDeleted;

		public IncomingSpecification(User currentUser)
		{
			this.currentUser = currentUser;
			this.fromAdmin = false;
			this.withDeleted = false;
		}

		public IncomingSpecification(User currentUser, boolean fromAdmin)
		{
			this.currentUser = currentUser;
			this.fromAdmin = fromAdmin;
			this.withDeleted = false;
		}

		public IncomingSpecification(User currentUser, boolean fromAdmin, boolean withDeleted)
		{
			this.currentUser = currentUser;
			this.fromAdmin = fromAdmin;
			this.withDeleted = withDeleted;
		}

		@Override
		public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			Admin admin=null;
			AdminService adminService = AppUI.getApplicationContext().getBean(AdminService.class);

			if(adminService!=null&&adminService.getCurrentUser()!=null) {
				admin = adminService.getCurrentUser();
			}
			final Join<Message, ReceiverEnvelope> receiver = root.join(Message_.receiverEnvelope);

			final Predicate fromAdminPredicate = cb.isNull(root.join(Message_.senderEnvelope).get(SenderEnvelope_.user));
			final Predicate fromUserPredicate = cb.isNotNull(root.join(Message_.senderEnvelope).get(SenderEnvelope_.user));
			final Join<Message, SenderEnvelope> sender = root.join(Message_.senderEnvelope);

			final Predicate predicate = cb.and
					(
							cb.equal(root.get(Message_.sent), true),
							cb.equal(receiver.get(ReceiverEnvelope_.user), currentUser),
							fromAdmin ? fromAdminPredicate : fromUserPredicate
					);

			Predicate spamFiltered =  cb.or
					(
							//cb.notEqual(root.get(Message_.senderEnvelope).get(Envelope_.user).get(User_.memberStatus), MemberStatus.ADMIN_BLOCKED),
							//cb.notEqual(root.get(Message_.senderEnvelope).get(Envelope_.user).get(User_.blockedStatus), BlockedStatus.ADMIN_BLOCKED)
							cb.isNull(root.get(Message_.senderEnvelope).get(Envelope_.user).get(User_.blockedDate)),
							cb.equal(root.get(Message_.receiverEnvelope).get(ReceiverEnvelope_.read), true),
							cb.gt(
									(cb.quot(cb.function("TIME_TO_SEC",
											Integer.class,cb.function(
													"TIMEDIFF",
													Integer.class,
													root.get(Message_.senderEnvelope).get(Envelope_.user).get(User_.blockedDate),
													root.get(Message_.createDate))
									),60)),
									3
							)
					);

			final Predicate filterDeleted = cb.and
					(
							cb.equal(root.get(Message_.deleted), false),
							cb.equal(receiver.get(ReceiverEnvelope_.hidden), false),
							cb.equal(receiver.get(ReceiverEnvelope_.deleted), false)
							//cb.notEqual(root.get(Message_.senderEnvelope).get(Envelope_.user).get(User_.blockedStatus),BlockedStatus.ADMIN_BLOCKED)
					);

			final Predicate spamFilter2 = cb.and
					(
							cb.notEqual(root.get(Message_.senderEnvelope).get(Envelope_.user).get(User_.blockedStatus),BlockedStatus.ADMIN_BLOCKED)
					);

			Predicate spamFiltered1 =  cb.or
					(
							//cb.notEqual(root.get(Message_.senderEnvelope).get(Envelope_.user).get(User_.memberStatus), MemberStatus.ADMIN_BLOCKED),
							//cb.notEqual(root.get(Message_.senderEnvelope).get(Envelope_.user).get(User_.blockedStatus), BlockedStatus.ADMIN_BLOCKED)
							cb.greaterThan(root.get(Message_.receiverEnvelope).get(Envelope_.user).get(User_.blockedDate), root.get(Message_.createDate)),
							cb.isNull(root.get(Message_.receiverEnvelope).get(Envelope_.user).get(User_.blockedDate))
					);
//							cb.gt(
//									(cb.quot(cb.function("TIME_TO_SEC",
//											Integer.class,cb.function(
//													"TIMEDIFF",
//													Integer.class,
//													root.get(Message_.receiverEnvelope).get(Envelope_.user).get(User_.blockedDate),
//													root.get(Message_.createDate))
//									),60)),
//									3
//							)
//					);

			if(fromAdmin || admin!=null)
				return withDeleted ? predicate : cb.and(predicate, filterDeleted);
			else
				return withDeleted ? predicate : cb.and(predicate, filterDeleted, spamFiltered,spamFiltered1, spamFilter2);
		}
	}



	public static class DraftSpecification implements Specification<Message>
	{
		private final User currentUser;

		public DraftSpecification(User currentUser)
		{
			this.currentUser = currentUser;
		}

		@Override
		public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Join<Message, SenderEnvelope> sender = root.join(Message_.senderEnvelope);

			return cb.and
					(
							cb.equal(root.get(Message_.deleted), false),
							cb.equal(root.get(Message_.sent), false),
							cb.equal(sender.get(SenderEnvelope_.hidden), false),
							cb.equal(sender.get(SenderEnvelope_.deleted), false),
							cb.equal(sender.get(SenderEnvelope_.user), currentUser)
					);
		}
	}

	public static class OutgoingSpecification implements Specification<Message>
	{
		private final User currentUser;
		private final boolean toAdmin;
		private final boolean withDeleted;

		public OutgoingSpecification(User currentUser)
		{
			this.currentUser = currentUser;
			this.toAdmin = false;
			this.withDeleted = false;
		}

		public OutgoingSpecification(User currentUser, boolean toAdmin)
		{
			this.currentUser = currentUser;
			this.toAdmin = toAdmin;
			this.withDeleted = false;
		}

		public OutgoingSpecification(User currentUser, boolean toAdmin, boolean withDeleted)
		{
			this.currentUser = currentUser;
			this.toAdmin = toAdmin;
			this.withDeleted = withDeleted;
		}

		@Override
		public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			Admin admin=null;
			AdminService adminService = AppUI.getApplicationContext().getBean(AdminService.class);

			if(adminService!=null&&adminService.getCurrentUser()!=null) {
				admin = adminService.getCurrentUser();
			}
			final Join<Message, SenderEnvelope> sender = root.join(Message_.senderEnvelope);

			final Predicate toAdminPredicate = cb.isNull(root.join(Message_.receiverEnvelope).get(ReceiverEnvelope_.user));
			final Predicate toUserPredicate = cb.isNotNull(root.join(Message_.receiverEnvelope).get(ReceiverEnvelope_.user));

			final Predicate predicate = cb.and
					(
							cb.isNotNull(root.get(Message_.sendDate)),
							cb.equal(root.get(Message_.sent), true),
							cb.equal(sender.get(SenderEnvelope_.user), currentUser),
							cb.notEqual(root.get(Message_.messageType), MessageType.LOVE_SCAMMER),
							toAdmin ? toAdminPredicate : toUserPredicate
					);

			final Predicate filterDeleted = cb.and
					(
							cb.equal(root.get(Message_.deleted), false),
							cb.equal(sender.get(SenderEnvelope_.hidden), false),
							cb.equal(sender.get(SenderEnvelope_.deleted), false)
					);

			Predicate spamFiltered =  cb.and
					(
							//cb.notEqual(root.get(Message_.senderEnvelope).get(Envelope_.user).get(User_.isBlocked),true)
							cb.notEqual(root.get(Message_.receiverEnvelope).get(Envelope_.user).get(User_.blockedStatus), BlockedStatus.ADMIN_BLOCKED)

					);


			if(toAdmin || admin!=null)
				return withDeleted ? predicate : cb.and(predicate, filterDeleted);
			else
				return withDeleted ? predicate : cb.and(predicate, filterDeleted, spamFiltered);

		}
	}

	public static class HiddenSpecification implements Specification<Message>
	{
		private final User currentUser;

		public HiddenSpecification(User currentUser)
		{
			this.currentUser = currentUser;
		}

		@Override
		public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> query, CriteriaBuilder cb)
		{
			final Subquery<Message> sq = query.subquery(Message.class);
			final Root<Envelope> envelope = sq.from(Envelope.class);

			final Predicate hiddenEnvelope = cb.and
					(
							cb.equal(envelope.get(Envelope_.hidden), true),
							cb.equal(envelope.get(Envelope_.deleted), false),
							cb.equal(envelope.get(Envelope_.user), currentUser)
					);

			sq.select(envelope.get(Envelope_.message)).where(hiddenEnvelope);
			return cb.and(cb.equal(root.get(Message_.deleted), false), cb.in(root).value(sq));
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(MailSendService.class);

	private static final int MAX_ATTACHMENTS = 10;

	private final MessageRepository messageRepository;
	private final MessageAttachmentRepository messageAttachmentRepository;
	private final AdminWorkItemRepository workItemRepository;
	private final FileService fileService;
	private final RelationshipRepository relationshipRepository;
	private final UserMailTemplateService userMailTemplateService;
	private final MailSendService mailSendService;
	private final TemplateEngineService templateEngineService;
	private final UserRepository userRepository;
	private Admin admin=null;

	@Lazy
	@Autowired
	private ScammingService scammingService;

	@Autowired
	public MessageService(MessageRepository messageRepository, MessageAttachmentRepository messageAttachmentRepository, AdminWorkItemRepository workItemRepository, FileService fileService, RelationshipRepository relationshipRepository, UserMailTemplateService userMailTemplateService, MailSendService mailSendService, TemplateEngineService templateEngineService,UserRepository userRepository)
	{
		this.messageRepository = Objects.requireNonNull(messageRepository);
		this.messageAttachmentRepository = Objects.requireNonNull(messageAttachmentRepository);
		this.workItemRepository = Objects.requireNonNull(workItemRepository);
		this.fileService = Objects.requireNonNull(fileService);
		this.relationshipRepository = Objects.requireNonNull(relationshipRepository);
		this.userMailTemplateService = Objects.requireNonNull(userMailTemplateService);
		this.mailSendService = Objects.requireNonNull(mailSendService);
		this.templateEngineService = Objects.requireNonNull(templateEngineService);
		this.userRepository = Objects.requireNonNull(userRepository);
	}

	public Message createNewMessage(User currentUser)
	{
		final Message message = new Message();
		message.setReceiverEnvelope(new ReceiverEnvelope());
		message.setSenderEnvelope(new SenderEnvelope());

		message.getReceiverEnvelope().setMessage(message);
		message.getSenderEnvelope().setMessage(message);
		message.getSenderEnvelope().setUser(currentUser);

		return message;
	}

	public Message createCancelMessage(User currentUser, User targetUser)
	{
		final Message message = createNewMessage(currentUser, targetUser);
		message.setSubject(I18N.MESSAGESERVICE_CANCELMESSAGE.msg());
		message.setBody(I18N.MESSAGESERVICE_CANCEL_BODY.msg());
		message.setMessageType(MessageType.CANCEL_MESSAGE);
		return message;
	}

	public Message createNewMessage(User currentUser, User targetUser)
	{
		final Message message = createNewMessage(currentUser);
		message.getReceiverEnvelope().setUser(targetUser);
		return message;
	}

	public Message createNewMessageByAdmin(User currentUser, User targetUser,Admin currentAdmin)
	{
		final Message message = createNewMessage(null);
		message.getReceiverEnvelope().setUser(targetUser);
		final Map<String, Object> model = new HashMap<>();
		model.put("firstname", targetUser.getAlias());
		model.put("lastname", "");
		model.put("admin", currentAdmin.getAlias());
		//model.put("memberStatus", targetUser.getMemberStatus().name());
		//model.put("memberStatus", targetUser.getMemberStatus().name());
		String signature = templateEngineService.getContentForTemplate(ADMIN_ANSWER, Language.DE, model);
		message.setBody(signature);
		return message;
	}

	private Language getLanguage(SignableUser user)
	{
		if(user instanceof User) return ((User) user).getUserSettings().getLanguage();
		return Language.DE;
	}

	public String createEmailFooter(Admin currentAdmin)
	{
		final Map<String, Object> model = new HashMap<>();
		model.put("admin", currentAdmin.getAlias());
		String signature = templateEngineService.getContentForTemplate(SIGNATURE, Language.DE, model);
		return signature;
	}


	public Message createAnswerMessage(Message messageToAnswer)
	{
		final Message answerMessage = createNewMessage(messageToAnswer.getReceiverEnvelope().getUser());
		answerMessage.setReplyToMessage(messageToAnswer);
		answerMessage.getReceiverEnvelope().setUser(messageToAnswer.getSenderEnvelope().getUser());
		answerMessage.setSubject(createAnswerSubject(messageToAnswer.getSubject()));

		return answerMessage;
	}

	public Message createAdminAnswerMessage(Message messageToAnswer, Admin currentUser)
	{
		final Map<String, Object> model = new HashMap<>();
		model.put("firstname", messageToAnswer.getSenderEnvelope().getUser().getFirstName());
		model.put("lastname", messageToAnswer.getSenderEnvelope().getUser().getLastName());
		model.put("admin", currentUser.getAlias());
		//model.put("memberStatus", messageToAnswer.getSenderEnvelope().getUser().getMemberStatus().name());
		//model.put("memberStatus", messageToAnswer.getSenderEnvelope().getUser().getMemberStatus().name());
		String signature =  templateEngineService.getContentForTemplate(ADMIN_ANSWER,  Language.DE, model);
		final Message answerMessage = createAnswerMessage(messageToAnswer);
		answerMessage.setBody(signature);

		return answerMessage;
	}

	private String createAnswerSubject(String subject)
	{
		final String answerWrapper = I18N.MESSAGE_MAILANSWERWRAPPER.msg("");
		String answerSubject = Strings.nullToEmpty(subject);

		final String prefix = Strings.commonPrefix(answerWrapper, answerSubject);
		final String suffix = Strings.commonSuffix(answerWrapper, answerSubject);

		if (!answerWrapper.equals(prefix + suffix))
			answerSubject = I18N.MESSAGE_MAILANSWERWRAPPER.msg(answerSubject);
		return answerSubject;
	}

	public Message createAbuseMessage(User currentUser, User targetUser)
	{
		final Message message = createNewMessage(currentUser);
		message.setSubject(I18N.MESSAGESERVICE_ABUSE.msg(targetUser.getAlias()));
		message.setMessageType(MessageType.ABUSE);

		return message;
	}

	@Transactional
	public void sendMessage(Message message, List<MessageUploadFile> messageUploadFiles, boolean withNotification) throws ValidationException
	{
		final User  targetUser;
		if(message.getReceiverEnvelope()!=null && message.getReceiverEnvelope().getUser()!=null  && userRepository.findById(message.getReceiverEnvelope().getUser().getId())!=null){
			targetUser = userRepository.findById(message.getReceiverEnvelope().getUser().getId());
		}
		else{
			targetUser = message.getReceiverEnvelope().getUser();
		}
		final User sourceUser = message.getSenderEnvelope().getUser();
		message.setSent(true);
		message.setSendDate(LocalDateTime.now());
		messageRepository.save(message);
		Message oldMessage = message.getReplyToMessage();
		messageRepository.save(message);

		if (messageUploadFiles != null)
		{
			checkValidation(messageUploadFiles.size());
			messageUploadFiles.forEach(AbstractUploadFile::saveWithUpload);
		}

		// Send a notification via E-Mail
		if (withNotification)
		{

			if (Objects.nonNull(targetUser) && Objects.nonNull(sourceUser) && Objects.isNull(admin))
			{
				final Relationship relationship = relationshipRepository.findRelationshipBySourceUserIdAndTargetUserId(sourceUser.getId(), targetUser.getId());
				final MailTemplateInstance mailTemplateInstance = userMailTemplateService.createMailTemplateInstance(UserMailTemplate.NEW_BOXNUMBER_CONTACT, sourceUser, targetUser, relationship.getMainCategory());
				try
				{
					if(!targetUser.isBlocked() && !sourceUser.isBlocked()) {
						mailSendService.sendEmailUser(targetUser, mailTemplateInstance, sourceUser);
					}
				}
				catch (MailException e)
				{
					logger.error("Error sending E-Mail, ", e);
				}
			}
			if (Objects.nonNull(admin) && Objects.nonNull(targetUser))
			{
				final MailTemplateInstance mailTemplateInstance;
				//final Relationship relationship = relationshipRepository.findRelationshipBySourceUserIdAndTargetUserId(targetUser.getId(), targetUser.getId());
				if(message.getReplyToMessage()==null && targetUser.getMemberStatus()!=(MemberStatus.CANCELED) && targetUser.getMemberStatus()!=(MemberStatus.ADMIN_CANCELED)){
					mailTemplateInstance = userMailTemplateService.createMailTemplateInstanceForAdminMessage(UserMailTemplate.ADMIN_FIRST_MESSAGE, admin, targetUser, message,oldMessage);
				}
				else if(targetUser.getMemberStatus()==(MemberStatus.CANCELED) || targetUser.getMemberStatus()==(MemberStatus.ADMIN_CANCELED)){
					mailTemplateInstance = userMailTemplateService.createMailTemplateInstanceForAdminMessage(UserMailTemplate.CANCELED_USER_MAIL, admin, targetUser, message, oldMessage);
				}
				else {
					mailTemplateInstance = userMailTemplateService.createMailTemplateInstanceForAdminMessage(UserMailTemplate.ADMIN_MESSAGE, admin, targetUser, message, oldMessage);
				}
				try
				{
					if(!targetUser.isBlocked()) {
						if(targetUser.getMemberStatus()==MemberStatus.CANCELED || targetUser.getMemberStatus()==(MemberStatus.ADMIN_CANCELED)) {
							mailSendService.sendEmail(targetUser, mailTemplateInstance, messageUploadFiles);
						}
						else{
							mailSendService.sendEmail(targetUser, mailTemplateInstance);
						}
					}
				}
				catch (MailException e)
				{
					logger.error("Error sending E-Mail, ", e);
				}
			}
		}
	}


	@Transactional
	public void sendMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		//Long userIdReceiver;
		Long userIdSender;
//		if(message!= null && message.getReceiverEnvelope()!=null && message.getReceiverEnvelope().getUser() != null)
//		{
//
//			//userIdReceiver = message.getReceiverEnvelope().getUser().getId();
//
//			if(userIdReceiver != null && userRepository.findById(userIdReceiver).isBlocked()){
//				Notification.show(I18N.USER_CAN_NOT_RECEIVE_MESSAGES.msg(), Notification.Type.TRAY_NOTIFICATION);
//				return;
//			}
//		}
		sendMessage(message, messageUploadFiles, true);
		Notification.show(I18N.USER_MESSAGES_SENT.msg(), Notification.Type.TRAY_NOTIFICATION);
		if(message.getSenderEnvelope() != null && message.getSenderEnvelope().getUser() != null)
		{
			userIdSender = message.getSenderEnvelope().getUser().getId();
			if(!userRepository.findById(userIdSender).isBlocked() && Objects.isNull(admin)) {

				scammingService.liveScammingDetectionFirstMessage(message);
				if(scammingService.isB()){
					scammingService.liveScammingDetection(message);
					scammingService.liveScammingDetectionForMail(message);
				}
				scammingService.setB(true);
			}}
	}

	//Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.162 Safari/537.36

	@Transactional
	public void sendMessageToAdmin(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
        if(UI.getCurrent()!=null && UI.getCurrent().getPage()!=null && UI.getCurrent().getPage().getWebBrowser()!=null) {
			WebBrowser check = UI.getCurrent().getPage().getWebBrowser();
			String mobile = check.getBrowserApplication();

			if (check.isChrome()) {
				String[] chrome = mobile.split("Chrome/");
				String s1 = chrome[1];
				String[] version = s1.split(" ");
				String s2 = version[0];
				if (mobile.contains("Mobile") || mobile.contains("mobile")) {
					message.setSubject(message.getSubject() + " (Mobile, Chrome " + s2 + ")");
				} else {
					message.setSubject(message.getSubject() + " (Desktop, Chrome " + s2 + ")");
				}
			} else if (check.isFirefox()) {
				String[] mozilla = mobile.split("Mozilla/");
				String s1 = mozilla[1];
				String[] version = s1.split(" ");
				String s2 = version[0];
				if (mobile.contains("Mobile") || mobile.contains("mobile")) {
					message.setSubject(message.getSubject() + " (Mobile, Mozilla " + s2 + ")");
				} else {
					message.setSubject(message.getSubject() + " (Desktop, Mozilla " + s2 + ")");
				}
			} else if (check.isSafari()) {
				String[] Safari = mobile.split("Safari/");
				String s1 = Safari[1];
				String[] version = s1.split(" ");
				String s2 = version[0];
				if (mobile.contains("Mobile") || mobile.contains("mobile")) {
					message.setSubject(message.getSubject() + " (Mobile, Safari " + s2 + ")");
				} else {
					message.setSubject(message.getSubject() + " (Desktop, Safari " + s2 + ")");
				}
			}
		}
		sendMessage(message, messageUploadFiles, false);

		/* from user to admin */
		final AdminWorkItem item = new AdminWorkItem();
		item.setMessage(message);
		workItemRepository.save(item);
	}

	@Transactional
	public void sendMessageFromAdmin(Message message, List<MessageUploadFile> messageUploadFiles, Admin admin) throws ValidationException
	{
		this.admin = admin;
		sendMessage(message, messageUploadFiles);

		/* from admin to user */
		final AdminWorkItem item = workItemRepository.findByMessage(message.getReplyToMessage());
		if(item!=null) {
			item.setAdmin(admin);
			item.setWorkItemStatus(AdminWorkItem.AdminWorkItemStatus.ANSWERED);
			workItemRepository.save(item);
		}

		/* save "copy" of original workitem on reply message */
		//first check if there is already a workitem (in case draft)

		Long userIdReceiver = message.getReceiverEnvelope().getUser().getId();

		if(userIdReceiver != null && userRepository.findById(userIdReceiver).isBlocked()){
			messageRepository.save(message);
		}

		final AdminWorkItem oldItem = workItemRepository.findByMessage(message);

		//then update item
		if (oldItem != null)
		{
			oldItem.setAdmin(admin);
			oldItem.setWorkItemStatus(AdminWorkItem.AdminWorkItemStatus.ANSWERED);
			workItemRepository.save(oldItem);
		}
		//or create new
		else
		{
			final AdminWorkItem copy = new AdminWorkItem();
			copy.setMessage(message);
			copy.setAdmin(admin);
			copy.setWorkItemStatus(AdminWorkItem.AdminWorkItemStatus.ANSWERED);
			workItemRepository.save(copy);
		}
	}

	@Transactional
	public void saveAdminMessage(Message message, List<MessageUploadFile> messageUploadFiles, Admin admin) throws ValidationException
	{
		checkValidation(messageUploadFiles.size());

		message.setSent(false);
		messageRepository.save(message);
		messageUploadFiles.forEach(AbstractUploadFile::save);

		/* save "copy" of original workitem on reply message */
		//first check if there is already a workitem
		final AdminWorkItem oldItem = workItemRepository.findByMessage(message);

		//then update item
		if (oldItem != null)
		{
			oldItem.setAdmin(admin);
			workItemRepository.save(oldItem);
		}
		//or create new
		else
		{
			final AdminWorkItem item = new AdminWorkItem();
			item.setMessage(message);
			item.setAdmin(admin);
			workItemRepository.save(item);
		}
	}

	@Transactional
	public void saveMessage(Message message, List<MessageUploadFile> messageUploadFiles) throws ValidationException
	{
		checkValidation(messageUploadFiles.size());

		message.setSent(false);
		messageRepository.save(message);

		messageUploadFiles.forEach(AbstractUploadFile::save);
	}

	public void deleteMessages(User currentUser, Collection<Message> messages)
	{
		messages.forEach(message ->
		{
			if (currentUser.equals(message.getReceiverEnvelope().getUser()))
				message.getReceiverEnvelope().setDeleted(true);
			if (currentUser.equals(message.getSenderEnvelope().getUser()))
				message.getSenderEnvelope().setDeleted(true);
		});

		messageRepository.save(messages);
	}

	@Transactional
	public void hideMessages(User currentUser, Collection<Message> messages)
	{
		messages.forEach(message ->
		{
			if (currentUser.equals(message.getReceiverEnvelope().getUser()))
				message.getReceiverEnvelope().setHidden(true);
			if (currentUser.equals(message.getSenderEnvelope().getUser()))
				message.getSenderEnvelope().setHidden(true);
		});

		messageRepository.save(messages);
	}

	@Transactional
	public void restoreMessages(User currentUser, Collection<Message> messages)
	{
		messages.forEach(message ->
		{
			if (currentUser.equals(message.getReceiverEnvelope().getUser()))
				message.getReceiverEnvelope().setHidden(false);
			if (currentUser.equals(message.getSenderEnvelope().getUser()))
				message.getSenderEnvelope().setHidden(false);
		});

		messageRepository.save(messages);
	}

	@Transactional
	public void readMessages(Collection<Message> messages)
	{
		messages.forEach(message -> message.getReceiverEnvelope().setRead(true));
		messageRepository.save(messages);
	}

	@Transactional
	public void readAdminMessage(AdminWorkItem item, Admin admin)
	{
		item.getMessage().getReceiverEnvelope().setRead(true);
		item.setAdmin(admin);
		item.setWorkItemStatus(AdminWorkItem.AdminWorkItemStatus.OPEN);
		workItemRepository.save(item);
		messageRepository.save(item.getMessage());
	}

	@Transactional
	public void unreadMessages(Collection<Message> messages)
	{
		messages.forEach(message -> message.getReceiverEnvelope().setRead(false));
		messageRepository.save(messages);
	}

	@Transactional
	public void unreadAdminMessage(AdminWorkItem item)
	{
		item.getMessage().getReceiverEnvelope().setRead(false);
		item.setAdmin(null);
		item.setWorkItemStatus(AdminWorkItem.AdminWorkItemStatus.NEW);
		workItemRepository.save(item);
	}

	@Transactional
	public void ignoreAdminMessage(AdminWorkItem item)
	{
		item.setWorkItemStatus(AdminWorkItem.AdminWorkItemStatus.IGNORED);
		workItemRepository.save(item);
	}

	@Transactional
	public void doneAdminMessage(AdminWorkItem item)
	{
		item.setWorkItemStatus(AdminWorkItem.AdminWorkItemStatus.DONE);
		workItemRepository.save(item);
	}

	public LazyBeanFilteredItemsHandler<Message> createDraftMessagesHandler(User currentUser)
	{
		final Specifications<Message> specs = Specifications.where(new DraftSpecification(currentUser));
		return (specification, pageable) -> messageRepository.findAll(specs.and(specification), pageable);
	}

	public LazyBeanFilteredItemsHandler<Message> createHiddenMessagesHandler(User currentUser)
	{
		final Specifications<Message> specs = Specifications.where(new HiddenSpecification(currentUser));
		return (specification, pageable) -> messageRepository.findAll(specs.and(specification), pageable);
	}

	public LazyBeanFilteredItemsHandler<Message> createIncomingMessagesHandler(User currentUser)
	{
		final Specifications<Message> specs = Specifications.where(new IncomingSpecification(currentUser));
		return (specification, pageable) -> messageRepository.findAll(specs.and(specification), pageable);
	}

	public LazyBeanFilteredItemsHandler<Message> createOutgoingMessagesHandler(User currentUser)
	{
		final Specifications<Message> specs = Specifications.where(new OutgoingSpecification(currentUser));
		return (specification, pageable) -> messageRepository.findAll(specs.and(specification), pageable);
	}

	public LazyBeanItemsHandler<Message> createListMessagesHandler(User currentUser, User targetUser)
	{
		User cUser = userRepository.findById(currentUser.getId());
		User tUser = userRepository.findById(targetUser.getId());
		if(tUser.getBlockedDate()!=null) {
			return pageable -> messageRepository.findIncomingsAndOutgoingsByUserAndTargetUser2(currentUser, targetUser,tUser.getBlockedDate().minusMinutes(2), pageable);
        }
        else if(cUser.getBlockedDate()!=null){
			return pageable -> messageRepository.findIncomingsAndOutgoingsByUserAndTargetUser1(currentUser, targetUser,cUser.getBlockedDate().minusMinutes(2), pageable);
		}
        else{
			return pageable -> messageRepository.findIncomingsAndOutgoingsByUserAndTargetUser(currentUser, targetUser, pageable);

        }
	}


	public LazyBeanFilteredItemsHandler<AdminWorkItem> createAdminWorkItemMessagesHandler(MessageDirectory messageDirectory)
	{
		return createAdminWorkItemMessagesHandler(messageDirectory, null);
	}

	public LazyBeanFilteredItemsHandler<AdminWorkItem> createAdminWorkItemMessagesHandler(MessageDirectory messageDirectory, User user)
	{
		final Specification<AdminWorkItem> directorySpec;
		switch (messageDirectory)
		{
			case INCOMING:
				directorySpec = new AdminWorkItemsIncomingSpecification(user);
				break;
			case OUTGOING:
				directorySpec = new AdminWorkItemsOutgoingMessageSpecification(user);
				break;
			case DRAFT:
				directorySpec = new AdminWorkItemsDraftMessageSpecification(user);
				break;
			default:
				return null;
		}

		final Specifications<AdminWorkItem> specs = Specifications.where(directorySpec);
		return (specification, pageable) -> workItemRepository.findAll(specs.and(specification), pageable);
	}

	public LazyBeanFilteredItemsHandler<Message> createUserAdminMessageIncomingMessagesHandler(User currentUser)
	{
		final Specifications<Message> specs = Specifications.where(new IncomingSpecification(currentUser, true));
		return (specification, pageable) -> messageRepository.findAll(specs.and(specification), pageable);
	}

	public LazyBeanFilteredItemsHandler<Message> createUserAdminMessageOutgoingMessagesHandler(User currentUser)
	{
		final Specifications<Message> specs = Specifications.where(new OutgoingSpecification(currentUser, true));
		return (specification, pageable) -> messageRepository.findAll(specs.and(specification), pageable);
	}

	private void checkValidation(long attachmentSize) throws ValidationException
	{
		if (attachmentSize > MAX_ATTACHMENTS)
		{
			throw new ValidationException("Maximale Anhänge überschritten");
		}
	}

	public List<MessageUploadFile> getMessageAttachments(Message message)
	{
		final List<MessageAttachment> messageAttachments = messageAttachmentRepository.findByMessage(message);
		return messageAttachments.stream().map(value -> new MessageUploadFile(value, fileService)).collect(Collectors.toList());
	}

	public MessageUploadFile createMessageAttachment(Message message, int currentAttachmentSize) throws ValidationException
	{
		if (message == null) return null;

		checkValidation(currentAttachmentSize + 1);
		return new MessageUploadFile(message, fileService);
	}

	public MessageDirectory getMessageDirectory(Message message, User currentUser)
	{
		if (currentUser == null || message.isDeleted())
			return MessageDirectory.DELETED_OR_UNKNOWN;

		if (currentUser.equals(message.getSenderEnvelope().getUser()))
		{
			if (message.getSenderEnvelope().isHidden())
				return MessageDirectory.HIDDEN;
			if (message.isSent())
				return MessageDirectory.OUTGOING;
			return MessageDirectory.DRAFT;
		}

		if (currentUser.equals(message.getReceiverEnvelope().getUser()) && message.isSent())
		{
			if (message.getReceiverEnvelope().isHidden())
				return MessageDirectory.HIDDEN;
			return MessageDirectory.INCOMING;
		}

		return MessageDirectory.DELETED_OR_UNKNOWN;
	}

	@Transactional(readOnly = true)
	public boolean isRelationshipsCancelled(Message message)
	{
		if (message == null) return false;
		if (message.getReceiverEnvelope() == null || message.getSenderEnvelope() == null)
			return false;

		final User sourceUser = message.getReceiverEnvelope().getUser();
		final User targetUser = message.getSenderEnvelope().getUser();

		if (sourceUser == null || targetUser == null) return false;

		if (sourceUser.isDataDeleted() || targetUser.isDataDeleted())
			return true;
		if (sourceUser.isCanceled() || targetUser.isCanceled()) return true;

		if (sourceUser.isAdminCanceled() || targetUser.isAdminCanceled()) return true;


		// check relationship deleted
		final Relationship relationshipSource = relationshipRepository.findRelationshipBySourceUserAndTargetUser(sourceUser, targetUser);
		if (relationshipSource == null) return false;
		if (relationshipSource.isDeleted()) return true;

		final Relationship relationshipTarget = relationshipRepository.findRelationshipBySourceUserAndTargetUser(targetUser, sourceUser);
		if (relationshipTarget == null) return false;
		if (relationshipTarget.isDeleted()) return true;

		// check deactivated categories
		return Collections.disjoint(relationshipSource.getCategories(), sourceUser.getCategories()) ||
				Collections.disjoint(relationshipTarget.getCategories(), targetUser.getCategories());

	}

	@Transactional(readOnly = true)
	public boolean isExistsMessages(Relationship relationship)
	{
		return 0 < messageRepository.countIncomingsAndOutgoingsByUserAndTargetUser(relationship.getSourceUser(), relationship.getTargetUser());
	}

	@Transactional(readOnly = true)
	public List<User> getReceiverList(User user)
	{
		return relationshipRepository.findAllRelationshipsForUser(user).stream()
				.filter(relationship -> !Collections.disjoint(relationship.getCategories(), relationship.getSourceUser().getCategories()))
				.filter(relationship -> !Collections.disjoint(relationship.getCategories(), relationship.getTargetUser().getCategories()))
				.map(Relationship::getTargetUser)
				.filter(targetUser -> !targetUser.isDataDeleted())
				.filter(targetUser -> !targetUser.isCanceled())
				.collect(Collectors.toList());
	}

	/**
	 * heavy operation: returns table with communication counts
	 * <p>
	 * keys: user_ids value: count of communications from row key to column key
	 *
	 * @return
	 */
	public Table<Long, Long, Long> getMessageStatistics()
	{
		final Set<Object[]> messageStatistics = messageRepository.getMessageStatistics();
		final Table<Long, Long, Long> statisticTable = HashBasedTable.create();
		for (Object[] entry : messageStatistics)
		{
			statisticTable.put((Long) entry[0], (Long) entry[1], (Long) entry[2]);
		}

		return statisticTable;
	}

	/**
	 * returns a map with communications statistics:
	 * <p>
	 * key: count of both side communications (3 writes, 1 answeres = 1; 2
	 * writes, 3 answered = 2; 5 writes, 0 answered = not exists) value: how
	 * often for all relationships the key one exists
	 *
	 * @param statisticTable
	 * @return
	 * @see #getMessageStatistics()
	 */
	public Map<Long, Long> getCommunicationStatistics(Table<Long, Long, Long> statisticTable)
	{
		final Map<Long, Long> resultMap = new HashMap<>();

		for (Table.Cell<Long, Long, Long> cell : statisticTable.cellSet())
		{
			if (cell.getRowKey() < cell.getColumnKey())
			{
				final Long refValue = statisticTable.get(cell.getColumnKey(), cell.getRowKey());
				if (refValue != null)
				{
					final Long value = Math.min(refValue, cell.getValue());
					resultMap.put(value, resultMap.getOrDefault(value, 0L) + 1);
				}
			}
		}

		return resultMap;
	}

	/**
	 * Returns the count of substantial communications.
	 *
	 * @param communicationStatistics
	 * @param substantialNumber
	 * @return
	 * @see #getCommunicationStatistics(Table)
	 */
	public Long countSubstantialCommunications(Map<Long, Long> communicationStatistics, long substantialNumber)
	{
		Objects.requireNonNull(communicationStatistics);

		long result = 0;

		for (Map.Entry<Long, Long> messageStaticEntry : communicationStatistics.entrySet())
		{
			if (messageStaticEntry.getKey() >= substantialNumber)
				result += messageStaticEntry.getValue();
		}

		return result;
	}

	/**
	 * Returns the count of substantial communications.
	 *
	 * @param messageStaticMap
	 * @param substantialNumber
	 * @return
	 * @see #getCommunicationStatistics(Table)
	 */
	public Long countSubstantialCommunications(Table<Long, Long, Long> messageStaticMap, long substantialNumber)
	{
		Objects.requireNonNull(messageStaticMap);

		return countSubstantialCommunications(getCommunicationStatistics(messageStaticMap), substantialNumber);
	}

	/**
	 * heavy operation: returns table with message sent dates
	 * <p>
	 * keys: user_ids value: set of sent dates from the messages between the
	 * user_ids
	 *
	 * @return
	 */
	public Table<Long, Long, TreeSet<LocalDateTime>> getMessageStatistics(User user)
	{
		final Set<Object[]> sendMessageStatistics = messageRepository.getSendMessageStatistics(user);
		final Set<Object[]> receiveMessageStatistics = messageRepository.getReceiveMessageStatistics(user);
		final Set<Object[]> messageStatistics = new HashSet<>();
		messageStatistics.addAll(sendMessageStatistics);
		messageStatistics.addAll(receiveMessageStatistics);

		final Table<Long, Long, TreeSet<LocalDateTime>> statisticTable = HashBasedTable.create();

		try {
			for (Object[] entry : messageStatistics) {
				TreeSet<LocalDateTime> localDateTimes = statisticTable.get(entry[0], entry[1]);
				if (localDateTimes == null) localDateTimes = new TreeSet<>();
				localDateTimes.add((LocalDateTime) entry[2]);
				statisticTable.put((Long) entry[0], (Long) entry[1], localDateTimes);
			}
		}catch (Exception ex)
		{
			LOG.error("getMessageStatistics", ex);
		}
		return statisticTable;
	}
	/**
	 * heavy operation: returns table with message sent dates
	 * <p>
	 * keys: user_ids value: set of sent dates from the messages between the
	 * user_ids
	 *
	 * @return
	 */
	public Table<Long, Long, TreeSet<LocalDateTime>> getMessageStatisticsByCategory(User user, RecommendationCategory recommendationCategory)
	{
		final Set<Object[]> sendMessageStatistics = messageRepository.getSendMessageStatistics(user,recommendationCategory);
		final Set<Object[]> messageStatistics = new HashSet<>();
		messageStatistics.addAll(sendMessageStatistics);

		final Table<Long, Long, TreeSet<LocalDateTime>> statisticTable = HashBasedTable.create();

		try {
			for (Object[] entry : messageStatistics) {
				TreeSet<LocalDateTime> localDateTimes = statisticTable.get(entry[0], entry[1]);
				if (localDateTimes == null) localDateTimes = new TreeSet<>();
				localDateTimes.add((LocalDateTime) entry[2]);
				statisticTable.put((Long) entry[0], (Long) entry[1], localDateTimes);
			}
		}catch (Exception ex)
		{
			LOG.error("getMessageStatisticsByCategory", ex);
		}
		return statisticTable;
	}
	public Long countFirstWrote(Table<Long, Long, TreeSet<LocalDateTime>> statisticTable, Long userId, boolean onlyAnswered)
	{
		Long count = 0L;
		final Map<Long, TreeSet<LocalDateTime>> sentMessages = statisticTable.row(userId);
		for (Map.Entry<Long, TreeSet<LocalDateTime>> entry : sentMessages.entrySet())
		{
			final LocalDateTime firstSentDate = entry.getValue().first();
			final TreeSet<LocalDateTime> receiverSentDates = statisticTable.get(entry.getKey(), userId);
			if (receiverSentDates == null && !onlyAnswered || receiverSentDates != null && receiverSentDates.first().isAfter(firstSentDate))
				count++;
		}

		return count;
	}


	public Long countFirstReceived(Table<Long, Long, TreeSet<LocalDateTime>> statisticTable, Long userId, boolean onlyAnswered)
	{
		Long count = 0L;
		final Map<Long, TreeSet<LocalDateTime>> sentMessages = statisticTable.column(userId);
		for (Map.Entry<Long, TreeSet<LocalDateTime>> entry : sentMessages.entrySet())
		{
			final LocalDateTime firstReceivedDate = entry.getValue().first();
			final TreeSet<LocalDateTime> senderSentDates = statisticTable.get(userId, entry.getKey());
			if (senderSentDates == null && !onlyAnswered || senderSentDates != null && senderSentDates.first().isAfter(firstReceivedDate))
				count++;
		}

		return count;
	}

	public Table<Long, Long, Long> convertMessageStatistics(Table<Long, Long, TreeSet<LocalDateTime>> statisticTable)
	{
		final Table<Long, Long, Long> convertedTable = HashBasedTable.create();
		for (Table.Cell<Long, Long, TreeSet<LocalDateTime>> cell : statisticTable.cellSet())
		{
			convertedTable.put(cell.getRowKey(), cell.getColumnKey(), (long) cell.getValue().size());
		}
		return convertedTable;
	}

	public boolean isSentToAdmin(User user, MessageType messageType, LocalDateTime startDate)
	{
		return messageRepository.isSentToAdmin(user, messageType, startDate);
	}

	public List<Message> getOutgoingMessages(User user)
	{
		return messageRepository.findAll(new OutgoingSpecification(user, false, true));
	}

	public List<Message> getAdminMessages(User user)
	{
		final Specifications<Message> specification = Specifications.where(new IncomingSpecification(user, true, true)).or(new OutgoingSpecification(user, true, true));
		return messageRepository.findAll(specification);
	}
}