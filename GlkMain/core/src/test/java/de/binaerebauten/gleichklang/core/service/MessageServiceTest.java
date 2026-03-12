package de.binaerebauten.gleichklang.core.service;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import de.binaerebauten.gleichklang.core.model.matching.Relationship;
import de.binaerebauten.gleichklang.core.model.media.FileEntity;
import de.binaerebauten.gleichklang.core.model.message.Message;
import de.binaerebauten.gleichklang.core.model.message.Message.MessageType;
import de.binaerebauten.gleichklang.core.model.message.MessageAttachment;
import de.binaerebauten.gleichklang.core.model.user.MemberStatus;
import de.binaerebauten.gleichklang.core.model.user.RecommendationCategory;
import de.binaerebauten.gleichklang.core.model.user.User;
import de.binaerebauten.gleichklang.core.repository.BasePersistenceTest;
import de.binaerebauten.gleichklang.core.repository.RelationshipRepository;
import de.binaerebauten.gleichklang.core.repository.message.AdminWorkItemRepository;
import de.binaerebauten.gleichklang.core.repository.message.MessageAttachmentRepository;
import de.binaerebauten.gleichklang.core.repository.message.MessageRepository;
import de.binaerebauten.gleichklang.core.repository.user.UserRepository;
import de.binaerebauten.gleichklang.core.service.MessageService.MessageDirectory;
import de.binaerebauten.gleichklang.core.service.file.FileService;
import de.binaerebauten.gleichklang.core.service.file.MessageUploadFile;
import de.binaerebauten.gleichklang.core.service.mail.MailSendService;
import de.binaerebauten.gleichklang.core.service.mail.UserMailTemplateService;
import de.binaerebauten.gleichklang.core.service.template.TemplateEngineService;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory;
import de.binaerebauten.gleichklang.core.utils.DefaultEntityFactory.MessageState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailSendException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageServiceTest extends BasePersistenceTest
{
	private final Map<MessageState, Message> messages = new HashMap<>();
	
	@Mock
	private RelationshipRepository relationshipRepository;
	
	@Mock
	private MessageAttachmentRepository messageAttachmentRepository;

	@Mock
	private UserRepository userRepository;
	
	@Mock
	private MailSendService mailSendService;
	
	@Mock
	private ScammingService scammingService;

	
	@Autowired
	private MessageRepository messageRepository;

	@Mock
	private MessageService messageService;
	
	@Autowired
	private DefaultEntityFactory entityFactory;
	
	private User userSender;
	
	private User userReceiver;

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		
		final FileService fileService = mock(FileService.class);
		final TemplateEngineService templateEngineService = mock(TemplateEngineService.class, (Answer) invocation -> "Template Engine Result");
		final UserMailTemplateService userMailTemplateService = mock(UserMailTemplateService.class);
		final AdminWorkItemRepository adminWorkItemRepository = mock(AdminWorkItemRepository.class);
		
		when(relationshipRepository.findRelationshipBySourceUserIdAndTargetUserId(Mockito.any(), Mockito.any())).thenReturn(new Relationship());
		
		messageService = new MessageService(messageRepository, messageAttachmentRepository, adminWorkItemRepository, fileService, relationshipRepository, userMailTemplateService, mailSendService, templateEngineService,userRepository);
		ReflectionTestUtils.setField(messageService, "scammingService", scammingService);
		
		userSender = entityFactory.persistDefaultUser("sender");
		userReceiver = entityFactory.persistDefaultUser("receiver");
		
		for (final MessageState messageState : MessageState.values())
		{
			final Message message = entityFactory.persistDefaultMessage(userReceiver, userSender, messageState);
			messages.put(messageState, message);
		}
	}
	
	@After
	public void teardown()
	{
		entityFactory.reset();
	}
	
	@Test
	public void createDraftMessagesHandler() throws Exception
	{
		// draft
		final long expected = 1;
		final Page<Message> list = messageService.createDraftMessagesHandler(userSender).getItems(null, new PageRequest(0, 1000));
		assertThat(list.getTotalElements(), equalTo(expected));
		for (final Message message : list.getContent())
		{
			assertThat(message, equalTo(messages.get(MessageState.DRAFT)));
		}
	}
	
	
	public void createHiddenMessagesHandler() throws Exception
	{
		// testMessageDeletedForReceiver
		{
			// incoming deleted
			final long expected = 1;
			final Page<Message> list = messageService.createHiddenMessagesHandler(userReceiver).getItems(null, new PageRequest(0, 1000));
			assertThat(list.getTotalElements(), equalTo(expected));
			for (final Message message : list.getContent())
			{
				assertThat(message, equalTo(messages.get(MessageState.HIDDEN_INCOMING)));
			}
		}
		
		// testMessageDeletedForSender
		{
			// draft + outgoing deleted
			final long expected = 2;
			final Page<Message> list = messageService.createHiddenMessagesHandler(userSender).getItems(null, new PageRequest(0, 1000));
			assertThat(list.getTotalElements(), equalTo(expected));
			for (final Message message : list.getContent())
			{
				assertThat(message, anyOf(equalTo(messages.get(MessageState.HIDDEN_DRAFT)), equalTo(messages.get(MessageState.HIDDEN_OUTGOING))));
			}
		}
	}
	
	
	public void createIncomingMessagesHandler() throws Exception
	{
		// incoming + deleted outgoing + hidden outgoing + cancel_message
		final long expected = 9;
		final Page<Message> list = messageService.createIncomingMessagesHandler(userReceiver).getItems(null, new PageRequest(0, 1000));
		assertThat(list.getTotalElements(), equalTo(expected));
		for (final Message message : list.getContent())
		{
			assertThat(message, anyOf(equalTo(messages.get(MessageState.SENT)), equalTo(messages.get(MessageState.DELETED_OUTGOING)), equalTo(messages.get(MessageState.HIDDEN_OUTGOING)), equalTo(messages.get(MessageState.SENT_CANCEL_MESSAGE))));
		}
	}
	
	
	public void createOutgoingMessagesHandler() throws Exception
	{
		// outgoing + deleted incoming + hidden incoming + cancel_message
		final long expected = 4;
		final Page<Message> list = messageService.createOutgoingMessagesHandler(userSender).getItems(null, new PageRequest(0, 1000));
		assertThat(list.getTotalElements(), equalTo(expected));
		for (final Message message : list.getContent())
		{
			assertThat(message, anyOf(equalTo(messages.get(MessageState.SENT)), equalTo(messages.get(MessageState.DELETED_INCOMING)), equalTo(messages.get(MessageState.HIDDEN_INCOMING)), equalTo(messages.get(MessageState.SENT_CANCEL_MESSAGE))));
		}
	}
	
	
	public void createListMessagesHandler() throws Exception
	{
		// sent + deleted incoming + hidden incoming + cancel_message
		final long expected = 4;
		final Page<Message> list = messageService.createListMessagesHandler(userSender, userReceiver).getItems(new PageRequest(0, 1000));
		assertThat(list.getTotalElements(), equalTo(expected));
		for (final Message message : list.getContent())
		{
			assertThat(message, anyOf(equalTo(messages.get(MessageState.SENT)), equalTo(messages.get(MessageState.DELETED_INCOMING)), equalTo(messages.get(MessageState.HIDDEN_INCOMING)), equalTo(messages.get(MessageState.SENT_CANCEL_MESSAGE))));
		}
	}
	
	private void checkMessage(Message message, User sender, User receiver)
	{
		assertThat(message, notNullValue());
		assertThat(message.getReceiverEnvelope(), notNullValue());
		assertThat(message.getReceiverEnvelope().getMessage(), equalTo(message));
		assertThat(message.getReceiverEnvelope().getUser(), equalTo(receiver));
		assertThat(message.getSenderEnvelope(), notNullValue());
		assertThat(message.getSenderEnvelope().getMessage(), equalTo(message));
		assertThat(message.getSenderEnvelope().getUser(), equalTo(sender));
	}
	
	
	public void createNewMessage() throws Exception
	{
		final Message message = messageService.createNewMessage(userSender);
		checkMessage(message, userSender, null);
	}
	
	
	public void createNewMessageWithTarget() throws Exception
	{
		final Message message = messageService.createNewMessage(userSender, userReceiver);
		checkMessage(message, userSender, userReceiver);
	}
	
	
	public void createCancelMessage() throws Exception
	{
		final Message message = messageService.createCancelMessage(userSender, userReceiver);
		checkMessage(message, userSender, userReceiver);
		assertThat(message.getBody(), not(emptyOrNullString()));
		assertThat(message.getSubject(), not(emptyOrNullString()));
		assertThat(message.getMessageType(), is(MessageType.CANCEL_MESSAGE));
	}
	
	
	public void createAnswerMessage() throws Exception
	{
		final Message originalMessage = messages.get(MessageState.SENT);
		final Message message = messageService.createAnswerMessage(originalMessage);
		checkMessage(message, userReceiver, userSender);
		assertThat(message.getReplyToMessage(), equalTo(originalMessage));
		assertThat(message.getSubject(), not(emptyOrNullString()));
		assertThat(message.getSubject(), not(equalTo(originalMessage.getSubject())));
		assertThat(message.getSubject(), endsWith(originalMessage.getSubject()));
	}

	
	public void createAdminAnswerMessage() throws Exception
	{
		final Message originalMessage = messages.get(MessageState.ADMIN_RECEIVED);
		final Message message = messageService.createAdminAnswerMessage(originalMessage,null);
		checkMessage(message, null, userSender);
		assertThat(message.getReplyToMessage(), equalTo(originalMessage));
		assertThat(message.getSubject(), not(emptyOrNullString()));
		assertThat(message.getSubject(), not(equalTo(originalMessage.getSubject())));
		assertThat(message.getSubject(), endsWith(originalMessage.getSubject()));
		assertThat(message.getBody(), not(emptyOrNullString()));
	}
	
	
	public void createAbuseMessage() throws Exception
	{
		final Message message = messageService.createAbuseMessage(userSender, userReceiver);
		checkMessage(message, userSender, null);
		assertThat(message.getSubject(), containsString(userReceiver.getAlias()));
		assertThat(message.getMessageType(), is(MessageType.ABUSE));
	}
	
	//
	public void sendMessage() throws Exception
	{
		final Message message = messages.get(MessageState.DRAFT);
		assertThat(message.isSent(), equalTo(false));
		messageService.sendMessage(message, null);
		assertThat(message.isSent(), equalTo(true));
	}
	
	//
	public void sendMessageNotThrownMailException() throws Exception
	{
		when(mailSendService.sendEmail(Mockito.any(User.class), Mockito.any()))
				.thenThrow(new MailSendException("User doesn't exist."));
		
		final Message message = messages.get(MessageState.DRAFT);
		assertThat(message.isSent(), equalTo(false));
		messageService.sendMessage(message, null);
		assertThat(message.isSent(), equalTo(true));
	}
	
	
	public void saveMessage() throws Exception
	{
		final Message message = messages.get(MessageState.DRAFT);
		messageService.saveMessage(message, Collections.emptyList());
		assertThat(message.isSent(), equalTo(false));
	}
	
	
	public void deleteMessages() throws Exception
	{
		final Message message = messages.get(MessageState.SENT);
		assertThat(message.getReceiverEnvelope().isDeleted(), equalTo(false));
		messageService.deleteMessages(userReceiver, Collections.singleton(message));
		assertThat(message.getReceiverEnvelope().isDeleted(), equalTo(true));
	}
	
	
	public void hideMessages() throws Exception
	{
		final Message message = messages.get(MessageState.SENT);
		assertThat(message.getReceiverEnvelope().isHidden(), equalTo(false));
		messageService.hideMessages(userReceiver, Collections.singleton(message));
		assertThat(message.getReceiverEnvelope().isHidden(), equalTo(true));
	}
	
	
	public void restoreMessages() throws Exception
	{
		final Message message = messages.get(MessageState.HIDDEN_INCOMING);
		assertThat(message.getReceiverEnvelope().isHidden(), equalTo(true));
		messageService.restoreMessages(userReceiver, Collections.singleton(message));
		assertThat(message.getReceiverEnvelope().isHidden(), equalTo(false));
	}
	
	
	public void readMessages() throws Exception
	{
		final Message message = messages.get(MessageState.SENT);
		assertThat(message.getReceiverEnvelope().isRead(), equalTo(false));
		messageService.readMessages(Collections.singleton(message));
		assertThat(message.getReceiverEnvelope().isRead(), equalTo(true));
	}
	
	
	public void unreadMessages() throws Exception
	{
		final Message message = messages.get(MessageState.SENT);
		assertThat(message.getReceiverEnvelope().isRead(), equalTo(false));
		messageService.readMessages(Collections.singleton(message));
		assertThat(message.getReceiverEnvelope().isRead(), equalTo(true));
		messageService.unreadMessages(Collections.singleton(message));
		assertThat(message.getReceiverEnvelope().isRead(), equalTo(false));
	}
	
	
	public void getMessageAttachments() throws Exception
	{
		final Message message = messages.get(MessageState.SENT);
		final MessageAttachment messageAttachment = new MessageAttachment();
		final FileEntity fileEntity = new FileEntity();
		messageAttachment.setMessage(message);
		messageAttachment.setFile(fileEntity);
		
		when(messageAttachmentRepository.findByMessage(message)).thenReturn(Collections.singletonList(messageAttachment));
		
		final List<MessageUploadFile> messageUploadFiles = messageService.getMessageAttachments(message);
		assertThat(messageUploadFiles.size(), equalTo(1));
		final MessageUploadFile messageUploadFile = messageUploadFiles.iterator().next();
		
		assertThat(messageUploadFile.getMessageAttachment(), equalTo(messageAttachment));
		assertThat(messageUploadFile.getFileEntity(), equalTo(fileEntity));
	}
	
	
	public void createMessageAttachment() throws Exception
	{
		final Message message = messages.get(MessageState.SENT);
		final MessageUploadFile messageUploadFile = messageService.createMessageAttachment(message, 0);
		
		assertThat(messageUploadFile.getMessageAttachment().getMessage(), equalTo(message));
		assertThat(messageUploadFile.getFileEntity(), notNullValue());
	}
	
	
	public void getMessageDirectory() throws Exception
	{
		for(MessageState messageState : MessageState.values())
		{
			final MessageDirectory senderMessageDirectory = messageService.getMessageDirectory(messages.get(messageState), userSender);
			final MessageDirectory receiverMessageDirectory = messageService.getMessageDirectory(messages.get(messageState), userReceiver);
			
			switch(messageState)
			{
				case SENT:
					assertThat(senderMessageDirectory, equalTo(MessageDirectory.OUTGOING));
					assertThat(receiverMessageDirectory, equalTo(MessageDirectory.INCOMING));
					break;
				case DRAFT:
					assertThat(senderMessageDirectory, equalTo(MessageDirectory.DRAFT));
					assertThat(receiverMessageDirectory, equalTo(MessageDirectory.DELETED_OR_UNKNOWN));
					break;
				case HIDDEN_DRAFT:
					assertThat(senderMessageDirectory, equalTo(MessageDirectory.HIDDEN));
					assertThat(receiverMessageDirectory, equalTo(MessageDirectory.DELETED_OR_UNKNOWN));
					break;
				case HIDDEN_INCOMING:
					assertThat(senderMessageDirectory, equalTo(MessageDirectory.OUTGOING));
					assertThat(receiverMessageDirectory, equalTo(MessageDirectory.HIDDEN));
					break;
				case HIDDEN_OUTGOING:
					assertThat(senderMessageDirectory, equalTo(MessageDirectory.HIDDEN));
					assertThat(receiverMessageDirectory, equalTo(MessageDirectory.INCOMING));
					break;
				case ADMIN_SENT:
					assertThat(senderMessageDirectory, equalTo(MessageDirectory.DELETED_OR_UNKNOWN));
					assertThat(receiverMessageDirectory, equalTo(MessageDirectory.INCOMING));
					break;
				case ADMIN_RECEIVED:
					assertThat(senderMessageDirectory, equalTo(MessageDirectory.OUTGOING));
					assertThat(receiverMessageDirectory, equalTo(MessageDirectory.DELETED_OR_UNKNOWN));
					break;
			}
		}
	}
	
	
	public void isRelationshipsCancelled() throws Exception
	{
		final Message message = messages.get(MessageState.SENT);
		
		final Relationship notDeletedRelationship = new Relationship();
		notDeletedRelationship.setDeleted(false);
		notDeletedRelationship.setCategories(Collections.singleton(RecommendationCategory.PARTNERSHIP));
		
		final Relationship deletedRelationship = new Relationship();
		deletedRelationship.setDeleted(true);
		deletedRelationship.setCategories(Collections.singleton(RecommendationCategory.PARTNERSHIP));
		
		userSender.setCategories(Collections.singleton(RecommendationCategory.PARTNERSHIP));
		userReceiver.setCategories(Collections.singleton(RecommendationCategory.PARTNERSHIP));
		
		assertThat(messageService.isRelationshipsCancelled(message), equalTo(false));
		
		when(relationshipRepository.findRelationshipBySourceUserAndTargetUser(userReceiver, userSender)).thenReturn(notDeletedRelationship);
		when(relationshipRepository.findRelationshipBySourceUserAndTargetUser(userSender, userReceiver)).thenReturn(deletedRelationship);
		assertThat(messageService.isRelationshipsCancelled(message), equalTo(true));
		
		when(relationshipRepository.findRelationshipBySourceUserAndTargetUser(userReceiver, userSender)).thenReturn(deletedRelationship);
		when(relationshipRepository.findRelationshipBySourceUserAndTargetUser(userSender, userReceiver)).thenReturn(notDeletedRelationship);
		assertThat(messageService.isRelationshipsCancelled(message), equalTo(true));
		
		when(relationshipRepository.findRelationshipBySourceUserAndTargetUser(userReceiver, userSender)).thenReturn(deletedRelationship);
		when(relationshipRepository.findRelationshipBySourceUserAndTargetUser(userSender, userReceiver)).thenReturn(deletedRelationship);
		assertThat(messageService.isRelationshipsCancelled(message), equalTo(true));
		
		when(relationshipRepository.findRelationshipBySourceUserAndTargetUser(userReceiver, userSender)).thenReturn(notDeletedRelationship);
		when(relationshipRepository.findRelationshipBySourceUserAndTargetUser(userSender, userReceiver)).thenReturn(notDeletedRelationship);
		assertThat(messageService.isRelationshipsCancelled(message), equalTo(false));
		
		userSender.setCategories(Collections.singleton(RecommendationCategory.FRIENDSHIP));
		assertThat(messageService.isRelationshipsCancelled(message), equalTo(true));
		
		userSender.setCategories(Collections.singleton(RecommendationCategory.PARTNERSHIP));
		userSender.setMemberStatus(MemberStatus.CANCELED);
		assertThat(messageService.isRelationshipsCancelled(message), equalTo(true));
		
		userSender.setMemberStatus(MemberStatus.REGISTERED);
		userSender.setEmail(null);
		assertThat(messageService.isRelationshipsCancelled(message), equalTo(true));
	}
	
	
	public void isExistsMessages() throws Exception
	{
		final Relationship relationship = new Relationship();
		relationship.setSourceUser(userSender);
		relationship.setTargetUser(userReceiver);
		
		assertThat(messageService.isExistsMessages(relationship), equalTo(true));
	}
	
	
	public void getReceiverList() throws Exception
	{
		final Relationship notDeletedRelationship = new Relationship();
		notDeletedRelationship.setSourceUser(userSender);
		notDeletedRelationship.setTargetUser(userReceiver);
		notDeletedRelationship.setDeleted(false);
		notDeletedRelationship.setCategories(Collections.singleton(RecommendationCategory.PARTNERSHIP));
		
		userSender.setCategories(Collections.singleton(RecommendationCategory.PARTNERSHIP));
		userReceiver.setCategories(Collections.singleton(RecommendationCategory.PARTNERSHIP));
		
		when(relationshipRepository.findAllRelationshipsForUser(userSender)).thenReturn(Collections.singletonList(notDeletedRelationship));
		assertThat(messageService.getReceiverList(userSender), hasItem(userReceiver));
		
		userSender.setCategories(Collections.singleton(RecommendationCategory.FRIENDSHIP));
		assertThat(messageService.getReceiverList(userSender), not(hasItem(userReceiver)));
		
		userSender.setCategories(Collections.singleton(RecommendationCategory.PARTNERSHIP));
		userReceiver.setMemberStatus(MemberStatus.CANCELED);
		assertThat(messageService.getReceiverList(userSender), not(hasItem(userReceiver)));
		
		userReceiver.setMemberStatus(MemberStatus.REGISTERED);
		userReceiver.setEmail(null);
		assertThat(messageService.getReceiverList(userSender), not(hasItem(userReceiver)));
	}
	
	
	public void getMessageStatisticsTest()
	{
		final Table<Long, Long, Long> messageStatistics = messageService.getMessageStatistics();
		
		assertThat(messageStatistics, notNullValue());
		assertThat(messageStatistics.size(), equalTo(1));
		
		final Cell<Long, Long, Long> cell = messageStatistics.cellSet().iterator().next();
		assertThat(cell.getRowKey(), equalTo(userSender.getId()));
		assertThat(cell.getColumnKey(), equalTo(userReceiver.getId()));
		assertThat(cell.getValue(), equalTo(5L));
	}
	
	
	public void getMessageStatisticsForUserTest()
	{
		final Table<Long, Long, TreeSet<LocalDateTime>> messageStatistics = messageService.getMessageStatistics(userSender);
		
		assertThat(messageStatistics, notNullValue());
		assertThat(messageStatistics.size(), equalTo(1));
		
		final Cell<Long, Long, TreeSet<LocalDateTime>> cell = messageStatistics.cellSet().iterator().next();
		assertThat(cell.getRowKey(), equalTo(userSender.getId()));
		assertThat(cell.getColumnKey(), equalTo(userReceiver.getId()));
		assertThat(cell.getValue().size(), equalTo(5));
	}
	
	
	public void countFirstWroteTest()
	{
		{
			final Table<Long, Long, TreeSet<LocalDateTime>> messageStatistics = messageService.getMessageStatistics(userSender);
			final long expected = 1;
			final Long count = messageService.countFirstWrote(messageStatistics, userSender.getId(), false);
			assertThat(count, equalTo(expected));
		}

		{
			final Table<Long, Long, TreeSet<LocalDateTime>> messageStatistics = messageService.getMessageStatistics(userSender);
			final long expected = 0;
			final Long count = messageService.countFirstWrote(messageStatistics, userSender.getId(), true);
			assertThat(count, equalTo(expected));
		}

		entityFactory.persistDefaultMessage(userSender, userReceiver, MessageState.SENT);

		{
			final Table<Long, Long, TreeSet<LocalDateTime>> messageStatistics = messageService.getMessageStatistics(userSender);
			final long expected = 1;
			final Long count = messageService.countFirstWrote(messageStatistics, userSender.getId(), true);
			assertThat(count, equalTo(expected));
		}
	}

	
	public void countFirstReceivedTest()
	{
		{
			final Table<Long, Long, TreeSet<LocalDateTime>> messageStatistics = messageService.getMessageStatistics(userSender);
			final long expected = 1;
			final Long count = messageService.countFirstReceived(messageStatistics, userReceiver.getId(), false);
			assertThat(count, equalTo(expected));
		}
		
		{
			final Table<Long, Long, TreeSet<LocalDateTime>> messageStatistics = messageService.getMessageStatistics(userSender);
			final long expected = 0;
			final Long count = messageService.countFirstReceived(messageStatistics, userReceiver.getId(), true);
			assertThat(count, equalTo(expected));
		}

		entityFactory.persistDefaultMessage(userSender, userReceiver, MessageState.SENT);

		{
			final Table<Long, Long, TreeSet<LocalDateTime>> messageStatistics = messageService.getMessageStatistics(userSender);
			final long expected = 1;
			final Long count = messageService.countFirstReceived(messageStatistics, userReceiver.getId(), true);
			assertThat(count, equalTo(expected));
		}
	}

	
	public void countSubstantialCommunicationTest()
	{
		{
			final Table<Long, Long, Long> messageStatistics = messageService.getMessageStatistics();
			final long expected = 0;
			final Long count = messageService.countSubstantialCommunications(messageStatistics, 1L);
			assertThat(count, equalTo(expected));
		}

		entityFactory.persistDefaultMessage(userSender, userReceiver, MessageState.SENT);

		{
			final Table<Long, Long, Long> messageStatistics = messageService.getMessageStatistics();
			final long expected = 1;
			final Long count = messageService.countSubstantialCommunications(messageStatistics, 1L);
			assertThat(count, equalTo(expected));
		}

		{
			final Table<Long, Long, Long> messageStatistics = messageService.getMessageStatistics();
			final long expected = 0;
			final Long count = messageService.countSubstantialCommunications(messageStatistics, 2L);
			assertThat(count, equalTo(expected));
		}

		entityFactory.persistDefaultMessage(userReceiver, userSender, MessageState.SENT);

		{
			final Table<Long, Long, Long> messageStatistics = messageService.getMessageStatistics();
			final long expected = 0;
			final Long count = messageService.countSubstantialCommunications(messageStatistics, 2L);
			assertThat(count, equalTo(expected));
		}

		entityFactory.persistDefaultMessage(userSender, userReceiver, MessageState.SENT);

		{
			final Table<Long, Long, Long> messageStatistics = messageService.getMessageStatistics();
			final long expected = 1;
			final Long count = messageService.countSubstantialCommunications(messageStatistics, 2L);
			assertThat(count, equalTo(expected));
		}

		{
			final Table<Long, Long, Long> messageStatistics = messageService.getMessageStatistics();
			final long expected = 0;
			final Long count = messageService.countSubstantialCommunications(messageStatistics, 3L);
			assertThat(count, equalTo(expected));
		}
	}
	
	
	public void convertMessageStatisticsTest()
	{
		final Table<Long, Long, TreeSet<LocalDateTime>> messageStatistics = messageService.getMessageStatistics(userSender);
		final Table<Long, Long, Long> convertedMessageStatistics = messageService.convertMessageStatistics(messageStatistics);
		
		assertThat(convertedMessageStatistics.size(), equalTo(1));
		
		final Cell<Long, Long, TreeSet<LocalDateTime>> cell = messageStatistics.cellSet().iterator().next();
		final Cell<Long, Long, Long> convertedCell = convertedMessageStatistics.cellSet().iterator().next();
		
		assertThat(convertedCell.getRowKey(), equalTo(cell.getRowKey()));
		assertThat(convertedCell.getColumnKey(), equalTo(cell.getColumnKey()));
		assertThat(convertedCell.getValue(), equalTo((long) cell.getValue().size()));
	}
	
	
	public void getOutgoingsTest()
	{
		{
			// SENT + HIDDEN_INCOMING + HIDDEN_OUTGOING + DELETED_INCOMING + DELETED_OUTGOING + SENT_CANCEL_MESSAGE
			final int expected = 6;
			final List<Message> messages = messageService.getOutgoingMessages(userSender);
			
			assertThat(messages.size(), equalTo(expected));
		}
		
		{
			final int expected = 0;
			final List<Message> messages = messageService.getOutgoingMessages(userReceiver);
			
			assertThat(messages.size(), equalTo(expected));
		}
	}
	
	
	public void getAdminMessagesTest()
	{
		{
			// ADMIN_RECEIVED
			final int expected = 11;
			final List<Message> messages = messageService.getAdminMessages(userSender);
			
			assertThat(messages.size(), equalTo(expected));
		}
		
		{
			// ADMIN_SENT
			final int expected = 1;
			final List<Message> messages = messageService.getAdminMessages(userReceiver);
			
			assertThat(messages.size(), equalTo(expected));
		}
	}
	
	// TODO RG: add tests for adminMessages like the way before
}
